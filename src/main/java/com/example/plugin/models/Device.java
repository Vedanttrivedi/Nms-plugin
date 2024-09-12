package com.example.plugin.models;

import java.util.List;

public class Device
{
  private String name, architecture,ip;
  List<Cpu> cores;
  Long processCount;
  double systemCpuPercentAvg,systemCpuInterruptAvg;
  double systemCpuUserPercentAvg;

  public Device(String name, String architecture, String ip, List<Cpu> cores, Long processCount, double systemCpuInterruptAvg, double systemCpuUserPercentAvg, double systemCpuPercentAvg) {
    this.name = name;
    this.architecture = architecture;
    this.ip = ip;
    this.cores = cores;
    this.processCount = processCount;
    this.systemCpuInterruptAvg = systemCpuInterruptAvg;
    this.systemCpuUserPercentAvg = systemCpuUserPercentAvg;
    this.systemCpuPercentAvg = systemCpuPercentAvg;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getArchitecture() {
    return architecture;
  }

  public void setArchitecture(String architecture) {
    this.architecture = architecture;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public Long getProcessCount() {
    return processCount;
  }

  public void setProcessCount(Long processCount) {
    this.processCount = processCount;
  }

  public List<Cpu> getCores() {
    return cores;
  }

  public void setCores(List<Cpu> cores) {
    this.cores = cores;
  }

  public double getSystemCpuPercentAvg() {
    return systemCpuPercentAvg;
  }

  public void setSystemCpuPercentAvg(double systemCpuPercentAvg) {
    this.systemCpuPercentAvg = systemCpuPercentAvg;
  }

  public double getSystemCpuUserPercentAvg() {
    return systemCpuUserPercentAvg;
  }

  public void setSystemCpuUserPercentAvg(double systemCpuUserPercentAvg) {
    this.systemCpuUserPercentAvg = systemCpuUserPercentAvg;
  }

  public double getSystemCpuInterruptAvg() {
    return systemCpuInterruptAvg;
  }

  public void setSystemCpuInterruptAvg(double systemCpuInterruptAvg) {
    this.systemCpuInterruptAvg = systemCpuInterruptAvg;
  }
}
