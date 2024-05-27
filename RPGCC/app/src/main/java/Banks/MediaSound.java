/* Copyright (c) 1996-2013 Clickteam
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
package Banks;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;

import Application.AudioFocusFusion;
import Runtime.MMFRuntime;
import Runtime.Log;
import Services.CServices;

public class MediaSound implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener
{

    public static final int ASSET_MODE = 0x0001;
    public static final int OBB_MODE = 0x0002;

    private int mode;
    private Uri uri;
    private AssetFileDescriptor fd;

    private int handle;

    // which file is getting played
//    public static final int URI_PLAYING = 1;
//    public static final int RESOURCE_PLAYING = 2;
//    public static final int ASSET_PLAYING = 3;

    // states of the media player
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_STOP = 3;
    public static final int STATE_LOADING = 4;
    public static final int STATE_PREPARED = 5;


    // current state
    private int state = STATE_STOP;
    private final MediaPlayer[] mps = new MediaPlayer[2];

    private MediaPlayer playing;

    // current volumes
    private float[] volumes;

    private int nLoops;
    private int remLoops;

    private AudioAttributes audioAttributes;
    private AudioFocusFusion audioFocusFusion;

    public MediaSound() {
        mps[0] = new MediaPlayer();
        mps[0].setOnErrorListener(this);
        mps[0].setOnPreparedListener(this);
        mps[0].setOnCompletionListener(this);

        mps[1] = new MediaPlayer();
        mps[1].setOnErrorListener(this);
        mps[1].setOnPreparedListener(this);
        mps[1].setOnCompletionListener(this);
    }

    public MediaSound(AudioAttributes audioAttributes, AudioFocusFusion audioFocusFusion, int mode) {
        this.mediaSoundListener = null;
        this.audioAttributes = audioAttributes;
        this.audioFocusFusion = audioFocusFusion;
        this.audioFocusFusion.requestAudioFocus();
        this.mode = mode;
        mps[0] = new MediaPlayer();
        if (Build.VERSION.SDK_INT >= 21)
            mps[0].setAudioAttributes(audioAttributes);
        else
            mps[0].setAudioStreamType(AudioManager.STREAM_MUSIC);

        mps[0] = new MediaPlayer();
        mps[0].setOnErrorListener(this);
        mps[0].setOnPreparedListener(this);
        mps[0].setOnCompletionListener(this);

        mps[1] = new MediaPlayer();
        if (Build.VERSION.SDK_INT >= 21)
            mps[1].setAudioAttributes(audioAttributes);
        else
            mps[1].setAudioStreamType(AudioManager.STREAM_MUSIC);
        mps[1].setOnErrorListener(this);
        mps[1].setOnPreparedListener(this);
        mps[1].setOnCompletionListener(this);
    }

    public void setSoundSettings(int handle, String filename, float[] vol, int loops) {
        this.volumes = vol;
        this.handle = handle;
        this.nLoops = loops;
        this.remLoops = loops;

        if (filename == null) {
            if ((mode & ASSET_MODE) != 0) {
                fd = MMFRuntime.inst.getSoundFromAssets(String.format(Locale.US, "s%04d", handle));
            } else if ((mode & OBB_MODE) != 0) {
                fd = MMFRuntime.inst.getSoundFromOBB(String.format(Locale.US, "res/raw/s%04d", handle));
            } else {
                if (fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    fd = null;
                }
                int resourceId = MMFRuntime.inst.getResourceID(String.format(Locale.US, "raw/s%04d", handle));
                fd = MMFRuntime.inst.getResources().openRawResourceFd(resourceId);
            }
        } else {
            if (filename.contains("/")) {
                if (filename.startsWith("http") || filename.startsWith("rtsp:") || filename.startsWith("file:")) {
                    uri = Uri.parse(filename);
                }
                else if(!filename.contains("content:")){
                    uri = CServices.filenameToURI(filename);
                    if (uri == null) {
                        Log.Log("Can't open file");
                        return;
                    }
                }
                else
                    uri = Uri.parse(filename);
            } else {
                try {
                    fd = MMFRuntime.inst.getResources().getAssets().openFd(filename);
                } catch (IOException e) {
                    Log.Log("Can't open file");
                    return;
                }
            }
        }
    }

    public void start() {
        try {
            initMediaPlayer(mps[0]);
            if (nLoops != 1) {
                initMediaPlayer(mps[1]);
                if (mps[0] != null && mps[1] != null) {
                    mps[0].setNextMediaPlayer(mps[1]);
                }
            }
            if (mps[0] != null) {
                playing = mps[0];
                playing.start();
                state = STATE_PLAYING;
                Log.DEBUG("Starting with loops: " + nLoops);
            }
        } catch (Exception  e) {
           Log.DEBUG("Starting problem: " + e.getMessage());
           if(mps[0] != null)
               mps[0].release();
            if(mps[1] != null)
               mps[1].release();
           return;
        }
    }

    private void initMediaPlayer(MediaPlayer mp) throws Exception
    {
        if (mp.isPlaying()) {
            mp.stop();
        }
        mp.reset();

        if(fd != null) {
            mp.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getDeclaredLength());
        }
        else if(uri != null) {
            if(uri.getScheme().startsWith("http") || uri.getScheme().startsWith("rtsp:"))
                mp.setDataSource(String.valueOf(uri));
            else
                mp.setDataSource(MMFRuntime.inst, uri);
        }

        mp.setVolume(volumes[0], volumes[1]);
        mp.prepare();
    }

    public boolean isPlaying() {
        return state == STATE_PLAYING;
    }

    /**
     * play if the mediaplayer is pause
     */
    public void play() {

        try {
            if (state == STATE_PAUSED || state == STATE_PREPARED
                    && playing != null) {
                audioFocusFusion.requestAudioFocus();
                playing.start();
                playing.setVolume(volumes[0], volumes[1]);
                Log.DEBUG( "Playing - handle: " + handle + " with volume " + volumes[0] + " - " + volumes[1]);
                state = STATE_PLAYING;
            }
        } catch (IllegalStateException e) {
            Log.DEBUG( "illegal state when about to play");
        }
    }

    /**
     * pause current playing session
     */
    public void pause() {
        try {
            if (state == STATE_PLAYING && playing != null) {
                if (playing.isPlaying())
                    playing.pause();
                Log.DEBUG( "pausing");
                state = STATE_PAUSED;
            }
        } catch (IllegalStateException | NullPointerException e) {
            Log.DEBUG( "illegal state when pausing");
        }
    }

    public void release() {

        for (int i = 0; i < 2; i++) {
            try {
                if (mps[i] != null) {
                    if (mps[i].isPlaying())
                        mps[i].stop();
                    mps[i].reset();
                    mps[i].release();
                    mps[i] = null;
                    Log.DEBUG( "releasing id: "+i);
                }
            } catch (IllegalArgumentException | IllegalStateException | NullPointerException e) {
                Log.DEBUG( "illegal state when releasing");
            }
        }

        if (fd != null) {
            try {
                fd.close();
                fd = null;
            } catch (IOException e) {
                Log.Log("Releasing "+e.toString());
            }
        }

        state = STATE_STOP;
    }


    /**
     * get current state
     *
     * @return flow state
     */
    public int getState() {
        return state;
    }

    /**
     * stop every MediaPlayer
     */
    public void stop() {
        try {
            if (state == STATE_PLAYING && playing != null) {
                if (playing.isPlaying())
                    playing.stop();
                Log.DEBUG( "stop");
            }
        } catch (IllegalStateException | NullPointerException e) {
            Log.DEBUG( "illegal state when stopping");
        }
        state = STATE_STOP;
    }

    /**
     * reset every MediaPlayer
     */
    public void reset() {
        try {
            if (state == STATE_PLAYING && playing != null) {
                if (playing.isPlaying())
                    playing.reset();
                Log.DEBUG( "resetting");
            }
        } catch (IllegalStateException | NullPointerException e) {
            Log.DEBUG( "illegal state when resetting");
        }
        state = STATE_STOP;
    }

    /**
     * set volume for every MediaPlayer
     *
     * @param volumes volumes per left/right from 0.0 to 1.0
     */
    public void setVolume(float[] volumes) {
        if (volumes[0] == this.volumes[0]
                && volumes[1] == this.volumes[1])
            return;

        this.volumes = volumes;
        if (state != STATE_STOP) {
            try {
                if (playing != null)
                    playing.setVolume(this.volumes[0], this.volumes[1]);
            } catch (IllegalStateException | NullPointerException e) {
                Log.DEBUG( "illegal state when setting volume");
            }

        }
    }

    public int getDuration() {
        if (state != STATE_STOP) {
            if (playing != null)
                return playing.getDuration();
        }
        return -1;
    }

    public int getCurrentPosition() {
        int position = 0;
        try {
            if (state == STATE_PLAYING || state == STATE_PAUSED) {
                if (playing != null)
                    position = playing.getCurrentPosition();
                //Log.DEBUG( "get position: " + position);
            }
        } catch (IllegalStateException e) {
            Log.DEBUG( "illegal state when reading position");
        }
        return position;
    }

    public void seekTo(int position) {
        if (state != STATE_STOP) {

            // New SeekTo with more precision from API 26
            try {
                if (playing == null)
                    return;
                Method m = playing.getClass().getMethod("seekTo", Long.class, Integer.class);
                Integer[] args = {position, 3};
                m.invoke(null, (Object) args);
            } catch (Exception e) {
                //Regular SeekTo
                try {
                    playing.seekTo(position);
                } catch (IllegalStateException e1) {
                    Log.DEBUG( "illegal state when seeking");
                }
            }
        }
    }

    public void setLoops(int loop) {
        this.nLoops = loop;
    }

   /**
    *
    *   MediaPlayer Listeners
    *
    */


    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.DEBUG( "Completed ...");
        if(nLoops != 0 && (--remLoops) == 0)
        {
            mediaSoundListener.OnMediaSoundCompletion();
            return;
        }
        playing = (mps[0] == mp ? mps[1] : mps[0]);
        if(playing == null)
            return;
        try {
            initMediaPlayer(mp);
            playing.setVolume(volumes[0], volumes[1]);
            playing.setNextMediaPlayer(mp);
        } catch (Exception e) {
            mediaSoundListener.OnMediaSoundCompletion();
            return;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        String szWhat;
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                szWhat = "MEDIA_ERROR_UNKNOWN";
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                szWhat = "MEDIA_ERROR_SERVER_DIED";
                break;
            default:
                szWhat = "NOT DEFINED";
        }

        String szExtra;
        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO:
                szExtra = "MEDIA_ERROR_IO";
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                szExtra = "MEDIA_ERROR_MALFORMED";
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                szExtra = "MEDIA_ERROR_UNSUPPORTED";
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                szExtra = "MEDIA_ERROR_TIMED_OUT";
                break;
            default:
                szExtra = "NOT DEFINED";
        }

        Log.DEBUG( "Error: " + szWhat + ", " + szExtra + " and handle " + handle + " ...");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.DEBUG( "Prepared ...");
//        if (state == STATE_PLAYING)
//            mp.start();
    }


    /**
     * external Interface for MediaSound
     */

    public interface MediaSoundListener {
        void OnMediaSoundCompletion();
    }

    private MediaSoundListener mediaSoundListener;

    public void setOnMediaSoundCompletionListener(MediaSoundListener msl) {
        mediaSoundListener = msl;
    }

}