package com.example.plugin.plugin;

import com.example.plugin.utils.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;
import java.util.Base64;

public class PluginSender extends AbstractVerticle
{

  private int totalDeviceToSend;

  private ZMQ.Socket dataSenderSocket;

  private final JsonArray dataToSend;

  public PluginSender(ZContext context)
  {
    totalDeviceToSend = 0;

    try
    {
      dataSenderSocket = context.createSocket(SocketType.PUSH);

      dataSenderSocket.bind(Config.PUSH_SOCKET);

      System.out.println("Socket Established "+dataSenderSocket.getLastEndpoint());


    }
    catch (Exception exception)
    {
      System.out.println("Eerror Establishing socket ");

    }
    dataToSend = new JsonArray();

  }
  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {
    //listen for total provision devices
    startPromise.complete();

    vertx.eventBus().<Integer>localConsumer("devicesLength",deviceLength->{

      totalDeviceToSend = deviceLength.body();

    });

    //listen for incoming requests from collector
    //once data is fetched collectors will send the information here
    vertx.eventBus().<JsonObject>localConsumer("send",

      device->{

      dataToSend.add(device.body());

        System.out.println("Received Device "+dataToSend.size()+"\t"+totalDeviceToSend);

        if(dataToSend.size()==totalDeviceToSend)
        {

          dataToSend.add(device.body().getString("metric"));

          dataToSend.add(LocalDateTime.now().toString());


          sendFinal();

        }

    });

  }

  private void sendFinal()
  {

    System.out.println("Before Sending "+dataToSend);

    var encodedData = Base64.getEncoder().encode(dataToSend.toString().getBytes());

    try
    {
      var status =   dataSenderSocket.send(encodedData);

      System.out.println("Status Send "+status);
    }

    catch (Exception exception)
    {
      System.out.println("Exception "+exception.getMessage());
    }

    dataToSend.clear();

    totalDeviceToSend = 0;

    System.out.println("After sending "+dataToSend+"\t"+totalDeviceToSend);

  }
}
