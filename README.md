# YoutubePlayerCustomOverlayExample
Example project to demonstrate how to build a custom video player UI that overlays a  [YouTubePlayerFragment](https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubePlayerFragment) from Google's [YouTube Android Player API](https://developers.google.com/youtube/android/player/).

## Why is this interesting?
While the API allows for you to implement custom controls for the YouTubePlayer, and provides an example for this in the the demo app, there is one big thing that they neglect to call out in the example - you cannot draw a view on top of a [YouTubePlayerView](https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubePlayerView). If you try, the player will stop playback as soon as the view is drawn on top of the player, and will throw an [YouTubePlayerView.ErrorReason.UNAUTHORIZED_OVERLAY](https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubePlayer.ErrorReason) error. Thus, on the surface it appears that you cannot make your own custom UI for the player that functions like most video players on Android, where the controls overlay the fullscreen video, and are show or hidden when the user taps the screen.

Fortunately, you can get around this "feature" by using a [PopupWindow](http://developer.android.com/reference/android/widget/PopupWindow.html) to contain the controls that will be drawn on top of the YouTubePlayerView. A PopupWindow doesn't seem to trigger the error.

##Cool, so how do I use this?
Basically, just copy the YouTubePlayerActivity in the app to your own app, and all it's related resources. Then style it however you wish. 

The YouTubePlayerActivity expects your Google API key for the YouTubePlayer to be located in the BuildConfig.GOOGLE_API_KEY field, and the build script expects the values for the key to be found in your gradle.properties file. See the build.gradle for the app for details.

##Anything I need to watch out for?
* Currently, the YouTubePlayerActivity crashes on orientation change. So lock it to landscape in your manifest. (Fixing this is on the TODO list)
* The cueVideo() method to load a video seems buggy and not always work. Stick with loadVideo().

