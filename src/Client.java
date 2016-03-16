import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.net.InetAddress;


public class Client implements Runnable{
  private String inetcfg;
  private int port;
  private String msg = "i";

  private String serverAddr;
  private String serverPort;
  private String sizePiece;

  private Fichier[] files;

  public Client(String inetcfg, int port){
    this.inetcfg = inetcfg;
    this.port = port;
    listFiles("../src");
  }

  public Client(String inetcfg, int port, String msg){
    this.inetcfg = inetcfg;
    this.port = port;
    this.msg = msg;
    listFiles("../src");
  }

  public void listFiles(String directoryName){
    File directory = new File(directoryName);
    File[] fileList = directory.listFiles();
    int numberOfFiles = directory.listFiles().length;
    int i = 0;

    files = new Fichier[numberOfFiles];

    for(File file : fileList){
      if(file.isFile()){
        files[i] = new Fichier(file.getName(), file.length());
        i++;
      }
    }
  }

  public void configInit() throws FileNotFoundException{
    String filePath="config.ini";
    Scanner scan = new Scanner(new File(filePath));

    while(scan.hasNextLine()){
      String line = scan.nextLine();
      if(line.contains("tracker-address")){
        serverAddr = line.substring(18);
      }
      if(line.contains("tracker-port")){
        serverPort = line.substring(15);
      }
      if(line.contains("piece-size")){
        sizePiece = line.substring(13);
      }
    }
    System.out.println("Server address is "+serverAddr+" and Server port is "+serverPort);
  }

  public void announceTracker(){
    String portAnnounce;
    String sizeAnnounce;

    portAnnounce = serverPort;
    sizeAnnounce = sizePiece;

    System.out.printf("announce listen "+portAnnounce+" seed [");

    for(Fichier file : files){
      System.out.printf(file.name+" "+file.length+" "+sizeAnnounce+" "+file.key+" ");
    }

    System.out.println("]");
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
    String str = "";
    pred.println(msg);
    while(true){
      str = sc.nextLine();//getInput
      if(str.equals("/q")) break;
      pred.println(str); //send input to serv
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
    String filename = "localhost";
    //LOL CONFLIT
    String inetcfg = "localhost";
    int port = 8080;

//     if(args.length <= 0 ){
//       System.out.println("give args : 0 if reading peers from file, 1 if giving args now. %n If 0 : follow the 0 with name of file. %n If 1 : 1 <ipadress> <port>");
//     }else if(Integer.parseInt(args[0]) == 0 && args.length < 3){
// //reads from file TODO
//       filename = args[1];
//       System.out.println("not implemented yet file name is "+filename);
//     }else if(Integer.parseInt(args[0]) == 1 && args.length < 4){
//     inetcfg = args[1];
//     Integer.parseInt(args[2]);
//   }else{
//     System.out.println("not good");
//     // System.exit();
//   }

    //Single thread test
    //\\\\\\\\\\\\\
    // Client obj = new Client(inetcfg, port);
    // Thread tobj = new Thread(obj);
    // tobj.start();

    //Thread test
    //\\\\\\\\\\\\\
    // int baseport = 8080;
    // int maxnb = 3;
    // Client[] object = new Client[maxnb];
    // Thread[] thobj = new Thread[maxnb];
    // for (int i = 0; i < maxnb ; i++) {
    //   System.out.println(baseport +i);
    //   object[i] = new Client("localhost", (baseport +i),"i am number "+i);
    //   thobj[i] = new Thread(object[i]);
    //   thobj[i].start();
    //   Thread.sleep(1000);
    // }

    Client object0 = new Client("localhost", 8080, "i am 0");
    Client object1 = new Client("localhost", 8080, "i am 1");
    Client object2 = new Client("localhost", 8080, "i am 2");

    try{
      object0.configInit();
    }
    catch(FileNotFoundException f){
      System.out.println("File not found");
    }
    object0.announceTracker();

    Thread to0 = new Thread(object0);
    Thread to1 = new Thread(object1);
    Thread to2 = new Thread(object2);
    to0.start();
    to1.start();
    to2.start();


    // Thread tobj = new Thread(obj);
    // tobj.start();
    }

  }
