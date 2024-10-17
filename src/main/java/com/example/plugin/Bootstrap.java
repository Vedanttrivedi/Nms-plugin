package com.example.plugin;

import com.example.plugin.linux.DataCollector;
import com.example.plugin.linux.DataReceiver;
import com.example.plugin.linux.DataSender;
import com.example.plugin.linux.FetchDetails;
import com.example.plugin.utils.Config;
import io.vertx.core.*;
import org.zeromq.ZContext;

public class Bootstrap
{

  public static final ZContext zContext = new ZContext();

  public static final Vertx vertx = Vertx.vertx();

  public static void main(String[] args)
  {

    new DataReceiver().start();

    vertx.deployVerticle(new DataCollector())

    .compose(result->vertx.deployVerticle(FetchDetails.class.getName(),
      new DeploymentOptions().setInstances(Config.FETCH_INSTANCES)))

    .compose(result->vertx.deployVerticle(new DataSender()))

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
