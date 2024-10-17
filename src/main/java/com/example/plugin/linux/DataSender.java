package com.example.plugin.linux;

import com.example.plugin.Bootstrap;
import com.example.plugin.utils.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class DataSender extends AbstractVerticle
{

  private  ZMQ.Socket socket;

  public DataSender()
  {
    try
    {
      socket = Bootstrap.zContext.createSocket(SocketType.PUSH);

      socket.bind(Config.PUSH_SOCKET);

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

        socket.send(device.body().toString().getBytes(),ZMQ.DONTWAIT);

      });

    startPromise.complete();

  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception
  {
    super.stop(stopPromise);
  }

}
