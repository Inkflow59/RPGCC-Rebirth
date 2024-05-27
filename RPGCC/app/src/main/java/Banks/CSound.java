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
//----------------------------------------------------------------------------------
//
// CSOUND : un echantillon
//
//----------------------------------------------------------------------------------
package Banks;

import android.media.SoundPool;
import android.os.Handler;

import Application.CRunApp;
import Application.CSoundPlayer;
import Runtime.Log;
import Runtime.MMFRuntime;


public class CSound {
    public CRunApp application;
    public final CSoundPlayer sPlayer;

    public short handle = -1;

    private long loadTime;

    public boolean bUninterruptible = false;

    public MediaSound mediaSound = null;

    private SoundPool mSoundPool;

    public int length;
    public int position;

    private int mode;
    public int soundID = -1;
    private int assignedChannel = -1;

    public int streamID = -1;

    public long streamAtStart;
    public long streamAtPause;
    public long streamDuration;
    public float streamRate;


    protected boolean bPaused;
    protected boolean bPlaying;
    protected boolean bGPaused;
    protected boolean bPrepared;
    protected boolean bLoaded;
    protected boolean bToPlay;

    public boolean ready;

    public int nLoops = 1;

    public float volume = 1.0f;
    public float pan = 0.0f;
    public float leftVolume = 1.0f;
    public float rightVolume = 1.0f;
    public int origFrequency = -1;
    public int frequency = -1;
    public String soundName;
    private final String filename;

    public int flags;
    private final Handler mHandlerOffPool;

    private final Runnable poolKiller = new Runnable() {

        @Override
        public void run() {
            tick();
            bPlaying = false;
        }

    };

    private final Runnable setPostKiller = new Runnable() {
        @Override
        public void run() {
            if (mHandlerOffPool.getLooper() != null) {
                mHandlerOffPool.removeCallbacks(poolKiller);
            }

            if (nLoops > 0)
                streamDuration = Math.round((1.0f / streamRate) * ((length * nLoops) + CSoundPlayer.STREAM_DELAY)); //Supporting rate less than 1
            else
                streamDuration = Integer.MAX_VALUE;

            mHandlerOffPool.postDelayed(poolKiller, streamDuration);
        }
    };

    ////////////////////////////////////////////////////////////////
    //
    //              SoundPool Listener for each sound
    //                 interface with SoundPlayer
    //
    ////////////////////////////////////////////////////////////////

    public final SoundPoolSounds.OnLoadCompleteCallback soundPoolLoadListener = new SoundPoolSounds.OnLoadCompleteCallback() {

//        CSound sound = null;

        @Override
        public void onLoadComplete(int handle, int soundId) {
            //Log_DEBUG("sampleId: "+soundId+" soundID: "+soundID+" and handle: "+handle);
            if (soundId == soundID) {
                Log.Log("Loaded Id: " + soundId + " and took: " + (System.currentTimeMillis() - loadTime) + " msecs");
                if (streamID <= 0 && !bLoaded && bToPlay) {
                    float[] volumes = getVolumes(volume);
                    bLoaded = true;
                    bToPlay = false;
                    streamRate = getRate();
                    sPlayer.runBg(() -> {
                        streamID = mSoundPool.play(soundID, volumes[0], volumes[1], 1,
                                nLoops <= 0 ? -1 : nLoops - 1, streamRate);
                        setPostKiller.run();
                        streamAtStart = (nLoops == 0 ? -1 : System.currentTimeMillis());
                        streamAtPause = (nLoops == 0 ? -1 : 0);

                        if (bPaused && streamID > 0)
                            mSoundPool.pause(streamID);
                    });
               }
            }
        }
    };

    public float[] getVolumes(float sVolume) {
        float[] volumes = new float[2];

        float volume = sPlayer.getVolume() * sVolume;
        float pan = sPlayer.getPan() + this.pan;

        if (pan != 0.0f) {
            float leftMod = 1.0f, rightMod = 1.0f;

            pan = Math.min(1.0f, Math.max(-1.0f, pan));

            if (pan >= 0.0f)
                leftMod = 1.0f - (pan);

            if (pan <= 0.0f)
                rightMod = 1.0f - (-pan);

            volumes[0] = (float) (volume * Math.pow(leftMod, 2.71));
            volumes[1] = (float) (volume * Math.pow(rightMod, 2.71));
        } else {
            volumes[0] = volume;
            volumes[1] = volume;
        }
        //Log_DEBUG("pan: "+pan +" pan left: "+leftMod+" pan right: "+rightMod);
        //Log_DEBUG("getVolume in: "+sVolume+" result->"+"left:"+volumes[0]+" right:"+volumes[1]);
        this.volume = sVolume;
        leftVolume = volumes[0];
        rightVolume = volumes[1];

        return volumes;
    }

    public float getRate() {
        if (frequency <= 0)
            return 1.0f;

        float rate = ((float) frequency) / ((float) origFrequency);

        if (rate < 0.5)
            rate = 0.5f;
        if (rate > 2.0)
            rate = 2.0f;
        return rate;
    }

    public void setVolume(float volume) {
        Log_DEBUG("Setting volume to : " + volume+" channel: "+(assignedChannel+1));
        this.volume = volume;
    }

    public void setPan(float pan) {
        Log_DEBUG("Setting pan to : " + pan+" channel: "+(assignedChannel+1));
        this.pan = pan;
    }

    public void setPitch(int frequency) {
        Log_DEBUG("Setting frequency to : " + frequency+" channel: "+(assignedChannel+1));
        this.frequency = frequency;
    }

    public void updatePan() {
        final float[] volumes = getVolumes(volume);
        Log_DEBUG("Updating pan - soundPool volume to : " + "left: " + volumes[0] + " right: " + volumes[1] + " on channel:" + (assignedChannel + 1));
        if (soundID == -1) {
            //Log_DEBUG("Updating media sound volume to : "+volume);
            if (mediaSound != null) {
                sPlayer.runBg(() -> mediaSound.setVolume(volumes));
            }
        } else {
            if ((flags & 0xFF) == 0) {
                sPlayer.runBg(() -> mSoundPool.setVolume(streamID, volumes[0], volumes[1]));
            }
        }
    }

    public void updatePitch() {
        if (soundID != -1) {
            if ((flags & 0xFF) == 0) {
                Log_DEBUG("Updating soundPool Pitch to : " + frequency);
                float newRate = getRate();
                sPlayer.runBg(() -> {
                    if (streamAtStart != -1)
                        streamDuration = (long) ((1.0f / streamRate) * ((streamAtStart - length * nLoops) + CSoundPlayer.STREAM_DELAY) / (newRate));
                    streamRate = newRate;
                    if (streamID != -1)
                        mSoundPool.setRate(streamID, newRate);
                    if (bPlaying)
                        setPostKiller.run();
                });
            }
        }
    }

    public void updateVolume(float volume) {
        final float[] volumes = getVolumes(volume);
        //Log_DEBUG("Updating volume - soundPool volume to : " + "left: " + volumes[0] + " right: " + volumes[1] + " on channel:" + (assignedChannel + 1));
        if (soundID == -1) {
            //Log_DEBUG("Updating media sound volume to : "+volume);
            if (mediaSound != null)
                sPlayer.runBg(() -> mediaSound.setVolume(volumes));
        } else {
            if ((flags & 0xFF) == 0 && bPlaying && streamID != -1) {
                sPlayer.runBg(() -> mSoundPool.setVolume(streamID, volumes[0], volumes[1]));
            }
        }

        //Log_DEBUG("Sound:"+this+" Volume [0]:"+volumes[0]+ " [1]:"+volumes[1]);

    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //
    //	CSound
    //
    /////////////////////////////////////////////////////////////////////////////////////////////
    public CSound(CSoundPlayer p, String name, short handle, int soundID, int frequency, int length, int flags) {
        sPlayer = p;

        this.soundID = soundID;
        this.ready = false;

        this.length = length;

        this.volume = 1.0f;
        this.pan = 0.0f;
        this.frequency = frequency;
        this.origFrequency = frequency;

        this.handle = handle;

        this.soundName = name;
        //Log_DEBUG("New Sound: " + this.soundName + ", freq: " + this.frequency);

        this.flags = flags;
        assignedChannel = -1;
        filename = null;

        mHandlerOffPool = new Handler();

        initSoundsFlags();
        //Log_DEBUG("New Sound in full");

    }

    public CSound(CSound sound) {
        sPlayer = sound.sPlayer;

        nLoops = sound.nLoops;

        soundID = sound.soundID;
        ready = sound.ready;

        volume = sound.volume;
        pan = sound.pan;
        frequency = sound.frequency;
        origFrequency = sound.origFrequency;
        length = sound.length;
        soundName = sound.soundName;

        flags = sound.flags;
        bUninterruptible = sound.bUninterruptible;
        handle = sound.handle;
        loadTime = sound.loadTime;
        mode = sound.mode;
        assignedChannel = -1;
        filename = null;

        mHandlerOffPool = new Handler();

        initSoundsFlags();
        //Log_DEBUG("New Sound from copy");

    }

    public CSound(CSoundPlayer p, String filename) {
        sPlayer = p;
        this.filename = filename;

        mHandlerOffPool = new Handler();

        initSoundsFlags();
    }

    public int load(short h, int priority) {
        handle = h;
        loadTime = System.currentTimeMillis();
        soundID = sPlayer.soundPoolSound.load(handle, priority, soundPoolLoadListener);
        Log_DEBUG("SoundPool load handle: " + handle + " and ID: " + soundID);

        return soundID;
    }

    public void load(short h, CRunApp app) {
        handle = h;
        application = app;
        mode = 0;
        if (MMFRuntime.inst.assetsAvailable) {
            mode = MediaSound.ASSET_MODE;
        } else if (MMFRuntime.inst.obbAvailable) {
            mode = MediaSound.OBB_MODE;
        }
    }

    private void setPreparedAndLoaded() {
        bPrepared = true;
        bLoaded = true;
    }

    private void initSoundsFlags() {
        bPaused = true;
        bGPaused = false;
        bPrepared = false;
        bLoaded = false;
        bPlaying = false;

        mSoundPool = sPlayer.soundPoolSound.getSoundPool();

    }

    public void resetSoundPool() {
        mSoundPool = sPlayer.soundPoolSound.getSoundPool();
    }

    private void removeSound() {
        sPlayer.removeChannel(assignedChannel);
    }

    public void setLoopCount(int count) {
        this.nLoops = Math.max(count, 0);
    }

    public void setUninterruptible(boolean bFlag) {
        this.bUninterruptible = bFlag;
    }

    public void DoUpdateStreamId(int streamId) {
        streamID = streamId;
        bLoaded = true;
        bPrepared = true;
        if (streamID <= 0) {
            bToPlay = true;
            bLoaded = false;
            sPlayer.soundPoolSound.setOnLoadListener(handle, soundPoolLoadListener);
        }
        //Log_DEBUG("Updating streamId bPlaying: "+bPlaying+" toPlay: "+bToPlay+" bLoaded:"+bLoaded);
    }

    public void DoUpdateSoundId(int sndId) {
        soundID = sndId;
    }

    public void start(int channel) {
        assignedChannel = channel;
        bPrepared = false;
        bToPlay = false;

        float[] volumes = getVolumes(volume);
        if (soundID == -1) {
            Log_DEBUG("MediaSound " + soundName + " with handle: " + handle + " on channel " + (assignedChannel + 1));
            mediaSound = new MediaSound(sPlayer.attributes, sPlayer.audioFocusFusion, mode);
            setPreparedAndLoaded();
            if (mediaSound != null) {
                mediaSound.setOnMediaSoundCompletionListener(() -> {
                    //Log_DEBUG("On completion ...");
                    removeSound();
                    bPlaying = false;
                });
                sPlayer.runBg(() -> {
                    mediaSound.setSoundSettings(handle, filename, volumes, nLoops);
                    mediaSound.start();
                    sPlayer.maxMediaSound = Math.max(sPlayer.maxMediaSound, ++sPlayer.actMediaSound);
                });
            } else
                bPrepared = false;
        } else {
                //long start = System.nanoTime();
            streamRate = getRate();
            sPlayer.runBg(() -> {
                int streamId = mSoundPool.play(soundID, volumes[0], volumes[1], 1, nLoops <= 0 ? -1 : nLoops - 1, streamRate);
                DoUpdateStreamId(streamId);
                setPostKiller.run();
                if (frequency != origFrequency)
                    updatePitch();
                Log_DEBUG("Sound in channel #:" + (assignedChannel + 1) + " is handle: " + handle + " had a streamID: " + streamId + " and soundID: " + soundID);

                //Log_DEBUG("duration set: "+streamDuration+" Rate: "+streamRate+" with a frequency: "+frequency+ " and original of: "+origFrequency);
                streamAtStart = (nLoops == 0 ? -1 : System.currentTimeMillis());
                streamAtPause = (nLoops == 0 ? -1 : 0);

                sPlayer.maxSoundPool = Math.max(sPlayer.maxSoundPool, ++sPlayer.actSoundPool);
            });
        }
        bPaused = false;
        bPlaying = true;
        //Log_DEBUG("Starting a sound with name:"+soundName+" handle:"+handle+" in channel: "+assignedChannel);
    }

    public void stop() {
        bPlaying = false;
        sPlayer.runBg(() -> {
            if (soundID == -1) {
                if (mediaSound != null) {
                    mediaSound.stop();
                    Log_DEBUG("Stopping ...");
                    bPrepared = false;
                    sPlayer.actMediaSound--;
                }
            } else {
                if ((flags & 0xFF) == 0) {
                    mHandlerOffPool.removeCallbacks(poolKiller);
                    Log_DEBUG("Stopping streamID: " + streamID);
                    if (streamID != -1)
                        mSoundPool.stop(streamID);
                    sPlayer.actSoundPool--;
                    //Log_DEBUG("Sound:" + this + " stream ID: " + streamID + " Stopping sound ID: " + soundID + " in Channel #: " + assignedChannel);
                }
            }
        });
        //Log_DEBUG("stopping channel:"+assignedChannel+" ...");
    }

    public boolean isPlaying() {
        //Log_DEBUG("Checking if channnel: "+(assignedChannel+1)+" playing: "+bPlaying);
        return bPlaying;
    }

    public boolean isPaused() {
        return bPaused;
    }

    public void pause() {
        if (bPaused)
            return;
        sPlayer.runBg(() -> {
            if (soundID == -1) {
                if (mediaSound != null && bLoaded) {
                    mediaSound.pause();
                }
            } else {
                if ((flags & 0xFF) == 0) {
                    if (streamID != -1)
                        mSoundPool.pause(streamID);
                    if (streamAtStart != -1) {
                        streamAtPause = (nLoops == 0 ? -1 : System.currentTimeMillis());
                    }
                    mHandlerOffPool.removeCallbacks(poolKiller);
                }
            }
        });
        bPaused = true;
        //Log_DEBUG("pausing channel:"+assignedChannel+" ...");
    }

    public void resume() {
        if (!bPaused)
            return;

        if (soundID == -1) {
            if (mediaSound != null && bLoaded) {
                final float[] volumes = getVolumes(volume);
                sPlayer.runBg(() -> {
                    mediaSound.setVolume(volumes);
                    mediaSound.play();
                });
                //Log.d("MMFRuntime","resumed handle: "+this.handle);
            }
        } else {
            if ((flags & 0xFF) == 0) {
                if (streamID != -1)
                    sPlayer.runBg(() -> mSoundPool.resume(streamID));

                if (mHandlerOffPool.getLooper() != null && streamAtPause != -1) {
                    mHandlerOffPool.postDelayed(poolKiller, streamDuration - (streamAtPause - streamAtStart));
                }
            }
        }

        bPaused = false;
        //Log_DEBUG(resuming channel:"+assignedChannel+" ...");
    }

    // Runtime Pause
    public void pause2() {
        if (soundID == -1) {
            if (mediaSound != null && mediaSound.isPlaying()) {
                sPlayer.runBg(() -> mediaSound.pause());
            }
        } else {
            if ((flags & 0xFF) == 0) {
                if (streamID != -1)
                    sPlayer.runBg(() -> mSoundPool.pause(streamID));
                if (mHandlerOffPool.getLooper() != null && streamAtStart != -1) {
                    mHandlerOffPool.removeCallbacks(poolKiller);
                    streamAtPause = (nLoops == 0 ? -1 : System.currentTimeMillis());
                }
            }
        }
        bGPaused = true;
        Log_DEBUG("pausing 2 ...");
    }

    // Runtime Resume
    public void resume2() {
        if (!bGPaused)
            return;
        if (soundID == -1) {
            if (mediaSound != null) {

                if (bLoaded) {
                    final float[] volumes = getVolumes(volume);
                    sPlayer.runBg(() -> {
                        mediaSound.setVolume(volumes);
                        mediaSound.play();
                    });
                }
            }
        } else {
            if ((flags & 0xFF) == 0) {
                if (!bPaused)
                    if (streamID != -1)
                        sPlayer.runBg(() -> mSoundPool.resume(streamID));

                if (mHandlerOffPool.getLooper() != null && streamAtPause != -1) {
                    mHandlerOffPool.postDelayed(poolKiller, streamDuration - (streamAtPause - streamAtStart));
                }

            }
        }
        bGPaused = false;
        Log_DEBUG("resuming 2 ...");
    }

    public int getDuration() {
        if (soundID == -1) {
            if (mediaSound == null || !bPrepared)
                return length;

            return mediaSound.getDuration();
        } else {
            return length;
        }
    }

    public void setPosition(int position) {
        sPlayer.runBg(() -> {
            if (soundID == -1) {
                this.position = -1;
                try {
                    if (mediaSound != null) {
                        if (bPrepared) {
                            mediaSound.seekTo(position);
                        } else
                            this.position = position;
                    }
                } catch (IllegalStateException e) {
                    Log_DEBUG("Set position but " + e);
                }
            }
        });
    }

    public int getPosition() {
        if (soundID == -1) {
            try {
                if (mediaSound != null && bPrepared)
                    return mediaSound.getCurrentPosition();
            } catch (IllegalStateException e) {
                Log_DEBUG("Checking the position but " + e);
            }
        } else {
            if ((flags & 0xFF) == 0)
                return -1;
        }

        return 0;
    }

    public void release() {

        sPlayer.runBg(() -> {
            if (mediaSound != null) {
                Log_DEBUG("Release " + soundName + " ...");
                mediaSound.stop();
                sPlayer.actMediaSound--;
                mediaSound.release();
                mediaSound = null;
            } else {
                if ((flags & 0xFF) == 0) {
                    mHandlerOffPool.removeCallbacks(poolKiller);
                    Log_DEBUG("Releasing streamID: " + streamID);
                    if (streamID != -1)
                        mSoundPool.stop(streamID);
                    streamID = -1;
                }

            }
        });

        soundName = null;

        bPaused = false;
        bGPaused = false;
        bPrepared = false;
        bPlaying = false;
    }

    public void tick() {
        if ((flags & 0xFF) == 0 && streamID != -1 && streamAtStart != -1) {
            streamAtStart = -1;
            Log_DEBUG("sound was tick and remove soundID: " + soundID + "...");
            sPlayer.removeChannel(assignedChannel);
        }
    }

    private void Log_DEBUG(String s)
    {
        if(MMFRuntime.inst.DEBUG)
            Log.Log(s);
    }
}
