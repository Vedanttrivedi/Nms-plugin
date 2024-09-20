package com.example.plugin.test;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.*;

public class Jscher
{
  public static void main(String[] args)
  {
    String host = "localhost"; // SSH server address

    String user = "vedant";    // SSH username
    String password = "Vedant#123"; // SSH password

    try
    {

      JSch jsch = new JSch();

      Session session = jsch.getSession(user, host, 22);

      session.setPassword(password);

      session.setConfig("StrictHostKeyChecking", "no");

      session.connect();

      Channel channel = session.openChannel("exec");

      ((ChannelExec) channel).setCommand("ps -eLF | wc -l");

      channel.setInputStream(null);

      ((ChannelExec) channel).setErrStream(System.err);

      channel.connect();

      java.io.InputStream in = channel.getInputStream();

      byte[] buffer = new byte[1024];

      int bytesRead;

      while ((bytesRead = in.read(buffer)) != -1)
      {

        System.out.print(new String(buffer, 0, bytesRead));

      }

      channel.disconnect();

      session.disconnect();

    }
    catch (Exception e)
    {

      e.printStackTrace();

    }
  }
}
