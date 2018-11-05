/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fiop_20
 */
public class ClientThread extends Thread{
    
    private Socket clientSocket;
    private Client client;
    private String lastMessage;
    
    public ClientThread(Socket socket) {
        this.clientSocket = socket;

        this.client = getClient();
    }
    
    public void run(){
        while(true){
            try {
                String message = new DataInputStream(clientSocket.getInputStream()).readUTF();
                setLastMessage(message);
            } catch (Exception ex) {
                System.out.println("I/O error: ");
                ex.printStackTrace();
            }
        }  
    }
    
    public void sendMessage(String message){
        try {
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            out.writeUTF(message);
            setLastMessage(message);
        } catch (IOException ex) {
            System.out.println("I/O error: ");
            ex.printStackTrace();
        }
    }

    public Client getClient() {
        return client;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setClient(Client client) {
        this.client = client;
    }
    
    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    
}
