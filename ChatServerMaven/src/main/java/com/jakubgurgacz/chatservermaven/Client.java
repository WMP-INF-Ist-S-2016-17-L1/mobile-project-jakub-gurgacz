/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jakubgurgacz.chatservermaven;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fiop_20
 */
public class Client extends Thread {
    
    private Socket clientSocket;
    private String clientAddr;
    private int clientPort;
    private boolean clientStatus;
    public String clientName;
    public boolean isOnline;
    private OutputStream outToClient;
    private DataOutputStream dos;
    private InputStream is;
    private DataInputStream in;
    private String requestingUsername;
    private Chat chat;
    
    public Client(Socket socket, boolean isChatting) {
        
        setClientSocket(socket);
        setClientPort(socket.getLocalPort());
        setClientAddr(socket.getInetAddress().toString().replaceFirst("/", ""));
        setClientName(socket.getInetAddress().toString().replaceFirst("/", ""));
        setClientStatus(isChatting);
        setOnline(true);
        ChatServer.addToLog("Do kolejki połączył się: "+socket.getInetAddress().toString()+" "+socket.getPort()+"\n");
    }

    public Client(Socket socket, boolean isChatting, Chat chat) {
        
        this(socket, isChatting);
//        setClientSocket(socket);
//        setClientPort(socket.getLocalPort());
//        setClientAddr(socket.getInetAddress().toString().replaceFirst("/", ""));
//        setClientName(socket.getInetAddress().toString().replaceFirst("/", ""));
//        setClientStatus(isChatting);
//        setOnline(true);
        setChat(chat);
        System.out.println(socket.getInetAddress().toString() + " " + socket.getPort());
    }
    
    @Override
    public void run() {
        
        sendMessage("SEND_CLIENT_NAME");
        boolean isChatting = false;
        if (getClientStatus()) {
            while (!isChatting && getClientStatus()) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClientSocket().getInputStream()));
                    String incomeing = bufferedReader.readLine();
                    if (!incomeing.equals("") && incomeing != null) {
                        String[] rawMessage = incomeing.split(";");
                        for (int i = 0; i < rawMessage.length; i++) {
                            System.out.println(rawMessage[i]);
                        }
                        switch (rawMessage[0]) {
                            case "USERNAME":
                                setClientName(rawMessage[1]);
                                ChatServer.clientList.sendNames();
                                ChatServer.addToLog(getClientSocket().getInetAddress().toString()+" ustawił swoją nazwę na: "+rawMessage[1]+"\n");
                                break;
                            default:
                                chat.handleIncomingData(incomeing);
                                ChatServer.clientList.removeByName(clientName);
                                break;
                        }
                    }
                    
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch(NullPointerException ex){
                    try{
                        chat.getClient1().sendMessage("STOP_CHAT");
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    try{
                        chat.getClient2().sendMessage("STOP_CHAT");
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    setClientStatus(false);
                }
            }
            System.out.println("Koniec czatu");
        } else {
            while (!isChatting) {
                try {
//                is = getClientSocket().getInputStream();
//                in = new DataInputStream(is);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClientSocket().getInputStream()));
                    String incomeing = bufferedReader.readLine();
                    if (!incomeing.equals("")) {
                        String[] rawMessage = incomeing.split(";");
                        switch (rawMessage[0]) {
                            case "USERNAME":
                                setClientName(rawMessage[1]);
                                ChatServer.clientList.sendNames();
                                ChatServer.addToLog(getClientSocket().getInetAddress().toString()+" ustawił swoją nazwę na: "+rawMessage[1]+"\n");
                                break;
                            case "CHAT_REQUEST":
                                if (rawMessage[1].equals("REQUESTING")) {
                                    ChatServer.clientList.sendChatRequest(rawMessage[2], getClientName());
                                } else if (rawMessage[1].equals("ACCEPTED")) {
                                    isChatting = true;
                                    ChatServer.PORT++;
                                    int port = ChatServer.PORT;
                                    if (ChatServer.clientList.initiateChat(port)) {
                                        ChatServer.clientList.sendMessageByName(rawMessage[2], "CHAT_REQUEST;START_CHAT;" + String.valueOf(port) + ";" + getClientName());
                                        ChatServer.clientList.sendMessageByName(getClientName(), "CHAT_REQUEST;START_CHAT;" + String.valueOf(port) + ";" + rawMessage[2]);
                                        ChatServer.clientList.removeByName(getClientName());
                                        ChatServer.clientList.removeByName(rawMessage[2]);
                                        sendAvailableClients();
                                        ChatServer.addToLog(rawMessage[2] + " i "+getClientName()+" rozpoczęli czat\n");
                                    }
                                } else if (rawMessage[1].equals("REJECTED")) {
                                    ChatServer.clientList.sendMessageByName(rawMessage[2], "CHAT_REQUEST;REJECTED;" + getClientName());
                                }
                                break;
                        }
                    }
                    
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch(NullPointerException e){
                    ChatServer.clientList.removeByName(getClientName());
                    break;
                }
            }
        }
        //System.out.println("Koniec czekania na czat");
    }
    
    
    private boolean sendAvailableClients() {
        try {
            ChatServer.clientList.sendNames();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    public boolean sendMessage(String message) {
        try {
            PrintWriter printWriter = new PrintWriter(getClientSocket().getOutputStream());
            printWriter.write(message + "\n");
            printWriter.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            
            return false;
        }
        return true;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }
    
    public String getClientName() {
        return clientName;
    }
    
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public Socket getClientSocket() {
        return clientSocket;
    }
    
    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    public String getClientAddr() {
        return clientAddr;
    }
    
    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }
    
    public int getClientPort() {
        return clientPort;
    }
    
    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }
    
    private void setClientStatus(boolean chatting) {
        this.clientStatus = chatting;
    }
    
    private boolean getClientStatus() {
        return clientStatus;
    }
    
    public Chat getChat() {
        return chat;
    }
    
    public void setChat(Chat chat) {
        this.chat = chat;
    }
    
}
