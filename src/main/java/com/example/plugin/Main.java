package com.example.plugin;

import com.example.plugin.models.Cpu_Metrics;
import com.example.plugin.models.Device;
import com.example.plugin.models.Memory_Metrics;
import com.example.plugin.models.Metric;
import com.example.plugin.test.FetchDetails;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.*;

public class Main
{
  static ConcurrentHashMap<String, Device> provisionedDevice = new ConcurrentHashMap<>();

  private final static Logger LOGGER =
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public static void main(String[] args)
  {
    //load the map with initial data

    var vertx = Vertx.vertx();

    var cpu_metrics = "";

    var memory_metrics = "";

    var device_metrics = "";

    try (ZContext context = new ZContext())
    {
      LogManager lgmngr = LogManager.getLogManager();

      Logger log = lgmngr.getLogger(Logger.GLOBAL_LOGGER_NAME);

      log.log(Level.INFO,"Plugin Started");
      //socket that receives the data collection information from app

      var socket = context.createSocket(SocketType.PULL);

      //socket that sends the data back to app once data is collected
      var dataSenderSocket = context.createSocket(SocketType.PUSH);

      dataSenderSocket.bind("tcp://localhost:4556");

      socket.connect("tcp://localhost:4555");

      ExecutorService threadPool  = Executors.newFixedThreadPool(16);

      while (true)
      {

        var data = socket.recvStr();

        var stringJsonFormationData = new String(Base64.getDecoder().decode(data), ZMQ.CHARSET);

        var jsonArray = new JsonArray(stringJsonFormationData);

        log.log(Level.FINE,"Received "+jsonArray);

        var lenOfData = jsonArray.size();

        if(lenOfData == 1)
        {
          //we can say that there is no new device and it must be some sort of periodical data collection.

          //start collecting the data and then send it to user
          log.log(Level.FINE,"Periodical");

          var dataToSend = new JsonArray();

          System.out.println("It is periodical data collection "+LocalDateTime.now().toString()+"\t"+jsonArray.getJsonObject(lenOfData-1));

          provisionedDevice.forEach(
            (ip,device)->
          {
            Future<Metric> future = threadPool.submit(new FetchDetails(device.username(),device.password(),device.ip(),
              jsonArray.getJsonObject(0).getString("metric")));

            try
            {

              var object = new JsonObject();

              var metric = future.get();

              if(jsonArray.getJsonObject(0).getString("metric").equals("memory"))
              {
                //this means that we are able to collect the data and device is not down!

                log.log(Level.INFO,"Memory data "+metric);

                var memData = (Memory_Metrics)(metric);

                if(memData.isStatus())
                {
                  System.out.println("Status True!");

                  object.put("free",memData.getFree());

                  object.put("used",memData.getUsed());

                  object.put("swap",memData.getSwap());

                  object.put("cached",memData.getCached());

                  object.put("disk_space",memData.getDisk_space());

                  object.put("ip",ip);

                  object.put("status",true);

                }
                else
                {
                  object.put("ip",ip);

                  object.put("status",false);


                }
              }
              else if(jsonArray.getJsonObject(0).getString("metric").equals("cpu"))
              {

                log.log(Level.INFO,"Cpu data "+metric);

                var cpuData = (Cpu_Metrics)(metric);

                if(cpuData.isStatus())
                {
                  object.put("ip",ip);

                  object.put("percentage",cpuData.getPercentage());

                  object.put("io_percent",cpuData.getIo_percent());

                  object.put("load_average",cpuData.getLoad_average());

                  object.put("status",true);

                  object.put("threads",cpuData.getThreads());

                  object.put("process_counts",cpuData.getProcess_counts());

                }
                else
                {
                  object.put("ip",ip);

                  object.put("status",false);


                }
              }


              dataToSend.add(object);

            }
            catch (Exception e)
            {

              System.out.println("Eror "+e.getLocalizedMessage());

            }

          });

          dataToSend.add(jsonArray.getJsonObject(0).getString("metric"));

          dataToSend.add(LocalDateTime.now().toString()); //1 sec delay when start data collection till now

          log.log(Level.INFO,"Sending TO App");

          log.log(Level.INFO,dataToSend.toString());

          var bas64Data = Base64.getEncoder().encode(dataToSend.toString().getBytes());

          dataSenderSocket.send(bas64Data);

        }
        else
        {
          //We can say that either new device is added/removed or it is the App is just started

          System.out.println("Need to add new Devices or the first time boot up "+LocalDateTime.now().toString() );

          //first fetch what info we need to collect

          var metrics = jsonArray.getJsonObject(lenOfData-1);

          System.out.println("Metrics are "+metrics);

          cpu_metrics = metrics.getString("cpu");

          memory_metrics = metrics.getString("memory");

          device_metrics = metrics.getString("device");

          System.out.println("Information first time : "+cpu_metrics+"\t"+memory_metrics+"\t"+device_metrics);

          //once metric information is received remove the last object,so data is only about discoveries
          jsonArray.remove(lenOfData-1);
          //add the devices to our provisioned devices

          String finalMemory_metrics = memory_metrics;

          jsonArray.forEach(

            device->{
              //coverting each device to jsonobject(explicit)

              var jsonDevice = (JsonObject)device;

              System.out.println("New Device information "+jsonDevice);

              provisionedDevice.put(
                jsonDevice.getString("ip"),
                new Device(jsonDevice.getString("ip"),
                  jsonDevice.getString("username"),jsonDevice.getString("password")));


              //also we can also start the fetch details

              threadPool.submit(new FetchDetails(jsonDevice.getString("username"),
                jsonDevice.getString("password"),jsonDevice.getString("ip"), finalMemory_metrics));


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
