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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

//select camera, start recording, every two minutes make clip detect motion

public class MainActivity extends CameraActivity {
    CameraBridgeViewBase cameraBridgeViewBase;
    Mat curr_gray, prev_gray, rgb, diff;
    List<MatOfPoint> cnts;
    private MediaRecorder recorder;
    int numcnt;
    int cntThreshold = 10;
    int seconds = 120;
    boolean is_init;
    int threshVal = 80;
    String email = "";
    private boolean recording = false;
    private boolean manual = false;
    private PreviewView previewView;
    private VideoCapture videoCapture;
    private ImageButton settingsButton;
    private ImageButton loadScreenButton;
    private ImageButton photoButton, recordButton;

    private static Handler mainThreadHandler;
    private static final String TAG = "Sigwise";
    private static final String senderEmail = "shaojinghong@gmail.com";
    private static final String senderPassword = "210395sc#as";
    private String mailhost = "smtp.gmail.com";
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
            email = extras.getString("email");
            //The key argument here must match that used in the other activity
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        loadScreenButton = (ImageButton) findViewById(R.id.loadScreenButton);

        //photoButton = (ImageButton) findViewById(R.id.photoButton);
        recordButton = (ImageButton) findViewById(R.id.recordButton);

        //previewView = (PreviewView) findViewById(R.id.previewView);

        //mainThreadHandler = new Handler(Looper.getMainLooper());

        cameraBridgeViewBase = findViewById(R.id.cameraView);

        recorder = new MediaRecorder();


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
        intent.putExtra("email",email);
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
            try {
                File m_videoFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.savefolder));
                if (!m_videoFolder.exists()) {
                    m_videoFolder.mkdirs();
                }

                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

                CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                recorder.setProfile(cpHigh);
                //recorder.setOutputFile("out.mp4");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String filename = m_videoFolder.getPath() + File.separator + sdf.format(new Date());
                        //may need to switch below variables
                recorder.setVideoSize(cameraBridgeViewBase.getWidth(), cameraBridgeViewBase.getHeight());

                //recorder.setOnInfoListener(this);
                //recorder.setOnErrorListener(this);
                recorder.prepare();
                cameraBridgeViewBase.setRecorder(recorder);
                recorder.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            recording = true;
        }
        else {
            recordButton.setImageResource(R.drawable.record4);
            recording = false;
        }
    }

    public void sendEmail(String filename, String date) {
        String messageToSend = "Motion Detected at " + date;
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        Session session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                //return super.getPasswordAuthentication();
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try{
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));  //unsure
            message.setSubject("Motion Detected At " + date);
            message.setText("Motion was detected from your Android Phone");
            //add attachment code
            Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            Transport.send(message);
            Toast.makeText(getApplicationContext(), "email sent successfully", Toast.LENGTH_LONG);
        }
        catch(MessagingException e){
            throw new RuntimeException(e);
        }
    }
}
