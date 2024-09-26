package com.example.plugin.service;

import com.example.plugin.models.Cpu_Metrics;
import com.example.plugin.models.Device;
import com.example.plugin.models.Memory_Metrics;
import com.example.plugin.models.Metric;
import com.example.plugin.utils.Utils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jdk.jshell.execution.Util;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

public class ProcessingRunnable implements Runnable
{

  private final ConcurrentHashMap<String, Device> provisionedDevice;

  private final BlockingQueue<JsonArray> dataQueue;

  private final Logger log;

  public ProcessingRunnable()
  {

    this.provisionedDevice = new ConcurrentHashMap<>();

    this.dataQueue = new LinkedBlockingQueue<>();

    LogManager lgmngr = LogManager.getLogManager();

    this.log = lgmngr.getLogger(Logger.GLOBAL_LOGGER_NAME);

  }

  @Override
  public void run()
  {
    try (ZContext context = new ZContext()) {

      log.log(Level.INFO, "Plugin Started");

      ZMQ.Socket socket = context.createSocket(SocketType.PULL);

      socket.connect("tcp://localhost:4555");

      Thread senderThread = new Thread(() -> runSender(context));

      senderThread.start();

      ExecutorService threadPool = Executors.newFixedThreadPool(16);

      while (!Thread.currentThread().isInterrupted())
      {
        var data = socket.recvStr();
        if (data == null) continue;

        processReceivedData(data, threadPool);

      }
    }
    catch (Exception e)
    {
      log.log(Level.SEVERE, "Error in main processing loop", e);
    }
    finally
    {
      log.log(Level.INFO, "ProcessingRunnable shutting down");
    }
  }

  private void processReceivedData(String data, ExecutorService threadPool)
  {
    try
    {
      var decodedData = new String(Base64.getDecoder().decode(data), ZMQ.CHARSET);

      JsonArray jsonArray = new JsonArray(decodedData);

      log.log(Level.FINE, "Received " + jsonArray);

      if (jsonArray.size() == 1)
      {
        processPeriodicalData(jsonArray, threadPool);
      }
      else
      {
        processDeviceProvisioningData(jsonArray);
      }
    }
    catch (Exception e)
    {
      log.log(Level.SEVERE, "Error processing received data", e);
    }
  }

  private void processPeriodicalData(JsonArray jsonArray, ExecutorService threadPool)
  {
    JsonArray dataToSend = new JsonArray();

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    provisionedDevice.forEach((ip, device) ->
    {
      CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {

        try
        {
          return new FetchDetails(device.username(), device.password(), device.ip(),
            jsonArray.getJsonObject(0).getString("metric")).call();
        }
        catch (Exception e)
        {
          log.log(Level.SEVERE, "Error collecting data for IP " + ip, e);
          return null;
        }
      }, threadPool)

        .thenAccept(metric ->
        {

          System.out.println("Metric Returned "+metric);

        JsonObject object = processMetric(metric, ip, jsonArray.getJsonObject(0).getString("metric"));

        synchronized (dataToSend) {
          System.out.println("Adding object "+object);
          dataToSend.add(object);
        }

      })
        .exceptionally(ex -> {

          log.log(Level.SEVERE, "Exception in processing for IP " + ip, ex);

        return null;
      });

      futures.add(future);
    });

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    dataToSend.add(jsonArray.getJsonObject(0).getString("metric"));

    dataToSend.add(LocalDateTime.now().toString());

    log.log(Level.INFO, "Queueing data to send: " + dataToSend);

    dataQueue.offer(dataToSend);

  }

  private JsonObject processMetric(Metric metric, String ip, String metricType)
  {

    JsonObject object = new JsonObject();

    object.put("ip", ip);

    if ("memory".equals(metricType) && metric instanceof Memory_Metrics)
    {
      Memory_Metrics memData = (Memory_Metrics) metric;
      if (memData.isStatus()) {
        object.put("free", memData.getFree())
          .put("used", memData.getUsed())
          .put("swap", memData.getSwap())
          .put("cached", memData.getCached())
          .put("disk_space", memData.getDisk_space())
          .put("status", true);
      }
      else
      {
        object.put("status", false);
      }
    }
    else if ("cpu".equals(metricType) && metric instanceof Cpu_Metrics)
    {
      Cpu_Metrics cpuData = (Cpu_Metrics) metric;

      if (cpuData.isStatus())
      {
        object.put("percentage", cpuData.getPercentage())
          .put("io_percent", cpuData.getIo_percent())
          .put("load_average", cpuData.getLoad_average())
          .put("threads", cpuData.getThreads())
          .put("process_counts", cpuData.getProcess_counts())
          .put("status", true);
      }
      else
      {
        object.put("status", false);
      }
    }

    return object;
  }

  private void processDeviceProvisioningData(JsonArray jsonArray)
  {
    log.log(Level.INFO, "Processing device provisioning data");

    JsonObject metrics = jsonArray.getJsonObject(jsonArray.size() - 1);

    jsonArray.remove(jsonArray.size() - 1);

    var secretKey = metrics.getString("secretKey");

    var encryptionAlgorithm =metrics.getString("encryptionAlgorithm");


    jsonArray.forEach(device ->
    {
      JsonObject jsonDevice = (JsonObject) device;

      if (jsonDevice.getBoolean("doPolling"))
      {
        provisionedDevice.put(
          jsonDevice.getString("ip"),
          new Device(
            jsonDevice.getString("ip"),
            jsonDevice.getString("username"),
            //Utils.decryptPassword(jsonDevice.getString("password"),secretKey,encryptionAlgorithm)//
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

  private void runSender(ZContext context)
  {
    try (ZMQ.Socket dataSenderSocket = context.createSocket(SocketType.PUSH))
    {

      dataSenderSocket.bind("tcp://localhost:4556");

      System.out.println("Sender socket loaded");

      while (!Thread.currentThread().isInterrupted())
      {
        try
        {
          var dataToSend = dataQueue.take();

          System.out.println("Sending data from plugin "+dataToSend);

          log.log(Level.INFO, "Sending data: " + dataToSend);

          var base64Data = Base64.getEncoder().encode(dataToSend.toString().getBytes());

          dataSenderSocket.send(base64Data);

          System.out.println("Sent!!!!!!!");
        }
        catch (InterruptedException e)
        {
          Thread.currentThread().interrupt();

          break;
        }
        catch (Exception e)
        {
          log.log(Level.SEVERE, "Error sending data", e);
        }
      }
    }
    log.log(Level.INFO, "Sender thread shutting down");
  }
}
