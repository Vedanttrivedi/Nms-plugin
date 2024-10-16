package com.example.plugin.plugin_linux;

import com.example.plugin.models.Device;
import com.example.plugin.utils.Config;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//This vertical listens plugin receiver for data
//It saves provisional devices
//also when periodic request arrives it distributes them to multiple verticals


public class DataCollector extends AbstractVerticle
{
  //First Maintain list of devices with map

  private final Map<String, Device> provisionedDevices;

  public DataCollector()
  {

    provisionedDevices = new HashMap<>();

  }


  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {

    vertx.eventBus().<JsonArray>localConsumer(Config.COLLECTOR, collectorHandler ->
      {

        var devices = collectorHandler.body();

        if (devices.size() == 1)
        {
          //it is periodic request
          //Use Send And it will load balance and also send the list of available devices length to sender

          var metric = devices.getJsonObject(0).getString("metric");

          provisionedDevices.forEach((ip, current_device) ->
          {

            var jsonDevice = new JsonObject();

            jsonDevice.put("ip", ip);

            jsonDevice.put("username", current_device.username());

            jsonDevice.put("password", current_device.password());

            jsonDevice.put("metric", metric);

            vertx.eventBus().send(Config.FETCH, jsonDevice);

          });

        }
        else
        {
          devices.remove(devices.size() - 1);//remove the initial extra object

          devices.forEach(device ->
          {
            var jsonDevice = (JsonObject) device;

            if (jsonDevice.getBoolean("doPolling"))
            {

              provisionedDevices.put(
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
              provisionedDevices.remove(jsonDevice.getString("ip"));
            }
          });
        }

      });

    startPromise.complete();

  }


  @Override
  public void stop(Promise<Void> stopPromise) throws Exception
  {
    super.stop(stopPromise);
  }

}
