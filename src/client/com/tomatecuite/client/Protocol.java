package com.tomatecuite.client;

import com.tomatecuite.*;
import java.util.*;
import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Protocol{
    private static Protocol instance;

    public Protocol() {
    }

    public static Protocol getInstance() {
        if (instance == null)
            instance = new Protocol();
        return instance;
    }


    public void pAnnounce(ClientConnector connector,
                              List<FilePeerDescriptor> seededFiles, List<FilePeerDescriptor> leechedFiles) throws InvalidAnswerException {
        //announce  listen  $Port  seed [ $Filename1 $Length1 $PieceSize1 $Key1  $Filename2 $Length2 $PieceSize2 $Key2 ...]  leech [ $Key3  $Key4 ...]
        //awaits a "OK" from server
        int hostPort = Configuration.getInstance().getPropertyAsInt(Constants.PEER_PORT_KEY, 0);

        FileStorage fs = FileStorage.getInstance();
        String announce = "announce listen " + hostPort + " seed ";

        for (int i = 0; i < seededFiles.size(); i++) {
            announce = announce + "[" + ( seededFiles.get(i)).getName()+ " "
                    + Integer.toString(seededFiles.get(i).getFileSize()) + " "
                    + String.valueOf(seededFiles.get(i).getPieceSize()) + " "
                    + seededFiles.get(i).getKey() + " ";
        }
        announce = announce + "]";

        connector.initConnection();
        connector.write(announce);
        String response = connector.read();

        if (response == null
                || !response.startsWith(InputMessagesPatternsBundle._OK_CST)) {
            throw new InvalidAnswerException(response);
        }

        // Close connection with the tracker
        connector.closeConnection();
    }

    private boolean pLook(String[] criterion) {
        //look [ $Criterion1 $Criterion2  ...]
        //serv : list [ $Filename1 $Length1 $PieceSize1 $Key1  $Filename2 $Length2 $PieceSize2 $Key2 ...]

//        filename="..."
//        filesize>"..." | filesize<"..."           One of the two or none.
//        piecesize>"..." | piecesize<"..."		  One of the two or none.

        String toserv = "look ";
        for (int i = 0; i < criterion.length; i++) {

        }
        System.out.println(toserv);


        String getStr = (new Scanner(System.in)).nextLine();
        return getStr.startsWith("list");
    }
    private ArrayList<Peer> pGetFile(String key) {
        //getfile  $Key
        //peers $ Key  [ $IP1:$Port1 $ IP2:$Port2 ...  ]
        String toserv = "getfile " + key;
        System.out.println(toserv);


        String servanswer = (new Scanner(System.in)).nextLine();
        String debut = "peers " +key;
        String[] peerpart={""};
        if(servanswer.startsWith(debut)) {
            peerpart = servanswer.split(" ", 3);
        }
        ArrayList<Peer> arPeer = null;
        if(peerpart.length > 2) {
            String[] couple = peerpart[2].split(" ");
            arPeer = new ArrayList<Peer>();
            for (int i = 0; i < couple.length; i++) {
                String[] ipport = couple[i].split(":", 2);
                arPeer.add(new Peer(ipport[0], Integer.valueOf(ipport[1])));
            }
        }
        return arPeer;
    }

    private boolean pInterested(String key, FilePeerDescriptor fpd) {
        // interested  $Key
        //have  $Key  $BufferMap
        FileStorage fs = FileStorage.getInstance();

        String toserv = "interested " + key;
        System.out.println(toserv);

        String servanswer = (new Scanner(System.in)).nextLine();
        String debut = "have " +key;
        String[] peerpart={""};
        if(servanswer.startsWith(debut)) {
            peerpart = servanswer.split(" ", 3);
        }
        BufferMap bm = fpd.getBufferMap();
        BufferMap receivedBm = new BufferMap();
        if(peerpart.length > 1){
            receivedBm.stringToBufferMap(peerpart[2]);
        }
        int rbm = receivedBm.cardinality();
        if(rbm > bm.cardinality()){
            fs.addLeechedFile(fpd);
            return true;
        }
        return false;
    }


private boolean sendRegularInterval(FilePeerDescriptor fpd) throws Exception{
    Timer t = new Timer("Vador", true);
    int updateValue =  Integer.valueOf(Constants.UPDATE_FREQUENCY_KEY);
    try {
        t.scheduleAtFixedRate(new TaskRepeating(fpd), 30000, updateValue * 60 * 1000);
    }catch(Exception e){
        e.printStackTrace();
    }
    return true;
}



private class TaskRepeating extends TimerTask{
    FilePeerDescriptor fpd;

    public TaskRepeating(FilePeerDescriptor fpd) {
        this.fpd = fpd;
    }

    public void run(){
        System.out.println("Roar, i am the timer");
        pHave(this.fpd);
        pUpdateToTracker();
    }

    private boolean pHave(FilePeerDescriptor fpd){
        //< have $ Key  $BufferMap
        //> have  $Key  $BufferMap

        FileStorage fs = FileStorage.getInstance();

        String toserv = "have " + fpd.getKey() + " " + fpd.getBufferMap().getStringForm();
        System.out.println(toserv);

        String servanswer = (new Scanner(System.in)).nextLine();
        String debut = "have " + fpd.getKey();
        String[] peerpart={""};
        if(servanswer.startsWith(debut)) {
            peerpart = servanswer.split(" ", 3);
        }

        BufferMap bm = fpd.getBufferMap();
        BufferMap receivedBm = new BufferMap();
        if(peerpart.length > 1){
            receivedBm.stringToBufferMap(peerpart[2]);
        }
        int rbm = receivedBm.cardinality();
        if(rbm > bm.cardinality()){
            fs.addLeechedFile(fpd);
            return true;
        }
        return false;
    }

    private boolean pUpdateToTracker(){
//        < update  seed [$ Key1 $Key2 $Key3 ...] leech [ $Key10 $Key11 $Key12 ... ]
        FileStorage fs = FileStorage.getInstance();
        List<FilePeerDescriptor> lfpd = fs.getFilesList();
        String toserv = "update seed [";
        for (int i = 0; i < lfpd.size(); i++) {
            toserv += lfpd.get(i) + " ";
        }

        toserv += ']';
        toserv += " leech ";
        List<FilePeerDescriptor> llfpd = fs.getLeechList();
        for (int i = 0; i < lfpd.size(); i++) {
            toserv += llfpd.get(i).getKey() + " ";
        }

        return true;
    }
}






}
