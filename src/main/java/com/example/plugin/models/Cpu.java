package com.example.plugin.models;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public record Cpu(float usr, float sys, float iowait, float idle, int processes)
{
  public JsonObject toJson()
  {

    return new JsonObject()
      .put("usr",usr)
      .put("sys",sys)
      .put("iowait",iowait)
      .put("idle",idle)
      .put("processes",processes);

  }

  public static Cpu fromJson(JsonObject object)
  {
    //static method is taken as it does not belong to perticular object
    //instead it returns new object based on map provided

    var cpu = new Cpu(
      object.getFloat("usr"),
      object.getFloat("sys"),
      object.getFloat("iowait"),
      object.getFloat("idle"),
      object.getInteger("proceses")
      );

    return cpu;

  }
}
