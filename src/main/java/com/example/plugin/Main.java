package com.example.plugin;

import com.example.plugin.service.DataReceiverVerticle;
import com.example.plugin.service.ProcessingRunnable;
import io.vertx.core.*;
import org.zeromq.ZContext;

import java.util.concurrent.TimeUnit;

public class Main extends AbstractVerticle
{

  public static void main(String[] args)
  {
    var vertx = Vertx.vertx(
      new VertxOptions().setMaxWorkerExecuteTime(600).
      setMaxWorkerExecuteTimeUnit(TimeUnit.SECONDS)

    );
    ZContext context = new ZContext();

    vertx.deployVerticle(new DataReceiverVerticle(context),
      new DeploymentOptions().
        setThreadingModel(ThreadingModel.WORKER)

    );


      Thread processingThread = new Thread(new ProcessingRunnable());

      processingThread.start();

  }
}
