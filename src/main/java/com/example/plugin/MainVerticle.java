package com.example.plugin;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class MainVerticle extends AbstractVerticle
{

  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {
    //ZMQ Talker to vert.x application
    var lowTimer = 3000;

    var medTimer = 5000;

    var highTimer = 7000;

    try(ZContext context = new ZContext())
    {

      var socket = context.createSocket(SocketType.PUB);

      socket.bind("tcp://*:4000");

      vertx.setPeriodic(lowTimer,handler->{


      });


    }

  }
}
