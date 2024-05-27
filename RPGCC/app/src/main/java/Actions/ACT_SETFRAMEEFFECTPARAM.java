/* Copyright (c) 1996-2023 Clickteam
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

package Actions;

import Application.CRunFrame;
import Expressions.CValue;
import OpenGL.GLRenderer;
import Params.CParamExpression;
import RunLoop.CRun;

public class ACT_SETFRAMEEFFECTPARAM extends CAct
{
    public void execute(CRun rhPtr)
    {

        CRunFrame frame = rhPtr.rhFrame;

        if((frame.effect & GLRenderer.BOP_MASK) == GLRenderer.BOP_EFFECTEX)
        {
            String paramName = rhPtr.get_EventExpressionString((CParamExpression)evtParams[0]);
            CValue paramValue = rhPtr.get_EventExpressionAny((CParamExpression)evtParams[1]);

            if(frame.effectEx != null)
            {
                boolean bModified = false;
                int paramIndex = frame.effectEx.getParamIndex(paramName);
                if(paramIndex != -1)
                {
                    bModified = frame.effectEx.setParamValue(paramIndex, paramValue);
                }
            }
        }
    }
}
