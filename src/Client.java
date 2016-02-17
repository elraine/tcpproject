import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client{
  //static final int port = 8080;

  public static void main(String[] args) throws Exception{
    Scanner sc = new Scanner(System.in);
    Socket socket = new Socket(args[0], args[1]);
    System.out.println("Socket is " + socket);

    //ReadingInfos
    BufferedReader plec = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    //Outputting Info
    PrintWriter pred = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

    //putting Strings
    String str = "";
    while(true){
      str = sc.nextLine();//getInput
      if(str.equals("/q")) break;
      pred.println(str); //send input to serv
    }
    System.out.println("/q");
    pred.println("/q");
    plec.close();
    pred.close();
    socket.close();

  }
}
