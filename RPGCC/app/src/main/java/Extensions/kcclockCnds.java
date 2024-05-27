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
/*
 * date n time OBJECT
 * 
 */

/**
 * by Greyhill
 * @author Admin
 */
import java.util.Calendar;

import Conditions.CCndExtension;
import Objects.CObject;

public class kcclockCnds
{
    static final int CND_CMPCHRONO = 0;
    static final int CND_NEWSECOND = 1;
    static final int CND_NEWMINUTE = 2;
    static final int CND_NEWHOUR = 3;
    static final int CND_NEWDAY = 4;
    static final int CND_NEWMONTH = 5;
    static final int CND_NEWYEAR = 6;
    static final int CND_CMPCOUNTDOWN = 7;
    static final int CND_VISIBLE = 8;
    CRunkcclock thisObject;

    public kcclockCnds(CRunkcclock object)
    {
        thisObject = object;
    }

    public boolean get(int num, CCndExtension cnd)
    {
        switch (num)
        {
            case CND_CMPCHRONO:
                return CmpChrono(cnd);
            case CND_NEWSECOND:
                return NewSecond();
            case CND_NEWMINUTE:
                return NewSecond();
            case CND_NEWHOUR:
                return NewSecond();
            case CND_NEWDAY:
                return NewSecond();
            case CND_NEWMONTH:
                return NewSecond();
            case CND_NEWYEAR:
                return NewSecond();
            case CND_CMPCOUNTDOWN:
                return CmpCountdown(cnd);
            case CND_VISIBLE:
                return IsVisible();
        }
        return false;//won't happen
    }

    private boolean CmpChrono(CCndExtension cnd)
    {
        if (this.thisObject.dChronoStart != 0.0)
        {
            Calendar c = this.thisObject.getCurrentTime();
            double dChronoStop = thisObject.months[c.get(Calendar.MONTH)] +
                    ((c.get(Calendar.DAY_OF_MONTH)) * 8640000) + (c.get(Calendar.HOUR_OF_DAY) * 360000) +
                    (c.get(Calendar.MINUTE) * 6000) + (c.get(Calendar.SECOND) * 100) + (c.get(Calendar.MILLISECOND) / 10);
            return cnd.compareTime(thisObject.rh, 0, ((this.thisObject.lChrono + (int) (dChronoStop - this.thisObject.dChronoStart)) * 10));
        }
        else
        {
            return cnd.compareTime(thisObject.rh, 0, this.thisObject.lChrono * 10);
        }
    }

    private boolean NewSecond()
    {
        if ((this.thisObject.ho.hoFlags & CObject.HOF_TRUEEVENT) != 0)
        {
            return true;
        }
        if (this.thisObject.rh.rh4EventCount == this.thisObject.sEventCount)
        {
            return true;
        }
        return false;
    }

    private boolean CmpCountdown(CCndExtension cnd)
    {
        int lCurrentChrono;
        if (this.thisObject.dChronoStart != 0.0)
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
            return cnd.compareTime(thisObject.rh, 0, lCurrentChrono * 10);
        }
        if (lCurrentChrono < 0)
        {
            lCurrentChrono = 0;
        }
        return cnd.compareTime(thisObject.rh, 0, lCurrentChrono * 10);
    }

    private boolean IsVisible()
    {
        return this.thisObject.sVisible;
    }
}
