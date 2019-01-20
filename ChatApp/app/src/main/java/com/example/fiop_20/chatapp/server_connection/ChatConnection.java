package com.example.fiop_20.chatapp.server_connection;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fiop_20.chatapp.MainActivity;
import com.example.fiop_20.chatapp.MessageActivity;
import com.example.fiop_20.chatapp.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ChatConnection extends AsyncTask<String, Void, Void> implements ConnectionInterface{

    private Socket socket;
    private int CHAT_PORT;
    private boolean isConnected;
    MessageActivity messageActivity;

    public ChatConnection(int PORT, MessageActivity messageActivity){
        this.messageActivity = messageActivity;
        setCHAT_PORT(PORT);
    }

    public boolean sendMessage(String message){
        try {
            PrintWriter printWriter = new PrintWriter(getSocket().getOutputStream());
            printWriter.write(message+"\n");
            printWriter.flush();
            getSocket().getOutputStream().flush();
            System.out.println("wysłałem: "+message);
        } catch (Exception e) {
            System.out.println("Został wyrzucony wyjątek");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            setSocket(new Socket(strings[0], getCHAT_PORT()));
            setConnected(true);
        } catch (IOException e) {
            e.printStackTrace();
            setConnected(false);
        }



        while (isConnected()) {
            BufferedReader bufferedReader;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
                String message = null;
                String rawMessage = bufferedReader.readLine();
                if (!rawMessage.equals("") && rawMessage != null) {
                    System.out.println(rawMessage);
                    String[] splittedMessage = rawMessage.split(";");
                    switch (splittedMessage[0]) {
                        case "MESSAGE":
                            int beggingOfCut = 9 + splittedMessage[1].length();
                            String finalMessage = rawMessage.substring(beggingOfCut);
                            new Handler(Looper.getMainLooper()).post(() -> {
                                messageActivity.addMessageToContext(finalMessage, false);
                            });
                            message = null;
                            break;
                        case "SEND_CLIENT_NAME":
                            sendMessage("USERNAME;" + MainActivity.thisUsername);
                            break;
                        case "STOP_CHAT":
                            new Handler(Looper.getMainLooper()).post(() -> {
                                messageActivity.toast("Czat został zakończony");
                                messageActivity.quitChat();
                            });
                    }

                }
                getSocket().getInputStream().skip(getSocket().getInputStream().available());
            }catch (SocketException e){
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if(messageActivity != null){
                        messageActivity.toast("Połączenie z serwerem zostało przerwane");
                        messageActivity.quitChat();
                    }
                });
                break;
            } catch (IOException e) {
                e.printStackTrace();
            } catch(NullPointerException e){
                e.printStackTrace();
            }
        }
        closeSocket();

        return null;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    @Override
    public void closeSocket() {
        try {
            getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        messageActivity = null;
        setConnected(false);
        closeSocket();
        try {
            this.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket chatSocket) {
        this.socket = chatSocket;
    }

    public int getCHAT_PORT() {
        return CHAT_PORT;
    }

    public void setCHAT_PORT(int CHAT_PORT) {
        this.CHAT_PORT = CHAT_PORT;
    }


}
