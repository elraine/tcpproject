package com.tomatecuite.client;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    private boolean pLook() {
        //look [ $Criterion1 $Criterion2  ...]
        //serv : list [ $Filename1 $Length1 $PieceSize1 $Key1  $Filename2 $Length2 $PieceSize2 $Key2 ...]

        //TODO criterion ?

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
        String toserv = "interested " + key;
        System.out.println(toserv);

        String servanswer = (new Scanner(System.in)).nextLine();
        String debut = "have " +key;
        String[] peerpart={""};
        if(servanswer.startsWith(debut)) {
            peerpart = servanswer.split(" ", 3);
        }
        BufferMap bm = fpd.getBufferMap();
        if(peerpart.length > 1){
           bm.stringToBufferMap(peerpart[2]);
        }
        return true;
    }
    private boolean pGetPieces(){
    return true;
    }

    private boolean pHave(){
    return true;
    }

    private boolean pUpdateToTracker(){
    return true;
    }

}
