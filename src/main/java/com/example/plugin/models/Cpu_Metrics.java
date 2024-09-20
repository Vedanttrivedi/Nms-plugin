package com.example.plugin.models;

import io.vertx.core.json.JsonObject;

public class Cpu_Metrics extends Metric
{
  private float percentage;
  private float load_average;
  private int process_counts;
  private int threads;
  private float io_percent;
  private boolean status;


  public Cpu_Metrics(String ip, float percentage, float load_average, int process_counts, int threads, float io_percent, boolean status)
  {
    super(ip);

    this.percentage = percentage;

    this.load_average = load_average;

    this.process_counts = process_counts;

    this.threads = threads;

    this.io_percent = io_percent;

    this.status = status;
  }

  public float getPercentage()
  {
    return percentage;
  }

  public float getLoad_average()
  {
    return load_average;
  }

  public int getProcess_counts()
  {
    return process_counts;
  }

  public int getThreads()
  {
    return threads;
  }

  public float getIo_percent()
  {
    return io_percent;
  }

  public boolean isStatus()
  {
    return status;
  }

  public JsonObject toJson(Cpu_Metrics cpu_metrics)
  {
    var cpuObjectInJson = new JsonObject();

    cpuObjectInJson.put("percentage",percentage);

    cpuObjectInJson.put("threads",threads);

    cpuObjectInJson.put("io_percent",io_percent);

    cpuObjectInJson.put("process_counts",process_counts);

    cpuObjectInJson.put("load_average",load_average);

    return cpuObjectInJson;

  }
}

