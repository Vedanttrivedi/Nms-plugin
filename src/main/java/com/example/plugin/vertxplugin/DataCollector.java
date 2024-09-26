package com.example.plugin.vertxplugin;

import com.example.plugin.models.Device;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.xml.stream.FactoryConfigurationError;
import java.util.concurrent.ConcurrentHashMap;

//This vertical listens plugin receiver for data
//It saves provisional devices
//also when periodic request arrives it distributes them to multiple verticals


public class DataCollector extends AbstractVerticle
{
  //First Maintain list of devices with map
  ConcurrentHashMap<String, Device> provisionedDevices;

  public DataCollector()
  {

    provisionedDevices = new ConcurrentHashMap<>();

  }


  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {
      //listen for incoming calls from receiver
    startPromise.complete();
    vertx.eventBus().<JsonArray>localConsumer("collector",

      collectorHandler->{


        var devices = collectorHandler.body();

        var devicesSize = devices.size();

        System.out.println("Collector received "+collectorHandler.body()+"\tLen "+devicesSize);

        if(devicesSize==1)
        {
          //it is periodic request
          System.out.println("Collector Request");

          //Deploy 5 verticals to distribute all provision devices request
          //Use Send and it will load balance and also send the list of available devices length to sender
          //so sender waits for all the devices to send at once

          var metric =  devices.getJsonObject(0).getString("metric");


          vertx.eventBus().send("devicesLength",provisionedDevices.size());

          vertx.deployVerticle(FetchDetails.class.getName(),new DeploymentOptions().setInstances(5),

            fetchDeploymentResult->{
              //Now we can distribute request to each vertical
                if(fetchDeploymentResult.succeeded())
                {
                  System.out.println("Deployment Result "+fetchDeploymentResult);

                  provisionedDevices.forEach((ip,current_device)->{

                      var jsonDevice = new JsonObject();

                      jsonDevice.put("ip",ip);

                      jsonDevice.put("username",current_device.username());

                      jsonDevice.put("password",current_device.password());

                      jsonDevice.put("metric",metric);

                    vertx.eventBus().send("fetch-send",jsonDevice);

                  });

                }
                else
                {
                  System.out.println("Deployment Of ALL Vertical failed ");
                }
            });

        }
        else
        {
          devices.remove(devicesSize- 1);//remove the initial extra object

          devices.forEach(device ->
          {
            var jsonDevice = (JsonObject)device;

            if (jsonDevice.getBoolean("doPolling"))
            {
              provisionedDevices.put(
                jsonDevice.getString("ip"),
                new Device(
                  jsonDevice.getString("ip"),
                  jsonDevice.getString("username"),
                  //Utils.decryptPassword(jsonDevice.getString("password"),secretKey,encryptionAlgorithm)//
                  jsonDevice.getString("password")
                )
              );

              System.out.println("Provisoned "+provisionedDevices);
            }
            else
            {
              provisionedDevices.remove(jsonDevice.getString("ip"));
            }
          });
        }

      });

  }
}
