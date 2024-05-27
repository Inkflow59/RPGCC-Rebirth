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
// -----------------------------------------------------------------------------
//
// SET FRAME RGB COEFFICIENT
//
// -----------------------------------------------------------------------------
package Actions;

import Application.CRunFrame;
import OpenGL.GLRenderer;
import Params.CParamExpression;
import RunLoop.CRun;
import Services.CServices;

public class ACT_SETFRAMERGBCOEF extends CAct
{
    @Override
    public void execute(CRun rhPtr)
    {
        int newRGB = rhPtr.get_EventExpressionInt((CParamExpression)evtParams[0]);

        CRunFrame frame = rhPtr.rhFrame;
        int cVal = CServices.swapRGB(newRGB);

        switch (frame.effect & GLRenderer.BOP_MASK) {
            case GLRenderer.BOP_EFFECTEX:
            {
                if(frame.effectEx != null)
                {
                    int RGBA = frame.effectEx.getRGBA();
                    frame.effectEx.setRGBA((RGBA & 0xFF000000) | cVal);
                }
            }
            break;
            case GLRenderer.BOP_BLEND:
            {
                int blendCoef = (CServices.SemiTranspToAlpha(frame.effectParam) << 24);
                frame.effect &= ~GLRenderer.BOP_MASK;
                frame.effect |= (GLRenderer.BOP_COPY | GLRenderer.BOP_RGBAFILTER);
                frame.effectParam = (blendCoef | cVal);

                if ( frame.effectParam == 0xFFFFFFFF )
                {
                    frame.effect = GLRenderer.BOP_COPY;
                    frame.effectParam = 0;
                }
            }
            break;
            default:
            {
                int dwBlendCoef = (frame.effectParam & 0xFF000000);
                if ( (frame.effect & GLRenderer.BOP_RGBAFILTER) == 0 )
                {
                    dwBlendCoef = 0xFF000000;
                    frame.effect |= GLRenderer.BOP_RGBAFILTER;
                }
                frame.effectParam = (dwBlendCoef | cVal);

                if ( frame.effectParam == 0xFFFFFFFF &&
                        (frame.effect & GLRenderer.BOP_MASK) == GLRenderer.BOP_COPY )
                {
                    frame.effect = GLRenderer.BOP_COPY;
                    frame.effectParam = 0;
                }
            }
            break;
        }
    }
}
