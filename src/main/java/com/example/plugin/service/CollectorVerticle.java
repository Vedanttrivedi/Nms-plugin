package com.example.plugin.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import com.jcraft.jsch.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CollectorVerticle extends AbstractVerticle
{
  private static final Logger LOGGER = LoggerFactory.getLogger(CollectorVerticle.class);

  private ConcurrentHashMap<String, JsonObject> provisionedDevices = new ConcurrentHashMap<>();

  @Override
  public void start(Promise<Void> startPromise)
  {
    vertx.eventBus().<JsonObject>consumer("data.collection.start", collectionHandler ->
    {
      handleDataCollection(collectionHandler.body());

    });

    vertx.eventBus().consumer("device.provisioning", this::handleDeviceProvisioning);

    startPromise.complete();
  }

  private void handleDataCollection(JsonObject metricInfo)
  {
    String metricType = metricInfo.getString("metricType");
    JsonArray results = new JsonArray();

    List<Promise<Void>> promises = new ArrayList<>();
    provisionedDevices.forEach((ip, device) -> {
      Promise<Void> promise = Promise.promise();
      promises.add(promise);

      vertx.executeBlocking(future -> {
        try {
          JsonObject result = collectMetrics(ip, device.getString("username"),
            device.getString("password"), metricType);
          future.complete(result);
        } catch (Exception e) {
          future.fail(e);
        }
      }, false, ar -> {
        if (ar.succeeded()) {
          results.add(ar.result());
        } else {
          LOGGER.error("Error collecting metrics for IP: " + ip, ar.cause());
          results.add(new JsonObject().put("ip", ip).put("status", false));
        }
        promise.complete();
      });
    });


    // Set a timeout of 4 seconds
    vertx.setTimer(4000, id -> {
      promises.forEach(promise -> {
        if (!promise.future().isComplete()) {
          promise.complete();
        }
      });
    });
  }

  private void handleDeviceProvisioning(io.vertx.core.eventbus.Message<JsonArray> message) {
    JsonArray devices = message.body();
    devices.forEach(device -> {
      JsonObject jsonDevice = (JsonObject) device;
      String ip = jsonDevice.getString("ip");
      if (jsonDevice.getBoolean("doPolling")) {
        provisionedDevices.put(ip, jsonDevice);
      } else {
        provisionedDevices.remove(ip);
      }
    });
  }

  private JsonObject collectMetrics(String ip, String username, String password, String metricType) throws JSchException {
    JSch jsch = new JSch();
    Session session = jsch.getSession(username, ip, 22);
    session.setPassword(password);
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect(5000);

    try {
      List<String> data = new ArrayList<>();
      String[] commands = metricType.equals("memory") ? getMemoryCommands() : getCpuCommands();

      for (String command : commands) {
        String output = executeCommand(session, command);
        data.add(output);
      }

      return processMetricData(ip, metricType, data);
    } finally {
      session.disconnect();
    }
  }

  private String[] getMemoryCommands() {
    return new String[]{
      "free | awk 'NR==2{print $4}'",
      "free | awk 'NR==2{print $3}'",
      "free | awk 'NR==3{print $2}'",
      "free | awk 'NR==2{print $6}'",
      "df | awk 'NR==4 {print $2}'"
    };
  }

  private String[] getCpuCommands() {
    return new String[]{
      "top -bn1 | grep '%Cpu' | awk '{print $2}'",
      "uptime | awk -F'load average:' '{print $2}' | awk '{print $1}'",
      "ps aux | wc -l",
      "ps -eLF | wc -l",
      "iostat | awk 'NR==4 {print $4}'"
    };
  }

  private String executeCommand(Session session, String command) throws JSchException {
    ChannelExec channel = (ChannelExec) session.openChannel("exec");
    channel.setCommand(command);

    try (InputStream input = channel.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
      channel.connect();
      StringBuilder output = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
      return output.toString().trim();
    } catch (Exception e) {
      LOGGER.error("Error executing command: " + command, e);
      return "";
    } finally {
      channel.disconnect();
    }
  }

  private JsonObject processMetricData(String ip, String metricType, List<String> data) {
    JsonObject result = new JsonObject().put("ip", ip).put("status", true);

    if ("memory".equals(metricType)) {
      result.put("free", data.get(0))
        .put("used", data.get(1))
        .put("swap", data.get(2))
        .put("cached", data.get(3))
        .put("disk_space", data.get(4));
    } else if ("cpu".equals(metricType)) {
      result.put("percentage", Float.parseFloat(data.get(0)))
        .put("load_average", Float.parseFloat(data.get(1)))
        .put("process_counts", Integer.parseInt(data.get(2)))
        .put("threads", Integer.parseInt(data.get(3)))
        .put("io_percent", Float.parseFloat(data.get(4)));
    }

    return result;
  }
}
