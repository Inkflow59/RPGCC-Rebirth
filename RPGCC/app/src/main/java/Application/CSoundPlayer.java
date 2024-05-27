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
// CSOUNDPLAYER : synthetiseur MIDI
//
//----------------------------------------------------------------------------------
package Application;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Banks.CSound;
import Banks.CSoundChannel;
import Banks.SoundPoolSounds;
import Runtime.Log;
import Runtime.MMFRuntime;


public class CSoundPlayer
{
	public static final int SNDF_LOADONCALL=0x0010;
	public static final int SNDF_PLAYFROMDISK=0x0020;
	public static final int STREAM_DELAY=10;

	public static final int NCHANNELS = 48;
	public CRunApp app;
	boolean bOn = true;
	protected CSoundChannel[] channels;
	public boolean bMultipleSounds = true;
	private float mainVolume = 1.0f;
	private float mainPan = 0.0f;

	protected AudioManager audioMgr;
	private boolean AudioFocus;

	public AudioAttributes attributes;

	public int maxSoundPool;
	public int maxMediaSound;
	public int actSoundPool;
	public int actMediaSound;

	public SoundPoolSounds soundPoolSound;
	public AudioFocusFusion audioFocusFusion;

	protected final ExecutorService threadPool= new ThreadPoolExecutor(1, 48, 90, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

	public void runBg(Runnable block) {
		try {
			threadPool.execute(block);
		}
		catch(Exception e)
		{
			Log.Log("Rejected ...");
		}
	}

	private boolean runBgBoolean(Callable<Boolean> block)
	{
		try {
			return threadPool.submit(block).get();
		}
		catch(InterruptedException | ExecutionException e)
		{
			return false;
		}
	}

	private int runBgInteger(Callable<Integer> block)
	{
		try {
			return threadPool.submit(block).get();
		}
		catch(InterruptedException | ExecutionException e)
		{
			return 0;
		}
	}

	private float runBgFloat(Callable<Float> block)
	{
		try {
			return threadPool.submit(block).get();
		}
		catch(InterruptedException | ExecutionException e)
		{
			return 0.0f;
		}
	}


	public void setVolume (float volume)
	{
		if (volume > 1.0f)
			volume = 1.0f;

		if (volume < 0.0f)
			volume = 0.0f;

		if(volume == this.mainVolume)
			return;

		this.mainVolume = volume;

		for (CSoundChannel channel : channels) {
			if (channel.currentSound != null) {
				channel.currentSound.updateVolume(channel.currentSound.volume);
			}
		}
	}

	public float getVolume ()
	{
		return this.mainVolume;
	}

	public void setPan (float pan)
	{
		if (pan > 1.0f)
			pan = 1.0f;

		if (pan < -1.0f)
			pan = -1.0f;

		this.mainPan = pan;

		for (CSoundChannel channel : channels)
			if (channel.currentSound != null)
				channel.currentSound.updatePan();
	}

	public float getPan()
	{
		return this.mainPan;
	}

	public CSoundPlayer(CRunApp a)
	{
		app = a;
		boolean hasLowLatencyFeature =
				MMFRuntime.inst.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);
		boolean hasAudioProFeature = false;
		if(Build.VERSION.SDK_INT >= 23)
			hasAudioProFeature = MMFRuntime.inst.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO);
		Log.Log("Device has Low latency: "+(hasLowLatencyFeature?"yes":"no"));
		Log.Log("Device has AudioPro: "+(hasAudioProFeature ? "yes":"no"));

		String packageCodePath = MMFRuntime.inst.getPackageCodePath();
		Log.Log("Package code path: "+packageCodePath);

		attributes = null;
		if (Build.VERSION.SDK_INT >= 21) {
			attributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_GAME)
					.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					.setLegacyStreamType(AudioManager.STREAM_MUSIC)
					.build();
		}

		if(audioMgr == null)
			audioMgr = (AudioManager) MMFRuntime.inst.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

		if (Build.VERSION.SDK_INT >= 17)
		{
			Log.Log("Sample rate for device: "+audioMgr.getProperty("android.media.property.OUTPUT_SAMPLE_RATE"));
		}
		else
			audioMgr.setParameters("android.media.property.OUTPUT_SAMPLE_RATE=44100");

		//audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC), 0);

		audioFocusFusion = new AudioFocusFusion(MMFRuntime.inst, attributes);
		AudioFocusFusion.AudioFocusListener audioflistener = result -> AudioFocus = result;
		audioFocusFusion.setAudioFocusListener(audioflistener);

		AudioFocus = audioFocusFusion.getAudioFocus();
		if(!AudioFocus)
			AudioFocus = audioFocusFusion.requestAudioFocus();

		soundPoolSound = new SoundPoolSounds(app);

		channels = new CSoundChannel[NCHANNELS];

		for (int i = 0; i < channels.length; ++ i)
			channels [i] = new CSoundChannel();

		this.getVolume();

	}

	/** Plays a simple sound.
	 Note: _volume, _pan and _freq are valid if _volume != -1
	 */
	public void play(short handle, int nLoops, int channel, boolean bPrio, int _volume, int _pan, int _freq)
	{
		int n;
		boolean withChannel = (channel != -1 ? true : false);

		if (bOn == false)
		{
			return;
		}

		CSound sound = app.soundBank.newSoundFromHandle(handle);
		//Log.Log("sound is created? "+(sound == null ? "no":"yes"));
		if (sound == null)
		{
			return;
		}
		if (bMultipleSounds == false)
		{
			channel = 0;
		}

		/* UNINTERRUPTABLE - This option means that the sound cannot be interrupted by a sound that hasn't this option.
		 * A sound can be interrupted by another one in 2 cases:
		 * 		(1) when you play the sound on a specific channel and another one is already playing on this channel, or
		 * 		(2) when you play a sound without specifying a channel and there is no free channel available.
		 * 			(a) If the playing sound has the option and the new sound hasn't the option, the new sound won't be played.
		 * 			(b) The sound will be played on the first channel whose sound hasn't this option.
		 */

		// Lance le son
		if (channel < 0)
		{
			for (n = 0; n < NCHANNELS; n++)
				if (channels[n].currentSound == null && !channels[n].locked)
					break;

			if (n == NCHANNELS)
			{
				// Stoppe le son sur un canal deja en route
				for (n = 0; n < NCHANNELS; n++) // Rule #2 part (b)
					if(!channels [n].locked && channels[n].stop (false)) // Rule #2 part (a) && channel locked
						break;
			}

			channel = n;	// assigned channel for Rule #2
		}

		// Rule #1
		if (channel < 0 || channel >= NCHANNELS || !channels [channel].stop(bPrio))
			return;

		if(channels[channel].currentSound != null)
			channels[channel].currentSound.release();

		sound.setUninterruptible(bPrio);
		sound.setLoopCount(nLoops);

		channels[channel].currentSound = sound;

		if (_volume != -1)
		{
			channels[channel].volume = (float)_volume / 100.0f;
			channels[channel].pan = (float)_pan / 100.0f;
			channels[channel].frequency = _freq;
		}
		else if (!withChannel)
		{
			channels[channel].volume = 1.0f;
			channels[channel].pan = 0.0f;
			channels[channel].frequency = -1;
		}

		channels[channel].currentSound.setVolume(channels[channel].volume);
		channels[channel].currentSound.setPan(channels[channel].pan);
		if(channels[channel].frequency > 0)
			channels[channel].currentSound.setPitch(channels[channel].frequency);

		if(!AudioFocus)
			AudioFocus = audioFocusFusion.requestAudioFocus();

		if(AudioFocus)
			channels[channel].currentSound.start(channel);

	}

	public void playFile(String filename, int nLoops, int channel, boolean bPrio, int _volume, int _pan, int _freq)
	{
		int n;
		boolean withChannel = (channel != -1 ? true : false);

		if (bOn == false)
		{
			return;
		}

		CSound sound;

		try
		{
			sound = new CSound(this, filename);
		}
		catch(Throwable e)
		{
			return;
		}

		if (bMultipleSounds == false)
		{
			channel = 0;
		}

		// Lance le son
		if (channel < 0)
		{
			for (n = 0; n < NCHANNELS; n++)
				if (channels[n].currentSound == null && !channels [n].locked)
					break;

			if (n == NCHANNELS)
			{
				// Stoppe le son sur un canal deja en route
				for (n = 0; n < NCHANNELS; n++)
					if (!channels [n].locked && channels[n].stop (false))
						break;
			}
			channel = n;
		}

		if (channel < 0 || channel >= NCHANNELS || !channels [channel].stop(false))
			return;

		channels[channel].currentSound = sound;
		sound.setUninterruptible(bPrio);
		sound.setLoopCount(nLoops);

		if (_volume != -1) {
			channels[channel].volume = (float) _volume / 100.0f;
			channels[channel].pan = (float) _pan / 100.0f;
			channels[channel].frequency = _freq;
		} else if (!withChannel) {
			channels[channel].volume = 1.0f;
			channels[channel].pan = 0.0f;
			channels[channel].frequency = -1;
		}

		channels[channel].currentSound.setVolume(channels[channel].volume);
		channels[channel].currentSound.setPan(channels[channel].pan);
		if(channels[channel].frequency > 0)
			channels[channel].currentSound.setPitch(channels[channel].frequency);

		if(!AudioFocus)
			AudioFocus = audioFocusFusion.requestAudioFocus();

		if(AudioFocus)
			channels[channel].currentSound.start(channel);

	}

	public void setMultipleSounds(boolean bMultiple)
	{
		bMultipleSounds = bMultiple;
	}

	public void stopAllSounds()
	{
		//Log.DEBUG("stopping all sounds ...");
		for (CSoundChannel channel : channels) {
			if (channel.currentSound != null) {
				channel.stop(true);
			}
		}
	}

	public void release()
	{
		if(soundPoolSound != null)
			soundPoolSound.release();
		if(audioFocusFusion != null) {
			audioFocusFusion.abandonAudioFocus();
			audioFocusFusion.dispose();
		}
	}

	public void stop(short handle)
	{
		for (CSoundChannel channel : channels) {
			if (channel.currentSound != null && channel.currentSound.handle == handle) {
				channel.stop(true);
			}
		}
	}

	public boolean isSoundPlaying()
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				if (channels[n].currentSound.isPlaying())
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean isSamplePlaying(short handle)
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				if (channels[n].currentSound.handle == handle)
				{
					if (channels[n].currentSound.isPlaying())
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isSamplePaused(short handle)
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				if (channels[n].currentSound.handle == handle)
				{
					if (channels[n].currentSound.isPaused())
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isChannelPlaying(int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			if (channels[channel].currentSound != null)
			{
				return channels[channel].currentSound.isPlaying();
			}
		}
		return false;
	}

	public boolean isChannelPaused(int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			if (channels[channel].currentSound != null)
			{
				return channels[channel].currentSound.isPaused();
			}
		}
		return false;
	}

	public void pause(short handle)
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				if (channels[n].currentSound.handle == handle)
				{
					channels[n].currentSound.pause();
				}
			}
		}
	}

	public void resume(short handle)
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				if (channels[n].currentSound.handle == handle)
				{
					channels[n].currentSound.resume();
				}
			}
		}
	}

	public void pause()
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				channels[n].currentSound.pause();
			}
		}
	}

	// Runtime Pause
	public void pause2()
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				channels[n].currentSound.pause2();
			}
		}
		if(AudioFocus) {
			audioFocusFusion.abandonAudioFocus();
			AudioFocus = false;
		}
	}

	public void pauseAllChannels()
	{
		//Log.DEBUG("pausing all sounds ...");
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				channels[n].currentSound.pause();
			}
		}
	}

	public void resume()
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				channels[n].currentSound.resume();
			}
		}
	}

	// Runtime Resume
	public void resume2()
	{
		int n;
		if(!AudioFocus)
			AudioFocus = audioFocusFusion.requestAudioFocus();

		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				channels[n].currentSound.resume2();
			}
		}
	}

	public void resumeAllChannels()
	{
		//Log.DEBUG("resuming all sounds ...");
		AudioFocus = audioFocusFusion.requestAudioFocus();
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				channels[n].currentSound.resume();
			}
		}
	}

	public void pauseChannel(int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			if (channels[channel].currentSound != null)
			{
				channels[channel].currentSound.pause();
			}
		}
	}

	public void stopChannel(int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
			channels[channel].stop(true);
	}

	public void resumeChannel(int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			if (channels[channel].currentSound != null)
			{
				channels[channel].currentSound.resume();
			}
		}
	}

	public int getChannel(String name)
	{
		for (int n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null)
			{
				if (name.contentEquals(channels[n].currentSound.soundName))
				{
					return n;
				}
			}
		}
		return -1;
	}

	public String getChannelSampleName (int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			if (channels[channel].currentSound != null && (channels[channel].currentSound.isPlaying() || channels[channel].currentSound.isPaused()) )
			{
				return channels[channel].currentSound.soundName;
			}
		}
		return "";
	}

	public int getChannelDuration(int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			if (channels[channel].currentSound != null)
			{
				return channels[channel].currentSound.getDuration();
			}
		}
		return 0;
	}

	public int getSampleDuration(String name)
	{
		int channel = getChannel(name);
		if (channel >= 0)
		{
			return channels[channel].currentSound.getDuration();
		}
		return 0;
	}

	public void setPositionChannel(int channel, int pos)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			if (channels[channel].currentSound != null)
			{
				channels[channel].currentSound.setPosition(pos);
			}
		}
	}

	public int getPositionChannel(int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			if (channels[channel].currentSound != null)
			{
				return channels[channel].currentSound.getPosition();
			}
		}
		return 0;
	}

	public int getSamplePosition(String name)
	{
		int channel = getChannel(name);
		if (channel >= 0)
		{
			return channels[channel].currentSound.getPosition();
		}
		return 0;
	}

	public void setFrequencyChannel (int channel, int frequency)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			channels[channel].frequency = frequency;

			if (channels[channel].currentSound != null)
			{
				channels[channel].currentSound.frequency = frequency;
				channels[channel].currentSound.updatePitch();
			}
		}
	}

	public void setVolumeChannel (int channel, float volume)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			volume = Math.max(0, Math.min(1.0f, volume));
			channels[channel].volume = volume;

			if (channels[channel].currentSound != null)
			{
				channels[channel].currentSound.updateVolume (channels[channel].volume);
			}
		}
	}

	public float getVolumeChannel (int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			return  channels[channel].volume;
		}
		return 0;
	}

	public float getSampleVolume(String name)
	{
		int channel = getChannel(name);
		if (channel >= 0)
		{
			return channels[channel].currentSound.volume;
		}
		return 0;
	}

	public void setPanChannel (int channel, float pan)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			pan = Math.max(-1.0f, Math.min(1.0f, pan));
			channels[channel].pan = pan;

			if (channels[channel].currentSound != null)
			{
				channels[channel].currentSound.pan = pan;
				channels[channel].currentSound.updatePan();
			}
		}
	}

	public float getPanChannel (int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			return channels[channel].pan;
		}
		return 0;
	}

	public float getSamplePan(String name)
	{
		int channel = getChannel(name);
		if (channel >= 0)
		{
			if(channels[channel].currentSound != null)
				return channels[channel].currentSound.pan;
			else
				return channels[channel].pan;
		}
		return 0;
	}

	public void setPosition(short handle, int pos)
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null && channels[n].currentSound.handle == handle)
			{
				channels[n].currentSound.setPosition(pos);
			}
		}
	}

	public void setVolume(short handle, float volume)
	{
		float minValue = 0.0f;
		float maxValue = 1.0f;
		float clampedVolume = Math.max(minValue, Math.min(maxValue, volume));

		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null && channels[n].currentSound.handle == handle)
			{
				channels[n].currentSound.volume = clampedVolume;
				channels[n].currentSound.updateVolume(clampedVolume);
			}
		}
	}

	public void setFrequency (short handle, int frequency)
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null && channels[n].currentSound.handle == handle) {
				channels[n].currentSound.frequency = frequency;
				channels[n].frequency = frequency;
				channels[n].currentSound.updatePitch();
			}
		}
	}

	public int getFrequency (int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			if(channels[channel].frequency > 0)
				return channels[channel].frequency;
			else if(channels[channel].currentSound != null)
				return channels[channel].currentSound.frequency;
		}
		return 0;
	}

	public int getSampleFrequency (String name)
	{
		int channel = getChannel(name);
		if (channel >= 0)
		{
			if(channels[channel].currentSound != null)
				return channels[channel].currentSound.frequency;
		}
		return 0;
	}

	public void setPan (short handle, float pan)
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound != null && channels[n].currentSound.handle == handle)
			{
				pan = Math.max(-1.0f, Math.min(1.0f, pan));
				channels[n].currentSound.pan = pan;
				channels[n].currentSound.updatePan();
			}
		}
	}

	public void removeSound(CSound sound)
	{
		int n;
		for (n = 0; n < NCHANNELS; n++)
		{
			if (channels[n].currentSound == sound)
			{
				channels[n].stop(true);
				break;
			}
		}
	}

	public void removeChannel(int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			channels[channel].stop(true);
		}
	}

	public void lockChannel(int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			channels[channel].locked = true;
		}
	}

	public void unlockChannel(int channel)
	{
		if (channel >= 0 && channel < NCHANNELS)
		{
			channels[channel].locked = false;
		}
	}

	public void tick ()
	{
		for (int i = 0; i < NCHANNELS; ++ i)
		{
			if (channels[i].currentSound != null)
				channels[i].currentSound.tick();
		}
	}

	public void dispose ()
	{
		threadPool.shutdown();
	}
}

