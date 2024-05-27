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

/**
 * by Greyhill
 * @author Admin
 */
import java.util.Calendar;

import Expressions.CValue;

public class kcclockExpr
{
    static final int EXP_GETCENTIEMES = 0;
    static final int EXP_GETSECONDES = 1;
    static final int EXP_GETMINUTES = 2;
    static final int EXP_GETHOURS = 3;
    static final int EXP_GETDAYOFWEEK = 4;
    static final int EXP_GETDAYOFMONTH = 5;
    static final int EXP_GETMONTH = 6;
    static final int EXP_GETYEAR = 7;
    static final int EXP_GETCHRONO = 8;
    static final int EXP_GETCENTERX = 9;
    static final int EXP_GETCENTERY = 10;
    static final int EXP_GETHOURX = 11;
    static final int EXP_GETHOURY = 12;
    static final int EXP_GETMINUTEX = 13;
    static final int EXP_GETMINUTEY = 14;
    static final int EXP_GETSECONDX = 15;
    static final int EXP_GETSECONDY = 16;
    static final int EXP_GETCOUNTDOWN = 17;
    static final int EXP_GETXPOSITION = 18;
    static final int EXP_GETYPOSITION = 19;
    static final int EXP_GETXSIZE = 20;
    static final int EXP_GETYSIZE = 21;
    CRunkcclock thisObject;

    public kcclockExpr(CRunkcclock object)
    {
        thisObject = object;
    }

    public CValue get(int num)
    {
        switch (num)
        {
            case EXP_GETCENTIEMES:
                return GetCentiemes();
            case EXP_GETSECONDES:
                return GetSeconds();
            case EXP_GETMINUTES:
                return GetMinutes();
            case EXP_GETHOURS:
                return GetHours();
            case EXP_GETDAYOFWEEK:
                return GetDayOfWeek();
            case EXP_GETDAYOFMONTH:
                return GetDayOfMonth();
            case EXP_GETMONTH:
                return GetMonth();
            case EXP_GETYEAR:
                return GetYear();
            case EXP_GETCHRONO:
                return GetChrono();
            case EXP_GETCENTERX:
                return GetCentreX();
            case EXP_GETCENTERY:
                return GetCentreY();
            case EXP_GETHOURX:
                return GetHourX();
            case EXP_GETHOURY:
                return GetHourY();
            case EXP_GETMINUTEX:
                return GetMinuteX();
            case EXP_GETMINUTEY:
                return GetMinuteY();
            case EXP_GETSECONDX:
                return GetSecondX();
            case EXP_GETSECONDY:
                return GetSecondY();
            case EXP_GETCOUNTDOWN:
                return GetCountdown();
            case EXP_GETXPOSITION:
                return GetXPosition();
            case EXP_GETYPOSITION:
                return GetYPosition();
            case EXP_GETXSIZE:
                return GetXSize();
            case EXP_GETYSIZE:
                return GetYSize();
        }
        return new CValue(0);//won't happen
    }

    private CValue GetCentiemes()
    {
		this.thisObject.tempValue.forceInt(this.thisObject.getCurrentTime().get(Calendar.MILLISECOND) / 10);
		return this.thisObject.tempValue;
    }

    private CValue GetSeconds()
    {
		this.thisObject.tempValue.forceInt(this.thisObject.getCurrentTime().get(Calendar.SECOND));
		return this.thisObject.tempValue;
    }

    private CValue GetMinutes()
    {
		this.thisObject.tempValue.forceInt(this.thisObject.getCurrentTime().get(Calendar.MINUTE));
		return this.thisObject.tempValue;
    }

    private CValue GetHours()
    {
        Calendar c = this.thisObject.getCurrentTime();
        int hour=c.get(Calendar.HOUR);
        int ampm=c.get(Calendar.AM_PM);
        if (ampm!=0 && hour<12)
        {
            hour+=12;
        }
		this.thisObject.tempValue.forceInt(hour);
		return this.thisObject.tempValue;
    }

    private CValue GetDayOfWeek()
    {
		this.thisObject.tempValue.forceInt(this.thisObject.getCurrentTime().get(Calendar.DAY_OF_WEEK) - 1);
		return this.thisObject.tempValue;
    }

    private CValue GetDayOfMonth()
    {
		this.thisObject.tempValue.forceInt(this.thisObject.getCurrentTime().get(Calendar.DAY_OF_MONTH));
		return this.thisObject.tempValue;
    }

    private CValue GetMonth()
    {
		this.thisObject.tempValue.forceInt(this.thisObject.getCurrentTime().get(Calendar.MONTH) + 1);
		return this.thisObject.tempValue;
    }

    private CValue GetYear()
    {
		this.thisObject.tempValue.forceInt(this.thisObject.getCurrentTime().get(Calendar.YEAR));
		return this.thisObject.tempValue;
    }

    private CValue GetChrono()
    {
        if (this.thisObject.dChronoStart != 0)
        {
            Calendar c = this.thisObject.getCurrentTime();
            double dChronoStop = thisObject.months[c.get(Calendar.MONTH)] +
                    ((c.get(Calendar.DAY_OF_MONTH)) * 8640000) + (c.get(Calendar.HOUR_OF_DAY) * 360000) +
                    (c.get(Calendar.MINUTE) * 6000) + (c.get(Calendar.SECOND) * 100) + (c.get(Calendar.MILLISECOND) / 10);
			this.thisObject.tempValue.forceInt(this.thisObject.lChrono + (int) (dChronoStop - this.thisObject.dChronoStart));
        }
        else
        {
			this.thisObject.tempValue.forceInt(this.thisObject.lChrono);
        }
		return this.thisObject.tempValue;
    }

    private CValue GetCentreX()
    {
        if (CRunkcclock.ANALOG_CLOCK == this.thisObject.sType)
        {
			this.thisObject.tempValue.forceInt(this.thisObject.sCenterX + this.thisObject.rh.rhWindowX);
        }
        else
        {
			this.thisObject.tempValue.forceInt(0);
        }
		return this.thisObject.tempValue;
    }

    private CValue GetCentreY()
    {
        if (CRunkcclock.ANALOG_CLOCK == this.thisObject.sType)
        {
			this.thisObject.tempValue.forceInt(this.thisObject.sCenterY + this.thisObject.rh.rhWindowY);
        }
        else
        {
			this.thisObject.tempValue.forceInt(0);
        }
		return this.thisObject.tempValue;
    }

    private CValue GetHourX()
    {
        if (CRunkcclock.ANALOG_CLOCK == this.thisObject.sType)
        {
			this.thisObject.tempValue.forceInt(this.thisObject.sHourX + this.thisObject.rh.rhWindowX);
        }
        else
        {
			this.thisObject.tempValue.forceInt(0);
        }
		return this.thisObject.tempValue;
    }

    private CValue GetHourY()
    {
        if (CRunkcclock.ANALOG_CLOCK == this.thisObject.sType)
        {
			this.thisObject.tempValue.forceInt(this.thisObject.sHourY + this.thisObject.rh.rhWindowY);
        }
        else
        {
			this.thisObject.tempValue.forceInt(0);
        }
		return this.thisObject.tempValue;
    }

    private CValue GetMinuteX()
    {
        if (CRunkcclock.ANALOG_CLOCK == this.thisObject.sType)
        {
			this.thisObject.tempValue.forceInt(this.thisObject.sMinuteX + this.thisObject.rh.rhWindowX);
        }
        else
        {
			this.thisObject.tempValue.forceInt(0);
        }
		return this.thisObject.tempValue;
    }

    private CValue GetMinuteY()
    {
        if (CRunkcclock.ANALOG_CLOCK == this.thisObject.sType)
        {
			this.thisObject.tempValue.forceInt(this.thisObject.sMinuteY + this.thisObject.rh.rhWindowY);
        }
        else
        {
			this.thisObject.tempValue.forceInt(0);
        }
		return this.thisObject.tempValue;
    }

    private CValue GetSecondX()
    {
        if (CRunkcclock.ANALOG_CLOCK == this.thisObject.sType)
        {
			this.thisObject.tempValue.forceInt(this.thisObject.sSecondX + this.thisObject.rh.rhWindowX);
        }
        else
        {
			this.thisObject.tempValue.forceInt(0);
        }
		return this.thisObject.tempValue;
    }

    private CValue GetSecondY()
    {
        if (CRunkcclock.ANALOG_CLOCK == this.thisObject.sType)
        {
			this.thisObject.tempValue.forceInt(this.thisObject.sSecondY + this.thisObject.rh.rhWindowY);
        }
        else
        {
			this.thisObject.tempValue.forceInt(0);
        }
		return this.thisObject.tempValue;
    }

    private CValue GetCountdown()
    {
        int lCurrentChrono;
        if (this.thisObject.dChronoStart != 0)
        {
            Calendar c = this.thisObject.getCurrentTime();
            double dChronoStop = thisObject.months[c.get(Calendar.MONTH)] +
                    ((c.get(Calendar.DAY_OF_MONTH)) * 8640000) + (c.get(Calendar.HOUR_OF_DAY) * 360000) +
                    (c.get(Calendar.MINUTE) * 6000) + (c.get(Calendar.SECOND) * 100) + (c.get(Calendar.MILLISECOND) / 10);
            lCurrentChrono = this.thisObject.lCountdownStart - (this.thisObject.lChrono + (int) (dChronoStop - this.thisObject.dChronoStart));
        }
        else
        {
            lCurrentChrono = this.thisObject.lCountdownStart - this.thisObject.lChrono;
        }
        if (lCurrentChrono < 0)
            lCurrentChrono = 0;
		this.thisObject.tempValue.forceInt(lCurrentChrono);
		return this.thisObject.tempValue;
    }

    private CValue GetXPosition()
    {
		this.thisObject.tempValue.forceInt(this.thisObject.ho.getX());
		return this.thisObject.tempValue;
    }

    private CValue GetYPosition()
    {
		this.thisObject.tempValue.forceInt(this.thisObject.ho.getY());
		return this.thisObject.tempValue;
    }

    private CValue GetXSize()
    {
		this.thisObject.tempValue.forceInt(this.thisObject.ho.getWidth());
		return this.thisObject.tempValue;
    }

    private CValue GetYSize()
    {
		this.thisObject.tempValue.forceInt(this.thisObject.ho.getHeight());
		return this.thisObject.tempValue;
    }
}
