package com.sa.coursedb.stonybrook.coursedb;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Sheryar Shah on 2/5/2016.
 */
public class Courses implements Parcelable {

    private String courseNumber;
    private String studentName;
    private String courseTitle;
    private ArrayList<String> questList = new ArrayList<>();

 /*   public Courses(String courseNumber, String courseTitle) {
        this.courseNumber = courseNumber;
        this.courseTitle = courseTitle;
    }*/

    public Courses(String course) {
        this.courseTitle = course;
    }

    public Courses() {

    }

    public ArrayList<String> getQuestList() {
        return questList;
    }

    public void setQuestList(ArrayList<String> questLists) {
        this.questList = questLists;
    }

    public Courses(Parcel in) {
        courseNumber = in.readString();
        courseTitle = in.readString();
    }

    public static final Creator<Courses> CREATOR = new Creator<Courses>() {
        @Override
        public Courses createFromParcel(Parcel in) {
            return new Courses(in);
        }

        @Override
        public Courses[] newArray(int size) {
            return new Courses[size];
        }
    };

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(courseNumber);
        dest.writeString(courseTitle);
        dest.writeString(studentName);
    }

}
