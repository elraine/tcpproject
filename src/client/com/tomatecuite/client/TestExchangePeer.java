package com.tomatecuite.client;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by Romain on 09/05/2016.
 */
public class TestExchangePeer {
    private static PassiveConnection _HANDLER;

    private static final FileStorage _STORAGE = FileStorage.getInstance();

    public static void main(String[] args) {

        // Announce and get file (to the tracker)
        ClientConnector trackerConnector = new ClientConnector(
                Configuration.getInstance().getProperty("tracker.host",
                        "192.168.23.13"), Configuration.getInstance()
                .getPropertyAsInt("tracker.port", 8334));
        _HANDLER = new PassiveConnection(trackerConnector.getHost(), trackerConnector.getPort());

        try {
            Protocol.getInstance().pAnnounce(trackerConnector,
                    _STORAGE.getFilesList(), null);
        } catch (InvalidAnswerException e) {
            e.printStackTrace();
        }

        // Start server mode
        PassiveConnection peer1 = new PassiveConnection(
                Configuration.getInstance().getProperty("peer.host",
                        "localhost"), Configuration.getInstance()
                .getPropertyAsInt("peer.port", 60002)) {

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
}
