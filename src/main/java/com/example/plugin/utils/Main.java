package com.example.plugin.utils;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import java.util.ResourceBundle;

public class Main
{

  public static void main(String[] args)
  {
    var vertx = Vertx.vertx();

    vertx.deployVerticle(Tester.class.getName(),new DeploymentOptions().setInstances(5), result->{

      if(result.succeeded())
      {
        System.out.println("Multiple handler "+result.result());

        for (int i = 0; i < 15; i++)
          vertx.eventBus().send("hello",i);

      }
      else
      {
        System.out.println("Deployment Failed "+result.cause());
      }

    });

  }
}
