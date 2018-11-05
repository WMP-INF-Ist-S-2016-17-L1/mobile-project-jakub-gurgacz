/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

/**
 *
 * @author Fiop_20
 */
public interface ClientMessenger {
   
    public boolean sendMessage(String message);
    
    public void receivedMessage(String message);
    
    public void setLastMessage(String message);
    
    public String getLastMessage();
}
