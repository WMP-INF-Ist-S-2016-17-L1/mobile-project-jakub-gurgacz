/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jakubgurgacz.chatservermaven;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 *
 * @author Fiop_20
 */
public class DatabaseOperations extends Thread {

    private Socket socket;
    MongoClient client;
    MongoDatabase database;
    MongoCollection<Document> collection;

    public DatabaseOperations(Socket socket) {
        ChatServer.addToLog("Do bazy połączył się: "+socket.getInetAddress().toString()+" "+socket.getPort()+"\n");
        this.socket = socket;
        client = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder
                                -> builder.hosts(Arrays.asList(new ServerAddress("localhost", 27017))))
                        .build());

        database = client.getDatabase("chat");

        collection = database.getCollection("users");
    }

    @Override
    public void run() {
        sendMessage("SEND_DATA");
        boolean x = true;
        while (x) {
            try {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
                String incomeing = bufferedReader.readLine();
                if (!incomeing.equals("") && incomeing != null) {
                    String[] rawMessage = incomeing.split(";");
                    for (int i = 0; i < rawMessage.length; i++) {
                        System.out.println(rawMessage[i]);
                    }
                    switch (rawMessage[0]) {
                        case "CHECKDATA":
                            boolean checkUserExistance = checkUserData(rawMessage[1], rawMessage[2]);
                            if(checkUserExistance){
                                sendMessage("ACCEPTED");
                                x = false;
                            } else {
                                sendMessage("REJECTED");
                                x = false;
                            }
                            break;
                        case "REGISTER":
                            boolean checkUser = checkIfUSerExits(rawMessage[1]);
                            
                            if(checkUser){
                                sendMessage("USER_ALREADY_EXIST");
                                x = false;
                            } else {
                                boolean isInserted = insertUser(rawMessage[1], rawMessage[2]);
                                if(isInserted){
                                    sendMessage("REGISTERED");
                                    x = false;
                                } else {
                                    sendMessage("ERROR");
                                    x = false;
                                }
                            }
                            client.close();
                            break;
                    }
                }
                incomeing = null;
                bufferedReader = null;
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex) {
                //setClientStatus(false);
            }
        }
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(DatabaseOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean insertUser(String username, String password){
        
        Document doc = new Document()
                .append("_id", new ObjectId())
                .append("username", username)
                .append("password", password);
        
        collection.insertOne(doc);
        
        return checkIfUSerExits(username);
    }
    
    public boolean checkIfUSerExits(String username){
        FindIterable<Document> iterDoc = collection.find(and(eq("username", username)));

        Iterator iterator = iterDoc.iterator();

        while (iterator.hasNext()) {
            System.out.println("Dodano");
            return true;
        }
        
        return false;
    
    }

    public boolean checkUserData(String username, String password) {
        //System.out.println(collection.countDocuments());

        FindIterable<Document> iterDoc = collection.find(and(eq("username", username), eq("password", password)));

        Iterator iterator = iterDoc.iterator();

        while (iterator.hasNext()) {
            
            client.close();
            return true;
        }
        
        client.close();
        return false;
    }

    public boolean sendMessage(String message) {
        try {
            PrintWriter printWriter = new PrintWriter(getSocket().getOutputStream());
            printWriter.write(message + "\n");
            printWriter.flush();
        } catch (IOException ex) {
            ex.printStackTrace();

            return false;
        }
        return true;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

}