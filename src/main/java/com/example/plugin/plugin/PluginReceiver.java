package com.example.plugin.plugin;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Base64;

//Job of this vertical is to listen for user request



public class PluginReceiver extends AbstractVerticle
{
  private ZMQ.Socket pullSocket;

  public PluginReceiver(ZContext context)
  {
    this.pullSocket = context.createSocket(SocketType.PULL);

    pullSocket.connect("tcp://localhost:4555");

  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {

    System.out.println("Receiver loaded ");

    startPromise.complete();

    while(true)
    {
      var data = pullSocket.recvStr();

      if(data!=null)
      {
        var devices = new JsonArray(new String(Base64.getDecoder().decode(data.getBytes())));

        vertx.eventBus().send("collector",devices);

      }

    }
  }
}
