package com.example.plugin.test;

import io.vertx.core.json.JsonObject;

import java.util.Base64;

public class Tester
{
  public static void main(String[] args)
  {
    String data = "Hello World";

    String base64String = Base64.getEncoder().encodeToString(data.getBytes());

    System.out.println("Base 64 : "+base64String);

    byte[] bytser = Base64.getDecoder().decode(base64String);

    String decodedString = new String(bytser);

    System.out.println("Decoded : "+decodedString);
  }
}
