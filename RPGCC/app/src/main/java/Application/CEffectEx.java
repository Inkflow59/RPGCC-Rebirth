package Application;

import android.util.Log;



import Banks.CEffect;
import Banks.CEffectParam;
import Banks.CImage;

import Expressions.CValue;
import OpenGL.GLRenderer;


public class CEffectEx {

    public static final int EFFECTPARAM_INT = 0;
    public static final int EFFECTPARAM_FLOAT = 1;
    public static final int EFFECTPARAM_INTFLOAT4 = 2;
    public static final int EFFECTPARAM_SURFACE = 3;

    CRunApp app;
    int     handle;
    String  name;
    String  vertexData;
    String  fragData;

    int     nParams;
    CEffectParam[] eParams;

    int     indexShader;
    int     blendColor;
    boolean hasExtras;
    boolean useBackground;

    public CEffectEx(CRunApp ap)
    {
        app = ap;
        indexShader = -1;
        blendColor  = 0xFFFFFFFF;
        nParams = 0;
        hasExtras = false;
        useBackground = false;
        name = null;
    }

    public void destroy()
    {
        if(eParams != null)
        {
            for(int i= 0; i < nParams; i++)
            {
                eParams[i].destroy(app);
            }
        }
        if(indexShader != -1)
        {
            GLRenderer.inst.removeShader(indexShader);
        }

    }

    public boolean initialize(int index, int rgba)
    {
        CEffect e = app.effectBank.getEffectFromIndex(index);
        if(e != null) {
            handle = index;     // Not sure handle is equal index yet
            blendColor = rgba;

            name = e.name;
            nParams= e.nParams;
            vertexData = e.vertexData;
            fragData = e.fragData;
            eParams = e.copyParams();

            useBackground = ((e.options & CEffect.EFFECTOPT_BKDTEXTUREMASK) != 0 ) ? true :false;
            return initializeShader();
        }
        return false;
    }

    public boolean initialize(String effectName, int rgba)
    {
        CEffect e = app.effectBank.getEffectByName(effectName);
        if(e != null) {
            handle = e.handle;     // Not sure handle is equal index yet
            blendColor = rgba;

            name = e.name;
            nParams= e.nParams;
            vertexData = e.vertexData;
            fragData = e.fragData;
            eParams = e.copyParams();

            useBackground = ((e.options & CEffect.EFFECTOPT_BKDTEXTUREMASK) != 0 ) ? true :false;
            return initializeShader();

        }
        return false;
    }

    public boolean initializeShader()
    {
        String[] vars = new String[nParams];
        for (int i=0 ; i < nParams; i++)
        {
            String name = eParams[i].name;
            vars[i] = name;
        }

        //Log.i("MMFRuntime", "shader name: "+name);

        if(indexShader != -1)
            GLRenderer.inst.removeShader(indexShader);
        if(vertexData != null && fragData != null)
            indexShader = GLRenderer.inst.addShaderFromString(name, vertexData, fragData, vars, true, false);
        else
            indexShader = -1;

        if(indexShader != -1) {
            if(useBackground)
                GLRenderer.inst.setBackgroundUse(indexShader);
            return true;
        }
        return false;
    }

    public void setEffectData(int[] values)
    {
        if(values.length == nParams)
        {
            for(int i=0; i < nParams; i++)
            {
                switch(eParams[i].nValueType)
                {
                    case EFFECTPARAM_SURFACE:
                        eParams[i].img_handle = app.imageBank.enumerate((short)values[i]);
                        if(eParams[i].img_handle != -1)
                        {
                            hasExtras |= true;
                            app.imageBank.loadImageByHandle((short) eParams[i].img_handle, (app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0);
                        }
                        break;
                    case EFFECTPARAM_FLOAT:
                        eParams[i].fValue = Float.intBitsToFloat(values[i]);
                        break;
                    default:
                        eParams[i].nValue = values[i];
                        break;
                }
            }
            updateShader();
        }

    }

    public boolean removeShader()
    {

        if(indexShader != -1)
        {
            GLRenderer.inst.removeShader(indexShader);
            return true;
        }

        return false;
    }

    public boolean destroyShader()
    {

        if(indexShader != -1)
        {
            GLRenderer.inst.removeShader(indexShader);
            indexShader = -1;
            return true;
        }
        return false;
    }

    public String getName()
    {
        return name;
    }

    public int getRGBA()
    {
        return blendColor;
    }

    public void setRGBA(int color)
    {
        blendColor = color;
    }

    public int getIndexShader()
    {
        return indexShader;
    }

    public int getParamType(int paramIdx)
    {
        if(eParams != null)
        {
            if(paramIdx >=0 && paramIdx < eParams.length)
            {
                return eParams[paramIdx].nValueType;
            }
        }
        return -1;
    }

    public int getParamIndex(String name)
    {
        int index = -1;
        for(int i=0; i < nParams; i++)
        {
            if(eParams[i].name.contentEquals(name))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    public String getParamName(int paramIdx)
    {
        if(eParams != null)
        {
            if(paramIdx >=0 && paramIdx < eParams.length)
            {
                return eParams[paramIdx].name;
            }
        }
        return null;
    }

    public int getParamInt(int paramIdx)
    {
        if(eParams != null)
        {
            if(paramIdx >=0 && paramIdx < eParams.length)
            {
                return eParams[paramIdx].nValue;
            }
        }
        return -1;
    }

    public float getParamFloat(int paramIdx)
    {
        if(eParams != null)
        {
            if(paramIdx >=0 && paramIdx < eParams.length)
            {
                return eParams[paramIdx].fValue;
            }
        }
        return -1.0f;
    }

    public boolean setParamValue(int index, CValue value)
    {
        if(indexShader != -1 && index >= 0 && index < nParams) {
            GLRenderer.inst.setEffectShader(indexShader);

                switch (eParams[index].nValueType) {
                    case EFFECTPARAM_FLOAT:
                        eParams[index].fValue = (float)value.doubleValue;
                        GLRenderer.inst.updateVariable1f(eParams[index].name, eParams[index].fValue);
                        break;
                    case EFFECTPARAM_INTFLOAT4: {
                        eParams[index].nValue = value.intValue;
                        float[] float4f = new float[4];
                        int color = eParams[index].nValue;
                        for (int i = 0; i < 4; i++) {
                            float4f[i] = (float) (color & 0xFF) / 255.0f;
                            color >>= 8;
                        }
                        GLRenderer.inst.updateVariable4f(eParams[index].name, float4f[0], float4f[1], float4f[2], float4f[3]);
                        break;
                    }
                    default:
                        eParams[index].nValue = value.intValue;
                        GLRenderer.inst.updateVariable1i(eParams[index].name, eParams[index].nValue);
                        break;

                }
            GLRenderer.inst.removeEffectShader();
            return true;
        }

        return false;
    }

    public void setParamInt(int paramIdx, int value)
    {
        if(eParams != null)
        {
            if(paramIdx >=0 && paramIdx < eParams.length)
            {
                eParams[paramIdx].nValue = value;
                if(indexShader != -1)
                {
                    GLRenderer.inst.setEffectShader(indexShader);
                    GLRenderer.inst.updateVariable1i(eParams[paramIdx].name, eParams[paramIdx].nValue);
                    GLRenderer.inst.removeEffectShader();
                }
            }
        }
    }

    public void setParamFloat(int paramIdx, float value)
    {
        if(eParams != null)
        {
            if(paramIdx >=0 && paramIdx < eParams.length)
            {
                eParams[paramIdx].fValue = value;
                if(indexShader != -1)
                {
                    GLRenderer.inst.setEffectShader(indexShader);
                    GLRenderer.inst.updateVariable1f(eParams[paramIdx].name, eParams[paramIdx].fValue);
                    GLRenderer.inst.removeEffectShader();
                }
            }
        }
    }

    public void setParamFloat4(int paramIdx, int value)
    {
        if(eParams != null)
        {
            if(paramIdx >=0 && paramIdx < eParams.length)
            {
                eParams[paramIdx].nValue = value;
                if(indexShader != -1
                        && eParams[paramIdx].nValueType ==  EFFECTPARAM_INTFLOAT4)
                {
                    float[] float4f = new float[4];
                    int color = eParams[paramIdx].nValue;
                    for (int i = 0; i < 4; i++)
                    {
                        float4f[i] = (float)(color & 0xFF) / 255.0f;
                        color >>= 8;
                    }
                    GLRenderer.inst.setEffectShader(indexShader);
                    GLRenderer.inst.updateVariable4f(eParams[paramIdx].name, float4f[0], float4f[1], float4f[2], float4f[3]);
                    GLRenderer.inst.removeEffectShader();
                }
            }
        }
    }

    public void setParamTexture(int paramIdx, int img_handle)
    {
        if(eParams != null)
        {
            if(paramIdx >=0 && paramIdx < eParams.length)
            {
                if(indexShader != -1
                        && eParams[paramIdx].nValueType ==  EFFECTPARAM_SURFACE)
                {
                    int index =0;
                    if (eParams[paramIdx].img_handle != -1) {
                        app.imageBank.removeImageWithHandle(eParams[paramIdx].img_handle);
                    }
                    eParams[paramIdx].img_handle = (short)img_handle;
                    CImage img = app.imageBank.getImageFromHandle((short) eParams[paramIdx].img_handle);

                    if (img != null) {
                        int i = ++index;
                        img.setRepeatMode(i);
                        GLRenderer.inst.setEffectShader(indexShader);
                        GLRenderer.inst.setSurfaceTextureAtIndex(img, eParams[paramIdx].name, i);
                        GLRenderer.inst.removeEffectShader();
                    }
                }
            }
        }
    }

    public void setParamTexture(String name, int img_handle)
    {
        if(eParams != null && indexShader != -1)
        {
            if(nParams == 0)
                return;

            GLRenderer.inst.setEffectShader(indexShader);
            int index =0;

            for(int n=0; n < nParams; n++) {
                switch(eParams[n].nValueType)
                {
                    case EFFECTPARAM_SURFACE:
                    {
                        if(eParams[n].name.contentEquals(name)) {
                            if (eParams[n].img_handle != -1) {
                                app.imageBank.removeImageWithHandle((short) img_handle);
                            }
                            eParams[n].img_handle = (short)img_handle;
                            CImage img = app.imageBank.getImageFromHandle((short) eParams[n].img_handle);
                            if (img != null) {
                                int i = ++index;
                                img.setRepeatMode(i);
                                GLRenderer.inst.setSurfaceTextureAtIndex(img, eParams[n].name, i);
                            }
                        }
                        break;
                    }
                }
            }
            GLRenderer.inst.removeEffectShader();
        }
    }

    public boolean updateShader()
    {
        if(eParams != null && indexShader != -1)
        {
            if(nParams == 0)
                return false;

            GLRenderer.inst.setEffectShader(indexShader);
            int index =0;

            for(int n=0; n < nParams; n++) {
                switch(eParams[n].nValueType)
                {
                    case EFFECTPARAM_SURFACE:
                    {
                        if (eParams[n].img_handle != -1)
                        {
                            CImage img = app.imageBank.getImageFromHandle((short) eParams[n].img_handle);
                            if(img != null) {
                                int i = ++index;
                                img.setRepeatMode(i);
                                GLRenderer.inst.setSurfaceTextureAtIndex(img, eParams[n].name, i);
                            }
                        }
                        break;
                    }
                    case EFFECTPARAM_FLOAT:
                    {
                        GLRenderer.inst.updateVariable1f(eParams[n].name, eParams[n].fValue);
                        break;
                    }
                    case EFFECTPARAM_INTFLOAT4:
                    {
                        float[] float4f = new float[4];
                        int color = eParams[n].nValue;
                        for (int i = 0; i < 4; i++)
                        {
                            float4f[i] = (float)(color & 0xFF) / 255.0f;
                            color >>= 8;
                        }
                        GLRenderer.inst.updateVariable4f(eParams[n].name, float4f[0], float4f[1], float4f[2], float4f[3]);
                        break;
                    }
                    default:
                        GLRenderer.inst.updateVariable1i(eParams[n].name, eParams[n].nValue);
                        break;

                }
            }
            GLRenderer.inst.removeEffectShader();
            return true;
        }
        return false;
    }

    public boolean updateParamTexture()
    {
        if(nParams == 0 || !hasExtras)
            return false;
        if(eParams != null && indexShader != -1)
        {

            for(int n=0; n < nParams; n++) {
                if(eParams[n].nValueType == EFFECTPARAM_SURFACE)
                {
                    if (eParams[n].img_handle != -1) {
                        CImage img = app.imageBank.getImageFromHandle((short) eParams[n].img_handle);
                        if (img == null)
                            app.imageBank.loadImageByHandle((short) eParams[n].img_handle, (app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0);
                    }
                }
            }
            //Log.d("MMFRuntime", "refreshing params textures ...");
            return true;
        }
        return false;
    }

    public boolean refreshParamSurface()
    {
        if(nParams == 0 || !hasExtras)
            return false;
        if(eParams != null && indexShader != -1)
        {
            GLRenderer.inst.setEffectShader(indexShader);
            int index =0;

            for(int n=0; n < nParams; n++) {
                if(eParams[n].nValueType == EFFECTPARAM_SURFACE)
                {
                    if (eParams[n].img_handle != -1) {
                        CImage img = app.imageBank.getImageFromHandle((short) eParams[n].img_handle);
                        if (img != null) {
                            int i = ++index;
                            img.setRepeatMode(i);
                            GLRenderer.inst.setSurfaceTextureAtIndex(img, eParams[n].name, i);
                        }
                        //Log.d("MMFRuntime", " params surface name: "+eParams[n].name+" texture id: "+img.texture()+" index: "+index);
                    }
                }
            }

            GLRenderer.inst.removeEffectShader();
            return true;
        }
        return false;
    }

}
