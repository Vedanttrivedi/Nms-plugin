package com.example.plugin.service;

import com.example.plugin.models.Cpu_Metrics;
import com.example.plugin.models.Memory_Metrics;
import com.example.plugin.models.Metric;
import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


//This class should be responsible for getting all the information based on the commands list it received

public class FetchDetails implements Callable<Metric>
{

  String username;

  String password;

  String host;

  String[] commands;

  String metric;

  String[] cpuMetricsCommand = {
    "top -bn1 | grep '%Cpu' | awk '{print $2}'" ,//CPU spent in system processes
      "uptime | awk -F'load average:' '{print $2}' | awk '{print $1}'" ,//1-minute load average, no of processes waiting to execute on cpu
      "ps aux | wc -l", //count total no of running processes
      "ps -eLF | wc -l",//count total threads //-L total no of threads
      "iostat | awk 'NR==4 {print $4}'" //iowait
  };
  String[] mem_commnds = {
    "free | awk 'NR==2{print $4}'",//Free memory
    "free | awk 'NR==2{print $3}'",//Used memory
    "free | awk 'NR==3{print $2}'",//Swap memory
    "free | awk 'NR==2{print $6}'",//Cache memory
    "df | awk 'NR==4 {print $2}'"//disk space
  };

  public FetchDetails(String username,String password,String host,String metric)
  {

    this.username = username;
    this.password = password;
    this.host = host;
    this.metric = metric;

  }

  @Override
  public Metric call()
  {

    try
    {
      JSch jsch = new JSch();

      Session session = jsch.getSession(username, host, 22);

      session.setTimeout(2000);

      session.setPassword(password);

      session.setConfig("StrictHostKeyChecking", "no");

      session.connect();

      List<String> data = new ArrayList<>();

      var commands = metric.equals("memory")?mem_commnds:cpuMetricsCommand;

      System.out.println("Commands for metric : "+metric+"\t"+commands);

      for (String command : commands)
      {

        var output = executeMemoryCommand(session, command);


        if(output.endsWith(","))
        {

          var removeComma =output.substring(0,output.length()-1);

          data.add(removeComma);

          continue;
        }

        data.add(output);

      }

      session.disconnect();

      if(metric.equals("memory"))
        return  new Memory_Metrics(host,data.get(0),data.get(1),data.get(2),data.get(3),data.get(4),true);

      System.out.println("CPU data length "+data);

      return  new Cpu_Metrics(
        host,
        Float.parseFloat(data.get(0)),
        Float.parseFloat(data.get(1)),
        Integer.parseInt(data.get(2)),
        Integer.parseInt(data.get(3)),
        Float.parseFloat(data.get(4)),true
      );

    }

    catch (Exception exception)
    {
      // Handle any other errors
      System.out.println("Exception :"+exception.getMessage());

      return metric.equals("memory")?new Memory_Metrics(host,"0","0","0","0","0",false)
        :new Cpu_Metrics(host,0,0,0,0,0,false);
    }

  }


  private static String executeMemoryCommand(Session session, String command)
  {
    StringBuilder output = new StringBuilder();

    Channel channel = null;

    BufferedReader reader = null;

    try
    {
      // Open an SSH channel for executing commands
      channel = session.openChannel("exec");
      ((ChannelExec) channel).setCommand(command);

      // Get the command's output
      InputStream input = channel.getInputStream();
      channel.connect();  // Connect to the channel

      reader = new BufferedReader(new InputStreamReader(input));
      String line;

      while ((line = reader.readLine()) != null)
      {

        output.append(line).append("\n");

      }

    }
    catch (Exception e)
    {
      e.printStackTrace(); // Handle command execution errors
    }
    finally
    {
      // Clean up and close the channel and reader
      try
      {
        if (reader != null) reader.close();
        if (channel != null) channel.disconnect();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    return output.toString().trim();  // Return the result
  }
}

