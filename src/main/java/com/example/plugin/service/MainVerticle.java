package com.example.plugin.service;
import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.example.plugin.Main;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.zeromq.ZContext;

public class MainVerticle extends AbstractVerticle
{
  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  @Override
  public void start(Promise<Void> startPromise)
  {
    var context1 = new ZContext();

    Future.all(

        vertx.deployVerticle(new DataReceiverVerticle(context1)),

        vertx.deployVerticle(new CollectorVerticle(),new DeploymentOptions().setWorker(true).setWorkerPoolSize(10))

      )
      .onComplete(deploymentResult->
      {

        if(deploymentResult.succeeded())
          System.out.println("Initial Verticles Deployed");

        else
          System.out.println("Error While Deploying verticle "+deploymentResult.cause());

      });
  }


}
