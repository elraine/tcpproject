package com.tomatecuite.client;

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


    public void pAnnounce(ActiveConnection connector,
                          List<FilePeerDescriptor> seededFiles, List<FilePeerDescriptor> leechedFiles) throws InvalidAnswerException {
        //announce  listen  $Port  seed [ $Filename1 $Length1 $PieceSize1 $Key1  $Filename2 $Length2 $PieceSize2 $Key2 ...]  leech [ $Key3  $Key4 ...]
        //awaits a "OK" from server
        int hostPort = Configuration.getInstance().getPropertyAsInt(Constants.PEER_PORT_KEY, 0);

        FileStorage fs = FileStorage.getInstance();
        String announce = "announce listen " + hostPort + " seed [";

        for (int i = 0; i < seededFiles.size(); i++) {
            announce +=  ( seededFiles.get(i)).getName()+ " "
                    + Integer.toString(seededFiles.get(i).getFileSize()) + " "
                    + String.valueOf(seededFiles.get(i).getPieceSize()) + " "
                    + seededFiles.get(i).getKey()+" ";
        }
        announce = announce + "]";


        LogWriter.getInstance().writeToLog(announce);
        connector.write(announce);
        String response = connector.read();

        if (response == null
                || !response.startsWith(InputMessagesPatternsBundle._OK_CST)) {
            throw new InvalidAnswerException(response);
        }

        // Close connection with the tracker

    }

    private ArrayList<FilePeerDescriptor> pLook(ClientConnector connector, String[] criterion) {
        //look [ $Criterion1 $Criterion2  ...]
        //serv : list [ $Filename1 $Length1 $PieceSize1 $Key1  $Filename2 $Length2 $PieceSize2 $Key2 ...]

//        filename="..."
//        filesize>"..." | filesize<"..."           One of the two or none.
//        piecesize>"..." | piecesize<"..."		  One of the two or none.

        FileStorage fs = FileStorage.getInstance();
        ArrayList<FilePeerDescriptor> afpd = new ArrayList<FilePeerDescriptor>();

        String toserv = "look ";
        for (int i = 0; i < criterion.length; i++) {
            toserv += criterion[i];
        }

        LogWriter.getInstance().writeToLog(toserv);
        connector.write(toserv);

        String servanswer = connector.read();
        String[] peerpart = {""};


        String debut = "list [";
        if (servanswer.startsWith(debut)) {
            peerpart = servanswer.split("\\[", 2);
        }
        String[] fileList = peerpart[1].split(" ");
        for (int i = 0; i < peerpart.length-3; i++) {
            String name = fileList[i];
            int length = Integer.valueOf(fileList[i+1]);
            int pieceSize =  Integer.valueOf(fileList[i+2]);
            String key = fileList[i+3];
            i+=3;
            afpd.add(new FilePeerDescriptor(name,key,length,pieceSize));
        }

        return afpd;
    }
    public ArrayList<Peer> pGetFile(ActiveConnection connector, String key) {
        //getfile  $Key
        //peers $ Key  [ $IP1:$Port1 $ IP2:$Port2 ...  ]
        String getfile = "getfile " + key;
        System.out.println(getfile);
        connector.write(getfile);

        String servanswer = connector.read();
        String debut = "peers " +key;
        String[] peerpart={""};
        if(servanswer.startsWith(debut)) {
            peerpart = servanswer.split(" ", 3);
        }
        peerpart[2] = peerpart[2].substring(1, (peerpart[2].length()) - 1);
        System.out.println(peerpart[2]);
        ArrayList<Peer> arPeer = null;
        if(peerpart.length > 2) {
            String[] couple = peerpart[2].split(" ");
            arPeer = new ArrayList<Peer>();
            for (int i = 0; i < couple.length; i++) {
                String[] ipport = couple[i].split(":", 2);
                System.out.println("Ip : " + ipport[0] + " & Port: " + ipport[1]);
                arPeer.add(new Peer(ipport[0], Integer.valueOf(ipport[1])));
            }
        }
        return arPeer;
    }

    private boolean pInterested(ClientConnector connector, String key, FilePeerDescriptor fpd) {
        // interested  $Key
        //have  $Key  $BufferMap
        FileStorage fs = FileStorage.getInstance();

        String toserv = "interested " + key;

        LogWriter.getInstance().writeToLog(toserv);
        connector.write(toserv);

        String servanswer = connector.read();
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


    private boolean sendRegularInterval(ClientConnector connector, FilePeerDescriptor fpd) throws Exception{
        Timer t = new Timer("Vador", true);
        int updateValue =  Integer.valueOf(Constants.UPDATE_FREQUENCY_KEY);
        try {
            t.scheduleAtFixedRate(new TaskRepeating(connector,fpd), 30000, updateValue * 60 * 1000);
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    private class TaskRepeating extends TimerTask{
        FilePeerDescriptor fpd;
        ClientConnector connector;

        public TaskRepeating(ClientConnector connector,FilePeerDescriptor fpd) {
            this.fpd = fpd;
            this.connector = connector;
        }

        public void run(){
            System.out.println("Roar, i am the timer");
            pHave(connector, this.fpd);
            //pUpdateToTracker();
        }

        private boolean pHave(ClientConnector connector, FilePeerDescriptor fpd){
            //< have $ Key  $BufferMap
            //> have  $Key  $BufferMap

            FileStorage fs = FileStorage.getInstance();

            String toserv = "have " + fpd.getKey() + " " + fpd.getBufferMap().getStringForm();

            connector.write(toserv);
            LogWriter.getInstance().writeToLog(toserv);

            String servanswer = connector.read();
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

        private void pUpdateToTracker(ActiveConnection connector, List<FilePeerDescriptor> seededFiles,
                                      List<FilePeerDescriptor> leechedFiles) throws InvalidAnswerException{
            // < update  seed [$ Key1 $Key2 $Key3 ...] leech [ $Key10 $Key11 $Key12 ... ]
            // Connect to the tracker


            // Send announcement
            connector.write(getUpdateMessage(seededFiles, leechedFiles));

            // Read the response
            String response = connector.read();

            // Handle the response
            if (response == null
                    || response.startsWith(InputMessagesPatternsBundle._OK_CST) == false) {
                throw new InvalidAnswerException(response);
            }

            // Close connection with the tracker

        }

        public String getUpdateMessage(List<FilePeerDescriptor> seededFiles,
                                       List<FilePeerDescriptor> leechedFiles) {

            StringBuilder update = new StringBuilder();

            update.append("update seed [");
            int i = 0;
            if (seededFiles != null) {
                for (FilePeerDescriptor file : seededFiles) {
                    update.append(file.getKey());
                    if (i++ < seededFiles.size() - 1) {
                        update.append(" ");
                    }
                }
            }
            update.append("] leech [");
            i = 0;
            if (leechedFiles != null) {
                for (FilePeerDescriptor file : leechedFiles) {
                    update.append(file.getKey());
                    if (i++ < seededFiles.size() - 1) {
                        update.append(" ");
                    }
                }
            }
            update.append("]");
            return update.toString();
        }
    }
}
