package com.example.bancosegurojmsl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Usuario extends AppCompatActivity {
    TextView saldo;
    TextView user;
    MasterKey masterKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);
        String user_name = getIntent().getStringExtra("user");
        user = findViewById(R.id.user_view);
        user.setText(user_name);
        saldo = findViewById(R.id.saldo_display);

        try {
            masterKey = new MasterKey.Builder(getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button save_SD = (Button) findViewById(R.id.save_sd);
        save_SD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Date date = Calendar.getInstance().getTime();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd_hh-mm");
                    String strDate = dateFormat.format(date);
                    escribirSD(saldo.getText().toString(), "Banco_"+strDate +".txt", masterKey);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void escribirSD(String info, String file, MasterKey masterKey) throws IOException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted
            if (isExternalStorageWritable()) {
                File[] externalStorageVolumes =
                        ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
                File primaryExternalStorage = externalStorageVolumes[1];

                String sdPath = primaryExternalStorage.getAbsolutePath();
                try {
                    EncryptedFile encryptedFile = new EncryptedFile.Builder(getApplicationContext(),
                            new File(sdPath, file), masterKey, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB).build();
                    byte[] fileContent = info.getBytes();
                    OutputStream outputStream = encryptedFile.openFileOutput();
                    outputStream.write(fileContent);
                    outputStream.flush();
                    outputStream.close();
                    Toast.makeText(this, "File Saved.", Toast.LENGTH_SHORT).show();
                }catch(Exception e) {
                    Toast.makeText(this, "Error.", Toast.LENGTH_SHORT).show();
                    Log.i("Error", e.toString());
                }
            } else {
                Toast.makeText(this, "Can't Write on File", Toast.LENGTH_SHORT).show();
            }
        }
        else {
        //Permission is not granted so you have to request it
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

    }

    private boolean isExternalStorageWritable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.i("State", "Yes, it's writable");
            return true;
        }
        else{
            Log.i("State", "No, it's not writable");
            return false;
        }
    }
}