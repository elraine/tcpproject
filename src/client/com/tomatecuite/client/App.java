/*package com.tomatecuite.client;

import sun.security.krb5.Config;

import java.io.IOException;
import java.nio.channels.SocketChannel;*/

/**
 * Created by Romain on 09/05/2016.
 */

/*public class App{
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

        ActiveConnection peer3 = new ActiveConnection("localhost", 10004);
        peer3.start(_REQUESTED_KEY);
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

import java.util.ArrayList;

public class App {
    // Local Storage
    private static FileStorage store;

    // Configuration
    private static final Configuration config = Configuration.getInstance();

    private static final String TRACKER_HOST = config.getProperty(
            Constants.TRACKER_HOST_KEY, "localhost");
    private static final int TRACKER_PORT = config.getPropertyAsInt(
            Constants.TRACKER_PORT_KEY, 1026);
    private static final String PEER_HOST = config.getProperty(
            Constants.PEER_HOST_KEY, "localhost");
    private static final int PEER_PORT = config.getPropertyAsInt(
            Constants.PEER_PORT_KEY, 13337);

    public static void main(String[] args){
        PassiveConnection peerConnector = new PassiveConnection(PEER_HOST, PEER_PORT);;
        ActiveConnection trackerConnector = new ActiveConnection(TRACKER_HOST, TRACKER_PORT);;
        store = FileStorage.getInstance();
        System.out.println("Bonjour");
        //System.out.println(FileStorage.getInstance().getFilesList());
        initiateP2P(peerConnector, trackerConnector);
        String[] criterions = {"test"};
        System.out.println("Début Look");
        ArrayList<FilePeerDescriptor> files = Protocol.getInstance().pLook(trackerConnector, criterions);
        System.out.println("Fin Look");
        for(FilePeerDescriptor file : files){
            System.out.println("Name : " + file.getName() + " & Key : " + file.getKey() + " & Piece Size : " +
                    file.getPieceSize() + " & File Size : " + file.getFileSize());
        }
        System.out.println("Aurevoir");
        trackerConnector.closeConnection();
        //System.out.println(FileStorage.getInstance().getFilesList());
    }

    private static void initiateP2P(PassiveConnection peerConnector, ActiveConnection trackerConnector){
        peerConnector.start();

        trackerConnector.initConnection();

        try {
            Protocol.getInstance().pAnnounce(trackerConnector,
                    store.getFilesList(), null);
            ArrayList<Peer> peers = Protocol.getInstance().pGetFile(trackerConnector, "638be43e65bdcb2d3152cf350b35581");
        } catch (InvalidAnswerException e) {
            e.printStackTrace();
        }
    }
}