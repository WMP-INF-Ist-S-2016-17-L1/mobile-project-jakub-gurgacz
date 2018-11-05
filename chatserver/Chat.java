/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fiop_20
 */
public class Chat extends Thread {

    private final Client client1;
    private final Client client2;
    private String chatLastMessage;
    
    private final String SETTINGS = "SETTINGS";
    private final String USER_NAME = "USER_NAME";
    private final String SEND_USER_NAME = "SEND_USER_NAME";
    private final String MESSAGE = "MESSAGE";
    private final String CLOSE = "CLOSE";
    private boolean chatStatus;

    private final ClientMessengerImpl cmi1;
    private final ClientMessengerImpl cmi2;
    private InputStream is;
    private DataInputStream in;
    //public String x="";

    //TODO: log file
    
    public Chat(Client client1, Client client2) {
        this.client1 = client1;
        this.client1.setClientName("client1");
        this.client2 = client2;
        this.client2.setClientName("client2");
        
        setChatStatus(true);

        cmi1 = new ClientMessengerImpl(this.client1);
        cmi2 = new ClientMessengerImpl(this.client2);

        cmi1.sendMessage(SETTINGS + ";" + USER_NAME + ";" + this.client1.getClientName());
        //cmi1.sendMessage(SETTINGS + ";" + SEND_USER_NAME);
        cmi2.sendMessage(SETTINGS + ";" + USER_NAME + ";" + this.client2.getClientName());

    }

    public void run() {
        System.out.println("Działa czat");
        cmi1.sendMessage(SETTINGS + ";" + SEND_USER_NAME);
        cmi2.sendMessage(SETTINGS + ";" + SEND_USER_NAME);
        while (client1.getClientSocket().isConnected() && client2.getClientSocket().isConnected() && getChatStatus()) {
            try {
                is = this.client1.getClientSocket().getInputStream();
                in = new DataInputStream(is);
                if (in.available() > 0) {
                    handleIncomingData(in.readUTF());
                    clearInputStream();
                } else {
                    is = this.client2.getClientSocket().getInputStream();
                    in = new DataInputStream(is);
                    if (in.available() > 0) {
                        handleIncomingData(in.readUTF());
                        clearInputStream();
                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("Koniec czatu");
    }

    private boolean handleIncomingData(String message) {
        

        //TODO: better message handler
        
        String[] rawMessage = message.split(";");
        System.out.println(rawMessage[0] + " " + rawMessage[1] + " " + rawMessage[2]);
        switch (rawMessage[0]) {
            case MESSAGE:
                if (rawMessage[1].equals(client1.getClientName())) {
                    cmi2.sendMessage(MESSAGE + ";" + rawMessage[2]);
                } else {
                    cmi1.sendMessage(MESSAGE + ";" + rawMessage[2]);
                }
                break;
            case SETTINGS:
                switch (rawMessage[1]) {
                    case USER_NAME:
                        try {
                            System.out.println(rawMessage[3]);
                            if (rawMessage[2].equals(client1.getClientName())) {
                                client1.setClientName(rawMessage[3]);
                                System.out.println("Zmiana nazwy client1");
                            } else {
                                client2.setClientName(rawMessage[3]);
                                System.out.println("Zmiana nazwy client2");
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            break;
                        }
                        break;
                    case CLOSE:
                        if(rawMessage[2].equals(client1.getClientName())){
                            client1.setIsOnline(false);
                            setChatStatus(false);
                        } else {
                            client2.setIsOnline(false);
                            setChatStatus(false);
                        }
                        break;
                }
                break;
        }

        return true;
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
