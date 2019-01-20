/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jakubgurgacz.chatservermaven;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author Fiop_20
 */
public class ChatServer extends Application {

    public static int x = 0;
    public static ArrayList<Client> list = new ArrayList();

    private static ServerSocket serverSocket;
    private static Socket socket;
    public static int PORT = 10000;

    static ServiceLaunhcer launhcer;

    static DatabaseConnection databaseConnection;

    public static ClientList clientList;

    
    public static TextArea ta;

    static ChatServer chatServer;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        /*
            //new Client(socket.getInetAddress().toString(), socket.getPort());
            //new ClientThread(socket).start();
            

            
         */
        //chatServer = new ChatServer();
        
        launch(args);
        
    }
    
    private static void startServer() {
        System.out.println("Up 'n running");
        clientList = new ClientList();
        try {
            databaseConnection = new DatabaseConnection();
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        databaseConnection.start();
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException ex) {
            Logger.getLogger(ServiceLaunhcer.class.getName()).log(Level.SEVERE, null, ex);
        }

        Client c;
        int x = 0;
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
                try {
                    //socket.close();
                    serverSocket.close();
                } catch (IOException ex1) {
                    Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex1);
                }
                break;
            }
        }
    }

    /*public ChatServer(){
        launhcer = new ServiceLaunhcer(this);
        launhcer.start();
    }*/
    @Override
    public void start(Stage stage) throws Exception {

        stage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        
        stage.setTitle("Logs");
        BorderPane bp = new BorderPane();
        ta = new TextArea();
        ta.setEditable(false);
        stage.setMinHeight(350);
        stage.setMaxWidth(300);
        bp.setCenter(ta);
        Scene scene = new Scene(bp, 300, 300);
        stage.setScene(scene);
        stage.show();
        new Thread(){
            
            @Override
            public void run(){
                startServer();
            }
        }.start();
    }

    static public boolean addToLog(String message) {
        try {
            ta.appendText(message);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

}
