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

package Actions;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import Application.CEffectEx;
import Application.CRunApp;
import Application.CRunFrame;
import Expressions.CValue;
import Objects.CObject;
import OpenGL.GLRenderer;
import Params.CParamExpression;
import Params.PARAM_STRING;
import RunLoop.CRun;

public class ACT_SETFRAMEEFFECTPARAMTEXTURE extends CAct
{
    public void execute(CRun rhPtr)
    {

        CRunFrame frame = rhPtr.rhFrame;

        if((frame.effect & GLRenderer.BOP_MASK) == GLRenderer.BOP_EFFECTEX)
        {
            CEffectEx effect = frame.effectEx;
            if(effect != null)
            {
                String paramName = rhPtr.get_EventExpressionString((CParamExpression)evtParams[0]);
                String fileName = null;
                if (evtParams[1].code == 40) { //PARAM_FILENAME
                    fileName = ((PARAM_STRING)evtParams[1]).string;
                }
                else {
                    fileName = rhPtr.get_EventExpressionString((CParamExpression) evtParams[1]);
                }
                if(fileName == null)
                    return;
                int paramIndex = effect.getParamIndex(paramName);
                if(paramIndex != -1)
                {
                    CRunApp.HFile hfile = rhPtr.rhApp.openHFile(fileName);
                    if(hfile == null)
                        return;

                    // Never smooth
                    boolean anti_flag = false;
                    // Size of this image must be max teh texture allowed in GPU.
                    Bitmap img = BitmapFactory.decodeStream(hfile.stream);
                    if(img == null)
                        return;
                    int imgHandle = rhPtr.rhApp.imageBank.addImage(img, (short)0, (short)0, (short)0, (short)0, anti_flag);
                    effect.setParamTexture(paramIndex, imgHandle);
                }
            }
        }

    }
}

