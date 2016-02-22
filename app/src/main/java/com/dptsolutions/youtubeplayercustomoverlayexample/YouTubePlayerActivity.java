package com.dptsolutions.youtubeplayercustomoverlayexample;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Activity for playing YouTube videos in Fullscreen landscape
 */
public class YouTubePlayerActivity extends Activity  {

    public static final String EXTRA_VIDEO_YOUTUBE_ID = YouTubePlayerActivity.class.getPackage().getName() + ".extra_video_youtube_id";
    private static final String PLAYER_FRAG_TAG = "player_fragment";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_youtube_player);

        Fragment playerFrag = CustomUIYouTubePlayerFragment.newInstance(getIntent().getStringExtra(EXTRA_VIDEO_YOUTUBE_ID));
        FragmentManager mgr = getFragmentManager();
        mgr.beginTransaction()
                .add(R.id.fragment_container, playerFrag, PLAYER_FRAG_TAG)
                .commit();
    }
}
