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

import Actions.CActExtension;
import Params.CPositionInfo;

public class kcclockActs
{
    static final int ACT_SETCENTIEMES = 0;
    static final int ACT_SETSECONDES = 1;
    static final int ACT_SETMINUTES = 2;
    static final int ACT_SETHOURS = 3;
    static final int ACT_SETDAYOFWEEK = 4;
    static final int ACT_SETDAYOFMONTH = 5;
    static final int ACT_SETMONTH = 6;
    static final int ACT_SETYEAR = 7;
    static final int ACT_RESETCHRONO = 8;
    static final int ACT_STARTCHRONO = 9;
    static final int ACT_STOPCHRONO = 10;
    static final int ACT_SHOW = 11;
    static final int ACT_HIDE = 12;
    static final int ACT_SETPOSITION = 13;
    static final int ACT_SETCOUNTDOWN = 14;
    static final int ACT_STARTCOUNTDOWN = 15;
    static final int ACT_STOPCOUNTDOWN = 16;
    static final int ACT_SETXPOSITION = 17;
    static final int ACT_SETYPOSITION = 18;
    static final int ACT_SETXSIZE = 19;
    static final int ACT_SETYSIZE = 20;
    CRunkcclock thisObject;

    public kcclockActs(CRunkcclock object)
    {
        thisObject = object;
    }

    public void action(int num, CActExtension act)
    {
        switch (num)
        {
            case ACT_SETCENTIEMES:
                SetCentiemes(act.getParamExpression(thisObject.rh, 0));
                break;
            case ACT_SETSECONDES:
                SetSeconds(act.getParamExpression(thisObject.rh, 0));
                break;
            case ACT_SETMINUTES:
                SetMinutes(act.getParamExpression(thisObject.rh, 0));
                break;
            case ACT_SETHOURS:
                SetHours(act.getParamExpression(thisObject.rh, 0));
                break;
            case ACT_SETDAYOFWEEK:
                SetDayOfWeek(act.getParamExpression(thisObject.rh, 0));
                break;
            case ACT_SETDAYOFMONTH:
                SetDayOfMonth(act.getParamExpression(thisObject.rh, 0));
                break;
            case ACT_SETMONTH:
                SetMonth(act.getParamExpression(thisObject.rh, 0));
                break;
            case ACT_SETYEAR:
                SetYear(act.getParamExpression(thisObject.rh, 0));
                break;
            case ACT_RESETCHRONO:
                ResetChrono();
                break;
            case ACT_STARTCHRONO:
                StartChrono();
                break;
            case ACT_STOPCHRONO:
                StopChrono();
                break;
            case ACT_SHOW:
                Show();
                break;
            case ACT_HIDE:
                Hide();
                break;
            case ACT_SETPOSITION:
                SetPosition(act.getParamPosition(thisObject.rh, 0));
                break;
            case ACT_SETCOUNTDOWN:
                SetCountdown(act.getParamTime(thisObject.rh, 0));
                break;
            case ACT_STARTCOUNTDOWN:
                StartCountdown();
                break;
            case ACT_STOPCOUNTDOWN:
                StopCountdown();
                break;
            case ACT_SETXPOSITION:
                SetXPosition(act.getParamExpression(thisObject.rh, 0));
                break;
            case ACT_SETYPOSITION:
                SetYPosition(act.getParamExpression(thisObject.rh, 0));
                break;
            case ACT_SETXSIZE:
                SetXSize(act.getParamExpression(thisObject.rh, 0));
                break;
            case ACT_SETYSIZE:
                SetYSize(act.getParamExpression(thisObject.rh, 0));
                break;
        }
    }

    private void SetCentiemes(int hundredths)
    {
        if ((hundredths >= 0) && (hundredths < 100))
        {
            Calendar c = this.thisObject.getCurrentTime();
            c.set(Calendar.MILLISECOND, hundredths * 10);
            this.thisObject.changeTime(c.getTime());
            this.thisObject.ho.redraw();
        }
    }

    private void SetSeconds(int secs)
    {
        if ((secs >= 0) && (secs < 60))
        {
            Calendar c = this.thisObject.getCurrentTime();
            c.set(Calendar.SECOND, secs);
            this.thisObject.changeTime(c.getTime());
            this.thisObject.ho.redraw();
        }
    }

    private void SetMinutes(int mins)
    {
        if ((mins >= 0) && (mins < 60))
        {
            Calendar c = this.thisObject.getCurrentTime();
            c.set(Calendar.MINUTE, mins);
            this.thisObject.changeTime(c.getTime());
            this.thisObject.ho.redraw();
        }
    }

    private void SetHours(int hours)
    {
        if ((hours >= 0) && (hours < 24))
        {
            Calendar c = this.thisObject.getCurrentTime();
            if(hours >= 12) {
                c.set(Calendar.AM_PM, Calendar.PM);
                hours -= 12;
            }
            else
                c.set(Calendar.AM_PM, Calendar.AM);
            c.set(Calendar.HOUR, hours);
            this.thisObject.changeTime(c.getTime());
            this.thisObject.ho.redraw();
        }
    }

    private void SetDayOfWeek(int day)
    {
        if ((day >= 0) && (day < 7))
        {
            Calendar c = this.thisObject.getCurrentTime();
            c.set(Calendar.DAY_OF_WEEK, day + 1);
            this.thisObject.changeTime(c.getTime());
            this.thisObject.ho.redraw();
        }
    }

    private void SetDayOfMonth(int day)
    {
        if ((day >= 1) && (day < 32)) //1 based from c++
        {
            Calendar c = this.thisObject.getCurrentTime();
            c.set(Calendar.DAY_OF_MONTH, day);
            this.thisObject.changeTime(c.getTime());
            this.thisObject.ho.redraw();
        }
    }

    private void SetMonth(int month)
    {
        if ((month >= 1) && (month < 13)) //1 based from c++
        {
            Calendar c = this.thisObject.getCurrentTime();
            c.set(Calendar.MONTH, month - 1);
            this.thisObject.changeTime(c.getTime());
            this.thisObject.ho.redraw();
        }
    }

    private void SetYear(int year)
    {
        if ((year > 1979) && (year < 2100)) //y2.1k
        {
            Calendar c = this.thisObject.getCurrentTime();
            c.set(Calendar.YEAR, year);
            this.thisObject.changeTime(c.getTime());
            this.thisObject.ho.redraw();
        }
    }

    private void ResetChrono()
    {
        this.thisObject.dChronoStart = 0;
        this.thisObject.lChrono = 0;
        this.thisObject.ho.redraw();
    }

    private void StartChrono()
    {
        if (this.thisObject.dChronoStart == 0)
        {
            Calendar c = this.thisObject.getCurrentTime();
            this.thisObject.dChronoStart = this.thisObject.months[c.get(Calendar.MONTH)] +
                    ((c.get(Calendar.DAY_OF_MONTH)) * 8640000) + (c.get(Calendar.HOUR_OF_DAY) * 360000) +
                    (c.get(Calendar.MINUTE) * 6000) + (c.get(Calendar.SECOND) * 100) + (c.get(Calendar.MILLISECOND) / 10);
        }
    }

    private void StopChrono()
    {
        if (this.thisObject.dChronoStart != 0)
        {
            Calendar c = this.thisObject.getCurrentTime();
            double dChronoStop = thisObject.months[c.get(Calendar.MONTH)] +
                    ((c.get(Calendar.DAY_OF_MONTH)) * 8640000) + (c.get(Calendar.HOUR_OF_DAY) * 360000) +
                    (c.get(Calendar.MINUTE) * 6000) + (c.get(Calendar.SECOND) * 100) + (c.get(Calendar.MILLISECOND) / 10);
            this.thisObject.lChrono += (dChronoStop - this.thisObject.dChronoStart);
            this.thisObject.dChronoStart = 0;
        }
    }

    private void Show()
    {
        if (!this.thisObject.sVisible)
        {
            this.thisObject.sVisible = true;
            this.thisObject.ho.redraw();
        }
    }

    private void Hide()
    {
        if (this.thisObject.sVisible)
        {
            this.thisObject.sVisible = false;
            this.thisObject.ho.redraw();
        }
    }

    private void SetPosition(CPositionInfo pos)
    {
        this.thisObject.ho.setPosition(pos.x, pos.y);
        this.thisObject.ho.redraw();
    }

    private void SetCountdown(int time)
    {
        this.thisObject.lCountdownStart = time / 10;
        this.thisObject.dChronoStart = 0;
        this.thisObject.lChrono = 0;
        this.thisObject.ho.redraw();
    }

    private void StartCountdown()
    {
        if (this.thisObject.dChronoStart == 0)
        {
            Calendar c = this.thisObject.getCurrentTime();
            this.thisObject.dChronoStart = thisObject.months[c.get(Calendar.MONTH)] +
                    ((c.get(Calendar.DAY_OF_MONTH)) * 8640000) + (c.get(Calendar.HOUR_OF_DAY) * 360000) +
                    (c.get(Calendar.MINUTE) * 6000) + (c.get(Calendar.SECOND) * 100) + (c.get(Calendar.MILLISECOND) / 10);
        }
    }

    private void StopCountdown()
    {
        if (this.thisObject.dChronoStart != 0)
        {
            Calendar c = this.thisObject.getCurrentTime();
            double dChronoStop = thisObject.months[c.get(Calendar.MONTH)] +
                    ((c.get(Calendar.DAY_OF_MONTH)) * 8640000) + (c.get(Calendar.HOUR_OF_DAY) * 360000) +
                    (c.get(Calendar.MINUTE) * 6000) + (c.get(Calendar.SECOND) * 100) + (c.get(Calendar.MILLISECOND) / 10);
            this.thisObject.lChrono += (dChronoStop - this.thisObject.dChronoStart);
            this.thisObject.dChronoStart = 0;
        }
    }

    private void SetXPosition(int x)
    {
        this.thisObject.ho.setX(x);
        this.thisObject.ho.redraw();
    }

    private void SetYPosition(int y)
    {
        this.thisObject.ho.setY(y);
        this.thisObject.ho.redraw();
    }

    private void SetXSize(int w)
    {
        this.thisObject.ho.setWidth(w);
        this.thisObject.ho.redraw();
    }

    private void SetYSize(int h)
    {
        this.thisObject.ho.setHeight(h);
        this.thisObject.ho.redraw();
    }
}
