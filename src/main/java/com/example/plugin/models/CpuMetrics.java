package com.example.plugin.models;

import io.vertx.core.json.JsonObject;

public class CpuMetrics extends Metric
{
  private float percentage;
  private float load_average;
  private int process_counts;
  private int threads;
  private float io_percent;
  private boolean status;


  public CpuMetrics(String ip, float percentage, float load_average, int process_counts, int threads, float io_percent, boolean status)
  {
    super(ip);

    this.percentage = percentage;

    this.load_average = load_average;

    this.process_counts = process_counts;

    this.threads = threads;

    this.io_percent = io_percent;

    this.status = status;
  }


  public JsonObject toJson()
  {
    return new JsonObject()
      .put("ip",getIp())
      .put("percentage", percentage)
      .put("io_percent", io_percent)
      .put("threads", threads)
      .put("process_counts", process_counts)
      .put("load_average", load_average)
      .put("status", status);
  }
}

