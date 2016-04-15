import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FilePeerDescriptor{
  String name;
  long length;
  int pieceSize;
  String key;

  public FilePeerDescriptor(String fileName, long fileLength){
    name = fileName;
    length = fileLength;
    key = encode(name);
  }

  private static String encode(String password){
    byte[] uniqueKey = password.getBytes();
    byte[] hash = null;
    try{
      hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
    }
    catch (NoSuchAlgorithmException e){
      throw new Error("No MD5 support in this VM.");
    }

    StringBuilder hashString = new StringBuilder();
    for (int i = 0; i < hash.length; i++){
      String hex = Integer.toHexString(hash[i]);
      if (hex.length() == 1){
        hashString.append('0');
        hashString.append(hex.charAt(hex.length() - 1));
      }
      else
        hashString.append(hex.substring(hex.length() - 2));
    }
    return hashString.toString();
  }

  private static int BufferMap(){
    int arrLength = length % pieceSize;
    int bm = new int[arrlength+1];
    return bm;
  }
}
