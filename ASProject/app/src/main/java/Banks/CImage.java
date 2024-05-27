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
// CIMAGEBANK : Stockage des images
//
//----------------------------------------------------------------------------------
package Banks;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import Application.CRunApp;
import OpenGL.ITexture;
import Runtime.MMFRuntime;
import Runtime.SurfaceView;
import Services.CFile;
import Services.CServices;
import Sprites.CMask;



/* This class is abstract because anyone using CImage MUST handle onDestroy */

public abstract class CImage extends ITexture
{
	public static final String TAG = "MMFRuntime";

	public static Set <CImage> images = new HashSet<CImage>();

	protected native void allocNative (boolean resample, int renderid);

	protected native void allocNative2
			(boolean resample, short handle, int [] img, int xSpot, int ySpot,
			 int xAP, int yAP, int width, int height, int renderid);

	protected native void allocNative4
			(boolean resample, CFile file, int renderid);

	protected native void allocNative5
			(boolean resample, short handle, final byte[] buffer, boolean transparent, boolean first_pixel, int color_transp, int renderid);

	protected native void allocNative6
			(boolean resample, short handle, final Bitmap bmp, boolean transparent, boolean first_pixel, int color_transp, boolean recycled, int renderid);

	private native void freeNative ();

	static int renderID = SurfaceView.ES;

	public CImage()
	{
		synchronized(images)
		{
			allocNative ((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0, renderID);

			images.add (this);
			//Log.v("MMFRuntime", "starting antialias: "+(this.getAntialias()? "yes":"no"));
		}
	}

	public CImage(boolean antialiased)
	{
		synchronized(images)
		{
			allocNative (antialiased, renderID);

			images.add (this);
			//Log.v("MMFRuntime", "starting antialias: "+(this.getAntialias()? "yes":"no"));
		}
	}

	/* For CImageBank */

	public CImage
			(short handle, Bitmap img, int xSpot, int ySpot,
			 int xAP, int yAP, int useCount, int width, int height, boolean antialiased)
	{

		synchronized(images) {
			allocNative2
					(antialiased,
							handle, getBitmapPixels (img), xSpot, ySpot,
							xAP, yAP, width, height, renderID);

			images.add (this);
			//Log.v("MMFRuntime", "from bank antialias: "+(this.getAntialias()? "yes":"no"));
		}
	}

	/* For the joystick images */

	public CImage(String resource, boolean antialiased)
	{
		Bitmap img = null;

		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			img = BitmapFactory.decodeResource (MMFRuntime.inst.getResources (),
					MMFRuntime.inst.getResourceID (resource), options);
		}
		catch (Throwable e)
		{
			Log.v(TAG, "error in image is: "+e.getMessage());
		}


		if (img == null)
			throw new RuntimeException ("Bad image resource : " + resource);

		synchronized(images)
		{
			allocNative2 (antialiased,
					(short) -1, getBitmapPixels (img), 0, 0, 0,
					0, img.getWidth (), img.getHeight (), renderID);

			images.add (this);
		}
		img.recycle();
	}

	/* For the Active Picture object (to load a file) */

	public CImage(InputStream input, boolean antialiased, boolean transparent, boolean first_pixel, int color_transp, int angle) {
		byte[] imageData;
//    long start = SystemClock.currentThreadTimeMillis();
		try {
			imageData = readFully(input, -1, true);
		} catch (IOException e) {
			Log.v(TAG, "error reading inputstream for apo ...");
			imageData = null;
		}
//    Log.v(TAG, "reading buffer took(msecs) "+ (SystemClock.currentThreadTimeMillis()-start));

		if (imageData != null) {
			Bitmap img = null;

			try {
				BitmapFactory.Options optRead = new BitmapFactory.Options();
				optRead.inJustDecodeBounds = true;
				BitmapFactory.decodeByteArray(imageData, 0, imageData.length, optRead);

				if (optRead.outWidth <= 0 || optRead.outHeight <= 0) {
					throw new RuntimeException("Bad image [byte array]");
				}

				int scale = 1;
				if (optRead.outWidth + optRead.outHeight > 3200)
					scale *= 2;

				optRead.inJustDecodeBounds = false;
				optRead.inSampleSize = scale;
				optRead.inMutable = true;
				optRead.inPreferredConfig = Bitmap.Config.ARGB_8888;

				img = rotateImage(BitmapFactory.decodeByteArray(imageData, 0, imageData.length, optRead), angle);
			} catch (Throwable t) {
				throw new RuntimeException("Bad Image while decoding");
			}

			if (img == null)
				throw new RuntimeException("Bad image [byte array]");

			//Log.v(TAG, "reading bitmap took(msecs) "+ (SystemClock.currentThreadTimeMillis()-start));
			synchronized (images) {
				Bitmap.Config c = img.getConfig();
				if (c == null || c != Bitmap.Config.ARGB_8888)
					allocNative2(antialiased,
							(short) -1, CServices.getBitmapPixels(img), 0, 0, 0,
							0, img.getWidth(), img.getHeight(), renderID);
				else {
					allocNative6(antialiased,
							(short) -1, img, transparent, first_pixel, color_transp, true, renderID);
				}
				images.add(this);
				img.recycle();
			}
		} else {
			throw new RuntimeException("No image data available");
		}
		//Log.v(TAG, "load apo texture took(msecs) "+ (SystemClock.currentThreadTimeMillis()-start));
	}

	public long ptr;

	public native CMask getMask(int nFlags, int angle, double scaleX, double scaleY);

	public native int getXSpot ();
	public native int getYSpot ();

	public native void setXSpot (int x);
	public native void setYSpot (int y);

	public native int getXAP ();
	public native int getYAP ();

	public native void setXAP (int x);
	public native void setYAP (int y);

	public native boolean getResampling();
	public native void setResampling (boolean antialias);

	@Override
	public native int getWidth();
	@Override
	public native int getHeight();

	public native int getPixel (int x, int y);

	public native int createTexture(int width, int height, boolean resample);

	public native int createTextureOES(int width, int height, boolean resample);

	@Override
	public native int texture ();

	/**
	 * Can be use as the following example
	 *
	 *	CImage cImage = ho.getImageBank().getImageFromHandle(Images[i]);
	 * 				...
	 * 	int[] mImage = cImage.getRawPixels();
	 * 	if(mImage == null)
	 *  	return;
	 *  image = Bitmap.createBitmap(cImage.getWidth(), cImage.getHeight(), cImage.getFormat());
	 * 	image.setPixels(mImage, 0, cImage.getWidth(), 0, 0, cImage.getWidth(), cImage.getHeight());
	 */
	public native int[] getRawPixels();

	public native short imageFormat();

	public native   int imageBegin();

	public native   int imageSize();

	public native void imageSetData(int [] pixels);

	public enum FORMATS {
		//texture Enums
		RGBA8888,   // 32 bit
		RGBA4444,   // 16 bit
		RGBA5551,   // 16 bit
		RGB888,     // 24 bit
		RGB565,     // 16 bit
		JPEG,		// store JPEG
		PNG			// store PNG
	}

	public Config getFormat() {
		/*
    	texture Enums
    	RGBA8888,   // 32 bit
        RGBA4444,   // 16 bit
        RGBA5551,   // 16 bit
        RGB888,     // 24 bit
        RGB565      // 16 bit
		 */
		int format = imageFormat();

		switch(format)
		{
			case 0:
				return Config.ARGB_8888;

			case 1:
				return Config.ARGB_4444;

			case 3:
				return Config.ALPHA_8;

			case 2:
			case 4:
				return Config.RGB_565;

		};

		return Config.ARGB_8888;
	}

	public native void setOpaqueMask(boolean mode);
	public native void deuploadNative ();

	public synchronized void destroy ()
	{
		if (ptr == 0)
			return;
		if(images != null) {
			onDestroy();
			deuploadNative();
			freeNative();
			images.remove(this);
			ptr = 0;
		}
	}

	public boolean isEmpty() {
		return (ptr == 0);
	}

	public abstract void onDestroy ();

	/**
	 * update texture with a pixels integer array 
	 *
	 * @param pixels of image
	 * @param width image size
	 * @param height image size
	 */
	public native void updateTextureWithPixels (int [] pixels, int width, int height);
	/**
	 * update texture with a bitmap
	 *
	 * @param bmp bitmap of image
	 * @param unpre_multiply boolean set false if bitmap should be leave as it is
	 */
	public native void updateTextureWithBitmap ( Bitmap bmp, boolean unpre_multiply);
	/**
	 * create a texture from screen 
	 *
	 * @param x, horizontal position in pixels
	 * @param y, vertical position in pixels
	 * @param width image size
	 * @param height image size
	 * @param vpheight, viewPort height
	 */
	public native void screenAreaToTexture (int x, int y, int width, int height, int vpwidth, int vpheight );

	/**
	 * update texture with a bitmap
	 *
	 * @param bmp bitmap of image
	 * @param unpreMult boolean set false if bitmap should be leave as it is
	 */
	public void updateWith (WeakReference<Bitmap> bmp, boolean unpreMult)
	{
		updateFromBitmap(bmp, unpreMult);
	}

	public native void setRepeatMode(int index);

	private void updateFromBitmap(WeakReference<Bitmap> bmp, boolean unpreMult)
	{
		if(bmp.get() == null)
			return;
		Bitmap img = bmp.get();
		updateTextureWithBitmap (img, unpreMult);
	}

	public void imageSetData (WeakReference<Bitmap> bmp)
	{
		if(bmp.get() == null)
			return;
		Bitmap img = bmp.get();
		int [] pixels = new int [img.getWidth () * img.getHeight ()];
		img.getPixels (pixels, 0, img.getWidth (), 0, 0, img.getWidth (), img.getHeight ());
		imageSetData (pixels);
	}

	public native short getHandle ();

	public native void getInfo (CImageInfo dest, int nAngle, float fScaleX, float fScaleY);

	public native void flipHorizontally ();
	public native void flipVertically ();

	public int [] getBitmapPixels (Bitmap img)
	{
		if(img == null)
			return null;
		int [] pixels = new int [img.getWidth () * img.getHeight ()];
		img.getPixels (pixels, 0, img.getWidth (), 0, 0, img.getWidth (), img.getHeight ());
		return pixels;
	}

	private static byte[] readFully(InputStream is, int length, boolean readAll)
			throws IOException {
		byte[] output = {};
		if (length == -1) length = Integer.MAX_VALUE;
		int pos = 0;
		while (pos < length) {
			int bytesToRead;
			if (pos >= output.length) { // Only expand when there's no room
				bytesToRead = Math.min(length - pos, output.length + 1024);
				if (output.length < pos + bytesToRead) {
					output = Arrays.copyOf(output, pos + bytesToRead);
				}
			} else {
				bytesToRead = output.length - pos;
			}
			int cc = is.read(output, pos, bytesToRead);
			if (cc < 0) {
				if (readAll && length != Integer.MAX_VALUE) {
					throw new EOFException("Detect premature EOF");
				} else {
					if (output.length != pos) {
						output = Arrays.copyOf(output, pos);
					}
					break;
				}
			}
			pos += cc;
		}
		return output;
	}

	public static Bitmap rotateImage(Bitmap source, int angle) {
		Matrix matrix = new Matrix();
		if(angle != 0) {
			matrix.postRotate(angle);
			return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
		}
		else
			return source;
	}

	public static Bitmap rotateScaleImage(Bitmap source, int angle, float scale) {
		Matrix matrix = new Matrix();
		matrix.postScale(1.0f/scale, 1.0f/scale);
		if(angle != 0)
			matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}
}

