package com.example.plugin;

import com.example.plugin.models.DB;
import com.example.plugin.models.Device;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Base64;

public class MainVerticle extends AbstractVerticle
{


  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {
      //listen for user to send request
  }
}
