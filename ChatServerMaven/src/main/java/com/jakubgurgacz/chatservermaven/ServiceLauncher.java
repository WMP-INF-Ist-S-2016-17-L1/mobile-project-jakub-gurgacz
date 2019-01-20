/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jakubgurgacz.chatservermaven;

import static com.jakubgurgacz.chatservermaven.ChatServer.PORT;
import static com.jakubgurgacz.chatservermaven.ChatServer.clientList;
import static com.jakubgurgacz.chatservermaven.ChatServer.databaseConnection;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fiop_20
 */
public class ServiceLauncher extends Thread {

    public static ChatServer cs;
    ServerSocket serverSocket;
    Socket socket;
    DatabaseConnection databaseConnection;
    ClientList clientList;

    public ServiceLauncher(ChatServer cs) {
        this.cs = cs;
    }

    @Override
    public void run() {

        ChatServer.addToLog("SERVER up&running");
        clientList = new ClientList();
        try {
            databaseConnection = new DatabaseConnection();
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        databaseConnection.start();
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException ex) {
            Logger.getLogger(ServiceLauncher.class.getName()).log(Level.SEVERE, null, ex);
        }

        Client c;
        int x = 0;
        while (true) {

            //TODO: interface, log files
            //TODO: list of not-chatting clients
            try {
                socket = serverSocket.accept();
                Client client = (new Client(socket, false));
                client.start();
                clientList.add(client);
                //clientList.sendNames();
            } catch (IOException ex) {
                System.out.println("I/O exception: ");
                ex.printStackTrace();
                try {
                    //socket.close();
                    serverSocket.close();
                } catch (IOException ex1) {
                    Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex1);
                }
                break;
            }
        }

    }

}
