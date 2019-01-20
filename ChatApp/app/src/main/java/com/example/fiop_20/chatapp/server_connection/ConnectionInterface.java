package com.example.fiop_20.chatapp.server_connection;

import java.io.PrintWriter;
import java.net.Socket;

public interface ConnectionInterface {

    Socket socket = null;

    abstract void setSocket(Socket socket);
    public abstract Socket getSocket();
    public boolean sendMessage(String message);
    public boolean isConnected();
    public void setConnected(boolean connected);
    public void closeSocket();
    public void close();

}
