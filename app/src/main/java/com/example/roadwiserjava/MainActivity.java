package com.example.roadwiserjava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

//select camera, start recording, every two minutes make clip detect motion

public class MainActivity extends CameraActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    CameraBridgeViewBase cameraBridgeViewBase;
    Mat curr_gray, prev_gray, rgb, diff;
    List<MatOfPoint> cnts;
    int numcnt;
    int cntThreshold = 10;
    int seconds = 120;
    boolean is_init;
    int threshVal = 80;
    private boolean recording = false;
    private boolean manual = false;
    private PreviewView previewView;
    private VideoCapture videoCapture;
    private ImageButton settingsButton;
    private ImageButton loadScreenButton;
    private ImageButton photoButton, recordButton;

    private static Handler mainThreadHandler;
    private static final String TAG = "Sigwise";
    private volatile int recstate = 0;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SigwiseLogger.i(TAG, "on create");

        getPermission();

        is_init = false;

        //threshVal = ConfigActivity.getThreshold();
        //threshVal = 80;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            threshVal = Integer.parseInt(extras.getString("threshVal"));
            //The key argument here must match that used in the other activity
        }

            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        loadScreenButton = (ImageButton) findViewById(R.id.loadScreenButton);

        //photoButton = (ImageButton) findViewById(R.id.photoButton);
        recordButton = (ImageButton) findViewById(R.id.recordButton);

        //previewView = (PreviewView) findViewById(R.id.previewView);

        mainThreadHandler = new Handler(Looper.getMainLooper());

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

        cameraBridgeViewBase = findViewById(R.id.cameraView);


        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                curr_gray = new Mat();
                prev_gray = new Mat();
                rgb = new Mat();
                diff = new Mat();
                cnts = new ArrayList<MatOfPoint>();
            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                if(!is_init) {
                    prev_gray = inputFrame.gray();
                    is_init = true;
                    return prev_gray;
                }

                rgb = inputFrame.rgba();
                curr_gray = inputFrame.gray();

                //todo: detect noises
                numcnt = 0;

                Core.absdiff(curr_gray, prev_gray, diff);
                Imgproc.threshold(diff, diff, threshVal, 255, Imgproc.THRESH_BINARY);
                Imgproc.findContours(diff, cnts, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

                Imgproc.drawContours(rgb, cnts, -1, new Scalar(255, 0, 0), 4);

                for(MatOfPoint m: cnts) {
                    Rect r = Imgproc.boundingRect(m);
                    Imgproc.rectangle(rgb, r, new Scalar(0,0,255), 3);
                    numcnt++;
                }

                if(numcnt > cntThreshold) {
                    //record
                    manual = false;
                    //recordForTime(seconds);
                }

                cnts.clear();
                ////



                prev_gray = curr_gray.clone();
                return rgb;
            }
        });

        if(OpenCVLoader.initDebug()){
            cameraBridgeViewBase.enableView();
        }

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
                manual = true;
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




    public void openSettings() {
        Intent intent = new Intent(this, ConfigActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("threshVal",threshVal);
        startActivity(intent);
        SigwiseLogger.i(TAG, "open settings");
    }

    public void openLoadScreen() {
        Intent intent = new Intent(this, LoadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        SigwiseLogger.i(TAG, "open load screen");
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    public void getPermission() {
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            getPermission();
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
        //Preview preview = new Preview.Builder().build();

        //preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //Image Capture use case

        //Video Capture use case
        videoCapture = new VideoCapture.Builder().setVideoFrameRate(30).build();

        //removed preview
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, videoCapture);
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

    public void recordForTime(int mseconds){
        Runnable delayedTask = new Runnable() {
            @Override
            public void run() {
                if(recording && manual) {
                    record();

                    //send email
                }
            }
        };
        mainThreadHandler.postDelayed(delayedTask, mseconds*1000);
    }
}
