package com.sa.coursedb.stonybrook.coursedb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

public class QuestionsActivity extends Activity {

    public static final String TAG = "QuestionsActivity";

    private TextView course;
    private ListView questions;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> questionsList = new ArrayList<String>();

    private SharedPreferences courseQuestionPrefs;

    private SharedPreferences userNamePrefs;

    private Context context = this;
    public InputMethodManager imm;

    private Courses c;

    private SwipeRefreshLayout swipeRefresh;

    private String netID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        this.makeActionOverflowMenuShown();

        course = (TextView) findViewById(R.id.course);
        questions = (ListView) findViewById(R.id.questions_list);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_update);
        swipeRefresh.setColorSchemeColors(R.color.orange, R.color.green, R.color.blue);
        Bundle bundle = getIntent().getExtras();
        c = bundle.getParcelable("CourseObject");

        courseQuestionPrefs = getSharedPreferences("CourseQuestion", MODE_PRIVATE);

        course.append(c.getCourseTitle());

        userNamePrefs = getSharedPreferences("userNamePrefs", Context.MODE_PRIVATE);

        netID = userNamePrefs.getString("username", null);
        Log.d(TAG, "Username is: " + netID);

        QuestionGetAsync questionGetAsyncTask = new QuestionGetAsync();
        questionGetAsyncTask.execute();

        registerForContextMenu(questions);

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
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                //  editNote(info.id);
              //  Toast.makeText(getApplicationContext(), "Edit Clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.delete:
                AdapterView.AdapterContextMenuInfo info1 = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                String q = questionsList.get(info1.position);
                Log.d(TAG, "Question is: " +q);
                QuestionDeleteAsync questionDeleteAsyncTask = new QuestionDeleteAsync();
                questionDeleteAsyncTask.execute(q);
                return true;

        }
        return super.onContextItemSelected(item);

    }

    private class QuestionDeleteAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String error = "nonuser";
            String question = params[0];

            try {
                // open a connection to the site
                URL url = new URL("http://130.245.191.166:8080/removeQuestion.php");
                URLConnection con = url.openConnection();
                // activate the output
                con.setDoOutput(true);
                PrintStream ps = new PrintStream(con.getOutputStream());


                ps.print("courseIDKey=" + courseQuestionPrefs.getString("courseQuest", null));
                ps.print("&questKey=" + question);
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

                if (!line1.contains(question)) {
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

            Log.d(TAG, "error is: " +result);

            if (result.equals("nonuser")) {
                Toast.makeText(getBaseContext(), "Can't delete another user question", Toast.LENGTH_LONG).show();
                return;
            } else {
                QuestionGetAsync questionGetAsyncTask = new QuestionGetAsync();
                questionGetAsyncTask.execute();

            }

            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.postQuestion:

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogLayout = inflater.inflate(R.layout.post_question, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(dialogLayout);
                final EditText writeQuestion = (EditText) dialogLayout.findViewById(R.id.writing);
                // Request focus and show soft keyboard automatically
                writeQuestion.requestFocus();
                imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                Log.d("QuestionsActivity", "Length is" + writeQuestion.getText().length());

                builder.setTitle(R.string.post_question);
                builder.setPositiveButton(R.string.post, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!writeQuestion.getText().toString().contains("?")) {
                            writeQuestion.append("?");
                        }
                        final String questionPost = writeQuestion.getText().toString();

                        Log.d(TAG, "Post question is = " + questionPost);

                        QuestionPostAsync questionPostAsyncTask = new QuestionPostAsync();
                        questionPostAsyncTask.execute(questionPost);


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

    private class QuestionPostAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String question = params[0];
            String error = "";

            try {
                // open a connection to the site
                URL url = new URL("http://130.245.191.166:8080/userQuestions1.php");
                URLConnection con = url.openConnection();
                // activate the output
                con.setDoOutput(true);
                PrintStream ps = new PrintStream(con.getOutputStream());

                ps.print("userIDKey=" + netID);
                ps.print("&courseIDKey=" + courseQuestionPrefs.getString("courseQuest", null));
                ps.print("&questKey=" + question);

                // we have to get the input stream in order to actually send the request
                con.getInputStream();

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    if (line.equals("Duplicate")) {
                        error = "Duplicate";
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

            if (result.equals("Duplicate")) {
                Toast.makeText(getBaseContext(), "Question Already Posted", Toast.LENGTH_LONG).show();
                return;
            } else {
                QuestionGetAsync questionGetAsyncTask = new QuestionGetAsync();
                questionGetAsyncTask.execute();

            }


            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

    }

    private void displayQuestions() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, questionsList);

        questions.setAdapter(adapter);

        questions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                Intent i = new Intent(getApplication(), ReplyActivity.class);
                i.putExtra("QUESTION", item);
                startActivity(i);
            }
        });

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                QuestionGetAsync questionGetAsyncTask = new QuestionGetAsync();
                questionGetAsyncTask.execute();
            }
        });

    }

    private class QuestionGetAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            questionsList.clear();

            try {
                // open a connection to the site
                URL url = new URL("http://130.245.191.166:8080/getCourseQuestions1.php");
                URLConnection con = url.openConnection();
                // activate the output
                con.setDoOutput(true);
                PrintStream ps = new PrintStream(con.getOutputStream());

                //   ps.print("userIDKey=" +prefs.getString("username", null));
                ps.print("courseIDKey=" + courseQuestionPrefs.getString("courseQuest", null));

                // we have to get the input stream in order to actually send the request
                con.getInputStream();

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    questionsList.add(line);
                }

                // close the print stream
                ps.close();

                System.out.println(questionsList);

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

            displayQuestions();
            swipeRefresh.setRefreshing(false);
        }
    }

}
