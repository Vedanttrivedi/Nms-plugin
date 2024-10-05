package com.example.plugin.models;


public abstract class Metric
{
  private String ip;



  public Metric(String ip)
  {
    this.ip = ip;
  }

  public String getIp()
  {
    return ip;
  }

}
