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
// CMUSICBANK : Stockage des musiques
//
//----------------------------------------------------------------------------------
package Banks;

import Application.CRunApp;
import Application.CSoundPlayer;
import Runtime.Log;
import Runtime.MMFRuntime;
import Services.CFile;

public class CSoundBank implements IEnum
{
	public CSound sounds[] = null;
	public int nHandlesReel;
	public int nHandlesTotal;

	public int handleToLength [];
	public int handleToFrequency [];
	public int handleToSoundID [];
	public short handleToFlags [];
	public String handleToSoundName [];
	public boolean handleIsReady [];

	short useCount[];
	public static CSoundPlayer sPlayer;

	public CSoundBank(CSoundPlayer p)
	{
		sPlayer=p;
	}

	public void preLoad(CFile f)
	{
		// Nombre de handles
		nHandlesReel = f.readAShort();

		Log.Log ("nHandlesReel: " + nHandlesReel);

		// WTF! Why not an object? O_o
		handleToLength = new int [nHandlesReel];
		handleToFrequency = new int [nHandlesReel];
		handleToFlags = new short [nHandlesReel];
		handleToSoundID = new int [nHandlesReel];
		handleIsReady = new boolean [nHandlesReel];
		//handleTime = new long [nHandlesReel];
		handleToSoundName = new String [nHandlesReel];

		int numberOfSounds = f.readAShort ();

		Log.Log ("numberOfSounds: " + numberOfSounds);

		int max_sounds = 0;

		for (int i = 0; i < numberOfSounds; ++ i)
		{
			int handle = f.readAShort ();

			handleToFlags[handle] = f.readAShort ();
			handleToLength [handle] = f.readAInt ();
			handleToFrequency [handle] = f.readAInt ();
			if ( (handleToFlags[handle] & 0x100) != 0 )				// SNDF_HASNAME
			{
				int l = f.readAShort();
				handleToSoundName [handle] = f.readAString(l);
			}

			if((handleToFlags[handle] & 0xFF & (CSoundPlayer.SNDF_LOADONCALL | CSoundPlayer.SNDF_PLAYFROMDISK)) == 0)
			{
				max_sounds++;
			}

			handleToSoundID [handle] = -1;
			handleIsReady [handle] = false;
		}

		useCount = new short[nHandlesReel];

		if ((sPlayer.app.gaNewFlags & CRunApp.GANF_SAMPLESOVERFRAMES) != 0) {
			Log.DEBUG("Max sounds over frame detected: "+max_sounds);
			sPlayer.soundPoolSound.startPoolSound(sPlayer.attributes, 24);
		}

		resetToLoad();
		sounds = null;
	}

	public CSound newSoundFromHandle(short handle)
	{
		if (sounds != null)
		{
			if (handle >= 0 && handle < nHandlesReel)
			{
				if (sounds[handle] != null)
				{
					CSound sound = sounds[handle];
					return new CSound (sound);
				}
			}
		}
		return null;
	}

	public int getSoundHandleFromName(String soundName)
	{
		if (sounds != null)
		{
			int h;
			for (h = 0; h < nHandlesReel; h++)
			{
				if (handleToSoundName[h] != null)
				{
					if (handleToSoundName[h].equalsIgnoreCase(soundName))
						return h;
				}
			}
		}
		return -1;
	}

	public void resetToLoad() {
		int n;
		for (n = 0; n < nHandlesReel; n++) {
			useCount[n] = 0;
		}
	}

	public void setToLoad(short handle)
	{
		useCount[handle]++;
	}

	// Entree enumeration
	@Override
	public short enumerate(short num)
	{
		setToLoad(num);
		return -1;
	}

	public void load(final CRunApp app) {
		int h;
		// Combien de musiques?
		int max_sounds = 0;

		for (h = 0; h < nHandlesReel; h++) {
			// Check if no sound over frame, remove the sound, it will ve created inside a new SoundPool
			if ((sPlayer.app.gaNewFlags & CRunApp.GANF_SAMPLESOVERFRAMES) == 0) {
				if (handleToSoundID[h] != -1) {
					sPlayer.soundPoolSound.unload(handleToSoundID[h]);
					handleToSoundID[h] = -1;
				}

				if (sounds != null && sounds[h] != null) {
					sounds[h].release();
					sounds = null;
				}
			}
			// Start counting sounds if is going to be used in frame
			if (useCount[h] != 0) {
				if ((handleToFlags[h] & (CSoundPlayer.SNDF_LOADONCALL | CSoundPlayer.SNDF_PLAYFROMDISK)) == 0) {
					max_sounds++;
				}
			}
			// It is played over frame set? if sound is not to be used in frame unload it and release sound
			else if ((sPlayer.app.gaNewFlags & CRunApp.GANF_SAMPLESOVERFRAMES) != 0) {
				if (sounds != null && sounds[h] != null) {
					sounds[h].release();
					sounds[h] = null;
				}

				if (handleToSoundID[h] != -1) {
					sPlayer.soundPoolSound.unload(handleToSoundID[h]);
					handleToSoundID[h] = -1;
				}

			}
		}

		Log.DEBUG("Max. preload sounds at frame detected: " + max_sounds);
		if ((sPlayer.app.gaNewFlags & CRunApp.GANF_SAMPLESOVERFRAMES) == 0) {
			sPlayer.soundPoolSound.startPoolSound(sPlayer.attributes, 24);
		}

		sPlayer.maxMediaSound = 0;
		sPlayer.maxSoundPool = 0;

		sPlayer.actMediaSound = 0;
		sPlayer.actSoundPool = 0;

		// Charge les images
		CSound newSounds[] = new CSound[nHandlesReel];

		int okLoaded = 0;
		sPlayer.soundPoolSound.setStartTime();

		for (h = 0; h < nHandlesReel; h++) {
			if (useCount[h] != 0) {
				if (sounds != null && sounds[h] != null) {
					newSounds[h] = sounds[h];
					if(sounds[h].soundID > 0) {
						okLoaded++;
						if ((sPlayer.app.gaNewFlags & CRunApp.GANF_SAMPLESOVERFRAMES) == 0)
							sounds[h].resetSoundPool();
					}

				} else {
					String name = handleToSoundName[h];
					if (name == null)
						name = "";

					newSounds[h] = new CSound
							(sPlayer, name, (short) h, handleToSoundID[h], handleToFrequency[h], handleToLength[h], handleToFlags[h]);

					//Log.DEBUG("checking sound loadoncall:"+((handleToFlags[h] & 0x10) != 0 ? "yes":"no")+" play from disk:"+((handleToFlags[h] & 0x20) != 0 ? "yes":"no"));
					if (handleToSoundID[h] == -1
							&& (handleToFlags[h] & (CSoundPlayer.SNDF_LOADONCALL | CSoundPlayer.SNDF_PLAYFROMDISK)) == 0) /* play from disk or load on call not set? */ {
						handleToSoundID[h] = newSounds[h].load((short) h, 1);
					}

					if (handleToSoundID[h] == -1) {
						newSounds[h].load((short) h, app);
					}
				}
			}
		}
		sPlayer.soundPoolSound.initPreLoad(max_sounds-okLoaded);
		sounds = newSounds.clone();
		nHandlesTotal = nHandlesReel;

		// Plus rien a charger
		resetToLoad();
	}

	public void unloadAll()
	{
		for (int h = 0; h < nHandlesReel; h++) {
			if (handleToSoundID[h] != -1) {
				sPlayer.soundPoolSound.unload(handleToSoundID[h]);
				handleToSoundID[h] = -1;
			}

			if (sounds != null && sounds[h] != null) {
				sounds[h].release();
				sounds[h] = null;
			}
		}
	}
}
