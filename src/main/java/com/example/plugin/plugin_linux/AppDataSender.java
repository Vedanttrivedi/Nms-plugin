package com.example.plugin.plugin_linux;

import com.example.plugin.utils.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import java.util.Base64;

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
      System.out.println("Eerror Establishing socket ");
    }

  }
  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {
    vertx.eventBus().<JsonObject>localConsumer(Config.send,

      device->{

      var encodedData = Base64.getEncoder().encode(device.body().toString().getBytes());

      var status = dataSenderSocket.send(encodedData,ZMQ.DONTWAIT);


    });

    startPromise.complete();

  }

  @Override
  public void stop(Promise<Void> stopPromise) throws Exception
  {
    super.stop(stopPromise);
  }

}
