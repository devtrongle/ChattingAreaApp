package com.example.chattingarea.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.example.chattingarea.R;

import timber.log.Timber;

public class VideoViewActivity extends AppCompatActivity {

    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        String url = getIntent().getStringExtra("url");
        Log.d("CheckApp", "onCreate: " + url);
        if(url.isEmpty()){
            finish();
        }

        VideoView videoView = findViewById(R.id.videoView);
        mProgressBar = findViewById(R.id.progress);
        mProgressBar.setVisibility(View.VISIBLE);
        videoView.setVideoPath(url);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
                mProgressBar.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.imvBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}