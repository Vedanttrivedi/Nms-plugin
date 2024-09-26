package com.example.plugin.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Base64;

public class DataReceiverVerticle extends AbstractVerticle
{

  private static final Logger LOGGER = LoggerFactory.getLogger(DataReceiverVerticle.class);

  private ZMQ.Socket socket;

  public DataReceiverVerticle(ZContext context)
  {
      socket = context.createSocket(SocketType.PULL);

      socket.connect("tcp://localhost:4555");
  }


  @Override
  public void start(Promise<Void> startPromise)
  {
    System.out.println("Data Receiver Loaded "+Thread.currentThread().getName());

    while(true)
    {

      var data = socket.recvStr();

      var decodedData = new String(Base64.getDecoder().decode(data.getBytes()));

      var devicesInfo = new JsonArray(decodedData);

      System.out.println("While Looop "+devicesInfo);

      LOGGER.info("Received: " + devicesInfo);

      if (devicesInfo.size() == 1)
        processPeriodicalData(devicesInfo);

      else
        processDeviceProvisioningData(devicesInfo);

    }

  }

  private void processPeriodicalData(JsonArray jsonArray)
  {
    var metricType = jsonArray.getJsonObject(0).getString("metric");

    vertx.eventBus().publish("data.collection.start", new JsonObject().put("metricType", metricType));
  }

  private void processDeviceProvisioningData(JsonArray jsonArray)
  {
    vertx.eventBus().publish("device.provisioning", jsonArray);
  }

  @Override
  public void stop(Promise<Void> stopPromise)
  {
    if (socket != null) {
      socket.close();

    } else {
      stopPromise.complete();
    }
  }
}
