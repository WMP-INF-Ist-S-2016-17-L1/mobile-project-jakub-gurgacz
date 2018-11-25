package com.example.fiop_20.chatapp.server_connection;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fiop_20.chatapp.MainActivity;
import com.example.fiop_20.chatapp.MessageActivity;
import com.example.fiop_20.chatapp.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatConnection extends AsyncTask<String, Void, Void> {

    private Socket socket;
    private int CHAT_PORT;
    private boolean isConnected;

    public ChatConnection(int PORT){
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

                String rawMessage = bufferedReader.readLine();
                if (!rawMessage.equals("") && rawMessage != null) {
                    System.out.println(rawMessage);
                    String[] splittedMessage = rawMessage.split(";");
                    switch (splittedMessage[0]) {
                        case "MESSAGE":
                            new Handler(Looper.getMainLooper()).post(() -> {
                                RelativeLayout relativeLayout = new RelativeLayout(MessageActivity.messageActivityContext);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                params.gravity = Gravity.LEFT;
                                params.setMargins(5, 15,0, 0);
                                relativeLayout.setBackgroundColor(MessageActivity.messageActivityContext.getResources().getColor(R.color.senderMessage));
                                relativeLayout.setPadding(20, 12, 20, 12);
                                TextView messageContainer = new TextView(MessageActivity.messageActivityContext);
                                messageContainer.setText(splittedMessage[1]);
                                messageContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                relativeLayout.setLayoutParams(params);
                                relativeLayout.addView(messageContainer);
                                MessageActivity.messageKeeper.addView(relativeLayout);
                            });
                            break;
                        case "SEND_CLIENT_NAME":
                            sendMessage("USERNAME;"+MainActivity.thisUsername);
                            //senderResult = new Sender(getSocket(), ServerConnection.this).execute("USERNAME;"+MainActivity.thisUsername);
                            //System.out.println(senderResult.getStatus());
                            break;
                    }

                }
                getSocket().getInputStream().skip(getSocket().getInputStream().available());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return null;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
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
