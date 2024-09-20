package com.example.plugin.models;

import java.sql.Timestamp;

public abstract class Metric
{
  private String ip;

  private Timestamp created_at;


  public Metric(String ip)
  {
    this.ip = ip;
  }

  public String getIp()
  {
    return ip;
  }

}
