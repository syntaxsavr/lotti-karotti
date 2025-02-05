package com.example.lottikarotti.network;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.socket.client.IO;
import io.socket.client.Socket;

public class Test_Network {

    static final String HOST = "http://127.0.0.1:3000";
    static Socket testSocket01;
    static Socket testSocket02;
    static Socket testSocket03;
    static Socket testSocket04;
    static Socket testSocket05;
    static Socket testSocket06;
    static Socket testSocket07;
    static int aliveFeedback;

    /**
     * This class contains JUnit tests for Socket.IO connection.
     */
    @BeforeAll
    static void setup(){

        // Establish a Socket.IO connection to the server
        try {
            testSocket01 = IO.socket(HOST);
            testSocket01.connect();
            Thread.sleep(100);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Listen for "alive" event from the server and set the feedback value
        testSocket01.on("alive", args -> {aliveFeedback = (Integer) args[0];});
    }

    public void waitFixedTime(){
        // Wait for the server to respond (MAX TIME IS 2 SECOND; The call is made synchronous on purpose to detect high latency!)
        // Please Check Internet Connection before testing, the internet connection should not be stressed when testing
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests if the server is able to respond with an "alive" event.
     */
    @Test
    void testConnection(){
        // Initialize feedback value
        aliveFeedback = 99;

        // Emit "alive" event to the server
        testSocket01.emit("alive");

        waitFixedTime();

        // Check if feedback value is set to 1
        Assertions.assertEquals(1, aliveFeedback);
    }
    @Test
    void testAdvancedServerOverflow(){
        Assertions.assertDoesNotThrow(()->{
            testSocket02 = IO.socket(HOST);
            testSocket02.connect();

            testSocket03 = IO.socket(HOST);
            testSocket03.connect();

            testSocket04 = IO.socket(HOST);
            testSocket04.connect();

            testSocket05 = IO.socket(HOST);
            testSocket05.connect();

            testSocket06 = IO.socket(HOST);
            testSocket06.connect();
        });

        testSocket06.on("alive", args -> {aliveFeedback = (Integer) args[0];});

        // Initialize feedback value
        aliveFeedback = 99;

        // Emit "alive" event to the server
        testSocket06.emit("alive");

        waitFixedTime();

        // Check if feedback value is set to 1
        Assertions.assertEquals(99, aliveFeedback);
    }

    boolean nameTaken;

    @Test
    void testRegisterNameOnServer() {
        nameTaken = false;

        testSocket01.emit("register", "Erwin");
        Assertions.assertDoesNotThrow(() -> {
            testSocket02 = IO.socket(HOST);
            testSocket02.connect();
        });

        testSocket02.on("error", message -> {
            System.out.println(message[0]);
            if ((int) message[0] == 400) nameTaken = true;
        });

        testSocket02.emit("register", "Erwin");

        waitFixedTime();

        Assertions.assertEquals(true, nameTaken);
    }

    @Test
    void testPlayOnline(){
        testSocket01.emit("register", "Erwin");
        testSocket01.emit("createlobby", 123456);

        Assertions.assertDoesNotThrow(() -> {
            testSocket02 = IO.socket(HOST);
            testSocket02.connect();
        });

        testSocket01.emit("register", "Lisa");
        testSocket02.emit("createlobby", 123456);

        waitFixedTime();
    }


}
