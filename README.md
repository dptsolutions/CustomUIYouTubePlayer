# Custom UI YouTube Player
An Android library containing a Fragment extending [YouTubePlayerFragment](https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubePlayerFragment) from Google's [YouTube Android Player API](https://developers.google.com/youtube/android/player/), allowing you to easily play YouTube videos in your application with a customizable player controls overlay. For API 16+.

## Why is this interesting?
While the API allows for you to implement custom controls for the YouTubePlayer, and provides an example for this in the the demo app, there is one big thing that they neglect to call out in the example - you cannot draw a view on top of a [YouTubePlayerView](https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubePlayerView). If you try, the player will stop playback as soon as the view is drawn on top of the player, and will throw an [YouTubePlayerView.ErrorReason.UNAUTHORIZED_OVERLAY](https://developers.google.com/youtube/android/player/reference/com/google/android/youtube/player/YouTubePlayer.ErrorReason) error. Thus, on the surface it appears that you cannot make your own custom UI for the player that functions like most video players on Android, where the controls overlay the fullscreen video, and are show or hidden when the user taps the screen.

Fortunately, you can get around this "feature" by using a [PopupWindow](http://developer.android.com/reference/android/widget/PopupWindow.html) to contain the controls that will be drawn on top of the YouTubePlayerView. A PopupWindow doesn't seem to trigger the error.

##Cool, so how do I use this?

####Running the demo app

1. Follow the [instructions](https://developers.google.com/youtube/android/player/register) to generate API keys (using both the debug and release keystores) for Android apps at the Google Developer Console.
2. Clone the repo.
3. Create a gradle.properties file in the root of the project, add the following properties, and set their values to the API key(s) you generated.
    * *youtubeDebugApiKey*
    * *youtubeReleaseApiKey*
4. Import the project into Android Studio.
5. Build and run on a device that has the Youtube app installed.

####Using the Fragment in your own application

1. Add the repo to your project's build.gradle
```groovy
    allprojects {
        repositories {
            jcenter()
            ...
            maven {
                url 'https://dl.bintray.com/dptsolutions/maven/'
            }
        }
    }
```

2. Add the dependency to your application's build.gradle
```groovy
compile 'com.dptsolutions.customuiyoutubeplayer:customuiyoutubeplayer:0.1'
```
3. Copy the `styles.xml` files from the library to your project, rename the theme, and change values as you wish in the defined styles.

4. Create an instance of the Fragment using CustomUIYouTubePlayerFragment.newInstance().
**Parameters**
    * *youtubeId* (required): ID of YouTube video
    * *apiKey* (required): Your YouTube Player API key
    * *themeResourceId* (optional): The ID of your custom theme from step 2 to style the player controls
    * *enableDebugLogging* (optional): Set to true if you want to see the Fragment's debug messages
5. Add `android:configChanges="keyboardHidden|orientation|screenSize"` to the `<Activity>` tag containing the Fragment in your manifest.
6. Profit!

## License
Copyright 2016 Don Phillips

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

