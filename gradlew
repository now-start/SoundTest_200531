package com.example.studyproject;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CourseListAdapter extends BaseAdapter {
    private Context context;
    private List<Course> courseList;
    private Fragment parent;
    private String userID = MainActivity.userID;
    private Schedule schedule;
    private List<Integer> courseIDList;

    public CourseListAdapter(Context context, List<Course> noticeList, Fragment parent) {
        this.context = context;
        this.courseList = noticeList;
        this.parent = parent;
        schedule = new Schedule();
        courseIDList = new ArrayList<>();
        new BackgroundTask().execute();
    }

    @Override
    public int getCount() {
        return courseList.size();
    }

    @Override
    public Object getItem(int position) {
        return courseList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup viewGroup) {
        View v = View.inflate(context, R.layout.course, null);
        TextView courseGrade = v.findViewById(R.id.courseGrade);
        TextView courseTitle = v.findViewById(R.id.courseTitle);
        TextView courseCredit = v.findViewById(R.id.courseCredit);
        TextView courseDivide = v.findViewById(R.id.courseDivide);
        TextView coursePersonnel = v.findViewById(R.id.coursePersonnel);
        TextView courseProfessor = v.findViewById(R.id.courseProfessor);
        TextView courseTime = v.findViewById(R.id.courseTime);

        if (courseList.get(position).getCourseGrade().equals("제한 없음") || courseList.get(position).getCourseGrade().equals("")) {
            courseGrade.setText("모든 학년");
        } else {
            courseGrade.setText(courseList.get(position).getCourseGrade() + "학년");
        }
        courseTitle.setText(courseList.get(position).getCourseTitle());
        courseCredit.setText(courseList.get(position).getCourseCredit() + "학점");
        courseDivide.setText(courseList.get(position).getCourseDivide() + "분반");
        if (courseList.get(position).getCoursePersonnel() == 0) {
            coursePersonnel.setText("인원 제한 없음");
        } else {
            coursePersonnel.setText("제한 인원 : " + courseList.get(position).getCoursePersonnel() + "명");
        }
        if (courseList.get(position).getCourseProfessor().equals("")) {
            courseProfessor.setText("개인 연구");
        }else{
            courseProfessor.setText(courseList.get(position).getCourseProfessor() + "교수님");
        }
        courseTime.setText(courseList.get(position).getCourseTime() + "");

        v.setTag(courseList.get(position).getCourseID());

        Button addButton = v.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validate = false;
                validate = schedule.validate(courseList.get(position).getCourseTime());

                String userID = MainActivity.userID;
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getActivity());
                                AlertDialog dialog = builder.setMessage("강의가 추가되었습니다.")
                                        .setPositiveButton("확인", null)
                                        .create();
                                dialog.show();
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getActivity());
                                AlertDialog dialog = builder.setMessage("강의 추가에 실패하였습니다.")
                                        .setNegativeButton("확인", null)
                                        .create();
                                dialog.show();
                            }
                        } catch (Exception e) {
                            e.printSta