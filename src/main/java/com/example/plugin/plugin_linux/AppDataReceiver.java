package com.example.plugin.plugin_linux;

import com.example.plugin.Bootstrap;
import com.example.plugin.utils.Config;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class AppDataReceiver extends Thread
{
  private final ZMQ.Socket socket;

  public AppDataReceiver()
  {
    this.socket = Bootstrap.zContext.createSocket(SocketType.PULL);

    socket.connect(Config.PULL_SOCKET);

  }

  public void run()
  {

    while(true)
    {

      var data = socket.recvStr();

      if(data!=null)
      {

        var devices = new JsonArray(new String(data.getBytes()));

        Bootstrap.vertx.eventBus().send(Config.COLLECTOR,devices);

      }

    }
  }
}
