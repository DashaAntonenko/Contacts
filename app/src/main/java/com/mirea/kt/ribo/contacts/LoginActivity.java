package com.mirea.kt.ribo.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText loginEditText = findViewById(R.id.loginEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String login = loginEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (!login.isEmpty() && !password.isEmpty()) {
                String result = makeRequest(login, password);
                if (handleResult(result)) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("data", result);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, R.string.login_empty, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Boolean handleResult(String result) {
        if (result == null) return false;
        try {
            JSONObject object = new JSONObject(result);
            int code = object.getInt("result_code");
            if (code == 1) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String makeRequest(String login, String password) {
        String address = "https://android-for-students.ru/coursework/login.php";
        HashMap<String, String> map = new HashMap<>();
        map.put("lgn", login);
        map.put("pwd", password);
        map.put("g", "RIBO-04-22");
        HttpRunnable httpRunnable = new HttpRunnable(address, map);
        Thread thread = new Thread(httpRunnable);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return httpRunnable.getResponseBody();
    }
}