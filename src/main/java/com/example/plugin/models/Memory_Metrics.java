package com.example.plugin.models;

import io.vertx.core.json.JsonObject;


public class Memory_Metrics extends Metric
{
  private String free;
  private String used;
  private String swap;
  private String cached;
  private String disk_space;
  private boolean status;

  public Memory_Metrics(String ip,String free, String used, String swap, String cached, String disk_space,boolean status)
  {

    super(ip);
    this.free=free;
    this.cached=cached;
    this.disk_space=disk_space;
    this.swap =swap;
    this.status = status;
    this.used = used;

  }

  public String getFree() {
    return free;
  }

  public String getUsed()
  {
    return used;
  }

  public String getSwap()
  {
    return swap;
  }

  public String getCached()
  {
    return cached;
  }

  public boolean isStatus() {
    return status;
  }

  public String getDisk_space() {
    return disk_space;
  }

  public JsonObject toJson(Memory_Metrics memoryMetrics)
  {
    var memoryObjectInJson = new JsonObject();

    memoryObjectInJson.put("free",free);

    memoryObjectInJson.put("used",used);

    memoryObjectInJson.put("swap",swap);

    memoryObjectInJson.put("cache",cached);

    memoryObjectInJson.put("disk_space",disk_space);


    return memoryObjectInJson;

  }
}
