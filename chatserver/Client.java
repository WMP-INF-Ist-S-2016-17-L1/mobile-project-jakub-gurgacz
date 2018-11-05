/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.net.Socket;

/**
 *
 * @author Fiop_20
 */
public class Client {

    private Socket clientSocket;
    private String clientAddr;
    private int clientPort;
    private boolean clientStatus;
    public String clientName;
    public boolean isOnline;
    
    public Client(String ip, int port, Socket socket) {
        
        setClientSocket(socket);
        setClientPort(port);
        ip = ip.replaceFirst("/", "");
        setClientAddr(ip);
        setClientStatus(false);
        setIsOnline(true);
        System.out.println(socket.getInetAddress().toString()+" "+socket.getPort());
    }

    public boolean isIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
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

    public boolean isChatting() {
        return clientStatus;
    }

    public void setClientStatus(boolean clientStatus) {
        this.clientStatus = clientStatus;
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
}
