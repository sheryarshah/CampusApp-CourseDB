package com.sa.coursedb.stonybrook.coursedb;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class CoursesActivity extends Activity {

    public static final String TAG = "CoursesActivity";

    TextView studentName;

    CourseManagment service;
    ImageView image;

    private SharedPreferences userNamePrefs;

    private SharedPreferences courseQuestionPrefs;
    private SharedPreferences.Editor courseQuestionEdit;

    private SharedPreferences studentNamePrefs;

    private ArrayList<String> myCourses;


    String loggedStudent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        image = (ImageView) findViewById(R.id.sbuLogo);

        service = new CourseManagment();


        courseQuestionPrefs = getSharedPreferences("CourseQuestion", Context.MODE_PRIVATE);
        courseQuestionEdit = courseQuestionPrefs.edit();

        userNamePrefs = getSharedPreferences("userNamePrefs", Context.MODE_PRIVATE);

        studentNamePrefs = getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE);

        loggedStudent = studentNamePrefs.getString("studentName", null);
        Log.d(TAG, "Student name is: " + loggedStudent);

        String netID = userNamePrefs.getString("username", null);
        Log.d(TAG, "username is: " + netID);

        myCourses = new ArrayList<>();

        CourseAsync courseAsyncTask = new CourseAsync();
        courseAsyncTask.execute(netID);

    }

    private class CourseAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String netid = params[0];

            try {
                // open a connection to the site
                URL url = new URL("http://130.245.191.166:8080/getUserCourses1.php");
                URLConnection con = url.openConnection();
                // activate the output
                con.setDoOutput(true);
                PrintStream ps = new PrintStream(con.getOutputStream());

                ps.print("userIDKey=" + netid);

                // we have to get the input stream in order to actually send the request
                con.getInputStream();

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    System.out.println(line);
                    myCourses.add(line);

                }
                myCourses.remove(0);

                // close the print stream
                ps.close();
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
            service.storeCourses(myCourses);

            addCoursesToView();

        }


    }

    private void addCoursesToView() {

        studentName = (TextView) findViewById(R.id.studentNameID);

        LinearLayout rl = (LinearLayout) findViewById(R.id.LinearLayout);

        studentName.append(loggedStudent);

        //get the list of courses from server and start adding it the fragment
        Log.d(TAG, "COURSE SIZE = " + service.getCoursesList().size());
        for (int i = 0; i < service.getCoursesList().size(); i++) {
            Button b = new Button(this);
            b.setBackgroundResource(R.drawable.blackboard);
            //b.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_right, 0);
            b.setText(service.getCoursesList().get(i).getCourseTitle());
            b.setTextColor(Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
            b.setTypeface(null, Typeface.BOLD);

            b.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            b.setId(i + 1);
            final int index = i;
            // Set click listener for button
            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.i(TAG, "index :" + index);
                    Intent i = new Intent(getApplication(), QuestionsActivity.class);
                    Bundle bundle = new Bundle();
                    //  i.putExtra("COURSES", courseList.get(index).toString());
                    bundle.putParcelable("CourseObject", service.getCoursesList().get(index));

                    Log.d(TAG, "Course clicked on:" + service.getCoursesList().get(index).getCourseTitle());

                    courseQuestionEdit.putString("courseQuest", service.getCoursesList().get(index).getCourseTitle());
                    courseQuestionEdit.commit();
                    i.putExtras(bundle);
                    startActivity(i);


                }
            });
            rl.addView(b);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CoursesActivity.this, LoginActivity.class));
        finish();
    }

}
