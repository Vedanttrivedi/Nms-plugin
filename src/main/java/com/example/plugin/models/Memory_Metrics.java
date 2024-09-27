package com.example.plugin.models;

import io.vertx.core.json.JsonObject;


public class Memory_Metrics extends Metric
{
  private int free;
  private int used;
  private int swap;
  private int cached;
  private int disk_space;
  private boolean status;


  public Memory_Metrics(String ip, int free, int used, int swap, int cached, int disk_space, boolean status) {
    super(ip);
    this.free = free;
    this.used = used;
    this.swap = swap;
    this.cached = cached;
    this.disk_space = disk_space;
    this.status = status;
  }


  public int getFree()
  {
    return free;
  }

  public int getUsed() {
    return used;
  }

  public int getSwap() {
    return swap;
  }

  public int getCached() {
    return cached;
  }

  public int getDisk_space() {
    return disk_space;
  }

  public boolean isStatus() {
    return status;
  }
  public JsonObject toJson()
  {
    return new JsonObject()
      .put("ip",getIp())
      .put("free", free)
      .put("used", used)
      .put("swap", swap)
      .put("cached", cached)
      .put("disk_space", disk_space)
      .put("status", status);
  }

}


