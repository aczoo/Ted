package com.example.ted;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ted.MainActivity;
import com.example.ted.R;


public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";
    public EditText etUsername;
    public EditText etPassword;
    public Button btnLogin;
    public CheckBox checkBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnLogin = findViewById(R.id.btnLogin);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etPassword.setTransformationMethod(new PasswordTransformationMethod());

        checkBox = findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                }
            }
        });
    }
    public void clicked(View view) {
        Log.i(TAG, "Clicked login");
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        loginUser(username, password);
    }

    protected void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login " + username + " in with password " +password);
        // CALL FIREBASE
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();

    }
}