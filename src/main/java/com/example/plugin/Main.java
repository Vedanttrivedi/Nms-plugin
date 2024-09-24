package com.example.plugin;

import com.example.plugin.service.ProcessingRunnable;

public class Main
{

  public static void main(String[] args)
  {

    try
    {
      Thread processingThread = new Thread(new ProcessingRunnable());

      processingThread.start();

    }
    catch (Exception exception)
    {

      System.out.println("Exception in shadow main "+exception.getMessage());

    }
  }
}
