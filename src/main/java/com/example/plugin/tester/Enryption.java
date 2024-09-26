package com.example.plugin.tester;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Enryption
{

  public static String encrypt(String data, SecretKey secretKey) throws Exception
  {
    Cipher cipher = Cipher.getInstance("AES");

    cipher.init(Cipher.ENCRYPT_MODE, secretKey);

    byte[] encryptedBytes = cipher.doFinal(data.getBytes());

    return Base64.getEncoder().encodeToString(encryptedBytes);

  }

  public static String decrypt(String encryptedData, SecretKey secretKey) throws Exception
  {
    Cipher cipher = Cipher.getInstance("AES");

    cipher.init(Cipher.DECRYPT_MODE, secretKey);

    byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));

    return new String(decryptedBytes);
  }

  public static SecretKey generateKey() throws Exception
  {

    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

    keyGenerator.init(128); // You can use 192 or 256 bits as well

    return keyGenerator.generateKey();

  }

  public static void main(String[] args)
  {
    try
    {

      SecretKey secretKey = generateKey();

      String originalData = "Hello, World!";
      System.out.println("Original Data: " + originalData);

      String encryptedData = encrypt(originalData, secretKey);
      System.out.println("Encrypted Data: " + encryptedData);

      String decryptedData = decrypt(encryptedData, secretKey);
      System.out.println("Decrypted Data: " + decryptedData);

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}

