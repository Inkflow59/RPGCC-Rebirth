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

//----------------------------------------------------------------------------------
//
// CRunkcclock: date & time object
//
//greyhill
//----------------------------------------------------------------------------------
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import Actions.CActExtension;
import Conditions.CCndExtension;
import Expressions.CValue;
import OpenGL.CTextSurface;
import OpenGL.GLRenderer;
import RunLoop.CCreateObjectInfo;
import RunLoop.CRun;
import Runtime.Log;
import Services.CBinaryFile;
import Services.CFontInfo;
import Services.CRect;
import Services.CServices;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Typeface;

public class CRunkcclock extends CRunExtension
{
	public boolean updateAnalog;

    String pad (String what, int l)
    {
        if(l < what.length())
            l = what.length();

        return "0000000".substring (0, l - what.length ()) + what;
    }
	
    public static final int ANALOG_CLOCK = 0;
    public static final int DIGITAL_CLOCK = 1;
    public static final int INVISIBLE = 2;
    public static final int CALENDAR = 3;
    public static final int CLOCK = 0;
    public static final int STOPWATCH = 1;
    public static final int COUNTDOWN = 2;
    public static final int SHORTDATE = 0;
    public static final int LONGDATE = 1;
    public static final int FIXEDDATE = 2;

//typedef struct tagEDATA
//{
//	extHeader		eHeader;
//	short			sSizeX;
//	short			sSizeY;
//	// Colors
//	COLORREF		ColorrefCustom [16];
//	// Type
//	short			sType;
//	// Clock
//	short			sClockMode;
//	short			sClockBorder;
//	// Analog clock
//	short			sAnalogClockLines;
//	short			sAnalogClockMarkerType;
//	LOGFONT			lfFont;
//	COLORREF		crFont;
//	char			szStyle [40];
//	short           sAnalogClockSeconds;
//	COLORREF		crAnalogClockSeconds;
//	short           sAnalogClockMinutes;
//	COLORREF		crAnalogClockMinutes;
//	short           sAnalogClockHours;
//	COLORREF		crAnalogClockHours;
//	// Digital clock
//	short			sDigitalClockType;
//	// Calendar
//	short			sCalendarType;
//	short			sCalendarFormat;
//	char			szCalendarUserFormat [40]; // For future use
//	short			sCountDownHours;
//	short			sCountDownMinutes;
//	short			sCountDownSeconds;
//	short			sMinWidth;
//	short			sMinHeight;	
//	npAppli			IdAppli;
//	short			lSecu;
//} editData;
//typedef editData	_far *		fpedata;
//typedef struct tagRDATA
//{
//	headerObject 	rHo;
//	rCom			roc;
//	rMvt			rmv;					// Structure de mouvements
//	rSpr			rsp;
//	rVal			rv;
//	short			sEventCount;
//	// Type
//	short			sType;
//	// Clock
//	short			sClockMode;
//	short			sClockBorder;
//	// Analog clock
//	short			sAnalogClockLines;
//	short			sAnalogClockMarkerType;
//	COLORREF		crFont;
//	short           sAnalogClockSeconds;
//	COLORREF		crAnalogClockSeconds;
//	short           sAnalogClockMinutes;
//	COLORREF		crAnalogClockMinutes;
//	short           sAnalogClockHours;
//	COLORREF		crAnalogClockHours;
//	// Digital clock
//	short			sDigitalClockType;
//	// Calendar
//	short			sCalendarType;
//	short			sCalendarFormat;
//	char			szCalendarUserFormat [40]; // For future use
//	// Chrono
//	short			sChronoStarted;
//	double			dChronoStart;
//	long			lChrono;
//	// Heure
//	short			sNewSecond;
//	short			sNewMinute;
//	short			sNewHour;
//	short			sNewDay;
//	short			sNewMonth;
//	short			sNewYear;
//	// Handles
//	short			sCenterX,sCenterY;
//	short			sHourX,sHourY;
//	short			sMinuteX,sMinuteY;
//	short			sSecondX,sSecondY;	
//	// Visibility
//	short			sVisible;	
//	// font
//	short			sFont;
//	// font
//	short			sUpdateCounter;
//	short			sMinWidth;
//	short			sMinHeight;
//	// Countdown
//	long			lCountdownStart;
//	// Display flag
//	short			sDisplay;
//	double			dChronoCounter;
//} runData;
    double months[] =
    {
        0D,
        267840000D,
        509760000D,
        777600000D,
        1123200000D,
        1304640000D,
        1563840000D,
        1831680000D,
        2099520000D,
        2358720000D,
        2626560000D,
        2885760000D,
    };
    String szRoman[] =
    {
        "I",
        "II",
        "III",
        "IV",
        "V",
        "VI",
        "VII",
        "VIII",
        "IX",
        "X",
        "XI",
        "XII"
    };
    String FORMAT[] =
    {
        "dd/MM/yy",
        "d MMMM yyyy",
        "d MMMM, yyyy",
        "MMMM d, yyyy",
        "dd-MMM-yy",
        "MMMM, yy",
        "MMM-yy"
    };
    int ADJ = 3;
    short sType;
    short sClockMode;
    boolean sClockBorder;
    boolean sAnalogClockLines;
    short sAnalogClockMarkerType;
    CFontInfo sFont;
    int crFont;
    boolean sAnalogClockSeconds;
    int crAnalogClockSeconds;
    boolean sAnalogClockMinutes;
    int crAnalogClockMinutes;
    boolean sAnalogClockHours;
    int crAnalogClockHours;
    short sDigitalClockType;
    short sCalendarType;
    short sCalendarFormat;
    int lCountdownStart;
    short sMinWidth;
    short sMinHeight;
    boolean sVisible;
    Calendar initialTime;
    Calendar startTimer;
    Calendar lastRecordedTime;
    Calendar currentTime;
    boolean sDisplay;
    short sUpdateCounter;
    double dChronoCounter;
    double dChronoStart;
    int lChrono;
    short sEventCount;
    short sCenterX;
    short sCenterY;
    short sHourX;
    short sHourY;
    short sMinuteX;
    short sMinuteY;
    short sSecondX;
    short sSecondY;
    CTextSurface textSurface;
    kcclockActs actions;
    kcclockCnds conditions;
    kcclockExpr expressions;
	public CValue	tempValue;

    int textWidth;
    int textHeight;
    
    public CRunkcclock()
    {
		tempValue = new CValue();
    }

    @Override
	public int getNumberOfConditions()
    {
        return 9;
    }

    @Override
	public boolean createRunObject(CBinaryFile file, CCreateObjectInfo cob, int version)
    {
        actions = new kcclockActs(this);
        conditions = new kcclockCnds(this);
        expressions = new kcclockExpr(this);

        ho.setX(cob.cobX);
        ho.setY(cob.cobY);
        ho.hoImgXSpot = 0;
        ho.hoImgYSpot = 0;
        ho.setWidth(file.readShort());
        ho.setHeight(file.readShort());
        file.skipBytes(4 * 16);
        this.sType = file.readShort();
        this.sClockMode = file.readShort();
        this.sClockBorder = (file.readShort() == 0) ? false : true;
        this.sAnalogClockLines = (file.readShort() == 0) ? false : true;
        this.sAnalogClockMarkerType = file.readShort();
        CFontInfo font = file.readLogFont();
        if ((font.lfHeight == 8) && (font.lfFaceName.toUpperCase(Locale.US).equals("SYSTEM")))
        {
            font.lfHeight = 13; //c++ bug i think
            font.lfWeight = 700;//bold
        }
        this.sFont = font;
        this.crFont = file.readColor();
        file.readString(40);
        this.sAnalogClockSeconds = (file.readShort() == 0) ? false : true;
        this.crAnalogClockSeconds = file.readColor();
        this.sAnalogClockMinutes = (file.readShort() == 0) ? false : true;
        this.crAnalogClockMinutes = file.readColor();
        this.sAnalogClockHours = (file.readShort() == 0) ? false : true;
        this.crAnalogClockHours = file.readColor();
        this.sDigitalClockType = file.readShort();
        this.sCalendarType = file.readShort();
        this.sCalendarFormat = file.readShort();
        file.readString(40);
        short sCountDownHours = file.readShort();
        short sCountDownMinutes = file.readShort();
        short sCountDownSeconds = file.readShort();
        this.lCountdownStart = (sCountDownHours * 360000) + (sCountDownMinutes * 6000) + (sCountDownSeconds * 100);
        this.sMinWidth = file.readShort();
        this.sMinHeight = file.readShort();
        switch (this.sType)
        {
            case ANALOG_CLOCK:
            case CALENDAR:
            case DIGITAL_CLOCK:
                {
                    this.sFont = font;
                }
                break;
            case INVISIBLE:
                break;
        }
        this.sVisible = true;

        this.initialTime = Calendar.getInstance();
        this.startTimer = Calendar.getInstance();
        this.lastRecordedTime = Calendar.getInstance();
        this.currentTime = Calendar.getInstance();
//  this.sNewSecond = (short)cTime.get(Calendar.SECOND);
//	this.sNewMinute = (short)cTime.get(Calendar.MINUTE);
//	this.sNewHour = (short)cTime.get(Calendar.HOUR);
//	this.sNewDay = (short)cTime.get(Calendar.DAY_OF_MONTH);
//	this.sNewMonth = (short)(cTime.get(Calendar.MONTH) + 1);
//	this.sNewYear = (short)cTime.get(Calendar.YEAR);
        this.dChronoStart = 0;
        this.lChrono = 0;
        this.sDisplay = true;
        
        textSurface = new CTextSurface(ho.hoAdRunHeader.rhApp, ho.hoImgWidth, ho.hoImgHeight);
        updateAnalog = true;
        
        return true;
    }

    @Override
	public void destroyRunObject(boolean bFast)
    {
        if (this.sVisible == true)
        {
            ho.redisplay();
        }

        if (textSurface != null)
            textSurface.recycle();
    }

    public Calendar getCurrentTime()
    {
        //output = initialTime + (currentTime - startTimer)

		// WTF? LOL
        //Date output = new Date(this.initialTime.getTime().getTime() +
        //        (Calendar.getInstance().getTime().getTime() - this.startTimer.getTime().getTime()));
        // Seems a bit more simple and faster!
        currentTime.setTimeInMillis(initialTime.getTimeInMillis() + System.currentTimeMillis() - startTimer.getTimeInMillis());
        Calendar c = Calendar.getInstance();
        c.setTime(currentTime.getTime());

		return c;
    }

    public void changeTime(Date date)
    {
        this.initialTime.setTime(date);
        this.lastRecordedTime.setTime(date);
        this.startTimer = Calendar.getInstance();
    }

    @Override
	public int handleRunObject()
    {
        double dCurrentChronoCounter;

        this.sUpdateCounter = 0;

        Calendar cTime = getCurrentTime();
        // If system time change
        dCurrentChronoCounter = months[cTime.get(Calendar.MONTH)] + ((double) cTime.get(Calendar.DAY_OF_MONTH) * (double) 8640000) + ((double) cTime.get(Calendar.HOUR_OF_DAY) * (double) 360000) + ((double) cTime.get(Calendar.MINUTE) * (double) 6000) + ((double) cTime.get(Calendar.SECOND) * (double) 100) + cTime.get(Calendar.MILLISECOND) / 10.0f;
        if ((dCurrentChronoCounter < this.dChronoCounter) || ((dCurrentChronoCounter > (this.dChronoCounter + 200)) && (this.dChronoCounter != 0)))
        {
            // Chrono: stop at old time, restart at new time
            if (this.dChronoStart != 0)
            {
                // Correction de bug quand on iconifie un objet Clock qui est mis en Stop
                this.lChrono += (long) Math.abs((int) (this.dChronoCounter - this.dChronoStart));
                this.dChronoStart = dCurrentChronoCounter;
            }
        }
        this.dChronoCounter = dCurrentChronoCounter;
        switch (this.sType)
        {
            case ANALOG_CLOCK:
            case DIGITAL_CLOCK:
            case INVISIBLE:
                if (this.lastRecordedTime.get(Calendar.SECOND) != cTime.get(Calendar.SECOND))
                {
                    this.sEventCount = (short) rh.rh4EventCount;
                    this.lastRecordedTime.set(Calendar.SECOND, cTime.get(Calendar.SECOND));
                    ho.pushEvent(kcclockCnds.CND_NEWSECOND, ho.getEventParam());
                    if (this.lastRecordedTime.get(Calendar.MINUTE) != cTime.get(Calendar.MINUTE))
                    {
                        this.sEventCount = (short) rh.rh4EventCount;
                        this.lastRecordedTime.set(Calendar.MINUTE, cTime.get(Calendar.MINUTE));
                        ho.pushEvent(kcclockCnds.CND_NEWMINUTE, ho.getEventParam());
                        if (this.lastRecordedTime.get(Calendar.HOUR) != cTime.get(Calendar.HOUR))
                        {
                            this.sEventCount = (short) rh.rh4EventCount;
                            this.lastRecordedTime.set(Calendar.HOUR, cTime.get(Calendar.HOUR));
                            ho.pushEvent(kcclockCnds.CND_NEWHOUR, ho.getEventParam());
                        }
                    }
                }
                break;
            case CALENDAR:
                if (this.lastRecordedTime.get(Calendar.HOUR) != cTime.get(Calendar.HOUR))
                {
                    this.lastRecordedTime.set(Calendar.HOUR, cTime.get(Calendar.HOUR));
                    if (this.lastRecordedTime.get(Calendar.DAY_OF_MONTH) != cTime.get(Calendar.DAY_OF_MONTH))
                    {
                        this.sEventCount = (short) rh.rh4EventCount;
                        this.lastRecordedTime.set(Calendar.DAY_OF_MONTH, cTime.get(Calendar.DAY_OF_MONTH));
                        ho.pushEvent(kcclockCnds.CND_NEWDAY, ho.getEventParam());
                        if (this.lastRecordedTime.get(Calendar.MONTH) != cTime.get(Calendar.MONTH))
                        {
                            this.sEventCount = (short) rh.rh4EventCount;
                            this.lastRecordedTime.set(Calendar.MONTH, cTime.get(Calendar.MONTH));
                            ho.pushEvent(kcclockCnds.CND_NEWMONTH, ho.getEventParam());
                            if (this.lastRecordedTime.get(Calendar.YEAR) != cTime.get(Calendar.YEAR))
                            {
                                this.sEventCount = (short) rh.rh4EventCount;
                                this.lastRecordedTime.set(Calendar.YEAR, cTime.get(Calendar.YEAR));
                                ho.pushEvent(kcclockCnds.CND_NEWYEAR, ho.getEventParam());
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
        this.lastRecordedTime.setTime(cTime.getTime());
        return REFLAG_DISPLAY;
    }

    @Override
	public void displayRunObject()
    {
        GLRenderer renderer = GLRenderer.inst;

        CRun rhPtr = ho.hoAdRunHeader;
        CRect rc = new CRect();
        if (this.sVisible)
        {
            // Compute coordinates
            rc.left = this.ho.hoX - rhPtr.rhWindowX;
            rc.right = rc.left + this.ho.hoImgWidth;
            rc.top = this.ho.hoY - rhPtr.rhWindowY;
            rc.bottom = rc.top + this.ho.hoImgHeight;

            renderer.pushBase(rc.left, rc.top, rc.right - rc.left, rc.bottom - rc.top);
             
            int ampm=this.lastRecordedTime.get(Calendar.AM_PM);
            short hour = (short) this.lastRecordedTime.get(Calendar.HOUR_OF_DAY);
            short hsecond = (short) (this.lastRecordedTime.get(Calendar.MILLISECOND) / 10);
            short minute = (short) this.lastRecordedTime.get(Calendar.MINUTE);
            short second = (short) this.lastRecordedTime.get(Calendar.SECOND);
            short day = (short) this.lastRecordedTime.get(Calendar.DAY_OF_MONTH);
            short year = (short) this.lastRecordedTime.get(Calendar.YEAR);
            short month = (short) (this.lastRecordedTime.get(Calendar.MONTH) + 1);
            short dayofweek = (short) (this.lastRecordedTime.get(Calendar.DAY_OF_WEEK) - 1);

            hour = (short)((hour == 0 && this.sDigitalClockType < 2) ? 12 : hour);

            switch (this.sType)
            {
                case ANALOG_CLOCK: // Analogue clock
                    if (CLOCK == this.sClockMode)
                    {
                        if (hour > 11)
                        {
                            hour -= 12;
                        }
                        if (this.sAnalogClockMarkerType != 2)
                        {
                            CRect rcNewRect = new CRect();
                            rcNewRect.left = (this.sMinWidth / 2);
                            rcNewRect.right = (rc.right - rc.left) - (this.sMinWidth / 2);
                            rcNewRect.top = (this.sMinHeight / 2);
                            rcNewRect.bottom = (rc.bottom - rc.top) - (this.sMinHeight / 2);
                            RunDisplayAnalogTime(hour, minute, second, rcNewRect, rc);
                        }
                        else
                        {
                            RunDisplayAnalogTime(hour, minute, second, rc, rc);
                        }
                    }
                    else
                    {
                        int lCurrentChrono;
                        int usHour, usMinute, usSecond;

                        // 	Get current chrono
                        if (this.dChronoStart != 0)
                        {
                            double dChronoStop = this.months[month - 1] + (day * 8640000) + (hour * 360000) + (minute * 6000) + (second * 100) + hsecond;
                            lCurrentChrono = this.lChrono + (int) (dChronoStop - this.dChronoStart);
                        }
                        else
                        {
                            lCurrentChrono = this.lChrono;
                        }

                        // Countdown
                        if (COUNTDOWN == this.sClockMode)
                        {
                            lCurrentChrono = this.lCountdownStart - lCurrentChrono;
                            if (lCurrentChrono < 0)
                            {
                                lCurrentChrono = 0;
                            }
                        }

                        // Compute hours, minutes & seconds
                        usHour = (int) (lCurrentChrono / 360000L);
                        if (usHour > 11)
                        {
                            usHour -= 12;
                        }
                        usMinute = (int) ((lCurrentChrono - (usHour * 360000L)) / 6000L);
                        usSecond = (int) ((lCurrentChrono - (usHour * 360000L) - (usMinute * 6000L)) / 100L);

                        // Display
                        if (this.sAnalogClockMarkerType != 2)
                        {
                            CRect rcNewRect = new CRect();
                            rcNewRect.left = (this.sMinWidth / 2);
                            rcNewRect.right = (rc.right - rc.left) - (this.sMinWidth / 2);
                            rcNewRect.top = (this.sMinHeight / 2);
                            rcNewRect.bottom = (rc.bottom - rc.top) - (this.sMinHeight / 2);
                            RunDisplayAnalogTime(usHour, usMinute, usSecond, rcNewRect, rc);
                        }
                        else
                        {
                            RunDisplayAnalogTime(usHour, usMinute, usSecond, rc, rc);
                        }
                    }
                    break;

                case DIGITAL_CLOCK: // Digital clock
                {
                    String szTime;

                    switch (this.sDigitalClockType)
                    {
                        case 0:
                            if (CLOCK == this.sClockMode)
                            {
                                if (hour > 12)
                                {
                                    hour -= 12;
                                }
                                //szTime = new Short(hour).toString();
                                //szTime = pad (szTime, 2);
                                //String tempMin = new Short(minute).toString();
                                //tempMin = pad (tempMin, 2);
                                //szTime += ":" + tempMin;
                                szTime = String.format("%02d:%02d %s", hour, minute, (ampm != 0 ? "p.m. ":"a.m. "));
                                RunDisplayDigitalTime(szTime);
                            }
                            else
                            {
                                int lCurrentChrono;
                                int usHour, usMinute;
                                // Get current chrono
                                if (this.dChronoStart != 0)
                                {
                                    double dChronoStop = this.months[month - 1] + (day * 8640000) + (hour * 360000) + (minute * 6000) + (second * 100) + hsecond;
                                    lCurrentChrono = this.lChrono + (int) (dChronoStop - this.dChronoStart);
                                }
                                else
                                {
                                    lCurrentChrono = this.lChrono;
                                }
                                // Countdown
                                if (COUNTDOWN == this.sClockMode)
                                {
                                    lCurrentChrono = this.lCountdownStart - lCurrentChrono;
                                    if (lCurrentChrono < 0)
                                    {
                                        lCurrentChrono = 0;
                                    }
                                }
                                // Compute hours, minutes & seconds
                                usHour = (int) (lCurrentChrono / 360000L);
                                if (usHour > 11)
                                {
                                    usHour -= 12;
                                }
                                usMinute = (int) ((lCurrentChrono - (usHour * 360000L)) / 6000L);

                                //szTime = Integer.valueOf(usHour).toString();
                                //szTime = pad (szTime, 2);
                                //String tempMin = Integer.valueOf(usMinute).toString();
                                //tempMin = pad (tempMin, 2);
                                //szTime += ":" + tempMin;
                                szTime = String.format("%02d:%02d", usHour, usMinute);
                                RunDisplayDigitalTime(szTime);
                            }
                            break;

                        case 1:
                            if (CLOCK == this.sClockMode)
                            {
                                if (hour > 12)
                                {
                                    hour -= 12;
                                }
                                //szTime = new Short(hour).toString();
                                //szTime = pad (szTime, 2);
                                //String tempMin = new Short(minute).toString();
                                //tempMin = pad (tempMin, 2);
                                //String tempSec = new Short(second).toString();
                                //tempSec = pad (tempSec, 2);
                                //szTime += ":" + tempMin + ":" + tempSec + " " + (ampm != 0 ? "p.m. ":"a.m. ");
                                szTime = String.format("%02d:%02d:%02d %s", hour, minute, second, (ampm != 0 ? "p.m. ":"a.m. "));
                                RunDisplayDigitalTime(szTime);
                            }
                            else
                            {
                                int lCurrentChrono;
                                int usHour, usMinute, usSecond;
                                // Get current chrono
                                if (this.dChronoStart != 0)
                                {
                                    double dChronoStop = this.months[month - 1] + (day * 8640000) + (hour * 360000) + (minute * 6000) + (second * 100) + hsecond;
                                    lCurrentChrono = this.lChrono + (int) (dChronoStop - this.dChronoStart);
                                }
                                else
                                {
                                    lCurrentChrono = this.lChrono;
                                }
                                // Countdown
                                if (COUNTDOWN == this.sClockMode)
                                {
                                    lCurrentChrono = this.lCountdownStart - lCurrentChrono;
                                    if (lCurrentChrono < 0)
                                    {
                                        lCurrentChrono = 0;
                                    }
                                }
                                // Compute hours, minutes & seconds
                                usHour = (int) (lCurrentChrono / 360000L);
                                if (usHour > 11)
                                {
                                    usHour -= 12;
                                }
                                usMinute = (int) ((lCurrentChrono - (usHour * 360000L)) / 6000L);
                                usSecond = (int) ((lCurrentChrono - (usHour * 360000L) - (usMinute * 6000L)) / 100L);

                                // Display
                                if (usHour > 11)
                                {
                                    usHour -= 12;
                                }
                                //szTime = Integer.valueOf(usHour).toString();
                                //szTime = pad (szTime, 2);
                                //String tempMin = Integer.valueOf(usMinute).toString();
                                //tempMin = pad (tempMin, 2);
                                //String tempSec = Integer.valueOf(usSecond).toString();
                                //tempSec = pad (tempSec, 2);
                                //szTime += ":" + tempMin + ":" + tempSec;
                                szTime = String.format("%02d:%02d:%02d", usHour, usMinute, usSecond);
                                RunDisplayDigitalTime(szTime);
                            }
                            break;

                        case 2:
                            if (CLOCK == this.sClockMode)
                            {
                                if (ampm!=0 && hour<12)
                                {
                                    hour+=12;
                                }
                                // Display
                                //szTime = new Short(hour).toString();
                                //szTime = pad (szTime, 2);
                                //String tempMin = new Short(minute).toString();
                                //tempMin = pad (tempMin, 2);
                                //szTime += ":" + tempMin;
                                szTime = String.format("%02d:%02d", hour, minute);
                                RunDisplayDigitalTime(szTime);
                            }
                            else
                            {
                                int lCurrentChrono;
                                int usHour, usMinute;

                                // Get current chrono
                                if (this.dChronoStart != 0)
                                {
                                    double dChronoStop = this.months[month - 1] + (day * 8640000) + (hour * 360000) + (minute * 6000) + (second * 100) + hsecond;
                                    lCurrentChrono = this.lChrono + (int) (dChronoStop - this.dChronoStart);
                                }
                                else
                                {
                                    lCurrentChrono = this.lChrono;
                                }

                                // Countdown
                                if (COUNTDOWN == this.sClockMode)
                                {
                                    lCurrentChrono = this.lCountdownStart - lCurrentChrono;
                                    if (lCurrentChrono < 0)
                                    {
                                        lCurrentChrono = 0;
                                    }
                                }

                                // Compute hours, minutes & seconds
                                usHour = (int) (lCurrentChrono / 360000L);
                                usMinute = (int) ((lCurrentChrono - (usHour * 360000L)) / 6000L);

                                // Display
                                //szTime = Integer.valueOf(usHour).toString();
                                //szTime = pad (szTime, 2);
                                //String tempMin = Integer.valueOf(usMinute).toString();
                                //tempMin = pad (tempMin, 2);
                                //szTime += ":" + tempMin;
                                szTime = String.format("%02d:%02d", usHour, usMinute);
                                RunDisplayDigitalTime(szTime);
                            }
                            break;

                        case 3:
                            if (CLOCK == this.sClockMode)
                            {
                                if (ampm!=0 && hour<12)
                                {
                                    hour+=12;
                                }
                                // Display
                                //szTime = new Short(hour).toString();
                                //szTime = pad (szTime, 2);
                                //String tempMin = new Short(minute).toString();
                                //tempMin = pad (tempMin, 2);
                                //String tempSec = new Short(second).toString();
                                //tempSec = pad (tempSec, 2);
                                //szTime += ":" + tempMin + ":" + tempSec;
                                szTime = String.format("%02d:%02d:%02d", hour, minute, second);
                                RunDisplayDigitalTime(szTime);
                            }
                            else
                            {
                                int lCurrentChrono;
                                int usHour, usMinute, usSecond;
                                // Get current chrono
                                if (this.dChronoStart != 0)
                                {
                                    double dChronoStop = this.months[month - 1] + (day * 8640000) + (hour * 360000) + (minute * 6000) + (second * 100) + hsecond;
                                    lCurrentChrono = this.lChrono + (int) (dChronoStop - this.dChronoStart);
                                }
                                else
                                {
                                    lCurrentChrono = this.lChrono;
                                }

                                // Countdown
                                if (COUNTDOWN == this.sClockMode)
                                {
                                    lCurrentChrono = this.lCountdownStart - lCurrentChrono;
                                    if (lCurrentChrono < 0)
                                    {
                                        lCurrentChrono = 0;
                                    }
                                }

                                // Compute hours, minutes & seconds
                                usHour = (int) (lCurrentChrono / 360000L);
                                usMinute = (int) ((lCurrentChrono - (usHour * 360000L)) / 6000L);
                                usSecond = (int) ((lCurrentChrono - (usHour * 360000L) - (usMinute * 6000L)) / 100L);

                                // Display
                                //szTime = Integer.valueOf(usHour).toString();
                                //szTime = pad (szTime, 2);
                                //String tempMin = Integer.valueOf(usMinute).toString();
                                //tempMin = pad (tempMin, 2);
                                //String tempSec = Integer.valueOf(usSecond).toString();
                                //tempSec = pad (tempSec, 2);
                                //szTime += ":" + tempMin + ":" + tempSec+ ":" + tempSec;
                                szTime = String.format("%02d:%02d:%02d", usHour, usMinute, usSecond);
                                RunDisplayDigitalTime(szTime);
                            }
                            break;

                        default:
                            break;
                    }
                    break;
                }

                case CALENDAR: // Calendar
                    String szDate;
                    switch (this.sCalendarType)
                    {
                        case SHORTDATE:
                            szDate = ComputeDate(year, month, day, dayofweek, DateFormat.getDateInstance(DateFormat.SHORT));
//                            GetProfileString("Intl", "sShortDate", "dd/MM/yyyy", szFormat, sizeof(szFormat));
//                                ComputeDate((short)RunDate.year, (short)RunDate.month, (short)RunDate.day, (short)RunDate.dayofweek, szFormat, szDate);
                            RunDisplayCalendar(szDate, rc);
                            break;

                        case LONGDATE:
                            szDate = ComputeDate(year, month, day, dayofweek, DateFormat.getDateInstance(DateFormat.FULL));
//                            GetProfileString("Intl", "sLongDate", "dddd, mm/yy", szFormat, sizeof(szFormat));
//                                ComputeDate((short)RunDate.year, (short)RunDate.month, (short)RunDate.day, (short)RunDate.dayofweek, szFormat, szDate);
                            RunDisplayCalendar(szDate, rc);
                            break;

                        case FIXEDDATE:
                            SimpleDateFormat sdf = new SimpleDateFormat(this.FORMAT[this.sCalendarFormat], Locale.getDefault());
                            szDate = ComputeDate(year, month, day, dayofweek, sdf);
//                            LoadString(hInstLib, M_FORMAT1 + rdPtr->sCalendarFormat, szFormat, sizeof(szFormat));
//                                ComputeDate((short)RunDate.year, (short)RunDate.month, (short)RunDate.day, (short)RunDate.dayofweek, szFormat, szDate);
                            RunDisplayCalendar(szDate, rc);
                            break;

                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
            renderer.popBase();
        }
    }

    private void RunDisplayAnalogTime(int sHour, int sMinutes, int sSeconds, CRect rc, CRect bRect)
    {
        GLRenderer renderer = GLRenderer.inst;

        Point pntPoints[] =
        {
            new Point(), new Point(), new Point()
        };
        
        Typeface hFont;
        //int textWidth;
        //int textHeight;
        
        Paint p;
        /*
        Typeface hFont;
        int textWidth;
        int textHeight;
        // Create font
        if (null == this.sFont)
        {
            return;
        }
        hFont = this.sFont.createFont();
        Paint p = textSurface.textPaint;
        p.setTypeface(hFont);
        p.setTextAlign(Align.CENTER);
        p.setColor(this.crFont);
        textHeight = (int) CServices.paintTextHeight(p);
        textWidth = (int) p.measureText("XX");
        */
        short align = CServices.DT_CENTER;
        int sRayon;
        // Set radius
        if ((rc.right - rc.left) > (rc.bottom - rc.top))
        {
            sRayon = ((rc.bottom - rc.top) / 2);
        }
        else
        {
            sRayon = ((rc.right - rc.left) / 2);
        }
        sRayon--;
               
        if(updateAnalog) {
	       	textHeight = (int) getAnalogTextHeight(textSurface.textPaint, this.sFont.createFont());       
	       	textWidth = (int) getAnalogTextWidth(textSurface.textPaint, this.sFont.createFont(), "WW1");       
        }
        
       	// Set center
        pntPoints[0].y = (rc.bottom - rc.top) / 2 +textHeight + ADJ; 
        pntPoints[0].x = (rc.right - rc.left) / 2 +textWidth*2/4; 
        this.sCenterX = (short) pntPoints[0].x;
        this.sCenterY = (short) pntPoints[0].y;
        
        // Display hours
        if (true == this.sAnalogClockHours)
        {
            pntPoints[1].x = pntPoints[0].x + (int) (Math.cos(((sHour + sMinutes / 60.0) * 0.523) - 1.570) * (sRayon / 1.5));
            pntPoints[1].y = pntPoints[0].y + (int) (Math.sin(((sHour + sMinutes / 60.0) * 0.523) - 1.570) * (sRayon / 1.5));
            this.sHourX = (short) pntPoints[1].x;
            this.sHourY = (short) pntPoints[1].y;

            renderer.renderLine(pntPoints[0].x, pntPoints[0].y, pntPoints[1].x, pntPoints[1].y, this.crAnalogClockHours, 2);
        }
        // Display minutes
        if (true == this.sAnalogClockMinutes)
        {
            pntPoints[1].x = pntPoints[0].x + (int) (Math.cos((sMinutes * 0.104) - 1.570) * sRayon);
            pntPoints[1].y = pntPoints[0].y + (int) (Math.sin((sMinutes * 0.104) - 1.570) * sRayon);
            this.sMinuteX = (short) pntPoints[1].x;
            this.sMinuteY = (short) pntPoints[1].y;

            renderer.renderLine(pntPoints[0].x, pntPoints[0].y, pntPoints[1].x, pntPoints[1].y, this.crAnalogClockMinutes, 2);
            
        }
        // Display seconds
        if (true == this.sAnalogClockSeconds)
        {
            pntPoints[1].x = pntPoints[0].x + (int) (Math.cos((sSeconds * 0.104) - 1.570) * sRayon);
            pntPoints[1].y = pntPoints[0].y + (int) (Math.sin((sSeconds * 0.104) - 1.570) * sRayon);
            this.sSecondX = (short) pntPoints[1].x;
            this.sSecondY = (short) pntPoints[1].y;

            renderer.renderLine(pntPoints[0].x, pntPoints[0].y, pntPoints[1].x, pntPoints[1].y, this.crAnalogClockSeconds, 2);
        }

        // Draw lines
        if (true == this.sAnalogClockLines)
        {
            //WinPen(rhPtr->rhIdEditWin, rdPtr->crFont, PS_SOLID, 2);	
            for (int a = 1; a < 13; a++)
            {
                pntPoints[1].x = pntPoints[0].x + (int) (Math.cos((a * 0.523) - 1.570) * (sRayon * 0.9));
                pntPoints[1].y = pntPoints[0].y + (int) (Math.sin((a * 0.523) - 1.570) * (sRayon * 0.9));
                pntPoints[2].x = pntPoints[0].x + (int) (Math.cos((a * 0.523) - 1.570) * (sRayon * 1.0));
                pntPoints[2].y = pntPoints[0].y + (int) (Math.sin((a * 0.523) - 1.570) * (sRayon * 1.0));

                renderer.renderLine(pntPoints[1].x, pntPoints[1].y, pntPoints[2].x, pntPoints[2].y, this.crFont, 2);
            }
        }

        // Draw markers
    	if(!updateAnalog && sAnalogClockMarkerType != 2)
    	{
    		textSurface.draw(0, 0, 0, -1);
    	}
    	else if (this.sAnalogClockMarkerType != 2)
        {
        	updateAnalog = false;
            String szString;
            CRect rcFont = new CRect();

            // Create font
            if (null == this.sFont)
            {
                return;
            }
            hFont = this.sFont.createFont();
            
            textSurface.manualClear(crFont);

            //p = textSurface.textPaint;
            //p.setTypeface(hFont);
            //p.setTextAlign(Align.CENTER);
            //p.setColor(this.crFont);
            
            //Canvas c = textSurface.textCanvas;

            //textHeight = getAnalogTextHeight(p, hFont);

            // Display
            for (int a = 1; a < 13; a++)
            {
                int x, y;
                if (0 == this.sAnalogClockMarkerType)
                {
                    szString = Integer.valueOf(a).toString();
                }
                else
                {
                    szString = szRoman[a - 1];
                }

                //textWidth = (int) p.measureText("WW");
                
                x = pntPoints[0].x + (int) (Math.cos((a * 0.523) - 1.570) * sRayon);
                y = pntPoints[0].y + (int) (Math.sin((a * 0.523) - 1.570) * sRayon) - ADJ;
                switch (a)
                {
                    case 1:
                    case 2:
                        rcFont.left = x+1;
                        rcFont.bottom = y+1;
                        rcFont.right = rcFont.left + textWidth;
                        rcFont.top = rcFont.bottom - textHeight;
                        align = CServices.DT_LEFT;
                        break;

                    case 3:
                        rcFont.left = x + 2;
                        rcFont.top = y - (textHeight / 2);
                        rcFont.right = rcFont.left + textWidth;
                        rcFont.bottom = rcFont.top + textHeight;
                        align = CServices.DT_LEFT;
                        break;

                    case 4:
                    case 5:
                        rcFont.left = x;
                        rcFont.top = y;
                        rcFont.right = rcFont.left + textWidth;
                        rcFont.bottom = rcFont.top + textHeight;
                        align = CServices.DT_LEFT;
                        break;

                    case 6:
                        rcFont.left = x - (textWidth / 2);
                        rcFont.top = y + 1;
                        rcFont.right = rcFont.left + textWidth;
                        rcFont.bottom = rcFont.top + textHeight;
                        align = CServices.DT_CENTER;
                        break;

                    case 7:
                    case 8:
                        rcFont.right = x-1;
                        rcFont.top = y-1;
                        rcFont.left = rcFont.right - textWidth;
                        rcFont.bottom = rcFont.top + textHeight;
                        align = CServices.DT_CENTER;
                        break;

                    case 9:
                        rcFont.right = x - 2;
                        rcFont.top = y - (textHeight / 2);
                        rcFont.left = rcFont.right - textWidth;
                        rcFont.bottom = rcFont.top + textHeight;
                        align = CServices.DT_CENTER;
                        break;

                    case 10:
                    case 11:
                        rcFont.right = x;
                        rcFont.bottom = y;
                        rcFont.left = rcFont.right - textWidth;
                        rcFont.top = rcFont.bottom - textHeight;
                        align = CServices.DT_CENTER;
                        break;

                    case 12:
                        rcFont.left = x - (textWidth / 2);
                        rcFont.bottom = y - 1;
                        rcFont.right = rcFont.left + textWidth;
                        rcFont.top = rcFont.bottom - textHeight;
                        align = CServices.DT_CENTER;
                        break;
                }
                
                CRect drawRc = new CRect();
                
                drawRc.left = rcFont.left + (rcFont.right - rcFont.left) / 2 - textWidth/4;
                drawRc.top = rcFont.top + (rcFont.bottom - rcFont.top) / 2 - textHeight/2;
                drawRc.right = drawRc.left + textWidth;
                drawRc.bottom = drawRc.top + textHeight + ADJ;
                
                //Log.Log("X:"+drawRc.left+" Y:"+drawRc.top+" Right:"+drawRc.right+" Bottom:"+drawRc.bottom+" S:"+szString);
                
                textSurface.manualDrawText(szString,
                		(short) (align | CServices.DT_VCENTER
                				| CServices.DT_SINGLELINE), drawRc, this.crFont, this.sFont, false);
             }
            
            //if (this.sClockBorder)
            //	c.drawCircle(pntPoints[0].x, pntPoints[0].y, sRayon + ADJ, p);
           
            textSurface.updateTexture();
            textSurface.draw(0, 0, 0, -1);
        }
    }

    private void RunDisplayDigitalTime(String szTime)
    {
    	/*
    	 * Typeface hFont;
        if (null == this.sFont)
        {
            return;
        }
        hFont = this.sFont.createFont();

        Paint p = textSurface.textPaint;
        
        p.setTypeface(hFont);

        int x = rc.left + (rc.right - rc.left) / 2 - ((int) p.measureText(szTime)) / 2;
        int y = rc.top + (rc.bottom - rc.top) / 2 + CServices.paintTextHeight(p) / 2 - ADJ; */
        textSurface.setDimension(ho.hoImgWidth, ho.hoImgHeight);
        textSurface.resize(ho.hoImgWidth, ho.hoImgHeight, false);
        textSurface.setText(szTime, (short) (CServices.DT_CENTER | CServices.DT_VCENTER), crFont, this.sFont, false);
        textSurface.draw(0, 0, 0, -1);
        
    	// Draw border if needed

        if (this.sClockBorder)
        	GLRenderer.inst.renderRect(0, 0, ho.hoImgWidth - 2, ho.hoImgHeight - 2, this.crFont, 2);
    }

    private String ComputeDate(short sYear, short sMonth, short sDayOfMonth, short sDayOfWeek, DateFormat df)
    {
        Calendar c = Calendar.getInstance();
        c.set(sYear, sMonth - 1, sDayOfMonth);
        return df.format(c.getTime());
    }

    private void RunDisplayCalendar(String szDate, CRect rc)
    {
     /*   Typeface hFont;
        if (null == this.sFont)
        {
            return;
        }
        hFont = this.sFont.createFont();


        Paint p = textSurface.getPaint();
        
        p.setTypeface(hFont);

        int x = rc.left + (rc.right - rc.left) / 2 - ((int) p.measureText(szDate)) / 2;
        int y = rc.top + (rc.bottom - rc.top) / 2 + CServices.paintTextHeight(p) / 2 - ADJ; */
        
        textSurface.setDimension(ho.hoImgWidth, ho.hoImgHeight);
        textSurface.resize(ho.hoImgWidth, ho.hoImgHeight, false);
        textSurface.setText(szDate, (short) (CServices.DT_CENTER | CServices.DT_VCENTER), this.crFont, this.sFont, false);

        textSurface.draw(0, 0, 0, -1);
     }

    /** Returns the font used by the digital clock.
     */
    @Override
	public CFontInfo getRunObjectFont()
    {
         return this.sFont;
    }

    /** Sets the font used by the digital clock.
     */
    @Override
	public void setRunObjectFont(CFontInfo info, CRect pRc)
    {
        if (this.sType == DIGITAL_CLOCK ||
        		this.sType == CALENDAR)	// DIGITAL CLOCK
        {
            sFont = info;
            if (pRc != null)
            {
                ho.hoImgWidth = pRc.right - pRc.left;
                ho.hoImgHeight = pRc.bottom - pRc.top;
            }
            ho.modif();
            ho.roc.rcChanged = true;
        }
    }

    /** Returns the color of the font.
     */
    @Override
	public int getRunObjectTextColor()
    {
        return crFont;
    }

    /** Changes the font color.
     */
    @Override
	public void setRunObjectTextColor(int rgb)
    {
        crFont = rgb;
        ho.modif();
        ho.roc.rcChanged = true;
    }
    
    @Override
	public boolean condition(int num, CCndExtension cnd)
    {
        return conditions.get(num, cnd);
    }
    
    @Override
	public void action(int num, CActExtension act)
    {
        actions.action(num, act);
    }
    
    @Override
	public CValue expression(int num)
    {
        return expressions.get(num);
    }
    
    ///////////////////////////////////////////////////////////////////
    
    private int getAnalogTextHeight(Paint p, Typeface t){
        p = textSurface.textPaint;
        p.setTypeface(t);
        p.setTextAlign(Align.CENTER);
        p.setColor(this.crFont);
        return CServices.paintTextHeight(p);
    }
    
    private int getAnalogTextWidth(Paint p, Typeface t, String s){
        p = textSurface.textPaint;
        p.setTypeface(t);
        p.setTextAlign(Align.LEFT);
        p.setColor(this.crFont);
        return (int) p.measureText(s);
    }
   
}
