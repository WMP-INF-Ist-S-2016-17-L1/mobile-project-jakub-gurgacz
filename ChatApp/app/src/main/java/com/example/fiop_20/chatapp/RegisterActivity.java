package com.example.fiop_20.chatapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fiop_20.chatapp.server_connection.ConnectionInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView loginView;
    private EditText passwordView;
    private Button registerButton;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.
        loginView = (AutoCompleteTextView) findViewById(R.id.login_register);

        passwordView = (EditText) findViewById(R.id.password_register);

        registerButton = findViewById(R.id.button_register_activity);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        String login = loginView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(!isLoginValid(login)){
            cancel = true;
        }

        if(!isPasswordValid(password)){
            cancel = true;
        }


        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(login, password);
            AsyncTask<Void, Void, Boolean> result = mAuthTask.execute((Void) null);
            boolean x = false;
            try {
                x = result.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(x){
                mAuthTask = null;
                Toast.makeText(getApplicationContext(), "Zarejestrowano", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("username", login);
                intent.putExtra("password", password);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                mAuthTask = null;
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Taki użytkownik już istnieje", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isLoginValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }




    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> implements ConnectionInterface {

        private final String login;
        private final String password;
        private Socket socket;
        private boolean isConnected;


        UserLoginTask(String login, String password) {
            this.login = login;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            System.out.println("łącze z bazą");
            try {
                setSocket(new Socket(MainActivity.SERVER_IP, 9999));
                setConnected(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean registered = false;
            while(isConnected()){
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
                    String message = null;
                    String rawMessage = bufferedReader.readLine();
                    if (!rawMessage.equals("") && rawMessage != null) {
                        System.out.println(rawMessage);
                        String[] splittedMessage = rawMessage.split(";");
                        for(int i = 0; i < splittedMessage.length; i++){
                            System.out.println(splittedMessage[i]);
                        }
                        switch (splittedMessage[0]) {
                            case "SEND_DATA":
                                sendMessage("REGISTER;"+login+";"+password);
                                break;
                            case "REGISTERED":
                                registered = true;
                                setConnected(false);
                                closeSocket();
                                break;
                            case "USER_ALREADY_EXIST":
                                registered = false;
                                setConnected(false);
                                closeSocket();
                                break;
                            case "ERROR":
                                registered = false;
                                setConnected(false);
                                closeSocket();
                                break;
                        }

                    }
                    getSocket().getInputStream().skip(getSocket().getInputStream().available());
                } catch (SocketException e){
                    setConnected(false);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){
                    //e.printStackTrace();
                }
            }
            //DatabaseConnection databaseConnection = new DatabaseConnection(dbAddress, "chat", "users");
            //databaseConnection.findUser(login, password)

            return registered;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                //mPasswordView.requestFocus();
            }
        }

        @Override
        public void close(){
            setConnected(false);
            closeSocket();
        }

        @Override
        public void closeSocket() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        @Override
        public void setSocket(Socket socket) {
            this.socket = socket;
        }

        @Override
        public Socket getSocket() {
            return socket;
        }

        @Override
        public boolean sendMessage(String message) {
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
        public boolean isConnected() {
            return isConnected;
        }

        @Override
        public void setConnected(boolean connected) {
            this.isConnected = connected;
        }
    }

}

