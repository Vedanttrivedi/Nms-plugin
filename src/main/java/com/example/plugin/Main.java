package com.example.plugin;

import com.example.plugin.vertxplugin.DataCollector;
import com.example.plugin.vertxplugin.PluginReceiver;
import com.example.plugin.vertxplugin.PluginSender;
import io.vertx.core.*;
import org.zeromq.ZContext;

import java.util.concurrent.TimeUnit;

public class Main
{
  public static void main(String[] args)
  {
    var vertx = Vertx.vertx(
      new VertxOptions().setMaxWorkerExecuteTime(600).
        setMaxWorkerExecuteTimeUnit(TimeUnit.SECONDS)

    );

    var context = new ZContext();

    CompositeFuture.all
      (
        vertx.deployVerticle(new PluginReceiver(context),
          new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
        ),

        vertx.deployVerticle(new DataCollector()),

        vertx.deployVerticle(

          new PluginSender(context))
      ).
      onComplete(deploymentResult->{

        if(deploymentResult.succeeded())
        {
          System.out.println("All the vreticals are deployed");
        }
        else
          System.exit(1);
      });
  }
}
