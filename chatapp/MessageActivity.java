package com.example.fiop_20.chatapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.fiop_20.chatapp.server_connection.ChatConnection;


public class MessageActivity extends AppCompatActivity {

    public ScrollView scrollView;
    public EditText editText;
    public Button button;
    public static LinearLayout messageKeeper;
    public RelativeLayout relativeLayout;
    public static Context messageActivityContext;
    ChatConnection chatConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        button = findViewById(R.id.sendMessage);
        editText = findViewById(R.id.message);
        messageKeeper = findViewById(R.id.message_keeper);
        relativeLayout = findViewById(R.id.relativeLayout);
        messageActivityContext = getApplicationContext();

        int chatPort = getIntent().getIntExtra(MainActivity.CHAT_PORT, 0);
        String username = getIntent().getStringExtra(MainActivity.REQUESTING_NAME);
        setTitle("Czat z "+username);


        chatConnection = new ChatConnection(chatPort);
        chatConnection.execute(MainActivity.SERVER_IP);


        button.setOnClickListener(v -> {

            sendMessage("MESSAGE;"+editText.getText().toString()+";"+MainActivity.thisUsername);

        });

        //ServerConnectionImpl.start("10.0.2.2");

        //MainActivity.serverConnection = null;
        //startMessageHandler();

    }

    /*public void startMessageHandler(){
        ServerConnectionHolder socketHandler = (ServerConnectionHolder)getApplicationContext();
        messageHandler = new MessageHandler(socketHandler.getSocket());
        messageHandler.execute();
    }*/

    public void sendMessage(String message) {
        final boolean[] isSend = {false};
        new Thread("Send Message Thread") {
            public void run(){
                try {
                    isSend[0] = chatConnection.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

            RelativeLayout relativeLayout = new RelativeLayout(messageActivityContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.RIGHT;
        params.setMargins(0, 15,5, 0);
            relativeLayout.setBackgroundColor(getResources().getColor(R.color.myMessage));
            relativeLayout.setPadding(20, 12, 20, 12);
            TextView messageContainer = new TextView(messageActivityContext);
            messageContainer.setText(message.split(";")[1]);
            messageContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            relativeLayout.setLayoutParams(params);
            relativeLayout.addView(messageContainer);
            messageKeeper.addView(relativeLayout);

    }
}
