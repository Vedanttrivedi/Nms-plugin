package com.example.plugin.models;
import io.vertx.core.json.JsonObject;


public class MemoryMetrics extends Metric
{
  private int free;
  private int used;
  private int swap;
  private int cached;
  private int disk_space;
  private boolean status;


  public MemoryMetrics(String ip, int free, int used, int swap, int cached, int disk_space, boolean status)
  {
    super(ip);
    this.free = free;
    this.used = used;
    this.swap = swap;
    this.cached = cached;
    this.disk_space = disk_space;
    this.status = status;
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


