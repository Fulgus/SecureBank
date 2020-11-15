package com.example.bancosegurojmsl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Bienvenida extends AppCompatActivity {
    protected String user;
    protected String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Button entrar_btn = (Button) findViewById(R.id.entrar_btn);
        entrar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_login = new Intent (getApplicationContext(), Login.class);
                startActivity(intent_login);
            }
        });
    }


}