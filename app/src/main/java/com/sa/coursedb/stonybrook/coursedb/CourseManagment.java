package com.sa.coursedb.stonybrook.coursedb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Sheryar Shah on 2/5/2016.
 */
public class CourseManagment {

    private JSONArray array;
    private ArrayList<Courses> coursesList = new ArrayList<>();

    public CourseManagment() {
    }

    public void obtainCourses(JSONArray jsonArray) {
        String courses = "[{\"Course\":\"ESE 441\",\"Description\":\"Senior Design\"}," +
                "{\"Course\":\"ESE 360\",\"Description\":\"Network Security Engineering\"}," +
                "{\"Course\":\"CSE 380\",\"Description\":\"Game Programming I\"}," +
                "{\"Course\":\"ESE 543\",\"Description\":\"Mobile Cloud Computing\"}," +
                "{\"Course\":\"ESE 575\",\"Description\":\"Advanced VLSI Systems Design\"}," +
                "{\"Course\":\"AMS 542\",\"Description\":\"Algorithms\"}," +
                "]}";

        try {
            array = new JSONArray(courses);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                storeData(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void storeData(JSONObject json) throws JSONException {
        //   coursesList.add(new Courses(json.getString("Course"), json.getString("Description")));
    }

    public void storeCourses(ArrayList<String> c) {
        for (int i = 0; i < c.size(); i++) {
            coursesList.add(new Courses(c.get(i)));
        }


    }

    public ArrayList<Courses> getCoursesList() {
        return coursesList;
    }


}
