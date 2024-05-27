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
package Extensions;

import java.util.HashMap;

import Actions.CActExtension;
import Conditions.CCndExtension;
import Expressions.CValue;
import RunLoop.CCreateObjectInfo;
import Runtime.MMFRuntime;
import Services.CBinaryFile;
import Services.FontUtils;
import android.widget.TextView;

public class CRunFontPacker extends CRunExtension
{
    // <editor-fold defaultstate="collapsed" desc=" A/C/E Constants ">
    public static final int CNDONINTERNALFONT = 0;
    public static final int CNDONPACKEDFONT = 1;
    public static final int CNDONINTERNALFONTAVAILABLE = 2;
    public static final int CNDONPACKEDFONTAVAILABLE = 3;
    public static final int CNDONERROR = 4;
    public static final int CND_LAST = 5;

    public static final int ACTREADINTERNALFONTS = 0;
    public static final int ACTREADPACKEDFONTS = 1;

    public static final int EXPINTERNALQTY = 0;
    public static final int EXPINTFONTNAME = 1;
    public static final int EXPPACKEDQTY = 2;
    public static final int EXPPACKFONTNAME = 3;
    public static final int EXPDEFAULTFONTSIZE = 4;
    public static final int EXPDEFAULTFONTSTYLE = 5;
    public static final int EXPLASTERROR = 6;

    // </editor-fold>

    private HashMap< String, String > PackedFonts = null;
    private HashMap< String, String > InternalFonts = null;
	private String[][] listInternal = null;
	private String[][] listPacked = null;
    
    
    public CRunFontPacker()
    {
    	MMFRuntime.FONTPACK = true;
    	
    }
    
    public @Override int getNumberOfConditions()
    {
	    return CND_LAST;
    }
    
    public @Override boolean createRunObject(CBinaryFile file, CCreateObjectInfo cob, int version)
    {
    	return false;
    }
    
    public @Override void destroyRunObject(boolean bFast)
    {
    }
    
    public @Override void pauseRunObject()
    {
    }
    
    public @Override void continueRunObject()
    {
    }

    public @Override int handleRunObject()
    {
        return REFLAG_ONESHOT;
    }

    // Conditions
    // -------------------------------------------------
    public @Override boolean condition(int num, CCndExtension cnd)
    {
        switch (num)
        {
            case CNDONINTERNALFONT:
                return cndOnInternalFont(cnd);
            case CNDONPACKEDFONT:
                return cndOnPackedFont(cnd);
            case CNDONINTERNALFONTAVAILABLE:
                return cndOnInternalFontAvailable(cnd);
            case CNDONPACKEDFONTAVAILABLE:
                return cndOnPackedFontAvailable(cnd);
            case CNDONERROR:
                return cndOnError(cnd);
        }
        return false;
    }

    // Actions
    // -------------------------------------------------
    public @Override void action(int num, CActExtension act)
    {
        switch (num)
        {
            case ACTREADINTERNALFONTS:
                actReadInternalFonts(act);
                break;
            case ACTREADPACKEDFONTS:
                actReadPackedFonts(act);
                break;
        }
    }

    // Expressions
    // -------------------------------------------------
    public @Override CValue expression(int num)
    {
        switch (num)
        {
            case EXPINTERNALQTY:
                return expInternalQty();
            case EXPINTFONTNAME:
                return expIntFontName();
            case EXPPACKEDQTY:
                return expPackedQty();
            case EXPPACKFONTNAME:
                return expPackFontName();
            case EXPDEFAULTFONTSIZE:
                return expDefaultFontSize();
            case EXPDEFAULTFONTSTYLE:
                return expDefaultFontStyle();
            case EXPLASTERROR:
                return expLastError();
        }
        return null;
    }

    private boolean cndOnInternalFont(CCndExtension cnd)
    {
        return true;
    }

    private boolean cndOnPackedFont(CCndExtension cnd)
    {
        return true;
    }

    private boolean cndOnInternalFontAvailable(CCndExtension cnd)
    {
        String param0 = cnd.getParamExpString(rh,0);
        if(param0.length() > 0) {
        	if(InternalFonts != null && InternalFonts.containsKey(param0))
        		return true;
        }
        return false;
    }

    private boolean cndOnPackedFontAvailable(CCndExtension cnd)
    {
        String param0 = cnd.getParamExpString(rh,0);
        if(param0.length() > 0) {
           	if(PackedFonts != null && PackedFonts.containsKey(param0.trim().toLowerCase()))
        		return true;
        }
        return false;
    }

    private boolean cndOnError(CCndExtension cnd)
    {
        return false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    private void actReadInternalFonts(CActExtension act)
    {
    	InternalFonts = FontUtils.enumInternalFonts();
    	if(InternalFonts != null) {
    		listInternal = (String[][])HashToArray(InternalFonts);
    		ho.pushEvent(CNDONINTERNALFONT, 0);   		
    	}
    }

    private void actReadPackedFonts(CActExtension act)
    {
    	PackedFonts = FontUtils.enumPackedFonts();
    	if(PackedFonts != null) {
    		listPacked = (String[][])HashToArray(PackedFonts);
    		ho.pushEvent(CNDONPACKEDFONT, 0);
    	}
    }

    private CValue expInternalQty()
    {
        if(InternalFonts == null)
        	return new CValue(0);
        
        return new CValue(InternalFonts.size());
    }

    private CValue expIntFontName()
    {
        String sRet = "";
    	int param0 = ho.getExpParam().getInt();
        if(listInternal != null)
        	sRet =	listInternal[param0][0];
        	
        return new CValue(sRet);
    }

    private CValue expPackedQty()
    {
        if(PackedFonts == null)
        	return new CValue(0);
        
        return new CValue(PackedFonts.size());
    }

    private CValue expPackFontName()
    {
        String sRet = "";
    	int param0 = ho.getExpParam().getInt();
        if(listPacked != null)
        	sRet =	listPacked[param0][0];
        	
        return new CValue(sRet);
    }

    private CValue expDefaultFontSize()
    {
        int Size = 12;
    	final TextView v = new TextView(ho.getControlsContext());
    	
        if(v != null)
        	Size = (int)v.getTextSize();
    	return new CValue(Size*72/96);
    }

    private CValue expDefaultFontStyle()
    {
        int Style = 0;
    	final TextView v = new TextView(ho.getControlsContext());
    	
        if(v != null && v.getTypeface() != null)
       		Style = v.getTypeface().getStyle();
        	
    	return new CValue(Style);
    }

    private CValue expLastError()
    {
        return new CValue(0);
    }
    
    
    /////////////////////////////////////////////////////////////////////////////
    //
    //                        Utilities
    //
    /////////////////////////////////////////////////////////////////////////////
    
    private Object[][] HashToArray(HashMap<?,?> map) {
    
	    Object[][] BidimArray = new String[map.size()][2];
	
	    Object[] keys = map.keySet().toArray();
	    Object[] values = map.values().toArray();
	
	    for (int i = 0; i < BidimArray.length; i++) {
	    	if(keys[i] != null) {
	    		BidimArray[i][0] = keys[i];
	    		BidimArray[i][1] = values[i];
	        }
	    }
	    
	    return BidimArray;
    
    }

}
