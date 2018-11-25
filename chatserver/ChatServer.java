/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Fiop_20
 */
public class ChatServer {

    public static int x = 0;
    public static ArrayList<Client> list = new ArrayList();

    private static ServerSocket serverSocket;
    private static Socket socket;
    public static int PORT = 10000;
    
        public static ClientList clientList;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Up 'n running");
        clientList = new ClientList();
        try {
            serverSocket = new ServerSocket(PORT);
            
        } catch (IOException ex) {
            System.out.println("I/O exception: ");
            ex.printStackTrace();
        }

        Client c;
        int x=0;
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
                //socket.close();
                serverSocket.close();
                break;
            }
            //new Client(socket.getInetAddress().toString(), socket.getPort());
            //new ClientThread(socket).start();
            

            
            
        }
    }
}
