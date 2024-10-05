package com.example.plugin;

import com.example.plugin.plugin_linux.AppDataReceiver2;
import com.example.plugin.plugin_linux.DataCollector;
import com.example.plugin.plugin_linux.AppDataReceiver;
import com.example.plugin.plugin_linux.AppDataSender;
import io.vertx.core.*;
import org.zeromq.ZContext;

public class Bootstrap
{

  public static void main(String[] args)
  {

    var vertx = Vertx.vertx();

    var zcontext = new ZContext();

    vertx.deployVerticle(new DataCollector())
      .compose(deploymentId->vertx.deployVerticle(new AppDataSender(zcontext)))
      .onComplete(deploymentResult->{

        if(deploymentResult.succeeded())
        {

          System.out.println("All the verticals are deployed");

        }
        else
        {
          System.out.println("System failure "+deploymentResult.cause());

        }
      });

    var dataReceiverThread = new AppDataReceiver(zcontext,vertx);

    dataReceiverThread.start();

    var dataReceiverThread2 = new AppDataReceiver2(zcontext,vertx);

    dataReceiverThread2.start();

  }
}
