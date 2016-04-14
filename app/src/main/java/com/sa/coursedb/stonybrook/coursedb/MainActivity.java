package com.sa.coursedb.stonybrook.coursedb;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    private SharedPreferences loginPreferences;
    private Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        Log.d(TAG, "login value: " + saveLogin);
        if(saveLogin){
            Intent intent = new Intent(this, CoursesActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
