package com.example.plugin.plugin_linux;

import com.example.plugin.utils.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class AppDataSender extends AbstractVerticle
{

  private ZMQ.Socket dataSenderSocket;

  public AppDataSender(ZContext context)
  {
    try
    {
      dataSenderSocket = context.createSocket(SocketType.PUSH);

      dataSenderSocket.bind(Config.PUSH_SOCKET);

    }

    catch (Exception exception)
    {
      System.out.println("Error Establishing socket!");
    }

  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {

    vertx.eventBus().<JsonObject>localConsumer(Config.SEND,

      device->{

      dataSenderSocket.send(device.body().toString().getBytes(),ZMQ.DONTWAIT);

    });

    startPromise.complete();

  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception
  {
    super.stop(stopPromise);
  }

}
