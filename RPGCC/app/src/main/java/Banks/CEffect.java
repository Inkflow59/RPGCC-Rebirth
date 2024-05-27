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
//----------------------------------------------------------------------------------
//
// CDEFCOUNTER : valeurs de depart counter
//
//----------------------------------------------------------------------------------
package Banks;

import Services.CFile;

public class CEffect implements Cloneable
{

    public static final int EFFECTPARAM_INT = 0;
    public static final int EFFECTPARAM_FLOAT = 1;
    public static final int EFFECTPARAM_INTFLOAT4 = 2;
    public static final int EFFECTPARAM_SURFACE = 3;

    public static final int EFFECTOPT_BKDTEXTUREMASK=0x000F;
    public static final int EFFECTOPT_ZBUFFER=0x0100;
    public static final int EFFECTOPT_WANTSPIXELSIZE=0x0200;

    public static final int EFFECTFLAG_INTERNALMASK = 0xF0000000;
    public static final int EFFECTFLAG_HASTEXTUREPARAMS = 0x10000000;
    public static final int EFFECTFLAG_WANTSPIXELSIZE = 0x00000001;
    public static final int EFFECTFLAG_WANTSMATRIX = 0x00000002;
    public static final int EFFECTFLAG_MODIFIED = 0x00000004;

    public int handle;
    public String name;

    public String vertexData;
    public String fragData;

    public int nParams;
    public int options;

    public CEffectParam[] effectParams;

    public CEffect()
    {

    }

    public void fillParams(CFile file, long paramOffset, int paramNameOffset, int paramTypeOffset)
    {
        if(nParams == 0)
            return;

        effectParams = new CEffectParam[nParams];

        for (int i = 0; i < nParams; i++)
            effectParams[i] = new CEffectParam();

        file.seek(paramOffset + paramTypeOffset);
        for (int i = 0; i < nParams; i++)
            effectParams[i].nValueType = (int)file.readByte();

        file.seek(paramOffset + paramNameOffset);
        for (int i = 0; i < nParams; i++)
            effectParams[i].name = file.readAString();

    }

    public void fillValues(CFile file, int fileOffset)
    {
        long debut = file.getFilePointer();
        file.seek(fileOffset);

        int number = file.readAInt();
        if(number == nParams)
        {
            for (int i=0; i < nParams; i++)
            {
                switch(effectParams[i].nValueType)
                {
                    case EFFECTPARAM_FLOAT:
                        effectParams[i].fValue = file.readAFloat();
                        break;
                    case EFFECTPARAM_INTFLOAT4:
                    default:
                        effectParams[i].nValue = file.readAInt();
                        break;
                }
            }
        }
        file.seek(debut);
    }

    public CEffect clone()
    {
        try
        {
            return (CEffect)super.clone();
        }
        catch( CloneNotSupportedException e )
        {
            return null;
        }
    }

    public CEffectParam[] copyParams()
    {
        if(effectParams == null)
            return null;

        CEffectParam[] copy = new CEffectParam[effectParams.length];

        for(int n=0; n < effectParams.length ; n++)
        {
            copy[n] = new CEffectParam();
            if(copy != null) {
                copy[n].name = effectParams[n].name;
                copy[n].nValueType = effectParams[n].nValueType;
                copy[n].nValue = effectParams[n].nValue;
                copy[n].fValue = effectParams[n].fValue;
            }
        }
        return (copy.length == effectParams.length ? copy:null);
    }

}
