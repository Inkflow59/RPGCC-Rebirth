package Banks;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import Application.CRunApp;
import Runtime.Log;
import Runtime.MMFRuntime;

public class SoundPoolSounds {

    private SoundPool mSoundPool;

    private CRunApp app;
    private int nSounds;
    private long startPreLoad;

    private Map<Integer, Integer> mHandleToSoundIdMap;
    private static Map<Integer, OnLoadCompleteCallback> mHandleToCallbackMap;

    public SoundPoolSounds(CRunApp a)
    {
        app = a;
    }
    @SuppressLint("NewApi")
    @TargetApi(21)
    protected void createSoundPoolWithBuilder(AudioAttributes attributes, int max_stream) {
        mSoundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(max_stream).build();
    }

    @SuppressWarnings("deprecation")
    protected void createSoundPoolWithConstructor(int max_stream) {
        mSoundPool = new SoundPool(max_stream, AudioManager.STREAM_MUSIC, 0);
    }

    public SoundPool getSoundPool() {
        return mSoundPool;
    }

    public void startPoolSound(AudioAttributes attributes, int max_sounds) {

        if (mSoundPool != null)
            mSoundPool.release();

        if (Build.VERSION.SDK_INT >= 21) {
            createSoundPoolWithBuilder(attributes, max_sounds);
        } else {
            createSoundPoolWithConstructor(max_sounds);
        }
        if (mSoundPool == null) {
            throw new RuntimeException("Audio: failed to create SoundPool");
        }

        mHandleToSoundIdMap = new HashMap<>(max_sounds);
        mHandleToCallbackMap = new HashMap<>(max_sounds);

        nSounds = 0;
        app.allSoundsPreLoaded = false;

        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int soundId, int status) {
                //Log.Log("OnLoadComplete --> sampleId: " + soundId + " status: " + status + " ...");
                if (status != 0) {
                    Log.Log("A preload sound with ID: "+soundId+" failed to preload ...");
                    return;
                }

                if (!app.WAIT4ALL) {
                    int handle = toHandle(soundId);
                    if (handle == -1)
                        return;
                    OnLoadCompleteCallback callback = mHandleToCallbackMap.get(handle);

                    if (callback != null) {
                        callback.onLoadComplete(handle, soundId);
                        mHandleToCallbackMap.remove(handle);
                    }
                }
                else {
                    nSounds--;
                    if (nSounds <= 0) {
                        app.allSoundsPreLoaded = true;
                        if(MMFRuntime.inst.app != null && MMFRuntime.inst.app.frame != null)
                            Log.Log("All loaded in frame: " + MMFRuntime.inst.app.frame.frame_number + " and took: " + (System.currentTimeMillis() - startPreLoad) + " (msecs) ...");
                    }
                }
            }
        });

    }

    public void setStartTime()
    {
        startPreLoad = System.currentTimeMillis();
    }

    public void initPreLoad(int pendingSounds)
    {
        if(app.WAIT4ALL && pendingSounds != 0)
            app.allSoundsPreLoaded = false;
        else
            app.allSoundsPreLoaded = true;

        nSounds = pendingSounds;

        if(app.WAIT4ALL && pendingSounds == 0)
            Log.Log("All loaded in frame: "+MMFRuntime.inst.app.frame.frame_number+" and took: "+(System.currentTimeMillis()-startPreLoad)+" (msecs) ...");
    }

    public int load(int handle, int priority, OnLoadCompleteCallback callback) {
        AssetFileDescriptor fd = null;
        int soundId = -1;
        try {
            if (MMFRuntime.inst.assetsAvailable)
                fd = MMFRuntime.inst.getSoundFromAssets(String.format(Locale.US, "s%04d", handle));
            else if (MMFRuntime.inst.obbAvailable)
                fd = MMFRuntime.inst.getSoundFromOBB(String.format(Locale.US, "res/raw/s%04d", handle));

            if (fd == null)
                fd = MMFRuntime.inst.getResources().openRawResourceFd(MMFRuntime.inst.getResourceID(String.format(Locale.US, "raw/s%04d", handle)));

            soundId = mSoundPool.load(fd, priority);
            fd.close();

            mHandleToSoundIdMap.put(handle, soundId);
            if(!app.WAIT4ALL) {
                if (callback != null) {
                    mHandleToCallbackMap.put(handle, callback);
                }
            }

        } catch (Throwable t) {
            Log.Log("Error during load ...");
        }
        return soundId;
    }

    public void setOnLoadListener(int handle, OnLoadCompleteCallback callback)
    {
        mHandleToCallbackMap.put(handle, callback);
    }
    public void unload(int soundID) {
        if (mSoundPool == null)
            return;
        mSoundPool.unload(soundID);
    }

    public void release() {
        if (mSoundPool != null)
            mSoundPool.release();
        mSoundPool = null;
    }

    private int toHandle(int soundId) {
        for(Map.Entry<Integer, Integer> handleId: mHandleToSoundIdMap.entrySet())
        {
            if(handleId.getValue() == soundId)
                return handleId.getKey();
        }
        return -1;
    }
    /////////////////////////////
    //
    //     SoundPool Listeners
    //
    /////////////////////////////
    public interface OnLoadCompleteCallback {
        void onLoadComplete(int handle, int soundId);
    }

}
