/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jakubgurgacz.chatservermaven;

import java.util.ArrayList;

/**
 *
 * @author Fiop_20
 */
public class ClientList extends ArrayList<Client> {

    private ArrayList<Client> clientList;
    private ArrayList<Chat> chatList;

    public ClientList() {
        super();
        clientList = new ArrayList<>();
        chatList = new ArrayList<>();
    }

    public boolean sendChatRequest(String requestedUser, String requestingUser) {
        for (Client c : clientList) {
            if (c.getClientName().equals(requestedUser)) {
                if (c.sendMessage("CHAT_REQUEST;REQUESTING;" + requestingUser)) {
                    System.out.println(c.getClientName() + "   Zaproszenie wysłane do " + requestedUser);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return getClientList().isEmpty();
    }

    @Override
    public boolean add(Client c) {
        clientList.add(c);
        return true;
    }

    @Override
    public Client get(int index) {
        return getClientList().get(index);
    }

    @Override
    public int size() {
        return getClientList().size();
    }

    public String getClientsName() {
        if (clientList.isEmpty()) {
            return "";
        }
        String names = "";
        for (Client c : clientList) {
            if (c.getClientName() == null || c.getClientName().equals("")) {
                continue;
            } else {
                names += ";" + c.getClientName();
            }
        }
        return names;
    }

    public boolean removeByName(String name) {
        for (Client c : clientList) {
            if (c.getClientName().equals(name)) {
                clientList.remove(c);
                sendNames();
                return true;
            }
        }
        return false;
    }

    public ArrayList<Client> getClientList() {
        return clientList;
    }

    void sendNames() {
        String clientsName = getClientsName();
        for (Client c : clientList) {
            c.sendMessage("CLIENTS_LIST" + clientsName);
        }
    }

    private Client getClientByName(String name) {
        Client client = null;
        for (Client c : clientList) {
            if (c.getClientName().equals(name)) {
                client = c;
                break;
            }
        }
        return client;
    }

    public void sendMessageByName(String name, String message) {
        for (Client c : clientList) {
            if (c.getClientName().equals(name)) {
                c.sendMessage(message);
                break;
            }
        }
    }

    boolean initiateChat(int PORT) {
        try {
            Chat chat = new Chat(PORT);
            chat.start();
            chatList.add(chat);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

}
