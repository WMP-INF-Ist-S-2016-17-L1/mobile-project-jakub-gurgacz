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

/**
 *
 * @author Fiop_20
 */
public class ChatServer {

    public static int x = 0;
    public static ArrayList<Client> list = new ArrayList();

    private static ServerSocket serverSocket;
    private static Socket socket;
    private static final int PORT = 10000;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException ex) {
            System.out.println("I/O exception: ");
            ex.printStackTrace();
        }

        while (true) {
            
            //TODO: list of not-chatting clients
            
            try {
                socket = serverSocket.accept();
                
            } catch (IOException ex) {
                System.out.println("I/O exception: ");
                ex.printStackTrace();
                //socket.close();
                serverSocket.close();
                break;
            }
            //new Client(socket.getInetAddress().toString(), socket.getPort());
            //new ClientThread(socket).start();
            list.add(new Client(socket.getInetAddress().toString(), socket.getPort(), socket));

            if (list.size()==2){
                new Chat(list.get(0), list.get(1)).start();
            }
            
            //break;
        }
    }
}
