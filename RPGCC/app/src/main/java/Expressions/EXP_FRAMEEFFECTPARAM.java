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
//----------------------------------------------------------------------------------
//
// GET FRAME EFFECT PARAM
//
//----------------------------------------------------------------------------------
package Expressions;

import Application.CRunFrame;
import Banks.CEffect;
import OpenGL.GLRenderer;
import RunLoop.*;

public class EXP_FRAMEEFFECTPARAM extends CExp
{
    public void evaluate(CRun rhPtr)
    {
        CRunFrame frame = rhPtr.rhFrame;
        rhPtr.rh4CurToken++;
        String paramName = rhPtr.getExpression().getString();

        if (frame.effectEx==null || paramName == null)
        {
            rhPtr.rh4Results[rhPtr.rh4PosPile].forceInt(0);
            return;
        }

        if((frame.effect & GLRenderer.BOP_MASK) == GLRenderer.BOP_EFFECTEX) {
            if (frame.effectEx != null) {
                int index = frame.effectEx.getParamIndex(paramName);
                if (index != -1) {
                    switch (frame.effectEx.getParamType(index)) {
                        case CEffect.EFFECTPARAM_INTFLOAT4:
                        case CEffect.EFFECTPARAM_INT:
                            rhPtr.rh4Results[rhPtr.rh4PosPile].forceInt(frame.effectEx.getParamInt(index));
                            return;
                        case CEffect.EFFECTPARAM_FLOAT:
                            rhPtr.rh4Results[rhPtr.rh4PosPile].forceDouble(frame.effectEx.getParamFloat(index));
                            return;
                    }

                }
            }
        }
        rhPtr.rh4Results[rhPtr.rh4PosPile].forceInt(0);
        return;
    }

}