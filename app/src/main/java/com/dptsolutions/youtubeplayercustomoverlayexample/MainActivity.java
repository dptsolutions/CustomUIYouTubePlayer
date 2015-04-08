package com.dptsolutions.youtubeplayercustomoverlayexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchPlayer(View view) {
        Intent launchPlayerIntent = new Intent(this, YouTubePlayerActivity.class);
        launchPlayerIntent.putExtra(YouTubePlayerActivity.EXTRA_VIDEO_YOUTUBE_ID, "66f4-NKEYz4");
        startActivity(launchPlayerIntent);
    }
}
