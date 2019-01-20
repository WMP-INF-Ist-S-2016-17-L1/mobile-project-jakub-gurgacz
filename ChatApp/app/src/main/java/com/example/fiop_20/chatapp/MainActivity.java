package com.example.fiop_20.chatapp;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fiop_20.chatapp.server_connection.ServerConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    public static LinearLayout clientsLayout;
    public LinearLayout mainLayout;
    public static Context context;
    View mainView;
    TextView nameView;
    public static String thisUsername;
    static ServerConnection serverConnection = null;

    public static final String SERVER_IP = "10.0.2.2";
    public static final String REQUESTING_NAME = "REQUESTING_NAME";
    public static final String USERNAME = "USERNAME";
    public static final String SERVER_CONNECTION = "SERVER_CONNECTION";
    public static final String CHAT_PORT = "CHAT_PORT";

    public long onBackPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainView = this.getCurrentFocus();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        nameView = findViewById(R.id.requesting_username);
        clientsLayout = findViewById(R.id.clientsLayout);

        context = getApplicationContext();
        //Bundle extraData = getIntent().getExtras();
        //String username = extraData.getString("USERNAME");
        Intent intent = getIntent();
        thisUsername = intent.getStringExtra("USERNAME");

        setUpConnectionWithServer();

        setTitle("Z kim chcesz czatować?");
    }

    private boolean setUpConnectionWithServer() {
        try {
            serverConnection = new ServerConnection(MainActivity.this);
            serverConnection.start(SERVER_IP);
        } catch (Exception e) {
            System.out.println("Błąd podczas łączenia z serwerem");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_logout:
                logout();
                //finish();
                return true;
            default:
                return false;
        }
    }

    public void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Na pewno chcesz się wylogować?")
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goBackToLogin();
                    }
                })
                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .create()
                .show();

    }

    public void goBackToLogin() {
        try {
            disableAutologin();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverConnection.close();
        serverConnection = null;
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finishAndRemoveTask();
        //finish();
    }

    public boolean disableAutologin() throws IOException {
        final boolean[] result = {false};
        new Thread() {
            public void run() {
                try {
                    File dir = getApplicationContext().getFilesDir();
                    File file = new File(dir, "autologin.xml");
                    if (file.exists()) {
                        file.delete();
                    }
                    result[0] = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    result[0] = false;
                }
            }
        }.start();
        return result[0];
    }

    public void showChatRequest(String username) {
        final boolean[] result = new boolean[1];
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setTitle("Prośba o rozpoczęcie czatu")
                .setMessage("Użytkownik " + username + " prosi o ropoczęcie czatu");
        alertBuilder.setPositiveButton("Akceptuj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendMessage("CHAT_REQUEST;ACCEPTED;" + username);
                //openMessagesActivity(username);
            }
        });
        alertBuilder.setNegativeButton("Odrzuć", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendMessage("CHAT_REQUEST;CANCEL;" + username);
            }
        });
        alertBuilder.create();
        alertBuilder.show();
    }

    public boolean addButtonToContext(String username) {
        if (!username.equals(thisUsername)) {
            //LinearLayout layout = new LinearLayout(context);
            try {
                Button btn = new Button(getApplicationContext());
                btn.setText(username);
                btn.setOnClickListener(v -> sendMessage("CHAT_REQUEST;REQUESTING;" + username));
                btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                clientsLayout.addView(btn);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean sendMessage(String message) {
        final boolean[] isSend = {false};
        new Thread("Send Message Thread") {
            public void run() {
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
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(REQUESTING_NAME, username);
        intent.putExtra(USERNAME, thisUsername);
        intent.putExtra(CHAT_PORT, PORT);
//        intent.putExtra(SERVER_CONNECTION, (Parcelable) serverConnection);
        startActivityForResult(intent, 1);
        finishAndRemoveTask();
        //finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if (serverConnection == null) {
                    thisUsername = getIntent().getStringExtra(USERNAME);
                    setTitle(thisUsername);
                    //serverConnection = new ServerConnection(MainActivity.this);
                    //serverConnection.start(SERVER_IP);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        serverConnection = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        int howLong = 3500;
        if (onBackPressedTime == 0) {
            onBackPressedTime = System.currentTimeMillis();
            new Thread("Time reset") {
                public void run() {
                    try {
                        sleep(howLong);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    onBackPressedTime = 0;
                }
            }.start();
            toast("Jeszcze jedno naciśnięcie spowoduje wyjście");
        } else {
            if ((System.currentTimeMillis() - onBackPressedTime) < howLong) {
                serverConnection.close();
                serverConnection = null;
                Intent intent = new Intent(this, LoginActivity.class);
                setResult(RESULT_CANCELED, intent);
                this.finishAndRemoveTask();
            } else {
                onBackPressedTime = 0;
            }
        }

    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void reconnect() {
        setUpConnectionWithServer();

    }
}


