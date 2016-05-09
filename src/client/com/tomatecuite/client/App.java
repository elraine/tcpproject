package com.tomatecuite.client;
import com.tomatecuite.*;
import java.util.*;
import java.io.*;

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
            Constants.PEER_PORT_KEY, 1026);

    public static void main(String[] args){
        store = FileStorage.getInstance();
        System.out.println("Bonjour");
        System.out.println(FileStorage.getInstance().getFilesList());
        initiateP2P();
        //System.out.println(FileStorage.getInstance().getFilesList());
    }

    private static void initiateP2P(){
        new PassiveConnection(PEER_HOST, PEER_PORT).start();

        ClientConnector peerConnector = new ClientConnector(TRACKER_HOST, TRACKER_PORT);

        try {
            Protocol.getInstance().pAnnounce(peerConnector,
                    store.getFilesList(), null);
        } catch (InvalidAnswerException e) {
            e.printStackTrace();
        }

        try {
            Protocol.getInstance().pAnnounce(peerConnector,
                    store.getFilesList(), null);
        } catch (InvalidAnswerException e) {
            e.printStackTrace();
        }
    }
}