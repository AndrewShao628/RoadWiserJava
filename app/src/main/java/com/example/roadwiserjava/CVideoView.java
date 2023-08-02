package com.example.roadwiserjava;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;


public class CVideoView extends VideoView {

    private PlayPauseListener mListener;

    public CVideoView(Context context) {
        super(context);
    }

    public CVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPlayPauseListener(PlayPauseListener listener) {
        mListener = listener;
    }

    @Override
    public void pause() {
        super.pause();
        if (mListener != null) {
            mListener.onVideoPause();
        }
    }

    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            mListener.onVideoPlay();
        }
    }

    public static interface PlayPauseListener {
        void onVideoPlay();
        void onVideoPause();
    }

}