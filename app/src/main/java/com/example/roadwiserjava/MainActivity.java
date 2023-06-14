package com.example.roadwiserjava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

//select camera, start recording, every two minutes make clip detect motion

public class MainActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private boolean recording = false;
    private PreviewView previewView;
    private VideoCapture videoCapture;
    private ImageButton settingsButton;
    private ImageButton loadScreenButton;
    private ImageButton photoButton, recordButton;
    private static final String TAG = "Sigwise";
    private volatile int recstate = 0;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SigwiseLogger.i(TAG, "on create");

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        loadScreenButton = (ImageButton) findViewById(R.id.loadScreenButton);

        //photoButton = (ImageButton) findViewById(R.id.photoButton);
        recordButton = (ImageButton) findViewById(R.id.recordButton);

        previewView = (PreviewView) findViewById(R.id.previewView);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, getExecutor());

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
        loadScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoadScreen();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Hi", Toast.LENGTH_SHORT).show();
                record();
            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

            }
        }
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll(); //unbinds camera

        //Camera Selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder()    //selects the default camera
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        //Preview use case
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //Image Capture use case

        //Video Capture use case
        videoCapture = new VideoCapture.Builder().setVideoFrameRate(30).build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, videoCapture);
    }


    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        SigwiseLogger.i(TAG, "open settings");
    }

    public void openLoadScreen() {
        Intent intent = new Intent(this, LoadScreenActivity.class);
        startActivity(intent);
        SigwiseLogger.i(TAG, "open load screen");
    }

    @SuppressLint("RestrictedApi")
    public void record() {
        if (!recording) {
            recordButton.setImageResource(R.drawable.stoprec2);

            if (videoCapture != null) {
                File movieDir = new File("mnt/sdcard/Movies/CameraXMovies");    //might be incorrect
                //File movieDir = new File(getExternalMediaDirs()[0]  + "/CameraXMovies");
                if (!movieDir.exists()) {
                    movieDir.mkdir();
                }
                Date date = new Date();
                String timestamp = String.valueOf(date.getTime());
                String vidFilePath = movieDir.getAbsolutePath() + "/" + timestamp + ".mp4";

                File vidFile = new File(vidFilePath);


                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
                    return;
                }
                videoCapture.startRecording(
                        new VideoCapture.OutputFileOptions.Builder(vidFile).build(),
                        getExecutor(),
                        new VideoCapture.OnVideoSavedCallback() {
                            @Override
                            public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                                Toast.makeText(MainActivity.this, "Video has been saved successfully.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                                Toast.makeText(MainActivity.this, "Error saving the video: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }

            recording = true;
        }
        else {
            recordButton.setImageResource(R.drawable.record4);
            videoCapture.stopRecording();
            recording = false;
        }
    }
}
