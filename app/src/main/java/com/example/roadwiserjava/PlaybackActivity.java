package com.example.roadwiserjava;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import com.example.roadwiserjava.SigwiseLogger;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class PlaybackActivity extends FragmentActivity {

    private static final String TAG = "Sigwise";
    private String fileToPlay = null;
    private volatile boolean mPlaying = false;

    private PlayActivity playFrag = null;
    public boolean IsPlaying() {return mPlaying;}
    public void IsPlaying(boolean playing){ mPlaying = playing; }

    public String file2Play(){
        return fileToPlay;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler((RoadWiserApp)getApplication()));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playback);

        Bundle loadData = getIntent().getExtras();
        if ( null != loadData ) {
            fileToPlay = loadData.getString("SELECTED_FILE");
            SigwiseLogger.i(TAG, "Playing video file  " + fileToPlay);
        }
        else{
            SigwiseLogger.e( TAG, "No video file specified.");
            fileToPlay = null;
        }

    }

    public void goToLoadActivity() {
        FragmentManager fManager = getSupportFragmentManager();
        //fManager.beginTransaction().remove(fManager.getFragments().get(0)).commit();
        //fManager.beginTransaction().remove(fManager.getFragments().get(0)).commit();
        fManager.popBackStackImmediate();
        fManager.popBackStackImmediate();
        Intent intent = new Intent(this, LoadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
        return true;
    }

/*    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToLoadActivity();
        //finish();
    }*/

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event )  {
        if ( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 ) {
            goToLoadActivity();
            return true;
        }

        return super.onKeyDown( keyCode, event );
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


    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FragmentManager fManager = getSupportFragmentManager();
        playFrag = new PlayActivity();
        FragmentTransaction ft = fManager.beginTransaction();
        ft.add(R.id.playholder, playFrag);
        //ft.addToBackStack(null);
        ft.commit();

    }

    public int getVideoCurPos(){
        return playFrag.getVideoCurPos();
    }
}
