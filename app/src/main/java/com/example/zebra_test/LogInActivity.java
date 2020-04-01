package com.example.zebra_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class LogInActivity extends AppCompatActivity {
    EditText userNameEditText;
    EditText passwordEditText;


    public void logInButton(View view){
        if(userNameEditText.getText().toString().equals("CGI") && passwordEditText.getText().toString().equals("Logimat")){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(getApplicationContext(), "User name or Password is incorrect!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        userNameEditText = (EditText)findViewById(R.id.userNameEditText);
        passwordEditText = (EditText)findViewById(R.id.passwordEditText);

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);//close tapping bar
        mgr.hideSoftInputFromWindow(userNameEditText.getWindowToken(), 0);//close tapping bar
        mgr.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);//close tapping bar
    }


}
