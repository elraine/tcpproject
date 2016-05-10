package com.tomatecuite.client;

import sun.security.krb5.Config;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by Romain on 09/05/2016.
 */

public class App{
            private static final String _REQUESTED_KEY = "638be43e65bdcb2d3152cf350b35581";

            public static void main(String[] args) {

                // Change download folder for the second peer
                Configuration.getInstance().getProperties()
                        .put("local.storage", "downloads2");

                // The file is created

                FileStorage.getInstance().createLeechedFile(new FilePeerDescriptor("test", _REQUESTED_KEY,
                        8, 2048, null));

                // LocalStorage.getInstance().createLeechedFile(
                // new PeerFile(_REQUESTED_KEY, "1920x1080v1.jpg", 2048, 1338096,
                // null));
                //

                // LocalStorage.getInstance().createLeechedFile(
                // new PeerFile(_REQUESTED_KEY, "snow.jpg", 2048, 510084, null));

                ClientConnector peer3 = new ClientConnector("192.168.21.219", 10000);
                peer3.start(_REQUESTED_KEY);
    }
}

//public class App {
//
//    // Configuration
//    private static final Configuration config = Configuration.getInstance();
//
//    private static final String TRACKER_HOST = config.getProperty(
//            Constants.TRACKER_HOST_KEY, "192.168.23.13");
//    private static final int TRACKER_PORT = config.getPropertyAsInt(
//            Constants.TRACKER_PORT_KEY, 9999);
//    private static final String PEER_HOST = config.getProperty(
//            Constants.PEER_HOST_KEY, "192.168.21.219");
//    private static final int PEER_PORT = config.getPropertyAsInt(
//            Constants.PEER_PORT_KEY, 1026);
//
//    private static PassiveConnection _HANDLER;
//
//    private static final FileStorage _STORAGE = FileStorage.getInstance();
//
//    public static void main(String[] args) {
//
//        // Announce and get file (to the tracker)
//        ClientConnector trackerConnector = new ClientConnector(TRACKER_HOST, TRACKER_PORT);
//        _HANDLER = new PassiveConnection(PEER_HOST, PEER_PORT);
//
//        try {
//            Protocol.getInstance().pAnnounce(trackerConnector,
//                    _STORAGE.getFilesList(), null);
//        } catch (InvalidAnswerException e) {
//            e.printStackTrace();
//        }
//
//        // Start server mode
//        PassiveConnection peer1 = new PassiveConnection("localhost", 10000) {
//
//            @Override
//            public void handleMessage(SocketChannel sChannel, String message) {
//
//                // Quit message
//                if ("quit".equalsIgnoreCase(message)) {
//                    try {
//                        sChannel.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return;
//                }
//
//                else if (message.startsWith("interested")) {
//                    // interested (> have)
//                    _HANDLER.handleFileBufferMapRequest(sChannel, message);
//                } else if (message.startsWith("getpieces")) {
//                    // getpieces (> data)
//                    _HANDLER.handlePiecesDataRequest(sChannel, message);
//                }
//            }
//        };
//        peer1.start();
//
//        // Print local storage
//        System.out.println(FileStorage.getInstance().getFilesList());
//    }
//}

//package com.tomatecuite.client;
//import com.tomatecuite.*;
//import java.util.*;
//import java.io.*;
//
//public class App {
//    // Local Storage
//    private static FileStorage store;
//
//    // Configuration
//    private static final Configuration config = Configuration.getInstance();
//
//    private static final String TRACKER_HOST = config.getProperty(
//            Constants.TRACKER_HOST_KEY, "localhost");
//    private static final int TRACKER_PORT = config.getPropertyAsInt(
//            Constants.TRACKER_PORT_KEY, 1026);
//    private static final String PEER_HOST = config.getProperty(
//            Constants.PEER_HOST_KEY, "localhost");
//    private static final int PEER_PORT = config.getPropertyAsInt(
//            Constants.PEER_PORT_KEY, 1026);
//
//    public static void main(String[] args){
//        store = FileStorage.getInstance();
//        System.out.println("Bonjour");
//        System.out.println(FileStorage.getInstance().getFilesList());
//        initiateP2P();
//        //System.out.println(FileStorage.getInstance().getFilesList());
//    }
//
//    private static void initiateP2P(){
//        new PassiveConnection(PEER_HOST, PEER_PORT).start();
//
//        ClientConnector peerConnector = new ClientConnector(TRACKER_HOST, TRACKER_PORT);
//
//        try {
//            Protocol.getInstance().pAnnounce(peerConnector,
//                    store.getFilesList(), null);
//        } catch (InvalidAnswerException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            Protocol.getInstance().pAnnounce(peerConnector,
//                    store.getFilesList(), null);
//        } catch (InvalidAnswerException e) {
//            e.printStackTrace();
//        }
//    }
//}