package com.example.plugin.linux;

import com.example.plugin.utils.Config;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

//This vertical listens plugin receiver for data
//also when periodic request arrives it distributes them to multiple verticals


public class DataCollector extends AbstractVerticle
{

  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {

    vertx.eventBus().<JsonArray>localConsumer(Config.COLLECTOR, collectorHandler ->
      {

        var devices = collectorHandler.body();//List of devices with metric arrived

        var metric = devices.remove(devices.size()-1);

        devices.forEach((current_device) ->
        {
          vertx.eventBus().send(Config.FETCH, ( ((JsonObject) current_device).put("metric",metric)));

        });

      });

    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception
  {
    super.stop(stopPromise);
  }

}
