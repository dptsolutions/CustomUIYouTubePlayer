package com.dptsolutions.youtubeplayercustomoverlayexample;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

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
