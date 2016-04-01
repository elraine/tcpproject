import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.net.InetAddress;


public class Client implements Runnable{
  //static final int port = 8080;
  String inetcfg;
  int port;

  Client(String inetcfg, int port){
    this.inetcfg = inetcfg;
    this.port = port;
  }
  public void run(){
    InetAddress iadr = null;
    Socket socket = null;
    BufferedReader plec = null;
    PrintWriter pred = null;

    System.out.println("My thread is in running state.");

    Scanner sc = new Scanner(System.in);
    //args[0] is ip adress, args[1] is connection port
    try{
      iadr = InetAddress.getByName(inetcfg);
    }catch(UnknownHostException uhe){
      uhe.printStackTrace();
    }
    try{
      socket = new Socket(iadr, port);
    }catch(IOException ie){
      ie.printStackTrace();
    }
    System.out.println("Socket is " + socket);

    //ReadingInfos
    try{
      plec = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }catch(IOException ie){
      ie.printStackTrace();
    }
    //Outputting Info
    try{
      pred = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
    }catch(IOException ie){
      ie.printStackTrace();
    }
    //putting Strings

    String str = "Hello this is Client from " + port;
    int i = 0;
    while(i < 10){
      //str = sc.nextLine();//getInput
      pred.println(str); //send input to serv
      if(str.equals("/q")) break;
      i++;
    }
    System.out.println("/q");
    pred.println("/q");
    try{
      plec.close();
      pred.close();
      socket.close();
    }catch(IOException ie){
      ie.printStackTrace();
    }

  }

  public static void main(String[] args) throws Exception{
    if(args.length < 2 ){
      System.out.println("usage : IPAdress port");
    }else{
      /*
      inetcfg = args[0];
      port = Integer.parseInt(args[1]);
      */
      Client obj = new Client("localhost", 8080);
      Client obj1 = new Client("localhost", 8081);
      Client obj2 = new Client("localhost", 8082);
      Thread tobj = new Thread(obj);
      Thread tobj1 = new Thread(obj1);
      Thread tobj2 = new Thread(obj2);
      tobj.start();
      tobj1.start();
      tobj2.start();
    }
  }
}
