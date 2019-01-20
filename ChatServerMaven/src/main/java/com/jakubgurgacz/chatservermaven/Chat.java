/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jakubgurgacz.chatservermaven;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Fiop_20
 */
public final class Chat extends Thread {

    private Client client1;
    private Client client2;
    private String chatLastMessage;

    private final String SETTINGS = "SETTINGS";
    private final String USER_NAME = "USER_NAME";
    private final String SEND_USER_NAME = "SEND_USER_NAME";
    private final String MESSAGE = "MESSAGE";
    private final String CLOSE = "CLOSE";
    private boolean chatStatus;

    ///private final ClientMessengerImpl cmi1;
    //private final ClientMessengerImpl cmi2;
    private InputStream is;
    private DataInputStream in;
    private int CHAT_PORT;
    private ServerSocket chatSocket;
    private Socket clientSocket;
    //public String x="";

    //TODO: log file
    public Chat(int PORT) {
        this.client1 = null;
        this.client2 = null;
        setCHAT_PORT(PORT);
        try {
            setChatSocket(new ServerSocket(getCHAT_PORT()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public boolean waitForClients(){
        while(this.client1 == null || this.client2 == null){
            try {
                clientSocket = getChatSocket().accept();
                if(this.client1 == null){
                    this.client1 = new Client(clientSocket, true, Chat.this);
                    this.client1.start();
                    System.out.println("Klient 1 ustawiony");
                } else {
                    this.client2 = new Client(clientSocket, true, Chat.this);
                    this.client2.start();
                    System.out.println("Klient 2 ustawiony");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        boolean clientsConnected = waitForClients();

        
        System.out.println("Działa czat");

        }
    
        

    public boolean handleIncomingData(String rawMessage) {
        try{
        String senderName = rawMessage.split(";")[1];
        String header = rawMessage.split(";")[0];
        String message = rawMessage.substring(9+senderName.length());
        switch (header) {
            case MESSAGE:
                if (senderName.equals(client1.getClientName())) {
                    client2.sendMessage(MESSAGE + ";" + senderName+";"+message);
                } else {
                    client1.sendMessage(MESSAGE + ";" + senderName+";"+message);
                }
                break;
            case "STOP_CHAT":
                if (senderName.equals(client1.getClientName())) {
                    client2.sendMessage("STOP_CHAT");
                    ChatServer.addToLog(client1.getClientName()+ " zakończył czat\n");
                } else {
                    client1.sendMessage("STOP_CHAT");
                    ChatServer.addToLog(client2.getClientName()+ " zakończył czat\n");
                }
                break;
        }
        } catch (ArrayIndexOutOfBoundsException ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public Client getClient1() {
        return client1;
    }

    public void setClient1(Client client1) {
        this.client1 = client1;
    }

    public Client getClient2() {
        return client2;
    }

    public void setClient2(Client client2) {
        this.client2 = client2;
    }

    public int getCHAT_PORT() {
        return CHAT_PORT;
    }

    public void setCHAT_PORT(int CHAT_PORT) {
        this.CHAT_PORT = CHAT_PORT;
    }

    public ServerSocket getChatSocket() {
        return chatSocket;
    }

    public void setChatSocket(ServerSocket chatSocket) {
        this.chatSocket = chatSocket;
    }
    
    

    public boolean getChatStatus() {
        return chatStatus;
    }

    public void setChatStatus(boolean chatStatus) {
        this.chatStatus = chatStatus;
    }

    private String getChatLastMessage() {
        return chatLastMessage;
    }

    private void setChatLastMessage(String msg) {
        this.chatLastMessage = msg;
    }

    private void clearInputStream() {
        try {
            in.skip(in.available());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
