package com.example.mathly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amplifyframework.core.Amplify;

public class LogIn extends AppCompatActivity {

    private EditText eUsername;
    private EditText ePassword;
    private Button bLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);


        eUsername = findViewById(R.id.et_name);
        ePassword = findViewById(R.id.et_password);
        bLogin = findViewById(R.id.but_login);
        
        bLogin.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = eUsername.getText().toString();
                String password = ePassword.getText().toString();

                Amplify.Auth.signIn(
                        username,
                        password,
                        result -> System.out.println(result.isSignInComplete()),
                        error -> Log.e("AuthQuickstart", error.toString())
                );
                AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("tag", userStateDetails.getUserState().toString());
                        switch (userStateDetails.getUserState()){
                            case SIGNED_IN:
                                Intent i = new Intent(LogIn.this, HomeScreen.class);
                                startActivity(i);
                                break;
                            case SIGNED_OUT:
                                System.out.println("SIGNED OUT");
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"Invalid user or password", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e("tag", e.toString());
                    }
                });
            }

        })); // end of button click

    } // End of onCreate

}


//    public void openHomeScreen(){
//        Intent intent = new Intent(this,HomeScreen.class);
//        startActivity(intent);
//    }
//    public void openRegisterScreen(){
//        Intent intent = new Intent(this,Register.class);
//        startActivity(intent);
//    }

