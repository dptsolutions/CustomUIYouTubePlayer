package com.dptsolutions.youtubeplayercustomoverlayexample;

import android.app.Activity;
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
import com.google.android.youtube.player.YouTubePlayerView;

import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Activity for playing YouTube videos in Fullscreen landscape
 */
public class YouTubePlayerActivity extends Activity implements YouTubePlayer.OnInitializedListener, YouTubePlayer.PlayerStateChangeListener, YouTubePlayer.PlaybackEventListener {

    YouTubePlayerFragment youtubePlayerFragment;

    protected String youtubeId;

    protected PlayerControlsPopupWindow playerControls;

    private YouTubePlayer youtubePlayer;
    private YouTubePlayerView youtubePlayerView;
    private GestureDetectorCompat gestureDetector;

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private static final int HIDE_STATUS_BAR_FLAGS_IMMERSIVE = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    private static final int HIDE_STATUS_BAR_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_FULLSCREEN;
    private static final int SEEK_BAR_UPDATE_INTERVAL = 1000;
    private static final int HIDE_STATUS_BAR_DELAY_MILLIS = 3000;
    private static final String TAG = "YouTubePlayerActivity";
    public static final String EXTRA_VIDEO_YOUTUBE_ID = YouTubePlayerActivity.class.getPackage().getName() + ".extra_video_youtube_id";

    private Handler seekBarPositionHandler = new Handler();
    private Runnable seekBarPositionRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, String.format("In YoutubePlayerActivity#seekBarPositionRunnable.run. Current time in millis is %d. Current length in millis is %d", youtubePlayer.getCurrentTimeMillis(), youtubePlayer.getDurationMillis()));
            //Docs say that getDurationMillis() can change over time, so if things have changed, update SeekBarMax so we don't potentially get out of range
            if(playerControls.getSeekBarMax() != youtubePlayer.getDurationMillis()) {
                Log.d(TAG, String.format("In YoutubePlayerActivity#seekBarPositionRunnable.run. Updating SeekBarMax from %d to %d", playerControls.getSeekBarMax(), youtubePlayer.getDurationMillis()));
                playerControls.setSeekBarMax(youtubePlayer.getDurationMillis());
            }
            playerControls.setSeekBarPosition(youtubePlayer.getCurrentTimeMillis());
            scheduleSeekBarUpdate();
        }
    };
    private View.OnTouchListener onPlayerTouchedListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }
    };

    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            toggleControlsVisibility();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        youtubeId = getIntent().getStringExtra(YouTubePlayerActivity.EXTRA_VIDEO_YOUTUBE_ID);

        Log.d(TAG, "In onCreate. Initialize player and controls");
        setContentView(R.layout.activity_youtube_player);
        youtubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_player_fragment);
        gestureDetector = new GestureDetectorCompat(this, onGestureListener);
        initializeYoutubePlayer();
        playerControls = new PlayerControlsPopupWindow(this);
        playerControls.setEnabled(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "In onStart. Attach touch listener to YoutubePlayerView in youtubePlayerFragment");
        //The YoutubePlayerView in the YoutubePlayerFragment seems to eat all touches,
        //so attach the touch listener for the player controls here,
        //after initializing the player earlier in the lifecycle

        //If using YoutubePlayerFragment, the YoutubePlayerView is the root view of the fragment.
        youtubePlayerView = (YouTubePlayerView) youtubePlayerFragment.getView();
        //If using YoutubePlayerSupportFragment, the YoutubePlayerView is the first child of the root view
        //ViewGroup playerView = (ViewGroup)youtubePlayerFragment.getView();
        //youtubePlayerView = (YouTubePlayerView)playerView.getChildAt(0);

        youtubePlayerView.setOnTouchListener(onPlayerTouchedListener);

        //Want to make sure we start in the state where things will be shown when we hit onResume
        if(playerControls.isShowing()) {
            playerControls.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "In onResume. Post runnable to root view of youtubePlayerFragment, that will initially show the player controls");
        //We have to wait till everything is running before we show the PopupWindow, otherwise you get an exception.
        //This runnable will run once the View is attached to the window
        youtubePlayerFragment.getView().post(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    final View decorView = getWindow().getDecorView();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        //Have to use Sticky Immersive mode on Lollipop devices, otherwise when the Status Bar is shown
                        //the YoutubePlayer will throw the YouTubePlayer.ErrorReason.UNAUTHORIZED_OVERLAY error and stop playback.
                        //This doesn't happen on KitKat but since KitKat has Immersive mode, we'll use it there as well
                        decorView.setSystemUiVisibility(HIDE_STATUS_BAR_FLAGS_IMMERSIVE);
                    } else {
                        //For Jelly Bean, things are a little different. Here we're faking Sticky Immersive mode. The user can swipe down
                        //from the top of the screen at any time and unhide the Status Bar, and it'll never go away. So detect
                        //when the fullscreen flag gets removed, and post a delayed runnable that will hide the system bar again
                        //just like Sticky Immersive mode
                        decorView.setSystemUiVisibility(HIDE_STATUS_BAR_FLAGS);
                        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                            @Override
                            public void onSystemUiVisibilityChange(int visibility) {
                                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                    decorView.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            decorView.setSystemUiVisibility(HIDE_STATUS_BAR_FLAGS);
                                        }
                                    }, HIDE_STATUS_BAR_DELAY_MILLIS);
                                }
                            }
                        });
                    }
                    toggleControlsVisibility();
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "In onStop. Hide controls, reset button state");
        //Stops window leaked error
        playerControls.dismiss();
        //If activity is restarted, player will be paused. So reset the button state on our way out
        playerControls.setPlayPauseButtonState(PlayerControlsPopupWindow.PlayPauseButtonState.PLAY);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "In onDestroy. Release youtube player");
        //Stops leaked ServiceConnection error
        youtubePlayer.release();
    }

    /////////////////////////////////////////////
    //YouTubePlayer.OnInitializedListener Methods
    /////////////////////////////////////////////

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        Log.d(TAG, String.format("In YoutubePlayerActivity.onInitializationSuccess. wasRestored = %s", wasRestored));
        youtubePlayer = player;
        youtubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        youtubePlayer.setPlayerStateChangeListener(this);
        youtubePlayer.setPlaybackEventListener(this);
        if (!wasRestored) {
            Log.d(TAG, String.format("In YoutubePlayerActivity.onInitializationSuccess. Cueing video with id = %s", youtubeId));
            player.loadVideo(youtubeId);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        Log.d(TAG, String.format("In YoutubePlayerActivity.onInitializationFailure. errorReason = %s", errorReason.toString()));
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    /////////////////////////////////////////////////
    //YouTubePlayer.PlayerStateChangeListener Methods
    /////////////////////////////////////////////////

    @Override
    public void onLoading() {
        Log.d(TAG, "In YoutubePlayerActivity.onLoading");
        //Set controls to their initial state and lock them until we have all the information
        //necessary to fully set their state
        playerControls.setEnabled(false);
        playerControls.setPlayPauseButtonState(PlayerControlsPopupWindow.PlayPauseButtonState.PLAY);
        playerControls.setSeekBarPosition(0);
    }

    @Override
    public void onLoaded(String videoId) {
        Log.d(TAG, "In YoutubePlayerActivity.onLoaded");
        //Complete initializing controls and enable them for interaction
        playerControls.setSeekBarMax(youtubePlayer.getDurationMillis());
        playerControls.setEnabled(true);
    }

    @Override
    public void onAdStarted() {
        Log.d(TAG, "In YoutubePlayerActivity.onAdStarted");
        playerControls.setEnabled(false);
    }

    @Override
    public void onVideoStarted() {
        Log.d(TAG, String.format("In YoutubePlayerActivity.onVideoStarted. youtubePlayer.isPlaying(): %s", youtubePlayer.isPlaying()));
        //We need to set button state to either play or pause, because if the video has ended and you scrub back
        //This callback fires but you won't necessarily actually be playing
        playerControls.setPlayPauseButtonState(youtubePlayer.isPlaying() ? PlayerControlsPopupWindow.PlayPauseButtonState.PAUSE : PlayerControlsPopupWindow.PlayPauseButtonState.PLAY);
    }

    @Override
    public void onVideoEnded() {
        Log.d(TAG, "In YoutubePlayerActivity.onVideoEnded");
        //Since the video has ended, set the controls to the restart state
        playerControls.setSeekBarPosition(playerControls.getSeekBarMax());
        playerControls.setPlayPauseButtonState(PlayerControlsPopupWindow.PlayPauseButtonState.REPLAY);


    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {
        Log.d(TAG, String.format("In YoutubePlayerActivity.onError: %s", errorReason));
        if(errorReason == YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
            finish();
        }
    }

    /////////////////////////////////////////////////
    //YouTubePlayer.PlaybackEventListener Methods
    /////////////////////////////////////////////////

    @Override
    public void onPlaying() {
        Log.d(TAG, "In YoutubePlayerActivity.onPlaying");
        scheduleSeekBarUpdate();
        playerControls.setPlayPauseButtonState(PlayerControlsPopupWindow.PlayPauseButtonState.PAUSE);
    }

    @Override
    public void onPaused() {
        Log.d(TAG, "In YoutubePlayerActivity.onPaused");
        stopSeekBarUpdates();
        playerControls.setPlayPauseButtonState(PlayerControlsPopupWindow.PlayPauseButtonState.PLAY);
    }

    @Override
    public void onStopped() {
        Log.d(TAG, "In YoutubePlayerActivity.onStopped");
        stopSeekBarUpdates();
    }

    @Override
    public void onBuffering(boolean isBuffering) {
        Log.d(TAG, String.format("In YoutubePlayerActivity.onBuffering, isBuffering = %s, youtubePlayer.isPlaying = %s", isBuffering, youtubePlayer.isPlaying()));

        //If we're buffering, we're not playing. So we don't need to monitor the video's current position
        //until we stop buffering and start playing again
        if(isBuffering) {
            stopSeekBarUpdates();
        } else if(youtubePlayer.isPlaying()) {
            scheduleSeekBarUpdate();
        }
    }

    @Override
    public void onSeekTo(int newPositionMillis) {
        Log.d(TAG, String.format("In YoutubePlayerActivity.onSeekTo, newPositionMillis = %d", newPositionMillis));
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            initializeYoutubePlayer();
        }
    }

    private void initializeYoutubePlayer() {
        Log.d(TAG, "In initializeYoutubePlayer");
        youtubePlayerFragment.initialize(BuildConfig.GOOGLE_API_KEY, this);
    }

    private void toggleControlsVisibility() {
        if(playerControls.isShowing()) {
            Log.d(TAG, "In toggleControlsVisibility. Hiding player controls");
            playerControls.dismiss();
        } else {
            Log.d(TAG, "In toggleControlsVisibility. Showing player controls");
            playerControls.showAtLocation(youtubePlayerFragment.getView(), Gravity.BOTTOM, 0, 0);
        }
    }

    private void scheduleSeekBarUpdate() {
        seekBarPositionHandler.postDelayed(seekBarPositionRunnable, SEEK_BAR_UPDATE_INTERVAL);
    }

    private void stopSeekBarUpdates() {
        seekBarPositionHandler.removeCallbacks(seekBarPositionRunnable);
    }


    /**
     * Class containing the player controls. Has to be a PopupWindow due to the YoutubePlayer library
     * disallowing you to draw a View on top of it.
     */
    private class PlayerControlsPopupWindow extends PopupWindow implements SeekBar.OnSeekBarChangeListener {

        private SeekBar seekBar;
        private ImageButton playPauseButton;
        private TextView elapsedTime;
        private boolean isEnabled;
        private int playPauseButtonState;

        private static final int PAUSE_BUTTON = R.drawable.ic_pause_white_24dp;
        private static final int PLAY_BUTTON = R.drawable.ic_play_arrow_white_24dp;
        private static final int REPLAY_BUTTON = R.drawable.ic_replay_white_24dp;
        private static final String LESS_THAN_HUNDRED_MINUTES_FORMAT = "mm:ss";
        private static final String HUNDRED_MINUTES_FORMAT = "mmm:ss";

        private class PlayPauseButtonState {
            public static final int PLAY = 1;
            public static final int PAUSE = 0;
            public static final int REPLAY = 2;
        }


        public PlayerControlsPopupWindow(Context context) {
            super(View.inflate(context, R.layout.youtube_player_controls, null),
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            setFocusable(false);
            setTouchable(true);
            setAnimationStyle(R.style.YoutubePlayerControlsAnimation);


            seekBar = (SeekBar) getContentView().findViewById(R.id.scrubber);
            seekBar.setOnSeekBarChangeListener(this);
            playPauseButton = (ImageButton) getContentView().findViewById(R.id.playPauseButton);
            playPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(youtubePlayer.isPlaying()) {
                        playPauseButton.setImageResource(PLAY_BUTTON);
                        if (youtubePlayer != null) {
                            youtubePlayer.pause();
                        }
                    } else {
                        playPauseButton.setImageResource(PAUSE_BUTTON);
                        if (youtubePlayer != null) {
                            youtubePlayer.play();
                        }
                    }
                }
            });
            elapsedTime = (TextView) getContentView().findViewById(R.id.elapsed_time);


        }

        /**
         * Sets the max value of the SeekBar
         *
         * @param milliseconds The # of milliseconds that the max value should be set to
         */
        public void setSeekBarMax(int milliseconds) {
            Log.d(TAG, String.format("In PlayerControlsPopupWindow.setSeekBarMax. Setting max to %d", milliseconds));
            seekBar.setMax(milliseconds);
        }
        /**
         * Gets the max value of the SeekBar
         *
         * @return The max value, in milliseconds
         */
        public int getSeekBarMax() {
            return seekBar.getMax();
        }
        /**
         * Sets the state of the Play/Pause button
         *
         * @param state Any of the constants in {@link com.dptsolutions.youtubeplayercustomoverlayexample.YouTubePlayerActivity.PlayerControlsPopupWindow.PlayPauseButtonState}
         */
        public void setPlayPauseButtonState(int state) {
            switch (state) {
                case PlayPauseButtonState.PLAY:
                    playPauseButton.setImageResource(PLAY_BUTTON);
                    break;
                case PlayPauseButtonState.PAUSE:
                    playPauseButton.setImageResource(PAUSE_BUTTON);
                    break;
                case PlayPauseButtonState.REPLAY:
                    playPauseButton.setImageResource(REPLAY_BUTTON);
                    break;
                default:
                    Log.w(TAG, String.format("Value passed into setPlayPauseButtonState was not a valid state constant. Value passed in: %d", state));
                    break;
            }
            playPauseButtonState = state;
        }

        public int getPlayPauseButtonState() {
            return playPauseButtonState;
        }
        /**
         * Sets the position of the SeekBar
         *
         * @param milliseconds The position the SeekBar should be set to, in milliseconds.
         *                     Should be between 0 and {@link com.dptsolutions.youtubeplayercustomoverlayexample.YouTubePlayerActivity.PlayerControlsPopupWindow#getSeekBarMax()}, inclusive
         */
        public void setSeekBarPosition(int milliseconds) {
            Log.d(TAG, String.format("In PlayerControlsPopupWindow.setSeekbarPosition. Passed in milliseconds: %d", milliseconds));
            if(milliseconds >=0 && milliseconds <= seekBar.getMax()) {
                Log.d(TAG, String.format("In PlayerControlsPopupWindow.setSeekbarPosition. Setting seekBar to %d", milliseconds));
                seekBar.setProgress(milliseconds);
            }
        }
        /**
         * Sets the enabled state of the Play/Pause button and the SeekBar
         *
         * @param isEnabled true, if the controls should be enabled
         */
        public void setEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
            playPauseButton.setEnabled(isEnabled);
            seekBar.setEnabled(isEnabled);
        }
        /**
         * Indicates if the Play/Pause button and SeekBar are enabled
         *
         * @return true, if enabled
         */
        public boolean isEnabled() {
            return isEnabled;
        }

        ///////////////////////
        //SeekBarChangeListener
        ///////////////////////
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.d(TAG, String.format("PlayerControlsPopupWindow.onProgressChanged progress:%d, fromUser:%s", progress, fromUser));
            String timeFormat = progress / 60000 >= 100 ? HUNDRED_MINUTES_FORMAT : LESS_THAN_HUNDRED_MINUTES_FORMAT;
            elapsedTime.setText(DurationFormatUtils.formatDuration(progress, timeFormat, true));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "PlayerControlsPopupWindow.onStartTrackingTouch");
            //The user is manipulating the SeekBar, so stop automatically updating it
            stopSeekBarUpdates();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, String.format("PlayerControlsPopupWindow.onStopTrackingTouch. Seeking to %d", seekBar.getProgress()));
            //The user ended manipulating the SeekBar. Seek the player to the current place, and restart
            //automatic updates if the video is playing
            youtubePlayer.seekToMillis(seekBar.getProgress());
            if(youtubePlayer.isPlaying()) {
                scheduleSeekBarUpdate();
            }
        }
    }
}
