package com.example.fiop_20.chatapp.server_connection;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.example.fiop_20.chatapp.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class ServerConnectionImpl extends AsyncTask<String, Void, Void> {

    private Socket socket;
    private final int MAX_READ_SIZE = 1024;
    private boolean isConnected;
    Queue<String> messageQueue;
    private MainActivity mainActivity;

    public ServerConnectionImpl(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public void start(String ip){
        messageQueue = new LinkedList<String>();
        //handleMessages();
        execute(ip);
    }

    public void setSocket(Socket socket){
        this.socket = socket;
    }

    public Socket getSocket(){
        return this.socket;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            setSocket(new Socket(strings[0], 10000));
            setConnected(true);
        } catch (IOException e) {
            e.printStackTrace();
            setConnected(false);
        }

        while (isConnected()) {
            BufferedReader bufferedReader;
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));

                    String rawMessage = bufferedReader.readLine();
                    if (!rawMessage.equals("") && !rawMessage.isEmpty()) {
                        System.out.println(rawMessage);
                        String[] splittedMessage = rawMessage.split(";");
                        switch (splittedMessage[0]) {
                            case "SEND_CLIENT_NAME":
                                sendMessage("USERNAME;"+MainActivity.thisUsername);
                                //senderResult = new Sender(getSocket(), ServerConnection.this).execute("USERNAME;"+MainActivity.thisUsername);
                                //System.out.println(senderResult.getStatus());
                                break;
                            case "CHAT_REQUEST":
                                if(splittedMessage[1].equals("REQUESTING")){
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        mainActivity.showChatRequest(splittedMessage[2]);
                                    });
                                } else if(splittedMessage[1].equals("ACCEPTED")){
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        //mainActivity.openMessagesActivity(splittedMessage[2]);
                                    });
                                } else if(splittedMessage[1].equals("REJECTED")){
                                    //TODO: reject prompt
                                } else if(splittedMessage[1].equals("START_CHAT")){
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        mainActivity.openMessagesActivity(Integer.valueOf(splittedMessage[2]), splittedMessage[3]);
                                    });
                                }
                                break;
                            case "CLIENTS_LIST":
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    MainActivity.clientsLayout.removeAllViews();
                                });
                                for(int i = 1;i<splittedMessage.length;i++) {
                                    int finalI = i;
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        if(!mainActivity.addButtonToContext(splittedMessage[finalI])){
                                            /*Button btn = new Button(mainActivity.getApplicationContext());
                                            btn.setText(splittedMessage[finalI]);
                                            btn.setOnClickListener(v -> MainActivity.sendMessage("CHAT_REQUEST;REQUESTING;"+MainActivity.thisUsername));
                                            btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                            MainActivity.clientsLayout.addView(btn);*/
                                        }
                                    });
                                }
                                break;
                        }

                    }
                    getSocket().getInputStream().skip(getSocket().getInputStream().available());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ex){
                    ex.printStackTrace();
            } catch (RuntimeException ex){
                    ex.printStackTrace();
            }
        }
        return null;
    }
    /*private void handleMessages() {
        new Thread("messageHandler") {
        @Override
            public void run() {
                while (isConnected()) {
                    if (!messageQueue.isEmpty()) {
                        String[] rawMessage = messageQueue.poll().split(";");
                        switch (rawMessage[0]) {
                            case "SEND_CLIENT_NAME":
                                System.out.println("SEND_CLIENT_NAME");
                                sendMessage("USERNAME;" + MainActivity.thisUsername);
                                break;
                        }

                    }
                }
            }
        }.start();
    }*/

    public boolean sendMessage(String message){
        try {
            PrintWriter printWriter = new PrintWriter(getSocket().getOutputStream());
            printWriter.write(message+"\n");
            printWriter.flush();
            getSocket().getOutputStream().flush();
            System.out.println("wysłałem: "+message);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void setMainActivityToNull(){
        mainActivity = null;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public void close() {
        try {
            mainActivity = null;
            setConnected(false);
            getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
