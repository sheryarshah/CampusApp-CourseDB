package com.sa.coursedb.stonybrook.coursedb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.DataOutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    KeyStore keyStore;

    private Button login;
    private EditText user, pass;
    private CheckBox loginCheckBox;
    private DataOutputStream cgiInput;
    private String wrongCredentials = "";

    protected ProgressDialog progressDialog;
    protected ArrayList<String> courses = new ArrayList<String>();
    protected boolean serverError = false;

    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;

    private SharedPreferences userNamePrefs;
    private SharedPreferences.Editor userNameEdit;

    private SharedPreferences studentNamePrefs;
    private SharedPreferences.Editor studentNameEdit;

    public String studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

  /*      login = (Button) findViewById(R.id.login);
        user = (EditText) findViewById(R.id.netid);
        pass = (EditText) findViewById(R.id.password);
        loginCheckBox = (CheckBox)findViewById(R.id.checkBox);

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        coursePrefs = getSharedPreferences("Course", Context.MODE_PRIVATE);
        courseEdit = coursePrefs.edit();

        studentNamePrefs = getSharedPreferences("Student", Context.MODE_PRIVATE);
        studentNameEdit = studentNamePrefs.edit();

        try {
            keyStore.getInstance("AndroidKeyStore");
//            keyStore.load(null);
        } catch (KeyStoreException e) {
            e.printStackTrace();

        }

        //  createNewKeys();


        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();

            }
        });
    }*/

    }
}
