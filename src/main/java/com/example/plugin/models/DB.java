package com.example.plugin.models;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Random;

public class DB
{

  private final JsonArray array = new JsonArray();

  private final Random random = new Random();

  public DB()
  {

    for (int i = 1; i <= 10; i++)
    {
      JsonObject deviceObject = createDummyDevice(i);

      array.add(deviceObject);
    }
  }

  private JsonObject createDummyDevice(int id)
  {

    var name = "Device " + id;

    var architecture = (id % 2 == 0) ? "x86_64" : "arm";

    var ip = "192.168.1." + id;

    var processCount = random.nextInt(1000) + 100;

    var systemCpuPercentAvg = random.nextFloat() * 100;

    var systemCpuInterruptAvg = random.nextFloat() * 10;

    var systemCpuUserPercentAvg = random.nextFloat() * 100;

    var cores = new JsonArray();

    for (int coreId = 0; coreId < 4; coreId++)
    {

      cores.add(createDummyCpu(coreId));

    }

    var deviceObject = new JsonObject()

      .put("name", name)

      .put("architecture", architecture)

      .put("ip", ip)

      .put("processCount", processCount)

      .put("systemCpuPercentAvg", systemCpuPercentAvg)

      .put("systemCpuInterruptAvg", systemCpuInterruptAvg)

      .put("systemCpuUserPercentAvg", systemCpuUserPercentAvg)

      .put("cores", cores);

    return deviceObject;
  }

  private JsonObject createDummyCpu(int coreId)
  {

    var usr = random.nextFloat() * 100;

    var sys = random.nextFloat() * 100;

    var iowait = random.nextFloat() * 10;

    var idle = 100 - (usr + sys + iowait);

    var processes = random.nextInt(100);

    return new JsonObject()
      .put("usr", usr)

      .put("sys", sys)

      .put("iowait", iowait)

      .put("idle", Math.max(0, idle))

      .put("processes", processes);

  }

  public JsonArray getArray()
  {
    return array;
  }
}
