/* Copyright (c) 1996-2020 Clickteam
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
// -----------------------------------------------------------------------------
//
// ACT_EXTSETFLAGBYEXP
//
// -----------------------------------------------------------------------------
package Actions;

import Objects.CObject;
import Params.CParamExpression;
import RunLoop.CRun;

public class ACT_EXTSETFLAGBYEXP extends CAct
{
    public void execute(CRun rhPtr)
    {
        CObject pHo=rhPtr.rhEvtProg.get_ActionObjects(this);
        if (pHo==null)
			return;
        if (pHo.rov != null)
		{
            int number = rhPtr.get_EventExpressionInt((CParamExpression)this.evtParams[0]);       // flag number
            int value = rhPtr.get_EventExpressionInt((CParamExpression)this.evtParams[1]);        // value
            pHo.rov.rvValueFlags &= ~(1 << number);
            if ( value != 0 )
                pHo.rov.rvValueFlags |= (1 << number);
        }
    }
}
