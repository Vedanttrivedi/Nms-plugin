package com.example.plugin.service;


import com.example.plugin.models.Cpu_Metrics;
import com.example.plugin.models.Device;
import com.example.plugin.models.Memory_Metrics;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ProcessingRunnable implements Runnable
{
  ConcurrentHashMap<String,Device> provisionedDevice;

  public ProcessingRunnable()
  {

    this.provisionedDevice = new ConcurrentHashMap<>();

  }

  @Override
  public void run()
  {
    var cpu_metrics = "";

    var memory_metrics = "";

    var device_metrics = "";

    try (ZContext context = new ZContext())
    {
      LogManager lgmngr = LogManager.getLogManager();

      Logger log = lgmngr.getLogger(Logger.GLOBAL_LOGGER_NAME);

      log.log(Level.INFO, "Plugin Started");

      var socket = context.createSocket(SocketType.PULL);

      socket.connect("tcp://localhost:4555");

      var dataSenderSocket = context.createSocket(SocketType.PUSH);

      dataSenderSocket.bind("tcp://localhost:4556");

      ExecutorService threadPool = Executors.newFixedThreadPool(16);

      while (true)
      {
        var data = socket.recvStr();

        var stringJsonFormationData = new String(Base64.getDecoder().decode(data), ZMQ.CHARSET);

        var jsonArray = new JsonArray(stringJsonFormationData);

        log.log(Level.FINE, "Received " + jsonArray);

        var lenOfData = jsonArray.size();

        if (lenOfData == 1)
        {
          log.log(Level.FINE, "Periodical");

          var dataToSend = new JsonArray();

          provisionedDevice.forEach((ip, device) ->
          {
            CompletableFuture.supplyAsync(() -> {
                try
                {
                  return new FetchDetails(device.username(), device.password(), device.ip(),
                    jsonArray.getJsonObject(0).getString("metric")).call();
                }
                catch (Exception e)
                {
                  log.log(Level.SEVERE, "Error collecting data: " + e.getMessage());
                  return null;
                }
              }, threadPool)

              .thenAccept(metric -> {
                var object = new JsonObject();

                if (jsonArray.getJsonObject(0).getString("metric").equals("memory"))
                {

                  log.log(Level.INFO, "Memory data " + metric);

                  var memData = (Memory_Metrics) (metric);

                  if (memData.isStatus())
                  {
                    object.put("free", memData.getFree());

                    object.put("used", memData.getUsed());

                    object.put("swap", memData.getSwap());

                    object.put("cached", memData.getCached());

                    object.put("disk_space", memData.getDisk_space());

                    object.put("ip", ip);

                    object.put("status", true);
                  }
                  else
                  {
                    object.put("ip", ip);

                    object.put("status", false);
                  }
                }

                else if (jsonArray.getJsonObject(0).getString("metric").equals("cpu"))
                {
                  log.log(Level.INFO, "Cpu data " + metric);

                  var cpuData = (Cpu_Metrics) (metric);

                  if (cpuData.isStatus())
                  {
                    object.put("ip", ip);

                    object.put("percentage", cpuData.getPercentage());

                    object.put("io_percent", cpuData.getIo_percent());

                    object.put("load_average", cpuData.getLoad_average());

                    object.put("status", true);

                    object.put("threads", cpuData.getThreads());

                    object.put("process_counts", cpuData.getProcess_counts());
                  }
                  else
                  {
                    object.put("ip", ip);

                    object.put("status", false);
                  }
                }

                dataToSend.add(object);

              });

          });
          dataToSend.add(jsonArray.getJsonObject(0).getString("metric"));

          dataToSend.add(LocalDateTime.now().toString()); // Timestamp

          log.log(Level.INFO, "Sending TO App");

          log.log(Level.INFO, dataToSend.toString());

          var base64Data = Base64.getEncoder().encode(dataToSend.toString().getBytes());

          dataSenderSocket.send(base64Data);

        }
        else
        {

          System.out.println("Need to add new Devices/remove device or the first time boot up " + LocalDateTime.now());

          var metrics = jsonArray.getJsonObject(lenOfData - 1);

          System.out.println("Metrics are " + metrics);

          cpu_metrics = metrics.getString("cpu");

          memory_metrics = metrics.getString("memory");

          device_metrics = metrics.getString("device");

          System.out.println("Device Discovery: " + cpu_metrics + "\t" + memory_metrics + "\t" + device_metrics);

          // Remove the last object; data is only about devices
          jsonArray.remove(lenOfData - 1);

          // Add or remove devices to the provisioned devices map
          jsonArray.forEach(device -> {

            var jsonDevice = (JsonObject) device;

            System.out.println("New Device information " + jsonDevice);

            if (jsonDevice.getBoolean("doPolling")) {
              provisionedDevice.put(
                jsonDevice.getString("ip"),
                new Device(
                  jsonDevice.getString("ip"),
                  jsonDevice.getString("username"),
                  jsonDevice.getString("password")
                )
              );
            }
            else
            {
              provisionedDevice.remove(jsonDevice.getString("ip"));
            }
          });
        }
      }

    }
    catch (Exception e)
    {
      System.out.println("Error while listening to data " + e.getLocalizedMessage());
    }
  }
}

