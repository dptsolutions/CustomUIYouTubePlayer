package com.dptsolutions.youtubeplayercustomoverlayexample;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.dptsolutions.customuiyoutubeplayer.CustomUIYouTubePlayerFragment;

/**
 * Activity for playing YouTube videos in Fullscreen landscape
 */
public class YouTubePlayerActivity extends Activity  {

    public static final String EXTRA_VIDEO_YOUTUBE_ID = YouTubePlayerActivity.class.getPackage().getName() + ".extra_video_youtube_id";
    public static final String EXTRA_USE_CUSTOM_THEME = YouTubePlayerActivity.class.getPackage().getName() + ".extra_use_custom_theme";
    private static final String PLAYER_FRAG_TAG = "player_fragment";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_youtube_player);

        FragmentManager mgr = getFragmentManager();
        Fragment playerFrag = mgr.findFragmentByTag(PLAYER_FRAG_TAG);

        if(playerFrag == null) {
            final Integer styleResId = getIntent().getBooleanExtra(EXTRA_USE_CUSTOM_THEME, false) ? R.style.AppTheme_YouTubePlayer : null;
            playerFrag = CustomUIYouTubePlayerFragment.newInstance(
                    getIntent().getStringExtra(EXTRA_VIDEO_YOUTUBE_ID),
                    BuildConfig.GOOGLE_API_KEY, styleResId);
            mgr.beginTransaction()
                    .add(R.id.fragment_container, playerFrag, PLAYER_FRAG_TAG)
                    .commit();
        }
    }
}
