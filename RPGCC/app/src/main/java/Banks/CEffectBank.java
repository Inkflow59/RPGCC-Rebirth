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
// CFONTBANK :banque de fontes
//
//----------------------------------------------------------------------------------
package Banks;


import Services.CFile;

public class CEffectBank
{

    long effectsBank;

    public CEffect effects[]=null;
    int effectsOffset[];

    int nEffects=0;


    public CEffectBank()
    {
    }

    public void preLoad(CFile file)
    {
        effectsBank = file.getFilePointer();
        // Number of Effects
        nEffects=file.readAInt();
        if (nEffects == 0)
            return;

        effects = new CEffect[nEffects];
        effectsOffset = new int[nEffects];

        int n;
        for(n=0; n < nEffects ; n++)
        {
            effectsOffset[n] = file.readAInt();
        }

        for(n=0; n < nEffects ; n++)
        {
            effects[n] = new CEffect();
        }

        loadEffects(file);
    }

    public boolean isEmpty()
    {
        return nEffects == 0;
    }

    public void loadEffects(CFile file)
    {
        for(int n = 0; n < nEffects ; n++)
        {
            effects[n] = new CEffect();
            if(effects[n] != null)
            {
                long offset = effectsBank + effectsOffset[n];

                effects[n].handle = n;
                file.seek(offset);

                int nameOffset = file.readAInt();
                int fxDataOffset = file.readAInt();
                int paramOffset = file.readAInt();
                effects[n].options = file.readAInt();
                file.readAInt();

                file.setUnicode(false);
                file.seek(offset+nameOffset);
                effects[n].name = file.readAString();

                file.seek(offset +fxDataOffset);
                String fxData= file.readAString();
                if(!fxData.isEmpty()) {
                    effects[n].vertexData = getVertexBlock(fxData);
                    effects[n].fragData = getFragmentBlock(fxData);
                }
                if(paramOffset != 0)
                {
                    file.seek(offset + paramOffset);
                    long startParams = file.getFilePointer();
                    effects[n].nParams = file.readAInt();

                    if (effects[n].nParams != 0) {
                        int paramTypeOffset = file.readAInt();
                        int paramNameOffset = file.readAInt();

                        effects[n].fillParams(file, startParams, paramNameOffset, paramTypeOffset);

                    }
                }
                file.setUnicode(true);
            }
        }
    }

    public CEffect getEffectFromIndex(int index)
    {
        if (index>=0 && index<nEffects)
            return effects[index];
        return null;
    }

    public CEffect getEffectByName(String name)
    {
        int n;
        for(n=0; n < nEffects ; n++)
        {
            if(effects[n].name.contains(name))
                return effects[n];
        }
        return null;
    }

    private String getVertexBlock(String block)
    {
        String s="";
        int preS = "//@Begin_vertex\r\n".length();
        if((block.indexOf("//@Begin_vertex") >= 0)
                && (block.indexOf("//@End") < block.length()))
        {
            s =block.substring(block.indexOf("//@Begin_vertex")+preS, block.indexOf("\r\n//@End"));
        }
        return s;
    }

    private String getFragmentBlock(String block)
    {
        String s="";
        int preS = "//@Begin_fragment\r\n".length();
        if((block.lastIndexOf("//@Begin_fragment") >= 0)
                && (block.lastIndexOf("//@End") < block.length()))
        {
            s =block.substring(block.lastIndexOf("//@Begin_fragment")+preS, block.lastIndexOf("\r\n//@End"));
        }
        return s;
    }

}
