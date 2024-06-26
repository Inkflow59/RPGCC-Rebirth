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
// CMOVEDEFEXTENSION : donnees d'un movement extension
//
//----------------------------------------------------------------------------------
package Movements;

import Services.CFile;

public class CMoveDefExtension extends CMoveDef
{
    public String moduleName;
    public int mvtID;
    public byte data[];
	public boolean isPhysics = false;
    
    public CMoveDefExtension() 
    {
    }

    @Override
	public void load(CFile file, int length) 
    {
	file.skipBytes(14);
        data=new byte[length-14];
	file.read(data);
    }
    public void setModuleName(String name, int id)
    {
        moduleName=new String(name);
        mvtID=id;

        if (moduleName.equalsIgnoreCase("box2d8directions")
            || moduleName.equalsIgnoreCase("box2dspring")
            || moduleName.equalsIgnoreCase("box2dspaceship")
            || moduleName.equalsIgnoreCase("box2dstatic")
            || moduleName.equalsIgnoreCase("box2dracecar")
            || moduleName.equalsIgnoreCase("box2daxial")
            || moduleName.equalsIgnoreCase("box2dplatform")
            || moduleName.equalsIgnoreCase("box2dbouncingball")
            || moduleName.equalsIgnoreCase("box2dbackground")
            )
			isPhysics = true;
    }
            
}
