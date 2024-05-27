/* Copyright (c) 1996-2020 Clickteam
 *
 * This source code is part of the Android exporter for Clickteam Multimedia Fusion 2.
 *
 * Permission is hereby granted to any person obtaining a legal copy
 * of Clickteam Multimedia Fusion 2 to use or modify this source code for
 * debugging, optimizing, or customizing applications created with
 * Clickteam Multimedia Fusion 2.  Any other use of this source code is prohibited.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
//----------------------------------------------------------------------------------
//
// Audio Focus for Fusion
//
//----------------------------------------------------------------------------------
package Application;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

public class AudioFocusFusion implements AudioManager.OnAudioFocusChangeListener {

    public interface AudioFocusListener {
         public void callback(boolean result);
    }

    private static final String TAG = "MMFRuntime";

    private static final int DUCK_DURATION = 250;
    private static       int DUCK_AUDIO_TO = 0;
    private static       int DUCK_AUDIO_AT = 0;

    private static final int FS_FOCUS_GAIN_TYPE = AudioManager.AUDIOFOCUS_GAIN;
    private static final int FS_STREAM_TYPE = AudioManager.STREAM_MUSIC;

    private final Context context;
    private final AudioManager audioManager;
    private Handler handler = new Handler();
    private final MediaController controller;
    private final MediaSession mediaSession;

    private final AudioFocusRequest request;
    private AudioFocusListener  audiofocusListener;
    private boolean audioFocus;

    public AudioFocusFusion(@NonNull final Context context, AudioAttributes attributes) {
        this.context = context;

        if (Build.VERSION.SDK_INT >= 21) {
            this.mediaSession =  new MediaSession(this.context, "AudioFusion");
            this.controller = new MediaController(this.context, this.mediaSession.getSessionToken());
        }
        else {
            this.controller = null;
            this.mediaSession= null;
        }

        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            request = new AudioFocusRequest.Builder(FS_FOCUS_GAIN_TYPE)
                    .setFocusGain(FS_FOCUS_GAIN_TYPE)
                    .setAudioAttributes(attributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();
        } else {
            request = null;
        }
        DUCK_AUDIO_TO = (int)(getMaxVolume()*0.1f);
        DUCK_AUDIO_AT = getVolume();
        audioFocus = false;
    }

    public void setAudioFocusListener(AudioFocusListener afl) {
        audiofocusListener = afl;
    }

    public void dispose() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(mediaSession != null)
                mediaSession.release();
        }
        abandonAudioFocus();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Audio Manager
    //////////////////////////////////////////////////////////////////////////*/

    public boolean requestAudioFocus() {
        int result = 0;
        if (Build.VERSION.SDK_INT >= 26) {
            result = audioManager.requestAudioFocus(request);
        } else {
            result = audioManager.requestAudioFocus(this, FS_STREAM_TYPE, FS_FOCUS_GAIN_TYPE);
        }
        return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    public void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= 26) {
            audioManager.abandonAudioFocusRequest(request);
        } else {
            audioManager.abandonAudioFocus(this);
        }
        audioFocus = false;
    }

    public int getVolume() {
        return audioManager.getStreamVolume(FS_STREAM_TYPE);
    }

    public int getMaxVolume() {
        return audioManager.getStreamMaxVolume(FS_STREAM_TYPE);
    }

    public void setVolume(final int volume) {
        audioManager.setStreamVolume(FS_STREAM_TYPE, volume, 0);
    }

    public boolean getAudioFocus() {
        return audioFocus;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Audio Focus Change Listener
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d(TAG, "A change in audio focus was detected with focus: " + focusChange );
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                onAudioFocusGain();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                onAudioFocusLossCanDuck();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    controller.getTransportControls().pause();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            controller.getTransportControls().stop();
                        }
                    }, 10000);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                onAudioFocusLoss();
                break;
        }
    }

    private void onAudioFocusGain() {
        Log.d(TAG, "audio focus change to gain ...");
        animateAudio(DUCK_AUDIO_TO, DUCK_AUDIO_AT);
        audioFocus = true;
        if(audiofocusListener != null)
            audiofocusListener.callback(true);
    }

    private void onAudioFocusLoss() {
        Log.d(TAG, "audio focus change to loss ...");
        audioFocus = false;
        abandonAudioFocus();
        if(audiofocusListener != null)
            audiofocusListener.callback(false);
    }

    private void onAudioFocusLossCanDuck() {
        Log.d(TAG, "audio focus change to loss can duck ...");
        DUCK_AUDIO_AT = getVolume();
        animateAudio(DUCK_AUDIO_AT, DUCK_AUDIO_TO);
    }

    private void animateAudio(final int from, final int to) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(from, to);
        valueAnimator.setDuration(AudioFocusFusion.DUCK_DURATION);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setVolume(from);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setVolume(to);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVolume(to);
            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setVolume((int)animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

}