package com.example.fiop_20.chatapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fiop_20.chatapp.server_connection.ChatConnection;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;


public class MessageActivity extends AppCompatActivity {

    private ScrollView messagesView;
    private EditText editText;
    private Button button;
    private static LinearLayout messageKeeper;
    private static Context messageActivityContext;
    String username;
    ChatConnection chatConnection;
    private int myMessageColor;
    private int incomeingMessageColor;
    private DateTimeFormatter dtf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        }

        button = findViewById(R.id.sendMessage);
        editText = findViewById(R.id.message);
        messageKeeper = findViewById(R.id.message_keeper);
        messagesView = findViewById(R.id.messagesView);

        messageActivityContext = getApplicationContext();

        int chatPort = getIntent().getIntExtra(MainActivity.CHAT_PORT, 0);
        String receiverUsername = getIntent().getStringExtra(MainActivity.REQUESTING_NAME);
        username = getIntent().getStringExtra(MainActivity.USERNAME);
        setTitle(getString(R.string.chatWith)+" "+receiverUsername);

        myMessageColor = R.color.myMessage;
        incomeingMessageColor = R.color.senderMessage;

        chatConnection = new ChatConnection(chatPort, this);
        chatConnection.execute(MainActivity.SERVER_IP);
        
        button.setOnClickListener(v -> {
            Toast.makeText(this, "Usuń konwersację", Toast.LENGTH_SHORT);
            checkMessage(editText.getText().toString());

        });
    }


    public void openColorPicker(){
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(getResources().getColor(R.color.colorPrimary))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {

                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        forWhomChangeColor(selectedColor);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            this.finalize();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                })
                .build()
                .show();
    }

    public void toast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public boolean sendMessage(String message) {
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
        return isSend[0];
    }

    public void checkMessage(String message){
        if(message.length() != 0) {
            addMessageToContext(message, true);
            sendMessage("MESSAGE;"+username+";"+message);
        }
    }

    public void addMessageToContext(String message, boolean fromWho){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        TextView dateTime = new TextView(messageActivityContext);
        dateTime.setText(simpleDateFormat.format(calendar.getTime()));

        RelativeLayout relativeLayout = new RelativeLayout(messageActivityContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if(fromWho){
            params.gravity = Gravity.RIGHT;
            params.setMargins((int) messageKeeper.getWidth()/4, 15,15, 0);
            relativeLayout.setId(R.id.my_message);
            relativeLayout.setBackgroundColor(myMessageColor);
        } else {
            params.gravity = Gravity.LEFT;
            params.setMargins(15, 15,(int) messageKeeper.getWidth()/4, 0);
            relativeLayout.setId(R.id.incoming_message);
            relativeLayout.setBackgroundColor(incomeingMessageColor);
        }
        //params.setMargins(0, 15,15, 0);
        relativeLayout.setPadding(20, 12, 20, 12);
        TextView messageContainer = new TextView(messageActivityContext);
        messageContainer.setText(message);
        messageContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        relativeLayout.setLayoutParams(params);
        relativeLayout.addView(messageContainer);
        dateTime.setLayoutParams(params);
        messageKeeper.addView(dateTime);
        messageKeeper.addView(relativeLayout);
        messagesView.postDelayed(new Runnable() {
            @Override
            public void run() {
                messagesView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.item_change_color:
                openColorPicker();
                return true;
            case R.id.quit_conversation:
                onBackPressed();
                return true;
            default:
                //onBackPressed();
                //return  super.onOptionsItemSelected(item);
                return false;
        }
    }

    private void forWhomChangeColor(int selectedColor){
        final int[] selectedButton = {0};
        CharSequence[] charSequences = {"Dla mnie", "Dla nadawcy", "Dla obu"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.changeColorTitle)
                .setSingleChoiceItems(charSequences, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedButton[0] = which;
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeChatColor(selectedColor, selectedButton[0]);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();

    }

    public void changeChatColor(int color, int toWhom){
        switch (toWhom){
            case 0:
                myMessageColor = color;
                break;
            case 1:
                incomeingMessageColor = color;
                break;
            case 2:
                myMessageColor = color;
                incomeingMessageColor = color;
                break;
        }
        int[] whoseMessages = {R.id.my_message, R.id.incoming_message, -1};
        final int childCount = messageKeeper.getChildCount();
        //System.out.println(childCount);
        for(int i = 1; i < childCount; i+=2){
            RelativeLayout rl =(RelativeLayout) messageKeeper.getChildAt(i);
            if(toWhom == 2){
                rl.setBackgroundColor(color);
                continue;
            }
            if(whoseMessages[toWhom] == rl.getId()){
                rl.setBackgroundColor(color);
            }
        }
    }

    @Override
    public void onBackPressed(){
        showAlert();
    }

    private void showAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Chcesz zakończyć czat?")
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        quitChat();
                    }
                })
                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                });
        builder.create();
        builder.show();
    }

    public void quitChat(){
        chatConnection.close();
        chatConnection = null;
        Intent intent = new Intent(MessageActivity.this, MainActivity.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finishAndRemoveTask();
    }


}
