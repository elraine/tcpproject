package com.tomatecuite.client;

import com.tomatecuite.*;
import java.util.*;
import java.io.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Configuration{
    private static String CONFIGURATION_FILE_PATH = "config.ini";
    private Properties prop;
    private static Configuration instance;

    public Configuration(){
        prop = new Properties();
        loadConfiguration();
    }

    public void loadConfiguration(){
        try{
            prop.load(new FileInputStream(CONFIGURATION_FILE_PATH));
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public Properties getProperties(){
        return prop;
    }

    public String getProperty(String key, String value){
        return prop.getProperty(key, value);
    }

    public int getPropertyAsInt(String key, int def) {
        return Integer.parseInt(getProperty(key, Integer.toString(def)));
    }

    public static Configuration getInstance(){
        if(instance == null)
            instance = new Configuration();
        return instance;
    }
}