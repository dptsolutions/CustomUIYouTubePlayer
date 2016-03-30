package com.dptsolutions.youtubeplayercustomoverlayexample;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class MainActivity extends AppCompatActivity {

    private RadioGroup activityChoices;
    private RadioButton platformActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityChoices = (RadioGroup) findViewById(R.id.activity_type);
        platformActivityButton = (RadioButton) findViewById(R.id.checkPlatform);

        activityChoices.check(R.id.checkAppCompat);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            platformActivityButton.setEnabled(false);
        }
    }

    public void launchPlayer(View view) {
        doLaunchPlayer(false);
    }

    public void launchStyledPlayer(View view) {
       doLaunchPlayer(true);
    }

    private void doLaunchPlayer(boolean useCustomTheme) {
        Class activityClass = activityChoices.getCheckedRadioButtonId() == R.id.checkPlatform
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
