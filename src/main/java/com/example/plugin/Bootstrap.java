package com.example.plugin;

import com.example.plugin.plugin_linux.DataCollector;
import com.example.plugin.plugin_linux.AppDataReceiver;
import com.example.plugin.plugin_linux.AppDataSender;
import io.vertx.core.*;
import org.zeromq.ZContext;

public class Bootstrap
{

  public static ZContext zContext = new ZContext();

  public static Vertx vertx = Vertx.vertx();

  public static void main(String[] args)
  {

    var dataReceiverThread = new AppDataReceiver();

    dataReceiverThread.start();

    vertx.deployVerticle(new DataCollector())

    .compose(deploymentId->vertx.deployVerticle(new AppDataSender()))

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
  }
}
