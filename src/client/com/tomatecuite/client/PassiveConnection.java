package com.tomatecuite.client;

import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by Romain on 09/05/2016.
 */
public class PassiveConnection extends Thread {
    private static final int _MAXIMUM_CONNECTED_PEERS = Configuration
            .getInstance().getPropertyAsInt(
                    Constants.MAXIMUM_CONNECTED_PEERS_KEY, 5);

    private InetSocketAddress host;
    private Selector select;

    public PassiveConnection(String host, int port) {
        this.host = new InetSocketAddress(host, port);
        initSelector();
    }

    private void initSelector(){
        try {
            this.select = Selector.open();
            ServerSocketChannel sChannel = createSocketChannel(host);
            sChannel.register(select, sChannel.validOps());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* create a socket to the tracket */
    private ServerSocketChannel createSocketChannel(InetSocketAddress host) throws IOException{
        // Create a non-blocking socket channel
        ServerSocketChannel sChannel = ServerSocketChannel.open();
        sChannel.configureBlocking(false);

        // Send a connection request to the server; this method is non-blocking
        sChannel.socket().bind(host);
        return sChannel;
    }

    @Override
    public void run(){
        listen();
    }

    private void listen(){
        while(true){
            try {
                // Wait for an event
                select.select();
            } catch (IOException e) {
                return;
            }

            // Get list of selection keys with pending events
            Iterator<SelectionKey> it = select.selectedKeys().iterator();

            // Process each key at a time
            while (it.hasNext()) {
                // Get the selection key
                SelectionKey selKey = it.next();
                // Remove it from the list to indicate that it is being
                // processed
                it.remove();
                // Accept if the number of connected clients is into the
                // interval defined into the configuration file
                if (selKey.isAcceptable()
                        && select.selectedKeys().size() <= _MAXIMUM_CONNECTED_PEERS) {
                    this.accept(selKey);
                } else if (selKey.isReadable() && selKey.isWritable()) {
                    this.read(selKey);
                }
            }
        }
    }

    private void accept(SelectionKey selKey){
        // For an accept to be pending the channel must be a server socket
        // channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selKey
                .channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel;
        try {
            socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            // Register the new SocketChannel with our Selector, indicating
            // we'd like to be notified when there's data waiting to be read
            socketChannel.register(this.select, SelectionKey.OP_READ
                    | SelectionKey.OP_WRITE);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey selKey){
        // Get channel with bytes to read
        SocketChannel sChannel = (SocketChannel) selKey.channel();
        ByteBuffer buf = ByteBuffer.allocateDirect(4096);
        String message = "";

        try {
            // Clear the buffer and read bytes from socket
            buf.clear();
            int numBytesRead = sChannel.read(buf);

            if (numBytesRead == -1) {
                // No more bytes can be read from the channel
                sChannel.close();
            } else {
                // To read the bytes, flip the buffer
                buf.flip();
                CharBuffer cBuff = Constants.NETWORK_STRING_DECODER
                        .decode(buf);
                message += cBuff.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clean and handles the incoming message
        message = cleanMessage(message);
        handleMessage(sChannel, message);
    }

    public static void write(SocketChannel sChannel, String message) {

        if (sChannel == null) {
            return;
        }

        String data = message + "\n";
        ByteBuffer buffer = ByteBuffer.allocate(data.length());
        buffer.clear();
        buffer.put(data.getBytes());
        buffer.flip();

        while (buffer.hasRemaining()) {
            try {
                sChannel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleMessage(SocketChannel sChannel, String message) {

        System.out.println("Message received from client : " + message);

        // Quit message
        if ("quit".equalsIgnoreCase(message)) {
            try {
                sChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // SERVER MODE : FROM ANOTHER PEER
        else if (message.startsWith("interested")) {
            // interested (> have)
            handleFileBufferMapRequest(sChannel, message);
        } else if (message.startsWith("getpieces")) {
            // getpieces (> data)
            handlePiecesDataRequest(sChannel, message);
        }
    }

    public void handleFileBufferMapRequest(SocketChannel sChannel, String inputMessage) {

        // Analyze message
        Matcher fileBufferMapMatcher = InputMessagesPatternsBundle._FILE_INTERESTED_PATTERN
                .matcher(inputMessage);

        if (sChannel == null || fileBufferMapMatcher.matches() == false
                || fileBufferMapMatcher.groupCount() == 0) {
            return;
        }
        String key = fileBufferMapMatcher.group(1);

        // Retrieves the concerned file
        FilePeerDescriptor concernedFile = FileStorage.getInstance().getFile(key);

        // Answer to the partner
        PassiveConnection.write(sChannel, returnHaveListMessage(concernedFile));
    }

    public String returnHaveListMessage(FilePeerDescriptor f) {
        // (> interested ) < have
        return "have " + f.getKey() + " " + f.getBufferMap().getStringForm();
    }

    public void handlePiecesDataRequest(SocketChannel sChannel,
                                        String inputMessage) {

        Matcher requestedPiecesMatcher = InputMessagesPatternsBundle._FILE_GET_PIECES_REQUEST_PATTERN
                .matcher(inputMessage);

        if (requestedPiecesMatcher.matches() == false
                || requestedPiecesMatcher.groupCount() == 0) {
            return;
        }

        // Retrieves the concerned file
        FilePeerDescriptor concernedFile = FileStorage.getInstance().getFile(
                requestedPiecesMatcher.group(1));

        // Retrieves requested pieces
        String[] pieces = requestedPiecesMatcher.group(2).split("\\s");
        List<Integer> i_pieces = new ArrayList<Integer>(pieces.length);

        for (String piece : pieces) {
            if (piece.isEmpty()) {
                continue;
            }
            i_pieces.add(Integer.parseInt(piece));
        }

        int messageMaxSize = Configuration.getInstance()
                .getPropertyAsInt(Constants.MAXIMUM_MESSAGE_SIZE_KEY, 50);
        if (i_pieces.size() > messageMaxSize) {
            for (List<Integer> list : Lists.partition(i_pieces, messageMaxSize)) {
                // Answer to the requester
                PassiveConnection.write(sChannel, returnDataMessage(concernedFile, list));
            }
        } else {
            // Answer to the requester
            PassiveConnection.write(sChannel, returnDataMessage(concernedFile, i_pieces));
        }
    }

    public String returnDataMessage(FilePeerDescriptor f, List<Integer> pieces) {
        // (> getpieces) < data
        if (f == null || f.getFile() == null) {
            return null;
        }
        StringBuilder message = new StringBuilder();
        message.append("data " + f.getKey() + " [");
        try {
            InputStream reader = new FileInputStream(f.getFile());
            int i = 0;
            for (int piece : pieces) {
                // Append the pieceID and the piece data into the message (into
                // binary format)
                FilePiece p = f.getPieces().get(piece);
                // Creates a positive BigInteger (1)
                BigInteger binary = new BigInteger(1, p.getData());
                String stringForm = binary.toString(2);
                message.append(piece + ":" + stringForm);

                // Append spaces except when it is the last element
                if (i++ < pieces.size() - 1) {
                    message.append(" ");
                }
            }
            // Close the file stream
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        message.append("]");
        return message.toString();
    }

    public String cleanMessage(String message) {
        return message.replaceAll("\\n", "").replaceAll("\\r", "");
    }
}
