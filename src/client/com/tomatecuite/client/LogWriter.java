package com.tomatecuite.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by Valtira on 10/05/2016.
 */
public class LogWriter extends Throwable {
    private File f;
    private PrintWriter pw;
    private String logName;
    private Date date;

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
            date = new Date();
            if(f.exists()){
                f.delete();
            }
            try {

                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pw = new PrintWriter(f);
        }catch(java.io.FileNotFoundException e) {
            e.printStackTrace(pw);
        }
        pw.println(date.toString());
    }

    void peerSaysToLog(String s){
        pw.println("Peer says : " + s);
    }

    void serverSaysToLog(String s){
        pw.println("Server says : " + s);
    }

    void peerConnected(String ip, int port){
        pw.println("Now connected to "+ ip +":"+ Integer.toString(port));
    }

    void disconnect(){
        pw.println("Disconnected");
    }

    void fileSpecs(String fileName, long fileSize, String key){
        pw.println("FPD file is "+ fileName + ", size is " + fileSize + ", key is " + key);

    };

    void write(String msg){
        pw.println(msg);
    }

    void close(){
        pw.close();
    }
}
