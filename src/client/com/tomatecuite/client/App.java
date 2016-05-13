/*package com.tomatecuite.client;

import java.lang.reflect.Array;
import com.oracle.tools.packager.Log;

import java.util.ArrayList;*/

/**
 * Created by Romain on 09/05/2016.
 */

/*public class App {
    // Local Storage
    private static FileStorage store;

    // Configuration
    private static final Configuration config = Configuration.getInstance();
    private static final Protocol protocole = Protocol.getInstance();

    private static final String TRACKER_HOST = config.getProperty(
            Constants.TRACKER_HOST_KEY, "192.168.23.13");
    private static final int TRACKER_PORT = config.getPropertyAsInt(
            Constants.TRACKER_PORT_KEY, 13337);
    private static final String PEER_HOST = config.getProperty(
            Constants.PEER_HOST_KEY, "localhost");
    private static final int PEER_PORT = config.getPropertyAsInt(
            Constants.PEER_PORT_KEY, 13337);

    private static final String _REQUESTED_KEY = "638be43e65bdcb2d3152cf350b35581";

    public static void main(String[] args) {

        // Change download folder for the second peer
        //Configuration.getInstance().getProperties().put("local.storage", "downloads2");

        ActiveConnection trackerConnector = new ActiveConnection(TRACKER_HOST, TRACKER_PORT);
        PassiveConnection peerConnector = new PassiveConnection(PEER_HOST, PEER_PORT);
        ArrayList<Peer> peers = new ArrayList<>();

        store = FileStorage.getInstance();
        System.out.println("Bonjour");
        //System.out.println(FileStorage.getInstance().getFilesList());
        LogWriter.getInstance().close();
        initiateP2T(peerConnector, trackerConnector);
        String[] criterions = {"test"};
        System.out.println("Début Look");
        ArrayList<FilePeerDescriptor> files = protocole.pLook(trackerConnector, criterions);
        System.out.println("Fin Look");
        for (FilePeerDescriptor file : files) {
            System.out.println("Name : " + file.getName() + " & Key : " + file.getKey() + " & Piece Size : " +
                    file.getPieceSize() + " & File Size : " + file.getFileSize());
            peers = protocole.pGetFile(trackerConnector, file.getKey());
        }
        initiateP2P(peers);

        System.out.println("Aurevoir tracker");
        trackerConnector.closeConnection();
    }

    private static void initiateP2P(ArrayList<Peer> peers) {
        ArrayList<ActiveConnection> P2P = new ArrayList<>();
        for (Peer peer : peers) {
            P2P.add(new ActiveConnection(peer.getAddress(), peer.getPort()));
            System.out.println("Address : " + peer.getAddress() + "Port : " + peer.getPort());
        }

        for (ActiveConnection peer : P2P) {
            try {
                protocole.pInterested(peer, "638be43e65bdcb2d3152cf350b35581");
            } catch (InvalidAnswerException e) {
                e.printStackTrace();
            }
            peer.closeConnection();
        }
    }

    private static void initiateP2T(PassiveConnection peerConnector, ActiveConnection trackerConnector) {
        peerConnector.start();
        trackerConnector.initConnection();

        try {
            protocole.pAnnounce(trackerConnector, store.getFilesList(), null);
        } catch (InvalidAnswerException e) {
            e.printStackTrace();
        }
    }
}*/

/*public class App {

    // Configuration
    private static final Configuration config = Configuration.getInstance();

    private static final String TRACKER_HOST = config.getProperty(
            Constants.TRACKER_HOST_KEY, "192.168.23.13");
    private static final int TRACKER_PORT = config.getPropertyAsInt(
            Constants.TRACKER_PORT_KEY, 8334);
    private static final String PEER_HOST = config.getProperty(
            Constants.PEER_HOST_KEY, "localhost");
    private static final int PEER_PORT = config.getPropertyAsInt(
            Constants.PEER_PORT_KEY, 1026);

    private static PassiveConnection _HANDLER;

    private static final FileStorage _STORAGE = FileStorage.getInstance();

    public static void main(String[] args) {

        // Announce and get file (to the tracker)
        ActiveConnection trackerConnector = new ActiveConnection(TRACKER_HOST, TRACKER_PORT);
        _HANDLER = new PassiveConnection(PEER_HOST, PEER_PORT);

        try {
            Protocol.getInstance().pAnnounce(trackerConnector,
                    _STORAGE.getFilesList(), null);
        } catch (InvalidAnswerException e) {
            e.printStackTrace();
        }

        // Start server mode
        PassiveConnection peer1 = new PassiveConnection("localhost", 10000) {

            @Override
            public void handleMessage(SocketChannel sChannel, String message) {

                // Quit message
                if ("quit".equalsIgnoreCase(message)) {
                    try {
                        sChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                else if (message.startsWith("interested")) {
                    // interested (> have)
                    _HANDLER.handleFileBufferMapRequest(sChannel, message);
                } else if (message.startsWith("getpieces")) {
                    // getpieces (> data)
                    _HANDLER.handlePiecesDataRequest(sChannel, message);
                }
            }
        };
        peer1.start();

        // Print local storage
        System.out.println(FileStorage.getInstance().getFilesList());
    }
}*/

package com.tomatecuite.client;

import java.lang.reflect.Array;
import com.oracle.tools.packager.Log;

import java.util.ArrayList;

public class App {
    // Local Storage
    private static FileStorage store;

    // Configuration
    private static final Configuration config = Configuration.getInstance();
    private static final Protocol protocole = Protocol.getInstance();

    private static final String TRACKER_HOST = config.getProperty(
            Constants.TRACKER_HOST_KEY, "192.168.23.13");
    private static final int TRACKER_PORT = config.getPropertyAsInt(
            Constants.TRACKER_PORT_KEY, 13337);
    private static final String PEER_HOST = config.getProperty(
            Constants.PEER_HOST_KEY, "localhost");
    private static final int PEER_PORT = config.getPropertyAsInt(
            Constants.PEER_PORT_KEY, 13337);

    public static void main(String[] args){
//        LogWriter.getInstance().write("Hello");
        LogWriter.getInstance().peerConnected(TRACKER_HOST,TRACKER_PORT);
        LogWriter.getInstance().peerConnected(PEER_HOST,PEER_PORT);

        PassiveConnection peerConnector = new PassiveConnection(PEER_HOST, PEER_PORT);
        ActiveConnection trackerConnector = new ActiveConnection(TRACKER_HOST, TRACKER_PORT);
        store = FileStorage.getInstance();

        initiateP2T(trackerConnector);

        peerConnector.start();
        LogWriter.getInstance().close();

        //String[] criterions = {"test"};
        //System.out.println("Début Look");
        //ArrayList<FilePeerDescriptor> files = protocole.pLook(trackerConnector, criterions);
        //System.out.println("Fin Look");
        //for(FilePeerDescriptor file : files){
        //    System.out.println("Name : " + file.getName() + " & Key : " + file.getKey() + " & Piece Size : " +
        //            file.getPieceSize() + " & File Size : " + file.getFileSize());
        //}
        //System.out.println("Aurevoir");
        //peerConnector.run();

        //trackerConnector.closeConnection();

        //System.out.println(FileStorage.getInstance().getFilesList());
    }

    private static void initiateP2T(ActiveConnection trackerConnector){
        trackerConnector.initConnection();

        try {
            protocole.pAnnounce(trackerConnector, store.getFilesList(), null);
        } catch (InvalidAnswerException e) {
            e.printStackTrace();
        }
    }

    private static void initiateP2P(ArrayList<Peer> peers){
        ArrayList<ActiveConnection> P2P = new ArrayList<>();
        for(Peer peer : peers){
            P2P.add(new ActiveConnection(peer.getAddress(), peer.getPort()));
        }

        for(ActiveConnection peer : P2P){
            try {
                protocole.pInterested(peer, "638be43e65bdcb2d3152cf350b35581");
            }
            catch (InvalidAnswerException e){
                e.printStackTrace();
            }
        }
    }


}