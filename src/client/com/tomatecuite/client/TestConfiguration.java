package com.tomatecuite.client;

/**
 * Created by Romain on 09/05/2016.
 */
public class TestConfiguration {
    public static void main(String[] args) {

        Configuration config = Configuration.getInstance();
        System.out.println(config.getProperties().entrySet());
    }
}
