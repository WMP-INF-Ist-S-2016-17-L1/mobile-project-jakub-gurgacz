/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jakubgurgacz.chatservermaven;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fiop_20
 */
public class DatabaseConnection extends Thread {

    private static ServerSocket serverSocket;
    private static Socket socket;
    public static int PORT = 9999;

    public DatabaseConnection() throws IOException {

    }

    @Override
    public void run() {
        ChatServer.addToLog("Database Connection up&running");
        try {
            serverSocket = new ServerSocket(PORT);

        } catch (IOException ex) {
            System.out.println("I/O exception: ");
            ex.printStackTrace();
        }

        while (true) {

            //TODO: interface, log files
            //TODO: list of not-chatting clients
            try {
                socket = serverSocket.accept();
                new DatabaseOperations(socket).start();
                //clientList.add(client);
                //clientList.sendNames();
            } catch (IOException ex) {
                try {
                    System.out.println("I/O exception: ");
                    ex.printStackTrace();
                    //socket.close();
                    serverSocket.close();
                    break;
                }
                //new Client(socket.getInetAddress().toString(), socket.getPort());
                //new ClientThread(socket).start();
                catch (IOException ex1) {
                    Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }

        }
    }
}
