package com.example.fiop_20.chatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fiop_20.chatapp.server_connection.ServerConnectionImpl;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity implements Serializable {

    Thread serverThread;
    //public static ServerConnection serverConnection;
    //private ClientsNames clientsNames = null;
    public static LinearLayout clientsLayout;
    public LinearLayout mainLayout;
    public static Context context;
    View mainView;
    TextView nameView;
    public static String thisUsername;
    static ServerConnectionImpl serverConnection;

    public static final String SERVER_IP = "10.0.2.2";
    public static final String REQUESTING_NAME = "REQUESTING_NAME";
    public static final String SERVER_CONNECTION = "SERVER_CONNECTION";
    public static final String CHAT_PORT = "CHAT_PORT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainView = this.getCurrentFocus();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        nameView = findViewById(R.id.requesting_username);
        clientsLayout = findViewById(R.id.clientsLayout);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        context = getApplicationContext();
        //Bundle extraData = getIntent().getExtras();
        //String username = extraData.getString("USERNAME");
        Intent intent = getIntent();
        thisUsername = intent.getStringExtra("USERNAME");

        serverConnection = new ServerConnectionImpl(MainActivity.this);
        serverConnection.start(SERVER_IP);

        setTitle(thisUsername);
        //new ServerConnection(username).start();
        //startConnection(thisUsername);
        //t.start();
    }



    /*public void startConnection(String username){

        try {
            serverConnection = new ServerConnection(username, getApplicationContext(), serverConnection, mainView, MainActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }


        serverConnection.execute("10.0.2.2");

    }*/

    public void showChatRequest(String username){
        final boolean[] result = new boolean[1];
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setTitle("Prośba o rozpoczęcie czatu")
        .setMessage("Użytkownik "+username+" prosi o ropoczęcie czatu");
        alertBuilder.setPositiveButton("Akceptuj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendMessage("CHAT_REQUEST;ACCEPTED;"+username);
                //openMessagesActivity(username);
            }
        });
        alertBuilder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendMessage("CHAT_REQUEST;CANCEL;"+username);
            }
        });
        alertBuilder.create();
        alertBuilder.show();
    }

    public boolean addButtonToContext(String username){
        //LinearLayout layout = new LinearLayout(context);
        try{
            Button btn = new Button(getApplicationContext());
            btn.setText(username);
            btn.setOnClickListener(v -> sendMessage("CHAT_REQUEST;REQUESTING;"+username));
            btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            clientsLayout.addView(btn);
        } catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean sendMessage(String message){
        final boolean[] isSend = {false};
        new Thread("Send Message Thread") {
            public void run(){
                try {
                    serverConnection.sendMessage(message);
                    isSend[0] = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    isSend[0] = false;
                }
            }
        }.start();

        return isSend[0];
    }


    public void openMessagesActivity(int PORT, String username) {
        serverConnection.close();
        serverConnection = null;
        //serverConnection.setMainActivityToNull();
        //ServerConnectionHolder.setServerConnection(serverConnection);
        //serverConnection.setConnected(false);
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(REQUESTING_NAME, username);
        intent.putExtra(CHAT_PORT, PORT);
//        intent.putExtra(SERVER_CONNECTION, (Parcelable) serverConnection);
        startActivity(intent);
    }



}
