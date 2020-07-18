package com.example.mathly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LogIn extends AppCompatActivity {

    private Button login_button;
    private Button signup_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);



        login_button = findViewById(R.id.login_butt);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openHomeScreen();
            }
        });
        signup_button = findViewById(R.id.register_butts);
        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegisterScreen();
            }
        });


    }
    public void openHomeScreen(){
        Intent intent = new Intent(this,HomeScreen.class);
        startActivity(intent);
    }
    public void openRegisterScreen(){
        Intent intent = new Intent(this,Register.class);
        startActivity(intent);
    }


}
