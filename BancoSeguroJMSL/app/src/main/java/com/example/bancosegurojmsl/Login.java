package com.example.bancosegurojmsl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;


public class Login extends AppCompatActivity {

    public static final int SERVER_PORT = 8888;
    public static final String SERVER_IP = "192.168.75.1";

    MasterKey masterKeyAlias;

    Thread SSLConnection = null;
    EditText username;
    EditText password;
    Button login;
    String textToSend;
    protected String user;
    protected String pass;
    boolean loggedIn;
    SharedPreferences pref;
    public static final String TAG = "Testing AUTH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE )
                    .build();
            masterKeyAlias = new MasterKey.Builder(getApplicationContext()).setKeyGenParameterSpec(spec).build();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loggedIn = already_logged_in();

        if(loggedIn){
            String textToSend = "+AUTH\n" + user + "\n" + pass;
            SSLConnection = new Thread(new SSLConnect(textToSend));
            SSLConnection.start();
        }
        else {
            setContentView(R.layout.activity_login);

            CheckBox remember = (CheckBox) findViewById(R.id.rememberUser);
            username = findViewById(R.id.editTextUser);
            password = findViewById(R.id.editTextPwd);
            login = findViewById(R.id.login_btn);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user = username.getText().toString();
                    pass = password.getText().toString();
                    if (user.matches("") || pass.matches("")) //Check if user or pass are not set
                        Toast.makeText(getApplicationContext(), "You must enter a valid username and password", Toast.LENGTH_SHORT).show();
                    else{
                        //Send request to java server
                        textToSend = "+AUTH\n" + user + "\n" + pass;
                        SSLConnection = new Thread(new SSLConnect(textToSend));
                        SSLConnection.start();
                        //This happens once you click Login button
                        if (remember.isChecked()){
                            try {
                                pref = EncryptedSharedPreferences.create(getApplicationContext(),
                                        "secret_shared_prefs",
                                        masterKeyAlias,
                                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
                            } catch (GeneralSecurityException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //pref = getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
                            SharedPreferences.Editor Ed = pref.edit();
                            Ed.putString("username", user);
                            Ed.putString("password", pass);
                            Ed.commit();
                        }
                    }
                }
            });

        }
    }

    private boolean already_logged_in() {
        try {
            pref = EncryptedSharedPreferences.create(getApplicationContext(),
                    "secret_shared_prefs",
                    masterKeyAlias,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (pref.contains("username") && pref.contains("password")) {
            user = pref.getString("username", null);
            pass = pref.getString("password", null);
            return true;
        }
        else
            return false;
    }

    private PrintWriter output;
    private BufferedReader input;
    class SSLConnect implements Runnable {
        private String message;
        SSLConnect(String msg){this.message = msg;}
        public void run() {
            Socket socket;
            try {
                socket = getSSLSocket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
                new Thread(new ClientMSG(message)).start();
                new Thread(new ServerMSG()).start();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class ServerMSG implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine();
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "server: " + message + "\n");
                                if (message.equals("OK")){
                                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                                    Intent intent_userSpace = new Intent (Login.this, Usuario.class);
                                    intent_userSpace.putExtra("user", user);
                                    startActivityForResult(intent_userSpace, 0);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "Failed to login, enter a valid User and/or Password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class ClientMSG implements Runnable {
        private String message;
        ClientMSG(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            output.write(message);
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "client: " + message + "\n");
                }
            });
        }
    }

    public SSLSocket getSSLSocket(String ipAddr, int portNum) throws Exception {

        //The KeyStore with the self-signed certificate.
        InputStream truststoreFile = getResources().openRawResource(R.raw.client);
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(truststoreFile, "1234".toCharArray());

        //The KeyManagerFactory that supposed to load the Android local //keystore, but here we just use the same keystore that has the //self-signed certificate.
        KeyManagerFactory keyManager = KeyManagerFactory.getInstance("X509");
        keyManager.init(store,"1234".toCharArray());

        //The TrustManagerFactory loads the keystore with the //self-signed certificate.
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        tmf.init(store);

        //Create an instance of SSLContext.
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManager.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        //Create the Socket using SSLContext.
        Socket socket = (SSLSocket)context.getSocketFactory().createSocket(ipAddr, portNum);

        return (SSLSocket) socket;

    }
}