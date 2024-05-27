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
// ------------------------------------------------------------------------------
//
// CForEach : info about the current loop
//
// ------------------------------------------------------------------------------
package Events;

import Objects.CObject;

public class CForEach
{
    private static int STEP = 32;
    public CForEach next = null;
    public int oi;
    public int number = 0;
    public int length = STEP;
    public CObject[] objects;
    public int index;
    public String name;
    public Boolean stop;

    public CForEach()
    {
        objects = new CObject[STEP];
    }
    public void addObject(CObject pHo)
    {
        if (number >= length)
        {
            int newLength = length + STEP;
            CObject[] temp = new CObject[newLength];
            System.arraycopy(objects, 0, temp, 0, length);
            objects = temp;
            length = newLength;
        }
        objects[number++] = pHo;
    }
}

