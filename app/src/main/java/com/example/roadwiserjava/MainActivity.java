package com.example.roadwiserjava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    private Camera mCamera;
    private MediaRecorder recorder;
    private SurfaceHolder holder;

    private ImageButton settingsButton;
    private ImageButton loadScreenButton;
    private static final String TAG = "Sigwise";
    private volatile int recstate = 0;


    @Override
    protected void finalize() throws Throwable {
        try{
            if(mCamera != null){
                mCamera.release();
                mCamera = null;
            }
        }
        finally {
            super.finalize();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SigwiseLogger.i(TAG, "on create");

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        loadScreenButton = (ImageButton) findViewById(R.id.loadScreenButton);

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

        final ImageButton button_rec = (ImageButton) findViewById(buttonRec);
        recorder = new MediaRecorder();
        button_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recstate == 0) {
                    mCamera.stopPreview();
                    startRecord(); //recstate is set inside start and stop record
                    button_rec.setImageDrawable(getResources().getDrawable(R.drawable.stoprec2));
                    settingsButton.setVisibility(View.INVISIBLE);
                    loadScreenButton.setVisibility(View.INVISIBLE);
                    EditText tvSpeed = (EditText)findViewById(R.id.editTextSpeed);
                    tvSpeed.setVisibility(View.VISIBLE);
                } else {
                    stopRecord();
                    mCamera.startPreview();
                    bProtected = false;
                    button_rec.setImageDrawable(getResources().getDrawable(R.drawable.record4));
                    button_config.setVisibility(View.VISIBLE);
                    button_load.setVisibility(View.VISIBLE);
                    EditText tvSpeed = (EditText)findViewById(R.id.editTextSpeed);
                    tvSpeed.setVisibility(View.INVISIBLE);
                }

            }

        });
    }

    private void initRecorder(){
        recorder.reset();
        mCamera.unlock();
        recorder.setCamera(mCamera);
        SigwiseLogger.i(TAG, "reset Recorder");
        try{
            recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            SigwiseLogger.i(TAG, "setAudioSource");
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            SigwiseLogger.i(TAG, "setVideoSource");}
        catch (Exception e){
            SigwiseLogger.i(TAG,e.toString());
            e.printStackTrace();
            finish();
        }

        CamcorderProfile cp;
        if(resostate == 1) {
            cp = CamcorderProfile
                    .get(CamcorderProfile.QUALITY_LOW);
        }
        else if(resostate == 3) {
            cp = CamcorderProfile
                    .get(CamcorderProfile.QUALITY_HIGH);
        }
        else{
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion >= Build.VERSION_CODES.HONEYCOMB) {
                if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
                    cp = CamcorderProfile
                            .get(CamcorderProfile.QUALITY_720P);
                } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
                    cp = CamcorderProfile
                            .get(CamcorderProfile.QUALITY_480P);
                } else {
                    cp = CamcorderProfile
                            .get(CamcorderProfile.QUALITY_LOW);
                }
            }
            else{
                cp = CamcorderProfile
                        .get(CamcorderProfile.QUALITY_LOW);
            }
        }
        recorder.setProfile(cp);
        SigwiseLogger.i(TAG, "setProfile");
        path = getOutputMediaFile().toString();
        SigwiseLogger.i(TAG,path);
        recorder.setOutputFile(path.substring(8));
        SigwiseLogger.i(TAG, "setOutputFile");
        recorder.setMaxDuration(videocliplen*60*1000); // videocliplen*60 seconds
        SigwiseLogger.i(TAG, "setMaxDuration");
        recorder.setMaxFileSize(1000000000); // Approximately 1000 megabytes per file
        SigwiseLogger.i(TAG, "setMaxFileSize");
        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecord();
                    startRecord();
                }
            }
        });
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        SigwiseLogger.i(TAG,"Recorder prepare");
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
}
