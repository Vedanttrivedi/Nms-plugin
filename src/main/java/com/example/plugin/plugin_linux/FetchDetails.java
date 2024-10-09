package com.example.plugin.plugin_linux;

import com.example.plugin.models.Cpu_Metrics;
import com.example.plugin.models.Memory_Metrics;
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

  final static String[] cpuMetricsCommand = {
    "top -bn1 | grep '%Cpu' | awk '{print $2}'",   // CPU spent in system processes
    "uptime | awk -F'load average:' '{print $2}' | awk '{print $1}'", // 1-minute load average
    "ps aux | wc -l",                             // Count total no of  processes
    "ps -eLF | wc -l",                            // Count total threads
    "iostat | awk 'NR==4 {print $4}'"             // I/O wait
  };

  final  String memoryCommand = "free";

  final static String diskSpaceCommand = "df | awk 'NR==4 {print $2}'";

  @Override
  public void start(Promise<Void> startPromise) throws Exception
  {

    vertx.eventBus().<JsonObject>localConsumer(Config.FETCH, device ->
    {

      var jsonDevice = device.body();

      vertx.executeBlocking(fetchFuture ->
      {

        var deviceMetricData = connectAndExecuteCommands(jsonDevice.getString("username"),
          jsonDevice.getString("password"), jsonDevice.getString("ip"), jsonDevice.getString("metric"));

        deviceMetricData.put("time", LocalDateTime.now().toString());

        deviceMetricData.put("metric", jsonDevice.getString("metric"));

        fetchFuture.complete(deviceMetricData);

      },false, fetchFutureRes ->
      {

        if (fetchFutureRes.failed())
          System.out.println("Not able to collect the details ");

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

      var commands = new ArrayList<String>();

      if (metric.equals("memory"))
      {
        commands.add(memoryCommand);

        commands.add(diskSpaceCommand);
      }
      else if (metric.equals("cpu"))
      {
        commands.addAll(Arrays.asList(cpuMetricsCommand));
      }

      var output = executeCommands(session, commands);

      session.disconnect();

      if (metric.equals("memory"))
        return  parseMemoryMetrics(output, ip);

     return parseCpuMetrics(output, ip);

    }
    catch (Exception exception)
    {

      System.out.println("Exception: " + exception.getMessage());

      var errorObject = new JsonObject();

      errorObject.put("status", false);

      errorObject.put("ip", ip);

      return errorObject;

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

    return new Memory_Metrics(
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

    return new Cpu_Metrics(
      ip,
      Float.parseFloat(output.get(0)),
      Float.parseFloat(output.get(1)),
      Integer.parseInt(output.get(2)),
      Integer.parseInt(output.get(3)),
      Float.parseFloat(output.get(4)),
      true).toJson();

  }

}
