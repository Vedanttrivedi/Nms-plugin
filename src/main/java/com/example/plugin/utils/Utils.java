package com.example.plugin.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Utils
{
  public static String decryptPassword(String encryptedData, String secretKey,String encryptionAlgorithm)
  {
    try
    {

      Cipher cipher = Cipher.getInstance(encryptionAlgorithm);

      var secretsKey =stringToSecretKey(secretKey,encryptionAlgorithm);

      System.out.println("New Secreter Key "+secretKey);

      cipher.init(Cipher.DECRYPT_MODE, secretsKey);

      var decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));

      System.out.println("Final password "+new String(decryptedBytes));

      return new String(decryptedBytes);
    }

    catch (Exception exception)
    {
      System.out.println("Exception "+exception.getMessage());
      return encryptedData;

    }
  }

  private static SecretKey stringToSecretKey(String secretKeyString,String encryptionAlgorithm)
  {
    var decodedKey = Base64.getDecoder().decode(secretKeyString);

    System.out.println("Decoded Key "+decodedKey);

    return new SecretKeySpec(decodedKey, 0, decodedKey.length, encryptionAlgorithm);

  }

}
