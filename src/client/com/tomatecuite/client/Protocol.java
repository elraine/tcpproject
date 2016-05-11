package com.tomatecuite.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;

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


        LogWriter.getInstance().peerSaysToLog(announce);
        connector.write(announce);
        String response = connector.read();
        LogWriter.getInstance().serverSaysToLog(response);
        if (response == null
                || !response.startsWith(InputMessagesPatternsBundle._OK_CST)) {
            throw new InvalidAnswerException(response);
        }
    }

    public ArrayList<FilePeerDescriptor> pLook(ActiveConnection connector, String[] criterion) {
        //look [ $Criterion1 $Criterion2  ...]
        //serv : list [ $Filename1 $Length1 $PieceSize1 $Key1  $Filename2 $Length2 $PieceSize2 $Key2 ...]

//        filename="..."
//        filesize>"..." | filesize<"..."           One of the two or none.
//        piecesize>"..." | piecesize<"..."		  One of the two or none.

        FileStorage fs = FileStorage.getInstance();
        ArrayList<FilePeerDescriptor> fileList = new ArrayList<FilePeerDescriptor>();

        String toserv = "look ";
        for (int i = 0; i < criterion.length; i++) {
            toserv += criterion[i];
        }

        LogWriter.getInstance().peerSaysToLog(toserv);
        connector.write(toserv);

        String servAnswer = connector.read();
        String[] peerPart = {""};
        LogWriter.getInstance().serverSaysToLog(servAnswer);


        String debut = "list [";
        if (servAnswer.startsWith(debut)) {
            peerPart = servAnswer.split(" ", 2);
            peerPart[1] = peerPart[1].substring(1, (peerPart[1].length()) - 1);
        }

        String[] params = peerPart[1].split(" ");
        int i = 0;
        String fileName = new String();
        String fileKey = new String();
        String fileSize = new String();
        String pieceSize = new String();

        for (String param : params) {
            if(i%4 == 0 && i != 0){
                fileList.add(new FilePeerDescriptor(fileName, fileKey, Integer.valueOf(fileSize), Integer.valueOf(pieceSize)));
                fileName = param;
            }
            else if(i%4 == 1)
                fileSize = param;
            else if(i%4 == 2)
                pieceSize = param;
            else if(i%4 == 3)
                fileKey = param;
            System.out.println(param + " " + i);
            i++;
        }

        if(i < 5)
            fileList.add(new FilePeerDescriptor(fileName, fileKey, Integer.valueOf(fileSize), Integer.valueOf(pieceSize)));

        for(FilePeerDescriptor file : fileList)
            System.out.println("Name : " + file.getName() + " && Key : " + file.getKey() + " && Piece Size : "
                    + file.getPieceSize() + " && File Size : " + file.getFileSize());
        return fileList;
    }

    public ArrayList<Peer> pGetFile(ActiveConnection connector, String key) {
        //getfile  $Key
        //peers $ Key  [ $IP1:$Port1 $ IP2:$Port2 ...  ]
        String getfile = "getfile " + key;
        System.out.println(getfile);
        LogWriter.getInstance().peerSaysToLog(getfile);
        connector.write(getfile);

        String servanswer = connector.read();
        LogWriter.getInstance().serverSaysToLog(servanswer);
        String debut = "peers " +key;
        String[] peerpart={""};
        if(servanswer.startsWith(debut)) {
            peerpart = servanswer.split(" ", 3);
        }
        peerpart[2] = peerpart[2].substring(1, (peerpart[2].length()) - 1);
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

    public String getFileBufferMap(String key){
        return "interested " + key;
    }

    public FilePeerDescriptor pInterested(ActiveConnection connector, String key) throws InvalidAnswerException {
        connector.write("interested " + key);
        String response = connector.read();
        System.out.println("Response : " + response);
        if(response == null || !(response.startsWith("have"))){
            throw new InvalidAnswerException(response);
        }
        else{
            String[] subResponse = response.split(" ", 3);
            if(subResponse[2] == null)
                return null;
            String bufferMap = subResponse[2];
            System.out.println("Buffeer map : " + bufferMap);

            FilePeerDescriptor file = new FilePeerDescriptor(null, key, 0, 1, null);
            file.getBufferMap().stringToBufferMap(bufferMap);
            System.out.println("File : " + file.getKey() + " Buffer Map : " + file.getBufferMap());

            return file;
        }


    }


    private boolean sendRegularInterval(ActiveConnection connector, FilePeerDescriptor fpd) throws Exception{
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
        ActiveConnection connector;

        public TaskRepeating(ActiveConnection connector,FilePeerDescriptor fpd) {
            this.fpd = fpd;
            this.connector = connector;
        }

        public void run(){
            System.out.println("Roar, i am the timer");
            pHave(connector, this.fpd);
            //pUpdateToTracker();
        }

        private boolean pHave(ActiveConnection connector, FilePeerDescriptor fpd){
            //< have $ Key  $BufferMap
            //> have  $Key  $BufferMap

            FileStorage fs = FileStorage.getInstance();

            String toserv = "have " + fpd.getKey() + " " + fpd.getBufferMap().getStringForm();

            connector.write(toserv);
            LogWriter.getInstance().peerSaysToLog(toserv);

            String servanswer = connector.read();
            LogWriter.getInstance().serverSaysToLog(servanswer);
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
            String updatemsg = "getUpdateMessage(seededFiles, leechedFiles)";
            LogWriter.getInstance().peerSaysToLog(updatemsg);
            connector.write(updatemsg);

            // Read the response
            String response = connector.read();
            LogWriter.getInstance().serverSaysToLog(response);
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
