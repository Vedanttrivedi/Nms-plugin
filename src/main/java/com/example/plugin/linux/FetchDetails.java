package com.example.plugin.linux;

import com.example.plugin.models.CpuMetrics;
import com.example.plugin.models.MemoryMetrics;
import com.example.plugin.utils.Config;
import com.jcraft.jsch.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class FetchDetails extends AbstractVerticle
{

  final static Map<String,List<String>> metricCommands = new HashMap<>();

  static {

    metricCommands.put("memory",Arrays.asList("free","df | awk 'NR==4 {print $2}'"));

    metricCommands.put("cpu", Arrays.asList(
      "echo $(top -bn1 | grep '%Cpu' | awk '{print $2}') $(uptime | awk -F'load average:' '{print $2}' | awk '{print $1}') $(ps aux | wc -l) $(ps -eLF | wc -l) $(iostat | awk 'NR==4 {print $4}')"

    ));

  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {

    vertx.eventBus().<JsonObject>localConsumer(Config.FETCH, device ->
    {

      var jsonDevice = device.body();

      vertx.executeBlocking(fetchFuture ->
      {

        var deviceMetricData = connectAndExecuteCommands(jsonDevice.getString("username"), jsonDevice.getString("password"), jsonDevice.getString("ip"), jsonDevice.getString("metric"));

        deviceMetricData.put("time", LocalDateTime.now().toString());

        deviceMetricData.put("metric", jsonDevice.getString("metric"));

        fetchFuture.complete(deviceMetricData);

      },false, fetchFutureRes ->
      {

        if (fetchFutureRes.failed())
          System.out.println("Not able to collect the details for "+device);

        else
          vertx.eventBus().send(Config.SEND, fetchFutureRes.result());

      });

    });

    startPromise.complete();

  }

  private JsonObject connectAndExecuteCommands(String username, String password, String ip, String metric)
  {
    try
    {
      var jsch = new JSch();

      var session = jsch.getSession(username, ip, 22);

      session.setTimeout(2000);

      session.setPassword(password);

      session.setConfig("StrictHostKeyChecking", "no");

      session.connect();

      var commands = metricCommands.get(metric);

      var output = executeCommands(session, commands);

      System.out.println("Output "+output);

      session.disconnect();

      if (metric.equals("memory"))
        return  parseMemoryMetrics(output, ip);

     return parseCpuMetrics(output, ip);

    }
    catch (Exception exception)
    {
      return new JsonObject()

      .put("status", false)

      .put("ip", ip);

    }

  }

  private static List<String> executeCommands(Session session, List<String> commands)
  {
    var results = new ArrayList<String>();

    Channel channel = null;

    BufferedReader reader = null;

    try
    {
      for (String command : commands)
      {
        channel = session.openChannel("exec");

        ((ChannelExec) channel).setCommand(command);

        InputStream input = channel.getInputStream();

        channel.connect();

        reader = new BufferedReader(new InputStreamReader(input));

        StringBuilder output = new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null)
        {
          if (line.contains(","))
          {

            line = line.substring(0, line.length() - 1);

          }
          output.append(line).append("\n");
        }

        results.add(output.toString().trim());

        channel.disconnect();
      }
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
    }
    finally
    {
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
    return results;
  }

  private JsonObject parseMemoryMetrics(List<String> output, String ip)
  {
    var memoryOutput = output.get(0);

    var diskSpaceOutput = output.size() > 1 ? output.get(1) : "0";

    var lines = memoryOutput.split("\n");

    var memoryData = lines[1].split("\\s+");

    var swapData = lines[2].split("\\s+");

    return new MemoryMetrics(
      ip,

      Integer.parseInt(memoryData[3]),

      Integer.parseInt(memoryData[2]),

      Integer.parseInt(swapData[1]),

      Integer.parseInt(memoryData[5]),

      Integer.parseInt(diskSpaceOutput),

      true).toJson();
  }

  private JsonObject parseCpuMetrics(List<String> output, String ip)
  {
    var data = output.get(0).split(" ");

    return new CpuMetrics(

      ip,

      Float.parseFloat(data[0]),

      Float.parseFloat(data[1].substring(0,data[1].length()-1)),

      Integer.parseInt(data[2]),

      Integer.parseInt(data[3]),

      Float.parseFloat(data[4]),

      true).toJson();

  }

}

//
//  final static String[] cpuMetricsCommand = {
//    "top -bn1 | grep '%Cpu' | awk '{print $2}'",   // CPU spent in system processes
//    "uptime | awk -F'load average:' '{print $2}' | awk '{print $1}'", // 1-minute load average
//    "ps aux | wc -l",                             // Count total no of  processes
//    "ps -eLF | wc -l",                            // Count total threads
//    "iostat | awk 'NR==4 {print $4}'"             // I/O wait
//  };
//
//  final static String memoryCommand = "free";
//
//  final static String diskSpaceCommand = "df | awk 'NR==4 {print $2}'";
//
//  final static String[] cpuMetricsCommand = {
//    "top -bn1 | grep '%Cpu' | awk '{print $2}'",   // CPU spent in system processes
//    "uptime | awk -F'load average:' '{print $2}' | awk '{print $1}'", // 1-minute load average
//    "ps aux | wc -l",                             // Count total no of  processes
//    "ps -eLF | wc -l",                            // Count total threads
//    "iostat | awk 'NR==4 {print $4}'"             // I/O wait
//  };
//
//  final static String memoryCommand = "free";
//
//  final static String diskSpaceCommand = "df | awk 'NR==4 {print $2}'";
