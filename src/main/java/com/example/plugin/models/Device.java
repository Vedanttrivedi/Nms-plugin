package com.example.plugin.models;

import java.util.List;

public class Device
{

  private String name; //mpstat | head -n 1 | awk '{print $3}'
  private String architecture; //mpstat | head -n 1 | awk '{print $5}'
  private String ip; //ip -4 addr show wlp0s20f3 | awk 'NR==2 {print $2}' // -4 means inet family
  List<Cpu> cores; //for this load the dummy data as of now for each cpu //mpstat -P coreNumber
  Long processCount; // ps -e | wc -l
  float systemCpuPercentAvg;//mpstat | awk 'NR==4 {print $7}'
  float systemCpuInterruptAvg;//mpstat | awk 'NR==7 {print $9}'
  float systemCpuUserPercentAvg;//mpstat | awk 'NR==4 {print $5}'

  public Device(String name, String architecture, String ip, List<Cpu> cores, Long processCount, float systemCpuInterruptAvg, float systemCpuUserPercentAvg, float systemCpuPercentAvg)
  {

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

  public float getSystemCpuPercentAvg() {
    return systemCpuPercentAvg;
  }

  public void setSystemCpuPercentAvg(float systemCpuPercentAvg) {
    this.systemCpuPercentAvg = systemCpuPercentAvg;
  }

  public double getSystemCpuUserPercentAvg() {
    return systemCpuUserPercentAvg;
  }

  public void setSystemCpuUserPercentAvg(float systemCpuUserPercentAvg) {
    this.systemCpuUserPercentAvg = systemCpuUserPercentAvg;
  }

  public double getSystemCpuInterruptAvg() {
    return systemCpuInterruptAvg;
  }

  public void setSystemCpuInterruptAvg(float systemCpuInterruptAvg) {
    this.systemCpuInterruptAvg = systemCpuInterruptAvg;
  }
}
