package com.dptsolutions.youtubeplayercustomoverlayexample;

import android.content.Intent;
import android.os.Build;
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
        doLaunchPlayer(false);
    }

    public void launchStyledPlayer(View view) {
       doLaunchPlayer(true);
    }

    private void doLaunchPlayer(boolean useCustomTheme) {
        Class activityClass = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                ? YouTubePlayerActivity.class
                : YouTubePlayerAppCompatActivity.class;
        final Intent launchPlayerIntent = new Intent(this, activityClass);
        launchPlayerIntent.putExtra(Constants.EXTRA_VIDEO_YOUTUBE_ID, "66f4-NKEYz4");
        if(useCustomTheme) {
            launchPlayerIntent.putExtra(Constants.EXTRA_USE_CUSTOM_THEME, true);
        }
        startActivity(launchPlayerIntent);
    }
}
