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

package OpenGL;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import Application.CRunApp;
import Banks.CImage;
import Runtime.Log;
import Runtime.MMFRuntime;
import Runtime.SurfaceView;
import Services.CFontInfo;
import Services.CRect;
import Services.CServices;


public class CTextSurface
{
	CRunApp app;

	String prevText;
	short prevFlags;
	int prevColor;
	CFontInfo prevFont;

	public float txtAngle;
	public float txtScaleX;
	public float txtScaleY;
	public int txtSpotX;
	public int txtSpotY;

	public boolean txtShadowEnabled;
	public float txtShadowRadius;
	public float txtShadowdX;
	public float txtShadowdY;
	public int txtShadowColor;

	public int width;
	public int height;
	public int density;

	public int Imgwidth;
	public int Imgheight;

	//private boolean bAntialias;

	int effect;
	int effectParam;

	int drawOffset;

	public Bitmap textBitmap;
	public Canvas textCanvas;
	public TextPaint textPaint;
	private StaticLayout layout;

	public int txtEffectShader;

	private SpannableStringBuilder spannable;

	class CTextTexture extends CImage
	{
		public CTextTexture ()
		{
			if(textBitmap != null) {
				allocNative ((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0, SurfaceView.ES);
				this.updateTextureWithBitmap(textBitmap, false);
			}
		}

		@Override
		public void onDestroy ()
		{
			textTexture = null;
		}

	}

	CTextTexture textTexture;

	private void createTextBitmap(int bmpWidth, int bmpHeight)
	{
		recycle();
		if(CServices.checkFitsInMemoryAndCollect(bmpWidth * bmpHeight * density)) {
			textBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
			textBitmap.eraseColor(Color.TRANSPARENT);
			if (textCanvas == null)
				textCanvas = new Canvas();
		}
	}

	public CTextSurface(CRunApp app, int width, int height)
	{
		this.app = app;
		density = (int) CServices.deviceDensity();

		this.Imgwidth = this.width = width;
		this.Imgheight = this.height = height;

		textPaint = new TextPaint();

		//Log.Log(String.format("Create Text Bitmap Width: %d, Height: %d", width, height));
		createTextBitmap(width, height);

		prevText = "";
		prevFlags = 0;
		prevFont = new CFontInfo();
		prevColor = 0;

		drawOffset = 0;

		//Text_Extras
		txtAngle = 0;
		txtScaleX= 1.0f;
		txtScaleY= 1.0f;
		txtSpotX = 0;
		txtSpotY = 0;
		txtShadowEnabled = false;
		txtShadowRadius = 0.0f;
		txtShadowdX = 0.0f;
		txtShadowdY = 0.0f;
		txtShadowColor = 0;
	}

	public void resize(int width, int height, boolean backingOnly) {
		if (!backingOnly) {
			this.width = width;
			this.height = height;
		}

		if (textBitmap != null && textBitmap.getWidth() >= width && textBitmap.getHeight() >= height)
			return;

		//294.1 added protection, max. texture string allowed is now limited by device.
		this.Imgwidth = (width >= SurfaceView.maxSize ? SurfaceView.maxSize : width);
		this.Imgheight =(height >= SurfaceView.maxSize ? SurfaceView.maxSize : height);

		try {
			//Log.Log(String.format("Resize Text Bitmap Width: %d, Heght: %d", width, height));
			createTextBitmap(this.Imgwidth, this.Imgheight);
		} catch (OutOfMemoryError e) {
			Log.Log("Text too big to create ...");
			textCanvas = null;
			textBitmap = null;
		}

	}

	public void measureText (String s, short flags, CFontInfo font, CRect rect, int newWidth)
	{
		if(textPaint == null )
			return;

		s = s.replaceAll("\\r", ""); // remove all carry return

		textPaint.setAntiAlias(true);
		if(!font.equals(prevFont))
			textPaint.setTypeface(font.createFont());
		textPaint.setTextSize(font.lfHeight);

		if(font.lfUnderline != 0)
			textPaint.setUnderlineText(true);
		else
			textPaint.setUnderlineText(false);

		int lWidth = newWidth;
		if ( newWidth == 0)
			lWidth = width;
		else if( newWidth == -1)
			lWidth = (int)textPaint.measureText(s);

		Alignment alignment = CServices.textAlignment(flags, CServices.containsRtlChars(s));
		StaticLayout layout = CServices.FSStaticLayout
				(s, textPaint, lWidth, alignment, 1.0f, 0.0f, false);

		if (layout == null)
			return;

		int height = layout.getHeight();
		int nCount = layout.getLineCount();
		for (int i = 0; i < nCount; i++) {
			if (layout.getLineBottom(i) - layout.getLineTop(i) > height / nCount) {
				height = (int) Math.round((layout.getLineBottom(i) - layout.getLineTop(i)) * nCount);
			}
		}

		if ((rect.top + height) <= rect.bottom)
			height = rect.bottom - rect.top;

		rect.bottom = rect.top + height;
		rect.right = rect.left + lWidth;
	}

	public void setDimension(int w, int h) {
		Imgwidth  = width  = w;
		Imgheight = height = h;
	}

	public boolean setText(String s, short flags, int color, CFontInfo font, boolean dynamic)
	{
		if(s.equals(prevText) && color == prevColor && flags == prevFlags && font.equals(prevFont) )
			return false;

		if(textBitmap == null) {		//Change for performance.
			createTextBitmap(Imgwidth, Imgheight);
			if (textBitmap == null)
				return false;
		}

		prevFont.copy(font);
		prevFont.createFont();
		prevText = s;
		prevColor = color;
		prevFlags = flags;

		s = s.replaceAll("\\r", ""); // remove all carry return
		textBitmap.eraseColor(color & 0x00FFFFFF);

		CRect rect = new CRect();

		rect.left = 0;

		if(Imgwidth > width)
			rect.right = Imgwidth;
		else
			rect.right = width;

		rect.top = 0;

		if(Imgheight > height)
			rect.bottom = Imgheight;
		else
			rect.bottom = height;

		manualDrawText(s, flags, rect, color, font, dynamic);
		updateTexture();
		return true;
	}

	public void manualDrawText(String s, short flags, CRect rect, int color, CFontInfo font, boolean dynamic) {
		if (textBitmap == null) {
			createTextBitmap(Imgwidth, Imgheight);
			if (textBitmap == null)
				return;
		}
		if (textCanvas == null) {
			textCanvas = new Canvas();
			if (textCanvas == null)
				return;
			if (textBitmap != null)
				textCanvas.setBitmap(textBitmap);
		}

		s = s.replaceAll("\\r", ""); // remove all carry return

		int rectWidth = rect.right - rect.left;
		int rectHeight = rect.bottom - rect.top;

		Alignment alignment = CServices.textAlignment(flags, CServices.containsRtlChars(s));

		textPaint.setAntiAlias((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0);
		textPaint.setColor(0xFF000000 | color);
		if (!font.equals(prevFont)
				|| prevFont.font == null
				|| prevFont.font != textPaint.getTypeface()) {
			prevFont.copy(font);
			prevFont.createFont();
		}
		textPaint.setTypeface(prevFont.font);
		textPaint.setTextSize(font.lfHeight);

		textPaint.setStyle(Paint.Style.FILL);
		if (txtShadowEnabled) {
			textPaint.setShadowLayer(txtShadowRadius, txtShadowdX, txtShadowdY, txtShadowColor);
		}
		textPaint.setFilterBitmap(true);

		if (font.lfUnderline != 0)
			textPaint.setUnderlineText(true);
		else
			textPaint.setUnderlineText(false);

		textPaint.setSubpixelText(true);
		textPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));

		if(dynamic && (flags & CServices.DT_SINGLELINE)  != 0)
			rectWidth = Math.round(textPaint.measureText(s));

		layout = CServices.FSStaticLayout
				(s, textPaint, rectWidth, alignment, 1.0f, 0.0f, false);
		if(layout == null)
			return;

		Paint.FontMetrics fm = textPaint.getFontMetrics();
		int paintOffset = (int) Math.ceil(fm.descent/ 2.0-1);
		paintOffset = paintOffset < 0 ? paintOffset : 0;

		int height = layout.getHeight ();
		int nCount = layout.getLineCount();

		for( int i = 0; i < nCount; i++ ) {
			if( layout.getLineBottom(i) -layout.getLineTop(i) > height/nCount ) {
				height = (int) Math.round((layout.getLineBottom(i) - layout.getLineTop(i))*nCount);
			}
		}

		if(dynamic && ((flags & CServices.DT_BOTTOM)  == 0
				&& (flags & CServices.DT_VCENTER) == 0)) {
			height = layout.getHeight();
			width = rectWidth;
		}

		if (dynamic && (Imgheight < height || Imgwidth < layout.getWidth()))
		{
			resize(rectWidth, layout.getHeight(), true);
			if (textBitmap == null)
				return;

			textCanvas.setBitmap(textBitmap);

			layout.draw(textCanvas);

//			if ((flags & CServices.DT_BOTTOM) != 0)
//				drawOffset = -paintOffset - (layout.getHeight() - rectHeight);
//			else if ((flags & CServices.DT_VCENTER) != 0)
//				drawOffset = -paintOffset - ((layout.getHeight() - rectHeight) / 2);
//			else
//				drawOffset = -paintOffset;

			if ((flags & CServices.DT_BOTTOM) != 0)
				drawOffset = - paintOffset - (layout.getHeight() - rectHeight);
			else if ((flags & CServices.DT_VCENTER) != 0)
				drawOffset = - paintOffset - ((layout.getHeight() - rectHeight) / 2);
			else
				drawOffset = - paintOffset;
		}
		else
		{
			textCanvas.setBitmap(textBitmap);
			textCanvas.save();
			drawOffset = 0;

//			if ((flags & CServices.DT_BOTTOM) != 0)
//				textCanvas.translate(rect.left, rect.bottom - height + paintOffset - drawOffset);
//			else if ((flags & CServices.DT_VCENTER) != 0)
//				textCanvas.translate(rect.left, rect.top + rectHeight / 2 - height / 2 + paintOffset / 2 - drawOffset / 2);
//			else
//				textCanvas.translate(rect.left, rect.top - paintOffset - drawOffset);

			if ((flags & CServices.DT_BOTTOM) != 0)
				textCanvas.translate(rect.left, rect.bottom - (height - paintOffset) );
			else if ((flags & CServices.DT_VCENTER) != 0)
				textCanvas.translate(rect.left, rect.top + rectHeight / 2 - (height / 2 - paintOffset/2));
			else
				textCanvas.translate(rect.left, rect.top - paintOffset);

			//Log.Log("offset paint: "+paintOffset+" draw: "+drawOffset+" padding top: "+layout.getTopPadding()+" bottom: "+layout.getBottomPadding());
			textCanvas.clipRect(0, 0, Imgwidth, Imgheight);

			layout.draw(textCanvas);

			textCanvas.restore();
		}
		textCanvas.setBitmap(null);

	}

	public void manualDrawTextEllipsis(String s, short flags, CRect rect, int color, CFontInfo font, boolean dynamic, int ellipsis_mode)
	{
		if(textCanvas == null) {
			textCanvas = new Canvas();
			if (textCanvas == null)
				return;
			if(textBitmap != null)
				textCanvas.setBitmap(textBitmap);
		}

		int rectWidth = rect.right - rect.left;
		int rectHeight = rect.bottom - rect.top;

		Alignment alignment = CServices.textAlignment(flags, CServices.containsRtlChars(s));

		TextView view = new TextView(MMFRuntime.inst);
		if(view == null)
			return;

		int widthSpec = View.MeasureSpec.makeMeasureSpec(rectWidth, View.MeasureSpec.EXACTLY);
		int heightSpec = View.MeasureSpec.makeMeasureSpec(rectHeight, View.MeasureSpec.EXACTLY);
		view.measure(widthSpec, heightSpec);

		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

		view.setBackgroundColor(Color.TRANSPARENT);
		view.setTextColor(0xFF000000|color);
		view.setTypeface(font.createFont());
		view.setTextSize(TypedValue.COMPLEX_UNIT_PX, font.lfHeight);

		if (ellipsis_mode == 0)
		{
			view.setEllipsize(TruncateAt.START);
		}
		else if (ellipsis_mode == 1)
		{
			view.setEllipsize(TruncateAt.END);
		}
		else if (ellipsis_mode == 2)
		{
			view.setEllipsize(TruncateAt.MIDDLE);
		}


		int gravity = 0;

		if ((flags & CServices.DT_CENTER) != 0)
			gravity |= Gravity.CENTER_HORIZONTAL;
		else {
			if(!CServices.containsRtlChars(s))
			{
				if ((flags & CServices.DT_RIGHT) != 0)
					gravity |= Gravity.RIGHT;
				else
					gravity |= Gravity.LEFT;
			}
			else
			{
				if ((flags & CServices.DT_RIGHT) != 0)
					gravity |= Gravity.LEFT;

				else
					gravity |= Gravity.RIGHT;
			}
		}


		if ((flags & CServices.DT_BOTTOM) != 0)
			gravity |= Gravity.BOTTOM;
		else if ((flags & CServices.DT_VCENTER) != 0)
			gravity |= Gravity.CENTER_VERTICAL;
		else
			gravity |= Gravity.TOP;

		view.setGravity(gravity);
		view.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);

		//Log.Log("Gravity is: "+gravity);

		if((flags & CServices.DT_SINGLELINE) == 0)
			view.setSingleLine(false);
		else
		{
			if(Build.VERSION.SDK_INT >= 23) {
				view.setMaxLines(1);
			}
			else {
				if (ellipsis_mode == 0)
					view.setMaxLines(2);
				else
					view.setMaxLines(1);
			}
		}
		view.setText(s);

		//Translate the Canvas into position and draw it
		textCanvas.setBitmap(textBitmap);
		textCanvas.save();

		int left = rect.left;

		if ((flags & CServices.DT_BOTTOM) != 0)
			textCanvas.translate (left, rect.bottom - view.getHeight());
		else if ((flags & CServices.DT_VCENTER) != 0)
			textCanvas.translate (left, rect.top + rectHeight / 2 - view.getHeight() / 2);
		else
			textCanvas.translate (left, rect.top);

		textCanvas.clipRect (0, 0, Imgwidth, Imgheight);

		view.draw(textCanvas);
		textCanvas.restore();
		textCanvas.setBitmap(null);
	}

	public void updateTexture()
	{
		if(textTexture != null && !textTexture.isEmpty()) {
			textTexture.updateWith(new WeakReference<Bitmap>(textBitmap), false);
		}
		else {
			textTexture = new CTextTexture();
		}
		if(textTexture != null)
		{
			textTexture.setXSpot(txtSpotX);
			textTexture.setYSpot(txtSpotY);
		}
	}

	public void manualClear(int color)
	{
		if(textBitmap != null)
			textBitmap.eraseColor(color & 0x00FFFFFF);
	}

	public void draw(int x, int y, int effect, int effectParam)
	{
		if (textTexture == null || textTexture.isEmpty()) {
			updateTexture();
		}

		// Not need of setAntialias it is done when surface is created ???
		//textTexture.setResampling((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0);

		//GLRenderer.inst.renderImage(textTexture, x, y + drawOffset, -1, -1, effect, effectParam);
		//if(x+txtSpotX+width >= 0 && y+drawOffset+txtSpotY+height >= 0 && y+drawOffset < GLRenderer.limitY && x < GLRenderer.limitX) {
			int bHotSpot = 0;
			if (textTexture.getXSpot() != 0 || textTexture.getYSpot() != 0)
				bHotSpot = 1;

			if ((effect & GLRenderer.BOP_MASK) != GLRenderer.BOP_EFFECTEX
					&& (effect & GLRenderer.BOP_MASK) == GLRenderer.BOP_COPY)
				effect = GLRenderer.BOP_TEXT;

			GLRenderer.inst.renderScaledRotatedImage2(textTexture, (MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0, txtAngle, txtScaleX, txtScaleY, bHotSpot, x + txtSpotX, y + drawOffset + txtSpotY, effect, effectParam);
		//}
	}

	public void recycle()
	{
		if(textCanvas != null)
		{
			textCanvas.setBitmap(null);
			textCanvas = null;
		}

		if (textBitmap != null && !textBitmap.isRecycled())
		{
			textBitmap.recycle();
			textBitmap = null;
		}

		if (textTexture != null)
			textTexture.destroy();
		textTexture = null;

	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setAngle(int angle)
	{
		txtAngle = (float)(angle%360);
	}

	public void setScaleX(int scalex)
	{
		txtScaleX = (float)(scalex)/100.0f;
	}

	public void setScaleY(int scaley)
	{
		txtScaleY = (float)(scaley)/100.0f;
	}

	public void setHotSpot(int spotX, int spotY)
	{
		txtSpotX = spotX;
		txtSpotY = spotY;
		if(textTexture != null)
		{
			textTexture.setXSpot(txtSpotX);
			textTexture.setYSpot(txtSpotY);
		}
	}

	public void enableShadow(boolean bShadow)
	{
		txtShadowEnabled = bShadow;
	}
	public void setShadowValues(float radius, float dx, float dy, int color)
	{
		txtShadowRadius = radius;
		txtShadowdX = dx;
		txtShadowdY = dy;
		txtShadowColor = 0xFF000000 | color;
	}

}
