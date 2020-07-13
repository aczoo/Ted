package com.example.ted;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class SignUpActivity extends LoginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btnLogin.setText("Sign Up");
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked sign up");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                createUser(username, password);
            }
        });
    }
    private void createUser(final String username, final String password){
        Log.i(TAG, "Attempting to create account : "+ username);
        //FIREBASE STUFF
    }
}