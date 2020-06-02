package com.example.soundttest_200531;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button btn_record;
    Button btn_play;
    Button btn_stop;
    Button btn_test;
    MediaRecorder mediaRecorder; //녹음을 도와주는 객체
    MediaPlayer mediaPlayer; //재생을 위한 객체
    MediaPlayer mediaPlayer2; //상쇄 간섭을 위한 객체

    String path = "";
    boolean isRecording = false; //녹음중인지 아닌지

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //기기마다 경로가 다르기 때문에 외부저장소 경로를 가져온다 / 없으면 내부저장소 경로를 가져옴
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio.wav"; //녹음저장확장자 wav

        btn_record = findViewById(R.id.btn_record);
        btn_play = findViewById(R.id.btn_play);
        btn_stop = findViewById(R.id.btn_stop);
        btn_test = findViewById(R.id.btn_test);

        //권한승인여부 확인후 메시지 띄워줌
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,  Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        //마이크가 있는지 없는지 확인
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) { //현재 하드웨어 정보를 가져옴
            btn_record.setEnabled(true);
            btn_play.setEnabled(false);
            btn_stop.setEnabled(false);
            btn_test.setEnabled(false);
        } else {
            btn_record.setEnabled(false);
            btn_play.setEnabled(false);
            btn_stop.setEnabled(false);
            btn_test.setEnabled(false);
        }

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_record.setEnabled(false);
                btn_play.setEnabled(false);
                btn_stop.setEnabled(true);
                btn_test.setEnabled(false);

                isRecording = true;

                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); //마이크로 녹음하겠다
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //저장파일 형식 녹음파일은 wav로 저장
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); //인코딩 방식설정
                mediaRecorder.setOutputFile(path); //경로설정

                try {
                    mediaRecorder.prepare(); //녹음을 준비함 : 지금까지의 옵션에서 문제가 발생했는지 검사함
                    mediaRecorder.start();
                    Toast.makeText(getApplicationContext(), "녹음시작", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_record.setEnabled(true);
                btn_play.setEnabled(true);
                btn_stop.setEnabled(false);
                btn_test.setEnabled(true);

                if (isRecording) {
                    mediaRecorder.stop();
                    mediaRecorder = null;
                    isRecording = false;

                    //상쇄 간섭용 음원을 만듬

                    System.out.println("======================================");
                    System.out.println("======================================");

                    Toast.makeText(getApplicationContext(), "녹음중지", Toast.LENGTH_LONG).show();

                } else {
                    mediaPlayer.stop();
                    mediaPlayer = null;
                    Toast.makeText(getApplicationContext(), "재생중지", Toast.LENGTH_LONG).show();
                }


            }
        });

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_record.setEnabled(false);
                btn_play.setEnabled(false);
                btn_stop.setEnabled(true);
                btn_test.setEnabled(false);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    Thread thread = new Thread(new myThread());
                    thread.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_record.setEnabled(false);
                btn_play.setEnabled(false);
                btn_stop.setEnabled(true);
                btn_test.setEnabled(false);

                mediaPlayer = new MediaPlayer();
                mediaPlayer2 = new MediaPlayer();
                try {
                    //기존의 음원과 상쇄 간섭 음원을 동시에 플레이
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    mediaPlayer2.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.wav");
                    mediaPlayer2.prepare();
                    mediaPlayer2.start();

                    Thread thread = new Thread(new myThread());
                    thread.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == 1) {
                btn_record.setEnabled(true);
                btn_play.setEnabled(true);
                btn_stop.setEnabled(false);
                btn_test.setEnabled(true);
            }
        }
    };

    public class myThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer = null;

                    Message msg = new Message();
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                    return;
                }
            }
        }
    }

}