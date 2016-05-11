package com.tomatecuite.client;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by Romain on 08/05/2016.
 */
public class ActiveConnection extends Thread {
    public enum remoteType{
        PEER, TRACKER
    };

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String fileKey;

    private String host;
    private int port;

    private static final FileStorage store = FileStorage.getInstance();

    public ActiveConnection(String host, int port){
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void initConnection(){
        try{
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            LogWriter.getInstance().peerConnected(host, port);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try{
                socket.close();
                LogWriter.getInstance().disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String message){
        try{
            System.out.println(message);
            writer.write(message);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String read(){
        try{
            String response = reader.readLine();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void start(String key){
        fileKey = key;
        start();
    }

    @Override
    public void run(){
        if(fileKey == null)
            return;

        try{
            initConnection();
            FilePeerDescriptor receivedBufferMapFile = getFileBufferMap(this, fileKey);

            if(receivedBufferMapFile == null) {
                LogWriter.getInstance().write("No bufferMap received");
                return;
            }
            FilePeerDescriptor localFile = store.getFile(receivedBufferMapFile.getKey());

            if(localFile == null) {
                LogWriter.getInstance().write("");
                return;
            }
            BufferMap receivedBufMap = receivedBufferMapFile.getBufferMap();
            BufferMap localBufMap = localFile.getBufferMap();

            List<Integer> requestedPieces = new ArrayList<Integer>();
            for (int i = 0 ; i<receivedBufMap.length() ; i++){
                if(!(localBufMap.get(i)) && receivedBufMap.get(i))
                    requestedPieces.add(i);
            }
            if(requestedPieces.isEmpty())
                return;

            Integer[] p = requestedPieces.toArray(new Integer[]{});
            FilePeerDescriptor receivedData = getFileData(this, fileKey, p);

            synchronized (localFile.getPieces()){
                localFile.getPieces().addAll(receivedData.getPieces());
            }

            localBufMap.or(receivedBufMap);

            if(localBufMap.cardinality() == localBufMap.length())
                store.saveFilePeerToDisk(localFile);
        } catch (InvalidAnswerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        closeConnection();
        ConnectorBundle.getInstance().getConnectors().remove(this);
    }

    public FilePeerDescriptor getFileBufferMap(ActiveConnection connector, String fileKey) throws InvalidAnswerException {
        String message = getFileBufferMapMessage(fileKey);
        connector.write(message);

        String response = connector.read();
        if(response == null || !(response.startsWith("have")))
            throw new InvalidAnswerException(response);

        return getRequestedFileBufferMap(response);
    }

    public String getFileBufferMapMessage(String fileKey){
        return "interested" + fileKey;
    }

    public FilePeerDescriptor getRequestedFileBufferMap(String inputMessage){
        Matcher fileBufferMapMatcher = InputMessagesPatternsBundle._FILE_BUFFERMAP_PATTERN
                .matcher(inputMessage);

        if (!(fileBufferMapMatcher.matches())
                || fileBufferMapMatcher.groupCount() == 0) {
            return null;
        }
        String key = fileBufferMapMatcher.group(1);
        String bufferMap = fileBufferMapMatcher.group(2);

        FilePeerDescriptor peer = new FilePeerDescriptor(null, key, 0, 1, null);
        peer.getBufferMap().stringToBufferMap(bufferMap);

        return peer;
    }

    public FilePeerDescriptor getFileData(ActiveConnection connector, String key, Integer[] p) throws InvalidAnswerException{
        connector.write(getPiecesMessage(key, p));

        String response = connector.read();

        if(response == null || !(response.startsWith("data")))
            throw new InvalidAnswerException(response);

        FilePeerDescriptor receivedFile = getRequestedPiecesData(response);
        while(receivedFile.getPieces().size() < p.length){
            response = connector.read();
            receivedFile.getPieces().addAll(getRequestedPiecesData(response).getPieces());
        }

        return receivedFile;
    }

    public String getPiecesMessage(String fileKey, Integer[] p){
        StringBuilder message = new StringBuilder();
        message.append("getpieces " + fileKey + " [");
        int i = 0;
        for (int piece : p) {
            message.append(piece);
            if (i++ < p.length - 1) {
                message.append(" ");
            }
        }
        message.append("]");
        return message.toString();
    }

    public FilePeerDescriptor getRequestedPiecesData(String message){
        Matcher requestedPiecesMatcher = InputMessagesPatternsBundle._FILE_GET_PIECES_ANSWER_PATTERN
                .matcher(message);

        if (requestedPiecesMatcher.matches() == false
                || requestedPiecesMatcher.groupCount() == 0) {
            return null;
        }

        String key = requestedPiecesMatcher.group(1);
        String[] pieces = requestedPiecesMatcher.group(2).split("\\s");

        FilePeerDescriptor resultedFile = new FilePeerDescriptor("", key, 0, 1, null);
        for (String piece : pieces) {
            String[] elements = piece.split(":");
            // The piece data
            BigInteger binary = new BigInteger(elements[1], 2);
            // Add the piece data
            FilePiece newPiece = new FilePiece(Integer.parseInt(elements[0]), binary.toByteArray(),piece.length());
            resultedFile.addPiece(newPiece);
        }

        return resultedFile;
    }
}
