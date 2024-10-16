package com.example.plugin;

import com.example.plugin.plugin_linux.DataCollector;
import com.example.plugin.plugin_linux.AppDataReceiver;
import com.example.plugin.plugin_linux.AppDataSender;
import com.example.plugin.plugin_linux.FetchDetails;
import com.example.plugin.utils.Config;
import io.vertx.core.*;
import org.zeromq.ZContext;

public class Bootstrap
{
  public static final ZContext zContext = new ZContext();

  public static final Vertx vertx = Vertx.vertx();

  public static void main(String[] args)
  {

    new AppDataReceiver().start();

    vertx.deployVerticle(new DataCollector())

    .compose(result->vertx.deployVerticle(FetchDetails.class.getName(),
      new DeploymentOptions().setInstances(Config.FETCH_INSTANCES)))

    .compose(result->vertx.deployVerticle(new AppDataSender()))

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
