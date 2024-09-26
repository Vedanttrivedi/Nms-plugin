package com.example.plugin.vertxplugin;

import com.example.plugin.utils.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.swing.tree.TreeCellEditor;
import java.io.FilterOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class PluginSender extends AbstractVerticle
{

  AtomicInteger totalDeviceToSend;

  ZMQ.Socket dataSenderSocket;

  final JsonArray dataToSend;

  public PluginSender(ZContext context)
  {
    totalDeviceToSend = new AtomicInteger(0);

    try
    {
      dataSenderSocket = context.createSocket(SocketType.PUSH);

      dataSenderSocket.bind(Config.PUSH_SOCKET);

      System.out.println("Socket Established "+dataSenderSocket.getLastEndpoint());


    }
    catch (Exception exception)
    {
      System.out.println("Eerror Establishing socket ");

    }
    dataToSend = new JsonArray();


  }
  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {
    //listen for total provision devices
    System.out.println("Sender loaded "+Thread.currentThread().getName());
    startPromise.complete();
    vertx.eventBus().<Integer>localConsumer("devicesLength",deviceLength->{

      totalDeviceToSend.set(deviceLength.body());

    });

    //listen for incoming requests from collector
    //once data is fetched collectors will send the information here
    vertx.eventBus().<JsonObject>localConsumer("send",

      device->{

        System.out.println("Received Device");

        synchronized (dataToSend){
          dataToSend.add(device.body());
        }

        //if the datatosend reaches full potential we can send this

        if(dataToSend.size()==totalDeviceToSend.get())
        {

          dataToSend.add(device.body().getString("metric"));

          dataToSend.add(LocalDateTime.now().toString());

          System.out.println("Data to send JsonObject"+Thread.currentThread().getName()+"\t"+dataToSend);

          System.out.println("Data to send CRL"+Thread.currentThread().getName()+"\t"+dataToSend);

          vertx.eventBus().send("inner-eventbus",true);

          //execute sendFinal(),but from the threda on which worker thred is deployed
          vertx.runOnContext(context->
          {
            System.out.println("Context "+context+"\t Thread "+Thread.currentThread().getName());

            sendFinal();

          });
        }

    });

  }

  private void sendFinal()
  {
    var encodedData = Base64.getEncoder().encode(dataToSend.toString().getBytes());

    try
    {
      boolean status =   dataSenderSocket.send(encodedData);

      System.out.println("Status Send "+status);
    }
    catch (Exception exception)
    {
      System.out.println("Exception "+exception.getMessage());
    }
    dataToSend.clear();

    totalDeviceToSend.set(0);

    System.out.println("After sending "+dataToSend+"\t"+totalDeviceToSend.get());

  }
}
