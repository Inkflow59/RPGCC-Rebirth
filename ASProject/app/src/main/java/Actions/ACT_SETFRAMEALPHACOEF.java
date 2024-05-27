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
// -----------------------------------------------------------------------------
//
// SET FRAME ALPHA COEFFICIENT
//
// -----------------------------------------------------------------------------
package Actions;

import Application.CRunFrame;
import OpenGL.GLRenderer;
import Params.CParamExpression;
import RunLoop.CRun;

public class ACT_SETFRAMEALPHACOEF extends CAct
{
    @Override
    public void execute(CRun rhPtr)
    {
        CRunFrame frame = rhPtr.rhFrame;
        int alpha=rhPtr.get_EventExpressionInt((CParamExpression)evtParams[0]);

        alpha = 255 - alpha;

        if (alpha < 0)
            alpha = 0;

        if (alpha > 255)
            alpha = 255;


        int rgbaCoeff = 0x00FFFFFF;

        if((frame.effect & GLRenderer.BOP_RGBAFILTER) != 0)
            rgbaCoeff = frame.effectParam;

        int alphaPart = alpha << 24;
        int rgbPart = (rgbaCoeff & 0x00FFFFFF);

        frame.effect = (frame.effect & GLRenderer.BOP_MASK) | GLRenderer.BOP_RGBAFILTER;
        frame.effectParam = alphaPart | rgbPart;

    }
}
