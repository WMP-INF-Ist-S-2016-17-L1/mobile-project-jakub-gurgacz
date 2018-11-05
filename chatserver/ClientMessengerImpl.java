/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fiop_20
 */
public class ClientMessengerImpl implements ClientMessenger{

    private String lastMessage;
    private Client c;
    private OutputStream outToClient;
    private DataOutputStream dos;
    
    public ClientMessengerImpl(Client c){
        this.c = c;
    }
    
    @Override
    public boolean sendMessage(String message) {
        try {
            outToClient = c.getClientSocket().getOutputStream();
            dos = new DataOutputStream(outToClient);
            dos.writeUTF(message);
            
            outToClient.flush();
            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(ClientMessengerImpl.class.getName()).log(Level.SEVERE, null, ex);
            
            return false;
        }
        return true;
    }

    @Override
    public void receivedMessage(String message) {
        // TODO: message reveiver
    }

    @Override
    public void setLastMessage(String message) {
        this.lastMessage = message;
    }

    @Override
    public String getLastMessage() {
        return this.lastMessage;
    }
    
}
