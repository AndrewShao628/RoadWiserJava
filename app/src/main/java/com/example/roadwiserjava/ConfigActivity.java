package com.example.roadwiserjava;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.roadwiserjava.R;
import com.example.roadwiserjava.SigwiseLogger;
import com.example.roadwiserjava.SigwiseLogger;
import com.google.android.material.slider.Slider;

import java.io.File;

import static com.example.roadwiserjava.R.id.buttonConfig;
import static com.example.roadwiserjava.R.id.editText;
import static com.example.roadwiserjava.R.id.editText2;
import static com.example.roadwiserjava.R.id.textView3;

import androidx.annotation.NonNull;

public class ConfigActivity extends Activity {
    public static float lowReso = 0.418f;
    public static float mediumReso = 1.071f;
    public static float highReso = 1.56f;
    private static float currentReso = mediumReso;
    private static int resoState = 2;
    private static int quota = 0;
    private static int videocliplen = 2;
    private float ratioMbperSec;
    private static final String TAG = "Sigwise";
    private int maxMemory = getAvailableExternalMemoryTime();

    Slider thresholdSlider;
    @SuppressLint("StaticFieldLeak")
    static TextView thresholdSliderVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler((RoadWiserApp)getApplication()));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_config);
        ImageButton button_config = (ImageButton) findViewById(buttonConfig);

        button_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMainActivity();
            }

        });
        TextView tv = (TextView)findViewById(textView3);
        tv.setText(getAvailableExternalMemoryTime() + " MB");
        EditText myedittext = (EditText)findViewById(editText);
        myedittext.setText(""+getAvailableExternalMemoryTime()/200*100, TextView.BufferType.EDITABLE);
        EditText myedittext2 = (EditText)findViewById(editText2);
        myedittext.setFilters(new InputFilter[]{new InputFilterMinMax(1,maxMemory)});
        myedittext2.setFilters(new InputFilter[]{new InputFilterMinMax(1,30)});

        SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.configfile), MODE_PRIVATE);
        int tmp = prefs.getInt("resolution", 0);
        if(tmp != 0) resoState = tmp;
        tmp = prefs.getInt("quota",0);
        if(tmp !=0) {quota = tmp; myedittext.setText(""+quota, TextView.BufferType.EDITABLE);}
        tmp = prefs.getInt("videocliplen",0);
        if(tmp !=0) {videocliplen = tmp;myedittext2.setText("" + videocliplen, TextView.BufferType.EDITABLE);}
        SigwiseLogger.i(TAG, "resolution = " + resoState + " quota = " + quota + " videocliplen = " + videocliplen);

        RadioButton b = (RadioButton) findViewById(R.id.radioButton);
        if(resoState == 2) b = (RadioButton) findViewById(R.id.radioButton2);
        else if(resoState == 3) b = (RadioButton) findViewById(R.id.radioButton3);
        b.setChecked(true);

        thresholdSlider = findViewById(R.id.thresholdSlider);
        thresholdSliderVal = findViewById(R.id.thresholdSliderVal);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            thresholdSliderVal.setText(extras.getString("threshVal"));
            //The key argument here must match that used in the other activity
        }

        thresholdSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                thresholdSliderVal.setText(Float.toString(value));
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        int id = view.getId();
        if (id == R.id.radioButton) {
            if (checked) {
                currentReso = lowReso;
                resoState = 1;
            }
        } else if (id == R.id.radioButton2) {
            if (checked) {
                currentReso = mediumReso;
                resoState = 2;
            }
        } else if (id == R.id.radioButton3) {
            if (checked) {
                currentReso = highReso;
                resoState = 3;
            }
        }
        TextView tv = (TextView)findViewById(textView3);
        int memAvailable = getAvailableExternalMemoryTime();
        tv.setText(memAvailable + " MB");
        if(quota == 0) {
            EditText myedittext = (EditText) findViewById(editText);
            quota = (int)memAvailable / 200 * 100;
            myedittext.setText("" + quota, TextView.BufferType.EDITABLE);
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("threshVal",getThreshold());
        intent.putExtra("resolution", resoState);
        EditText edittext_quota = (EditText)findViewById(editText);
        int storage = Integer.parseInt(edittext_quota.getText().toString());
        quota = storage;
        intent.putExtra("quota", quota);
        EditText edittext_length = (EditText) findViewById(editText2);
        int videocliplen = Integer.parseInt(edittext_length.getText().toString());
        intent.putExtra("videocliplen",videocliplen);
        startActivity(intent);
        SharedPreferences.Editor editor = getSharedPreferences(getResources().getString(R.string.configfile), MODE_PRIVATE).edit();
        editor.putInt("resolution", resoState);
        editor.putInt("quota", quota);
        editor.putInt("videocliplen", videocliplen);
        SigwiseLogger.i(TAG,"resoState = "+resoState+" quota = "+quota+" videocliplen = "+videocliplen);
        editor.commit();
        this.finish();
    }

    private void goToConfigActivity() {
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivity(intent);
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return formatSize(availableBlocks * blockSize);
    }

    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return formatSize(totalBlocks * blockSize);
    }

    public static String getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return formatSize(availableBlocks * blockSize);
        } else {
            return null;
        }
    }

    public static int getAvailableExternalMemoryTime() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return (int)((availableBlocks * blockSize/1024)/1024);
        } else {
            return 0;
        }
    }
    public static String getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return formatSize(totalBlocks * blockSize);
        } else {
            return null;
        }
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static int getThreshold() {
        return Integer.parseInt((String) thresholdSliderVal.getText());
    }
}
