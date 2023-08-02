package com.example.roadwiserjava;

import androidx.annotation.NonNull;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//select camera, start recording, every two minutes make clip detect motion

public class MainActivity extends CameraActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    CameraBridgeViewBase cameraBridgeViewBase;
    Mat curr_gray, prev_gray, rgb, diff;
    List<MatOfPoint> cnts;
    int numcnt;
    final int cntThreshold = 10;
    boolean is_init;
    int threshVal = 80;
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
        intent.putExtra("threshVal",threshVal);
        startActivity(intent);
        SigwiseLogger.i(TAG, "open settings");
    }

    public void openLoadScreen() {
        Intent intent = new Intent(this, LoadActivity.class);
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

}
