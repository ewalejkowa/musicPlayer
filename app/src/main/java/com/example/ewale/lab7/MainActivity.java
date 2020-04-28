package com.example.ewale.lab7;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import java.io.File;
import java.io.IOException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    protected MediaPlayer mediaPlayer;
    protected MediaRecorder mediaRecorder;
    protected float volumeValue=10;
    protected   Button stopButton;
    protected   Button pauseButton;
    protected   Button stopRecord;
    protected   Button startRecord;
    protected   Button openFile;
    protected   String path=null;
    protected  int currentPos = 0;
    protected  Button radioButton;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notificationManager = NotificationManagerCompat.from(this);
        radioButton = (Button) findViewById(R.id.radioButton);
        Button clickButton = (Button) findViewById(R.id.mediaPlayer);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                    requestPermissionAndContinue();
            }
        });
        pauseButton = (Button) findViewById(R.id.pausePlayer);
        pauseButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Pause();
            }
        });
        stopButton = (Button) findViewById(R.id.stopPlayer);
        stopButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Stop();
            }
        });
        openFile = (Button) findViewById(R.id.openFile);
        openFile.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
            }
        });

        startRecord = (Button) findViewById(R.id.startRecord);
        startRecord.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                recording();
            }
        });
        stopRecord = (Button) findViewById(R.id.stopRecord);
        stopRecord.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                stopRecording();
            }
        });

    }
    private void recording() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE
                        ,RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE,
                        RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
            }
        }
        else {
                startRecording();
        }
    }
    public void createNotification(String title, String message)
    {
        /**Creates an explicit intent for an Activity in your app**/
        Intent resultIntent = new Intent();
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this, "1");
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)
        .setPriority(NotificationManager.IMPORTANCE_HIGH);

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
    }
    public void startRecording(){
        if (mediaRecorder==null) {
            File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            try {
                File file = File.createTempFile("audio", "mp3", rootDir);
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioChannels(1);
                mediaRecorder.setAudioSamplingRate(8000);
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
                mediaRecorder.setOutputFile(file.getAbsolutePath());
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.prepare();
                mediaRecorder.start();

                CompoundButton button = (CompoundButton) findViewById(R.id.radioButton);
                CompoundButtonCompat.setButtonTintList(button, ContextCompat.getColorStateList(this, R.color.red));
                createNotification("Nagrywanie","Rozpoczęcie nagrywania");


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {

        }

    }

    public void stopRecording(){
        if (mediaRecorder!=null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder=null;
            createNotification("Nagrywanie","Koniec nagrywania");
            CompoundButton button = (CompoundButton) findViewById(R.id.radioButton);
            CompoundButtonCompat.setButtonTintList(button, ContextCompat.getColorStateList(this, R.color.colorPrimary));
        }

    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123 && resultCode==RESULT_OK) {

            Uri selectedfile = data.getData(); //The uri with the location of the file
            path = getRealPathFromURI(getApplicationContext(),selectedfile);
        }
    }
    private void Stop(){
        if (mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private void Pause() {
        if( mediaPlayer!=null) {
            currentPos = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        }
    }

    void startPlayer(String path) throws IOException {
        if (path!=null) {
            if (mediaPlayer == null && path != null) {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                file = new File(path);

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(file.getAbsolutePath());
                mediaPlayer.prepare();

                mediaPlayer.setVolume(volumeValue, volumeValue);
                mediaPlayer.start();
            } else {

                mediaPlayer.seekTo(currentPos);
                mediaPlayer.start();
            }
        }
    }
    private static final int PERMISSION_REQUEST_CODE = 200;

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE
                        , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }

        }
         else {
            try {
                startPlayer(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
