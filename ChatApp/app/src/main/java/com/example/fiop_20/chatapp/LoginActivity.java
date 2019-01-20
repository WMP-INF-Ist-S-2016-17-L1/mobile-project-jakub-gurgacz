package com.example.fiop_20.chatapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.fiop_20.chatapp.server_connection.ConnectionInterface;


import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

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

    public final String USERNAME = "USERNAME";

    // UI references.
    private AutoCompleteTextView loginView;
    private EditText passwordView;
    private CheckBox rememberPassword;
    private View mProgressView;
    private View mLoginFormView;

    // User pass and loing
    private String login = null;
    private String password = null;

    private boolean autologin;

    public Activity loginActivity;

    public static void exit() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginView = (AutoCompleteTextView) findViewById(R.id.email);

        autologin = isAutologin();
        rememberPassword = (CheckBox) findViewById(R.id.remember_pswd_checkbox);
        rememberPassword.setChecked(autologin);
        passwordView = (EditText) findViewById(R.id.password);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button register = (Button) findViewById(R.id.register_button);
        register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        if(rememberPassword.isChecked()){
            setUsersPassAndLogin();
            attemptLogin();
        }
    }

    private void createXML(){
        String data;
        File file = new File("autologin.xml");
        if(file.exists()){
            file.delete();
        }
        if(rememberPassword.isChecked()){
            data="<?xml version = \"1.0\"?>\n" +
                    "<root>"+
                    "\t<autologin>\n" +
                    "\t\t<decision>true</decision>\n" +
                    "\t</autologin>\n" +
                    "\t<login>"+ this.login+"</login>\n" +
                    "\t<password>"+hashPassword()+"</password>\n" +
                    "</root>";
        } else {
            data="<?xml version = \"1.0\"?>\n" +
                    "<root>"+
                    "\t<autologin>\n" +
                    "\t\t<decision>false</decision>\n" +
                    "\t</autologin>\n" +
                    "</root>";
        }
        try {

            FileOutputStream fos = openFileOutput("autologin.xml", Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex){
            ex.printStackTrace();
        }

    }

    private String hashPassword() {


        return this.password;
    }

    private void registerUser() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                this.login = intent.getStringExtra("username");
                this.password = intent.getStringExtra("password");
                startMainActivity();
            } else {
                finishAndRemoveTask();
            }
        }
    }

    private void setUsersPassAndLogin(){
        try {
            File directory = getApplicationContext().getFilesDir();
            File xml = new File(directory, "autologin.xml");
            if(xml.isFile()) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(xml);
                loginView.setText(document.getElementsByTagName("login").item(0).getTextContent());
                passwordView.setText(document.getElementsByTagName("password").item(0).getTextContent());
            }
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isAutologin() {
        try {
            File directory = getApplicationContext().getFilesDir();
            File xml = new File(directory, "autologin.xml");
            if(xml.isFile()) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(xml);
                String option = document.getElementsByTagName("decision").item(0).getTextContent();
                return Boolean.parseBoolean(option);
                }
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        }
        return false;
    }



    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(loginView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        loginView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String email = loginView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        if (!autologin) {
            // Check for a valid email address.
            if (TextUtils.isEmpty(email)) {
                loginView.setError(getString(R.string.error_field_required));
                focusView = loginView;
                cancel = true;
            } else if (!isEmailValid(email)) {
                loginView.setError(getString(R.string.error_invalid_email));
                focusView = loginView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            this.login = loginView.getText().toString();
            this.password = passwordView.getText().toString();
            mAuthTask = new UserLoginTask(this.login, this.password, rememberPassword.isChecked());
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
                startMainActivity();
            } else {
                mAuthTask = null;
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Złe dane logowania", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(USERNAME, this.login);

        startActivityForResult(intent, 1);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        //return email.contains("@");
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        //return password.length() > 4;
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

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> implements ConnectionInterface {

        private String login;
        private String password;

        Socket socket;
        boolean isConnected;

        UserLoginTask(String login, String password, boolean decision) {
            this.login = login;
            this.password = password;
            setConnected(true);
            createXML();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                setSocket(new Socket(MainActivity.SERVER_IP, 9999));
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean verified = false;
            while(isConnected()){
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
                    String message = null;
                    String rawMessage = bufferedReader.readLine();
                    if (!rawMessage.equals("") && rawMessage != null) {
                        System.out.println(rawMessage);
                        String[] splittedMessage = rawMessage.split(";");
                        for(int i = 0; i< splittedMessage.length; i++){
                            System.out.println(splittedMessage[i]);
                        }
                        switch (splittedMessage[0]) {
                            case "SEND_DATA":
                                sendMessage("CHECKDATA;"+login+";"+password);
                                break;
                            case "ACCEPTED":
                                verified = true;
                                close();
                                break;
                            case "REJECTED":
                                verified = false;
                                close();
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
            return verified;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
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
        public void close() {
            setConnected(false);
            closeSocket();
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

