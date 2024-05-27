/* Copyright (c) 1996-2022 Clickteam
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
// ------------------------------------------------------------------------------
// 
// COMPARE OBJECT EXPRESSION
// 
// ------------------------------------------------------------------------------
package Conditions;

import Objects.CObject;
import RunLoop.CRun;
import RunLoop.CObjInfo;
import Events.CQualToOiList;
import Params.CParamExpression;
import Expressions.CValue;

public class CND_EXTCOMPARE extends CCnd
{    
    public boolean eva1(CRun rhPtr, CObject hoPtr)
    {
		return eva2(rhPtr);
    }
    public boolean eva2(CRun rhPtr)
    {
        CObject pHo = rhPtr.rhEvtProg.evt_FirstObject(evtOiList);
        if (pHo == null)
            return false;
        int cpt = rhPtr.rhEvtProg.evtNSelectedObjects;

        short comp = ((CParamExpression)evtParams[1]).comparaison;

        // Qualifier?
        CQualToOiList pqoi = null;
        short saveQoiOi = 0;
        short saveQoiOiList = 0;
        short oil = evtOiList;
        if ((oil & 0x8000) != 0) {
            // Modify first object in qualifier object list as Get_CurrentExpressionObjects takes the first selected object from this list
            pqoi = rhPtr.rhEvtProg.qualToOiList[oil & 0x7FFF];
            saveQoiOi = pqoi.qoiList[0];
            saveQoiOiList = pqoi.qoiList[1];
        }

        while (pHo != null) {
            // Save selection and set current object as first selected (warning: selection chain is broken, this is just for GetExpression)
            CObjInfo poil = pHo.hoOiList;
            int saveOILEventCount = poil.oilEventCount;
            poil.oilEventCount = rhPtr.rhEvtProg.rh2EventCount;
            short saveOILListSelected = poil.oilListSelected;
            poil.oilListSelected = pHo.hoNumber;
            short saveHONextSelected = pHo.hoNextSelected;		// this is not required for GetExpression but this is in case some extension objects scan the object list in expressions
            pHo.hoNextSelected = -1;
            if (pqoi != null) {
                // Qualifier? force first OI
                pqoi.qoiList[0] = pHo.hoOi;
                pqoi.qoiList[1] = poil.oilIndex;
            }

            // Get expressions
            CValue value1 = rhPtr.get_EventExpressionAny((CParamExpression)evtParams[0]);
            CValue value2 = rhPtr.get_EventExpressionAny((CParamExpression)evtParams[1]);

            // Restore selection
            poil.oilEventCount = saveOILEventCount;
            poil.oilListSelected = saveOILListSelected;
            pHo.hoNextSelected = saveHONextSelected;

            // Compare expressions
            if (CRun.compareTo(value1, value2, comp) == false) {
                cpt--;
                rhPtr.rhEvtProg.evt_DeleteCurrentObject();
            }

            pHo = rhPtr.rhEvtProg.evt_NextObject();
        }

        // Restore first qualifier object
        if (pqoi != null) {
            pqoi.qoiList[0] = saveQoiOi;
            pqoi.qoiList[1] = saveQoiOiList;
        }

        return (cpt != 0);
    }
}
