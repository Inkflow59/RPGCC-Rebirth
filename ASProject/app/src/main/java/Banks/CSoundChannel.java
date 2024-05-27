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


public class CSoundChannel
{
	public static final int UNUSED  = 0;
	public static final int PLAYING = 1;
	public static final int PAUSED  = 2;
	public static final int STOPPED = 3;
	public CSound currentSound;
	public boolean locked;

	public int frequency;
	public float volume=1.0f;
	public float pan=0.0f;
	public boolean loaded;
	public int state = UNUSED;

	public boolean stop (boolean bForce)
	{
		if (currentSound != null)
		{
			if (currentSound.bUninterruptible && !bForce)
				return false;

			currentSound.stop ();
			currentSound.release();
			currentSound = null;
			state = UNUSED;
		}

		return true;
	}

}
