package com.example.plugin.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class Tester extends AbstractVerticle
{
  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {
    vertx.eventBus().localConsumer("hello",data->{

      System.out.println("Thread "+Thread.currentThread().getName()+"\t Data "+data.body());

    });
    startPromise.complete();
  }
}
