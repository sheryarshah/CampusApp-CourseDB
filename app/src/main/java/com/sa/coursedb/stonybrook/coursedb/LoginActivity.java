package com.sa.coursedb.stonybrook.coursedb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;

import javax.security.auth.x500.X500Principal;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    private KeyStore keyStore;

    private Button login;
    private EditText user, pass;
    private CheckBox loginCheckBox;
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

        login = (Button) findViewById(R.id.login);
        user = (EditText) findViewById(R.id.netid);
        pass = (EditText) findViewById(R.id.password);
        loginCheckBox = (CheckBox) findViewById(R.id.checkBox);

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        userNamePrefs = getSharedPreferences("userNamePrefs", Context.MODE_PRIVATE);
        userNameEdit = userNamePrefs.edit();

        studentNamePrefs = getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE);
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
    }

    private void createNewKeys() {
        String alias = "SBCourseDiscussionAPP";
        try {
            // Create new key if needed
            if (!keyStore.containsAlias(alias)) {
                //  Calendar start = Calendar.getInstance();
                //  Calendar end = Calendar.getInstance();
                // end.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
                        .setAlias(alias)
                        .setSubject(new X500Principal("CN=Course Discussion, O=Stony Brook"))
                        .setSerialNumber(BigInteger.ONE)
                                //     .setStartDate(start.getTime())
                                //     .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);

                KeyPair keyPair = generator.generateKeyPair();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Exception " + e.getMessage() + " occured", Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void login() {

        String netID = user.getText().toString();
        String password = pass.getText().toString();
        if (netID.matches("^\\s*$") || password.equalsIgnoreCase("")) {
            Toast.makeText(getBaseContext(), "Please input all the fields", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "NetID is = " + netID);
        if (!validateLogin(netID, password)) {
            onLoginFailed();
            return;
        }

        login.setEnabled(false);

    }

    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        Log.d(TAG, "Activity Restarted");
        Log.d(TAG, "Value of wrong credentals = " + wrongCredentials);
        login = (Button) findViewById(R.id.login);
        user = (EditText) findViewById(R.id.netid);
        pass = (EditText) findViewById(R.id.password);

        try {
            keyStore.getInstance("AndroidKeyStore");
//            keyStore.load(null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        userNameEdit.clear();
        userNameEdit.commit();

        //  createNewKeys();

        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();

            }
        });
    }

    public boolean validateLogin(String netID, String password) {
        boolean valid = true;

        progressDialog = new ProgressDialog(LoginActivity.this);
        login.setEnabled(false);

        LoginAsync loginAsyncTask = new LoginAsync();
        loginAsyncTask.execute(netID, password);

        return valid;
    }

    private class LoginAsync extends AsyncTask<String, Void, String> {


        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.setCanceledOnTouchOutside(false);
            if (!progressDialog.isShowing())
                progressDialog.show();

        }

        @Override
        protected String doInBackground(String... strings) {

            String netID, password;

            netID = strings[0];
            password = strings[1];

            try {
                Log.d(TAG, "Constructing Data");
                // Construct data
                StringBuilder dataBuilder = new StringBuilder();
                dataBuilder.append(URLEncoder.encode("userName=", "UTF-8")).append('=').append(URLEncoder.encode("password", "UTF-8"));

                // Send data
                Log.d(TAG, "Sending Data");
               // URL url = new URL("http://10.0.3.2:8080/getCourses");

                URL url = new URL("http://192.168.141.112:8080/getCourses");
                //home
                // URL url = new URL("http://192.168.1.237:8080/getCourses");
                //send over secure connection
                //   HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);
                urlConn.setUseCaches(false);
                urlConn.setReadTimeout(30000 /* milliseconds */);
                urlConn.setConnectTimeout(30000 /* milliseconds */);
                urlConn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                urlConn.setRequestProperty("Connection", "Keep-Alive");
                urlConn.connect();

                // Send POST output.
                Log.d(TAG, "Send Post Output");
                DataOutputStream cgiInput = new DataOutputStream(urlConn.getOutputStream());

                String content = "userName=" + URLEncoder.encode(netID) + "&password=" + URLEncoder.encode(password);

                cgiInput.writeBytes(content);
                cgiInput.flush();
                cgiInput.close();

                // Get the response
                Log.d(TAG, "Get the response");
                BufferedReader cgiOutput = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String line;
                int counter = 0;
                while ((line = cgiOutput.readLine()) != null) {
                    if (line.equalsIgnoreCase("Wrong Credentials")) {
                        wrongCredentials = line;
                        break;
                    } else {
                        if (counter == 0) {
                            studentName = line;
                        }
                        counter++;
                        courses.add(line);
                    }
                }

                courses.remove(0);

                for (int i = 0; i < courses.size(); i++) {
                    Log.d(TAG, i + courses.get(i));
                }

                cgiOutput.close();
                urlConn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Connection Error: " + e.toString());
                serverError = true;
            }

            return null;

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Log.d(TAG, "Line is = " + wrongCredentials);
            if (wrongCredentials.contains("Wrong Credentials")) {
                onLoginFailed();
            } else if (serverError) {
                onServerError();
            } else {

                onLoginSuccess();
            }

        }
    }

    public void onLoginSuccess() {

        ServerAsync serverAsyncTask = new ServerAsync();
        serverAsyncTask.execute();

    }


    public void nextActivity() {

        login.setEnabled(true);
        if (loginCheckBox.isChecked()) {
            loginPrefsEditor.putBoolean("saveLogin", true);
            loginPrefsEditor.commit();
        } else {
            loginPrefsEditor.clear();
            loginPrefsEditor.commit();
        }

        Log.d(TAG, "Login in successful");

        userNameEdit.putString("username", user.getText().toString());
        userNameEdit.commit();

        Log.d(TAG, "Student name is: " + studentName);

        studentNameEdit.putString("studentName", studentName);
        studentNameEdit.commit();


        Intent i = new Intent(getApplication(), CoursesActivity.class);
        startActivity(i);

    }

    private class ServerAsync extends AsyncTask<String, Void, String> {

        String netID = user.getText().toString();

        @Override
        protected String doInBackground(String... params) {

            try {
                // open a connection to the site
                URL url = new URL("http://130.245.191.166:8080/users1.php");
                URLConnection con = url.openConnection();
                // activate the output
                con.setDoOutput(true);
                PrintStream ps = new PrintStream(con.getOutputStream());

                ps.print("userIDKey=" + netID);

                // we have to get the input stream in order to actually send the request
                con.getInputStream();

                // close the print stream
                ps.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < courses.size(); i++) {
                try {

                    // open a connection to the site
                    URL url = new URL("http://130.245.191.166:8080/userCourses1.php");
                    URLConnection con = url.openConnection();
                    // activate the output
                    con.setDoOutput(true);
                    PrintStream ps = new PrintStream(con.getOutputStream());

                    ps.print("userIDKey=" + netID);
                    ps.print("&courseKey=" + courses.get(i).toString());

                    // we have to get the input stream in order to actually send the request
                    con.getInputStream();

                    // close the print stream
                    ps.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            nextActivity();

        }
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Incorrect netID or Password", Toast.LENGTH_LONG).show();
        wrongCredentials = "";
        user.setText("");
        pass.setText("");
        login.setEnabled(true);
        loginCheckBox.setChecked(false);
    }

    public void onServerError() {
        Toast.makeText(getBaseContext(), "Server Connection Time Out Error", Toast.LENGTH_LONG).show();
        user.setText("");
        pass.setText("");
        login.setEnabled(true);
        loginCheckBox.setChecked(false);
        serverError = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed Called");
        login.setEnabled(true);
        this.finish();
        System.exit(0);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


}
