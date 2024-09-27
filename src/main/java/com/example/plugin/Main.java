package com.example.plugin;

import com.example.plugin.plugin_linux.DataCollector;
import com.example.plugin.plugin_linux.PluginDataReceiver;
import com.example.plugin.plugin_linux.PluginDataSender;
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

    Future.all(
        vertx.deployVerticle(new PluginDataReceiver(context),
          new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
        ),

        vertx.deployVerticle(new DataCollector()),

        vertx.deployVerticle(

          new PluginDataSender(context))
      ).
      onComplete(deploymentResult->{

        if(deploymentResult.succeeded())
        {
          System.out.println("All the vreticals are deployed");
        }
        else
        {
          System.out.println("System failure "+deploymentResult.cause());

          System.exit(1);
        }
      });
  }
}
