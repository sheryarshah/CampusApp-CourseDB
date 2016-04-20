
package com.sa.coursedb.stonybrook.coursedb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ReplyActivity extends Activity {

    public static final String TAG = "ReplyActivity";

    private Context context = this;

    private InputMethodManager imm;
    private String question;

    private SharedPreferences courseQuestionPrefs;

    private SharedPreferences userNamePrefs;

    ArrayAdapter<String> adapter;
    ArrayList<String> repliesList = new ArrayList<String>();

    private ListView replies;
    private TextView courseText;
    private TextView questionText;
    private SwipeRefreshLayout swipeRefresh;

    private String netID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        this.makeActionOverflowMenuShown();

        question = getIntent().getExtras().getString("QUESTION");
        Log.d(TAG, "Question selected is: " + question);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_update);
        swipeRefresh.setColorSchemeColors(R.color.orange, R.color.green, R.color.blue);

        courseQuestionPrefs = getSharedPreferences("CourseQuestion", MODE_PRIVATE);

        userNamePrefs = getSharedPreferences("userNamePrefs", Context.MODE_PRIVATE);

        netID = userNamePrefs.getString("username", null);

        replies = (ListView) findViewById(R.id.replies_list);
        courseText = (TextView) findViewById(R.id.course);
        questionText = (TextView) findViewById(R.id.question);

        courseText.append(courseQuestionPrefs.getString("courseQuest", null));
        questionText.append(question);

        ReplyGetAsync replyGetAsyncTask = new ReplyGetAsync();
        replyGetAsyncTask.execute();

        registerForContextMenu(replies);
    }

    private void makeActionOverflowMenuShown() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.edit:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                final String re = repliesList.get(info.position);
                Log.d(TAG, "Reply is: " + re);

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogLayout = inflater.inflate(R.layout.post_reply, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(dialogLayout);
                final EditText writeReply = (EditText) dialogLayout.findViewById(R.id.writing);
                writeReply.setText(re);
                // Request focus and show soft keyboard automatically
                writeReply.requestFocus();
                imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                builder.setTitle(R.string.post_reply);
                builder.setPositiveButton(R.string.post, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        final String replyPost = writeReply.getText().toString();

                        Log.d(TAG, "Post reply is = " + replyPost);

                        ReplyEditAsync replyEditAsyncTask = new ReplyEditAsync();
                        replyEditAsyncTask.execute(re, replyPost);


                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getWindow().setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                        );
                    }
                });

                AlertDialog questionDialog = builder.create();
                questionDialog.show();


                return true;
            case R.id.delete:
                AdapterView.AdapterContextMenuInfo info1 = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                String rd = repliesList.get(info1.position);
                Log.d(TAG, "Question is: " + rd);
                ReplyDeleteAsync replyDeleteAsyncTask = new ReplyDeleteAsync();
                replyDeleteAsyncTask.execute(rd);
                return true;

        }
        return super.onContextItemSelected(item);

    }

    private class ReplyEditAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String error = "nonuser";
            String reply = params[0];
            String newReply = params[1];

            try {
                // open a connection to the site
                URL url = new URL("http://130.245.191.166:8080/editReply.php");
                URLConnection con = url.openConnection();
                // activate the output
                con.setDoOutput(true);
                PrintStream ps = new PrintStream(con.getOutputStream());


                ps.print("courseIDKey=" + courseQuestionPrefs.getString("courseQuest", null));
                ps.print("&questKey=" + question);
                ps.print("&userIDKey=" + netID);
                ps.print("&replyKey=" + reply);
                ps.print("&newReply=" + newReply);

                // we have to get the input stream in order to actually send the request
                con.getInputStream();

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                String line1 = "";
                while ((line = rd.readLine()) != null) {
                    Log.d(TAG, "replyedit text: " + line);
                    if (line.equals(newReply)) {
                        error = "non";
                    }

                }

                // close the print stream
                ps.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return error;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d(TAG, "error is: " + result);

            if (result.equals("nonuser")) {
                Toast.makeText(getBaseContext(), "Can't edit another user reply", Toast.LENGTH_LONG).show();
                return;
            } else {
                ReplyGetAsync replyGetAsyncTask = new ReplyGetAsync();
                replyGetAsyncTask.execute();

            }

            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    private class ReplyDeleteAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String error = "nonuser";
            String reply = params[0];

            try {
                // open a connection to the site
                URL url = new URL("http://130.245.191.166:8080/removeReplies.php");
                URLConnection con = url.openConnection();
                // activate the output
                con.setDoOutput(true);
                PrintStream ps = new PrintStream(con.getOutputStream());


                ps.print("courseIDKey=" + courseQuestionPrefs.getString("courseQuest", null));
                ps.print("&questKey=" + question);
                ps.print("&replyKey=" + reply);
                ps.print("&userIDKey=" + netID);

                // we have to get the input stream in order to actually send the request
                con.getInputStream();

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                String line1 = "";
                while ((line = rd.readLine()) != null) {
                    line1 += line;

                }

                if (!line1.contains(reply)) {
                    error = "non";
                }

                // close the print stream
                ps.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return error;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d(TAG, "error is: " + result);

            if (result.equals("nonuser")) {
                Toast.makeText(getBaseContext(), "Can't delete another user reply", Toast.LENGTH_LONG).show();
                return;
            } else {
                ReplyGetAsync replyGetAsyncTask = new ReplyGetAsync();
                replyGetAsyncTask.execute();

            }

            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reply, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.postReply:

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogLayout = inflater.inflate(R.layout.post_reply, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(dialogLayout);
                final EditText writeReply = (EditText) dialogLayout.findViewById(R.id.writing);
                // Request focus and show soft keyboard automatically
                writeReply.requestFocus();
                imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                builder.setTitle(R.string.post_reply);
                builder.setPositiveButton(R.string.post, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        final String replyPost = writeReply.getText().toString();

                        Log.d(TAG, "Post reply is = " + replyPost);

                        ReplyPostAsync replyPostAsyncTask = new ReplyPostAsync();
                        replyPostAsyncTask.execute(replyPost);


                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getWindow().setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                        );
                    }
                });

                AlertDialog questionDialog = builder.create();
                questionDialog.show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ReplyPostAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String reply = params[0];
            String error = "";

            try {
                // open a connection to the site
                URL url = new URL("http://130.245.191.166:8080/userReplies1.php");
                URLConnection con = url.openConnection();
                // activate the output
                con.setDoOutput(true);
                PrintStream ps = new PrintStream(con.getOutputStream());

                ps.print("courseIDKey=" + courseQuestionPrefs.getString("courseQuest", null));
                ps.print("&questKey=" + question);
                ps.print("&replyKey=" + reply);
                ps.print("&userID=" + netID);

                // we have to get the input stream in order to actually send the request
                con.getInputStream();

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

                // close the print stream
                ps.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return error;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ReplyGetAsync replyGetAsyncTask = new ReplyGetAsync();
            replyGetAsyncTask.execute();

            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

    }

    private void displayReplies() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, repliesList);

        replies.setAdapter(adapter);

        replies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*        String item = ((TextView) view).getText().toString();
                Intent i = new Intent(getApplication(), ReplyActivity.class);
                i.putExtra("QUESTION", item);
                startActivity(i);*/
            }
        });

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                ReplyGetAsync replyGetAsyncTask = new ReplyGetAsync();
                replyGetAsyncTask.execute();
            }
        });

    }

    private class ReplyGetAsync extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            repliesList.clear();

            try {
                // open a connection to the site
                URL url = new URL("http://130.245.191.166:8080/getQuestionReplies1.php");
                URLConnection con = url.openConnection();
                // activate the output
                con.setDoOutput(true);
                PrintStream ps = new PrintStream(con.getOutputStream());

                ps.print("courseIDKey=" + courseQuestionPrefs.getString("courseQuest", null));
                ps.print("&questKey=" + question);

                // we have to get the input stream in order to actually send the request
                con.getInputStream();

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    repliesList.add(line);
                }

                repliesList.remove(repliesList.size() - 1);

                // close the print stream
                ps.close();
                System.out.println(repliesList);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            displayReplies();

            swipeRefresh.setRefreshing(false);
        }
    }

}



