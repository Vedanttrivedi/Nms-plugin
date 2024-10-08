package com.example.plugin.plugin_linux;

import com.example.plugin.utils.Config;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Base64;

public class AppDataReceiver extends Thread
{

  private final ZMQ.Socket pullSocket;

  private Vertx vertx;

  public AppDataReceiver(ZContext context, Vertx vertx)
  {
    this.pullSocket = context.createSocket(SocketType.PULL);

    pullSocket.connect(Config.PULL_SOCKET);

    this.vertx = vertx;
  }

  public void run()
  {

    while(true)
    {

      var data = pullSocket.recvStr();

      if(data!=null)
      {
        var devices = new JsonArray(new String(Base64.getDecoder().decode(data.getBytes())));

        vertx.eventBus().send(Config.collector,devices);

      }
    }
  }
}
