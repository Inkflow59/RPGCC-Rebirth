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
// CRUNFRAME : Classe Frame
//
//----------------------------------------------------------------------------------
package Frame;

import android.opengl.Matrix;

import java.util.ArrayList;

import Application.CRunApp;
import Application.CEffectEx;
import RunLoop.CBkd2;
import Services.CArrayList;
import Services.CFile;
import Services.CRect;

/**
 * Layer object. Defines a layer on the frame.
 */
public class CLayer {
	public static final int FLOPT_XCOEF = 0x0001;
	public static final int FLOPT_YCOEF = 0x0002;
	public static final int FLOPT_NOSAVEBKD = 0x0004;
	// public static final int FLOPT_WRAP_OBSOLETE=0x0008;
	public static final int FLOPT_VISIBLE = 0x0010;
	public static final int FLOPT_WRAP_HORZ = 0x0020;
	public static final int FLOPT_WRAP_VERT = 0x0040;
	public static final int FLOPT_REDRAW = 0x000010000;
	public static final int FLOPT_TOHIDE = 0x000020000;
	public static final int FLOPT_TOSHOW = 0x000040000;

	public String pName; // / Name

	// Offset
	public int x; // / Current offset
	public int y;
	public int dx; // / Offset to apply to the next refresh
	public int dy;

	public ArrayList<CBkd2> pBkd2 = null;

	// Ladders
	public ArrayList<CRect> pLadders = null;

	// Z-order max index for dynamic objects
	public int nZOrderMax;

	// Permanent data (EditFrameLayer)
	public int dwOptions; // / Options
	public float xCoef;
	public float yCoef;
	public int nBkdLOs; // / Number of backdrop objects
	public int nFirstLOIndex; // / Index of first backdrop object in LO table

	// Backup for restart
	public int backUp_dwOptions;
	public float backUp_xCoef;
	public float backUp_yCoef;
	public int backUp_nBkdLOs;
	public int backUp_nFirstLOIndex;

	// Effect Layer
//	public short effect;
//	public short effectParam;
	public int effect;
	public int effectParam;
	public int effectIndex;
	public int effectNParams;
	public int effectPOffset;
	public int[] effectData;

	public int effectShader;
	public CEffectEx effectEx = null;

	// Frame Layer effect for future use
//	DWORD	dwInkFx;
//	DWORD	dwRGBA;
//	DWORD	dwExtInkFxIdx;
//	DWORD	nParams;
//	DWORD	paramData;		// offset

	// JAMES:
	public float angle = 0;
	public float scale = 1;
	public float scaleX = 1;
	public float scaleY = 1;
	public int xSpot;
	public int ySpot;
	public int xDest;
	public int yDest;

	// Collision optimization
	public CArrayList<CArrayList<Integer>> m_loZones;
	public float[] transform;

	public CLayer() {
		transform = new float[16];
		effectShader = -1;
		effectEx = null;
	}

	/**
	 * Loads the data from application file. Data include scrolling
	 * coefficients, and index of the background LO objects.
	 */
	public void load(CFile file) {
		dwOptions = file.readAInt();
		xCoef = file.readAFloat();
		yCoef = file.readAFloat();
		nBkdLOs = file.readAInt();
		nFirstLOIndex = file.readAInt();
		pName = file.readAString();

		backUp_dwOptions = dwOptions;
		backUp_xCoef = xCoef;
		backUp_yCoef = yCoef;
		backUp_nBkdLOs = nBkdLOs;
		backUp_nFirstLOIndex = nFirstLOIndex;
		Matrix.setIdentityM(transform, 0);
	}

	public void calculateTransformation(final CRunApp app) {
		double radian = (double) (Math.PI * (app.scAngle+angle) / 180.0);
		float cosA, sinA;
		cosA = (float)Math.cos(radian);
		sinA = (float)Math.sin(radian);
		float sX = app.scScaleX*scaleX;
		float sY = app.scScaleY*scaleY;
		int posX = app.scXDest + xDest - dx;
		int posY = app.scYDest + yDest - dy;
		int hoX  = app.scXSpot + xSpot;
		int hoY  = app.scYSpot + ySpot;

		transform[ 0] = sX*cosA;
		transform[ 1] = -sY*sinA;
		transform[ 2] = 0;
		transform[ 3] = 0;

		transform[ 4] = sX*sinA;
		transform[ 5] = sY*cosA;
		transform[ 6] = 0;
		transform[ 7] = 0;

		transform[ 8] = 0;
		transform[ 9] = 0;
		transform[10] = 1;
		transform[11] = 0;

		transform[12] = posX-hoX*sX*cosA-hoY*sX*sinA;
		transform[13] = posY-hoY*sY*cosA+hoX*sY*sinA;
		transform[14] = 0;
		transform[15] = 1;
	}

	/*
    CEffect code for Layers
    */
	public int checkOrCreateEffectIfNeeded(final CRunApp app)
	{
		if(!app.effectBank.isEmpty())
		{
			if (effectEx == null) {

				effectEx = new CEffectEx(app);
				if (effectEx.initialize(effectIndex, effectParam))
				{
					if(effectData != null)
						effectEx.setEffectData(effectData);
					effectShader = effectEx.getIndexShader();
				} else
					effectShader = -1;

			} else
				effectShader =  effectEx.getIndexShader();
		}
		else
			effectShader =  -1;
		return effectShader;
	}

	public int checkOrCreateEffectIfNeeded(final CRunApp app, String name)
	{
		if(!app.effectBank.isEmpty())
		{
			if (effectEx == null) {

				effectEx = new CEffectEx(app);
				if (effectEx.initialize(name, effectParam))
					effectShader = effectEx.getIndexShader();
				else
					effectShader = -1;

			} else if (effectEx.getName().contains(name))
				effectShader = effectEx.getIndexShader();
			else {
				effectEx.destroy();

				effectEx = new CEffectEx(app);
				if (effectEx.initialize(name, effectParam))
					effectShader = effectEx.getIndexShader();
				else
					effectShader = -1;

			}
		}
		else
			effectShader = -1;
		return effectShader;
	}
}
