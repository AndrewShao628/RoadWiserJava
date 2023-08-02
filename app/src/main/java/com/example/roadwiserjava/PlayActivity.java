package com.example.roadwiserjava;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

public class PlayActivity extends Fragment implements CVideoView.PlayPauseListener {

    private static final String TAG = "Sigwise";
    private CVideoView videoView = null;
    private int mapstate = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.activity_play, null, false);
        videoView = (CVideoView) v.findViewById(R.id.videoView);
        videoView.setPlayPauseListener(this);
/*
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                PlaybackActivity act = (PlaybackActivity)getActivity();
                if(mapstate == 0){
                    act.removeMap();
                    mapstate=1;
                }
                else {
                    act.addMap();
                    mapstate=0;
                }
                Log.i(TAG, "videoview touched");
                return true;
            }
        });
        */
/*        Button button = (Button)v.findViewById(R.id.buttonplayReturn);
        if(button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
*//*                    Intent intent = new Intent(getActivity(), LoadActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);*//*
                    PlaybackActivity act = (PlaybackActivity)getActivity();
                    act.goToLoadActivity();
                }

            });
        }*/

        PlaybackActivity act = (PlaybackActivity)getActivity();
        String filetoplay = act.file2Play();
        if(null != filetoplay) {

            videoView.setVideoPath(filetoplay);

            videoView.requestFocus();
            MediaController mc = new MediaController(getActivity()){

            };
            videoView.setMediaController(mc);
            mc.setAnchorView(videoView);
            videoView.start();
            act.IsPlaying(true);
            videoView.getCurrentPosition();
        }
        else {
            SigwiseLogger.i("Sigwise", "No playing video file found");
        }

        return v;
    }

    public int getVideoCurPos(){
        if(videoView != null){
            return videoView.getCurrentPosition();
        }else {
            return 0;
        }
    }

    @Override
    public void onVideoPlay() {
        PlaybackActivity act = (PlaybackActivity)getActivity();
        act.IsPlaying(true);
    }

    @Override
    public void onVideoPause() {
        PlaybackActivity act = (PlaybackActivity)getActivity();
        act.IsPlaying(false);
    }
}
