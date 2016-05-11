package com.tomatecuite.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Valtira on 10/05/2016.
 */
public class LogWriter extends Throwable {
    private File f;
    private PrintWriter pw;
    private String logName;

    private static LogWriter instance;

    public static LogWriter getInstance() {
        if (instance == null)
            instance = new LogWriter();
        return instance;
    }

    public PrintWriter getPw() {
        return pw;
    }

    private LogWriter() {
        logName = "LogWriter.log";
        try {
            f = new File(logName);
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pw = new PrintWriter(f);
        }catch(java.io.FileNotFoundException e) {
            e.printStackTrace(pw);
        }
    }

    void peerSaysToLog(String s){
        pw.println("Peer says : " + s);
        pw.close();
    }

    void serverSaysToLog(String s){
        pw.println("Server says : " + s);
        pw.close();
    }

    void peerConnected(String ip, int port){
        pw.println("Now connected to "+ ip +":"+ Integer.toString(port));
        pw.close();
    }

    void disconnect(){
        pw.println("Disconnected");
        pw.close();
    }

    void fileSpecs(String fileName, long fileSize, String key){
        pw.println("FPD file is "+ fileName + ", size is " + fileSize + ", key is " + key);
        pw.close();

    };

    void write(String msg){
        pw.println(msg);
        pw.close();
    }
}
