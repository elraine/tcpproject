package com.tomatecuite.client;

import java.io.File;
import java.io.PrintWriter;

/**
 * Created by Valtira on 10/05/2016.
 */
public class LogWriter extends Throwable {
    File f;
    PrintWriter pw;

    private static LogWriter instance;

    public static LogWriter getInstance() {
        if (instance == null)
            instance = new LogWriter();
        return instance;
    }

    public PrintWriter getPw() {
        return pw;
    }

    public LogWriter() {
        try {
            f = new File("LogWriter.log");
            pw = new PrintWriter(f);
        }catch(java.io.FileNotFoundException e) {
            e.printStackTrace(pw);
        }
    }

    public void writeToLog(String s){
        pw.println(s);
    }
}
