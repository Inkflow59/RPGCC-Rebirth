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
package Services;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.fonts.FontStyle;

import androidx.core.graphics.TypefaceCompatUtil;

import com.badlogic.gdx.physics.box2d.Shape;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import Runtime.MMFRuntime;

public class FontUtils {
	
	public boolean packed = false;
    private IgnoreCaseMap PackedFonts = null;
    private IgnoreCaseMap InternalFonts = null;
    
    // store the packed typefaces(fonts) read from assets
    private final Hashtable<String, Typeface>  mCacheFonts = new Hashtable<String, Typeface>();
    
    public FontUtils() {
    	InternalFonts = enumInternalFonts();
    	if(MMFRuntime.FONTPACK) {
    		PackedFonts = enumPackedFonts();
    		if(PackedFonts != null)
    			packed = true;
    	}
    }
    
 
    public Typeface loadFontFromAssets(String fontName, int italic, int weight, int style) {

        String name = fontName;
        int lItalic = italic;
        int lWeight = weight;
        if (weight >= 500)
        {
            name += " Bold";
        }
        if (italic != 0) {
            name += " Italic";
        }

        // make sure we load each font only once
        synchronized (mCacheFonts) {

            if (! mCacheFonts.containsKey(name)) {

                Typeface typeface = Typeface.DEFAULT;
                if(PackedFonts != null)
                {
                    if(PackedFonts.containsKey(name))
                    {
                        typeface = Typeface.createFromAsset(MMFRuntime.inst.getAssets(), "fonts/" + PackedFonts.get(name));
                    }
                    else
                    {
                        String fontRegular = fontName;
                        if (PackedFonts.get(fontRegular) == null)
                            fontRegular += " Regular";

                        Typeface tmpFont = Typeface.DEFAULT;
                        if (!PackedFonts.containsKey(fontRegular))
                        {
                            for (Map.Entry<String, String> set : PackedFonts.entrySet())
                            {
                                String fontfacename = set.getKey().toLowerCase();
                                if (fontfacename.contains(fontRegular.toLowerCase()))
                                {
                                    tmpFont = Typeface.createFromAsset(MMFRuntime.inst.getAssets(), "fonts/" + PackedFonts.get(fontfacename));
                                    break;
                                }
                            }
                        }
                        else
                        {
                            tmpFont = Typeface.createFromAsset(MMFRuntime.inst.getAssets(), "fonts/" + PackedFonts.get(fontRegular));
                        }
                        typeface = Typeface.create(tmpFont, style);
                    }
                }
                else
                    typeface = Typeface.DEFAULT;

                mCacheFonts.put(name, typeface);
            }

            return mCacheFonts.get(name);
 
        }
 
    }
    
    public static IgnoreCaseMap enumInternalFonts()
    {
        // set the internal directory for fonts in android
        String[] fontdirs = { "/system/fonts", "/system/font", "/data/fonts" };
        IgnoreCaseMap fonts = new IgnoreCaseMap();
        // create the true type font reader
        TTFReader reader = new TTFReader();
 
        for ( String fontdir : fontdirs )
        {
            File dir = new File( fontdir );
 
            if ( !dir.exists() )
                continue;
 
            File[] files = dir.listFiles();
 
            if ( files == null )
                continue;
 
            for ( File file : files )
            {
                String fontname = reader.getTTFname( file.getAbsolutePath() );
 
                if ( fontname != null )
                    fonts.put( fontname.trim(), file.getAbsolutePath() );
            }
        }
 
        return fonts.isEmpty() ? null : fonts;
    }

    public static IgnoreCaseMap enumPackedFonts()
    {

    	IgnoreCaseMap fonts = new IgnoreCaseMap();
    	// create the true type font reader

        Resources resources = MMFRuntime.inst.getResources();
        AssetManager assetmanager = resources.getAssets();
        String assetList[];
		try {
			assetList = assetmanager.list("fonts");
            if (assetList != null)
            {   
                for ( int i = 0; i < assetList.length; i++)
                {
            		String fontname = assetList[i].substring(0, assetList[i].lastIndexOf('.'));;

            		if ( fontname != null )
            			fonts.put( fontname.trim(), assetList[i] );

                    //Log.Log(""+assetList[i]); 
                }
            }
		} catch (IOException e) {
            if (MMFRuntime.isDebuggable)
                e.printStackTrace();
		}


    	return fonts.isEmpty() ? null : fonts;
    }

    public boolean isFontInternal(String name) {
    	   	
    	if(InternalFonts != null && InternalFonts.containsKey(name))
    		return true;
    	
    	return false;
    }
    
    public boolean isFontPacked(String name) {
    	
    	if(PackedFonts != null && PackedFonts.containsKey(name))
    		return true;

    	String lowerName=name.toLowerCase();

    	for (String key : PackedFonts.keySet()) {
    	    if(key.startsWith(lowerName))
    	    	return true;
    	}

    	return false;
    }
    
    public String InternalFontPath(String name) {
	   	
		if(InternalFonts != null && InternalFonts.containsKey(name)) {	
			return InternalFonts.get(name);	
		}
		return null;
    }

    private static class IgnoreCaseMap extends HashMap<String, String> {

        @Override
        public String put(String key, String value) {
            return super.put(key.toLowerCase(), value);
        }

        @Override
        public boolean isEmpty() {
            return super.isEmpty();
        }
        public String remove(String key) {
            return super.remove(key.toLowerCase());
        }

        public String get(String key) {
            return super.get(key.toLowerCase());
        }

        public boolean containsKey(String key) {
            return super.containsKey(key.toLowerCase());
        }
    }

}

class TTFReader
{
    // This function parses the TTF file and returns the font name specified in the file
    public String getTTFname( String fontFilename )
    {
        try
        {
            // Parses the TTF file format.
            // See http://developer.apple.com/fonts/ttrefman/rm06/Chap6.html
            m_file = new RandomAccessFile( fontFilename, "r" );
 
            // Read the version first
            int version = readDword();
 
            // The version must be either 'true' for TTF (0x74727565) or 0x00010000 and for OTF (0x4f54544f)
            if ( version != 0x74727565 && version != 0x00010000  && version != 0x4f54544f) {
                m_file.close();
                return null;
            }
            // The TTF file consist of several sections called "tables", and we need to know how many of them are there.
            int numTables = readWord();
 
            // Skip the rest in the header
            readWord(); // skip searchRange
            readWord(); // skip entrySelector
            readWord(); // skip rangeShift
 
            // Now we can read the tables
            for ( int i = 0; i < numTables; i++ )
            {
                // Read the table entry
                int tag = readDword();
                readDword(); // skip checksum
                int offset = readDword();
                int length = readDword();
 
                // Now here' the trick. 'name' field actually contains the textual string name.
                // So the 'name' string in characters equals to 0x6E616D65
                if ( tag == 0x6E616D65 )
                {
                    // Here's the name section. Read it completely into the allocated buffer
                    byte[] table = new byte[ length ];
 
                    m_file.seek( offset );
                    read( table );
 
                    // This is also a table. See http://developer.apple.com/fonts/ttrefman/rm06/Chap6name.html
                    // According to Table 36, the total number of table records is stored in the second word, at the offset 2.
                    // Getting the count and string offset - remembering it's big endian.
                    int count = getWord( table, 2 );
                    int string_offset = getWord( table, 4 );
 
                    // Record starts from offset 6
                    for ( int record = 0; record < count; record++ )
                    {
                        // Table 37 tells us that each record is 6 words -> 12 bytes, and that the nameID is 4th word so its offset is 6.
                        // We also need to account for the first 6 bytes of the header above (Table 36), so...
                        int nameid_offset = record * 12 + 6;
                        int platformID = getWord( table, nameid_offset );
                        int nameid_value = getWord( table, nameid_offset + 6 );
 
                        // Table 42 lists the valid name Identifiers. We're interested in 4 but not in Unicode encoding (for simplicity).
                        // The encoding is stored as PlatformID and we're interested in Mac encoding
                        if ( nameid_value == 4 && platformID == 1 )
                        {
                            // We need the string offset and length, which are the word 6 and 5 respectively
                            int name_length = getWord( table, nameid_offset + 8 );
                            int name_offset = getWord( table, nameid_offset + 10 );
 
                            // The real name string offset is calculated by adding the string_offset
                            name_offset = name_offset + string_offset;
 
                            // Make sure it is inside the array
                            if ( name_offset >= 0 && name_offset + name_length < table.length )
                            {
                                m_file.close();
                                return new String(table, name_offset, name_length);
                            }
                        }
                    }
                }
            }
            m_file.close();
            return null;
        }
        catch (FileNotFoundException e)
        {
            // Permissions?
            return null;
        }
        catch (IOException e)
        {
            // Most likely a corrupted font file
            return null;
        }
    }
 
    // Font file; must be seekable
    private RandomAccessFile m_file = null;
 
    // Helper I/O functions
    private int readByte() throws IOException
    {
        return m_file.read() & 0xFF;
    }
 
    private int readWord() throws IOException
    {
        int b1 = readByte();
        int b2 = readByte();
 
        return b1 << 8 | b2;
    }
 
    private int readDword() throws IOException
    {
        int b1 = readByte();
        int b2 = readByte();
        int b3 = readByte();
        int b4 = readByte();
 
        return b1 << 24 | b2 << 16 | b3 << 8 | b4;
    }
 
    private void read( byte [] array ) throws IOException
    {
        if ( m_file.read( array ) != array.length )
            throw new IOException();
    }
 
    // Helper
    private int getWord( byte [] array, int offset )
    {
        int b1 = array[ offset ] & 0xFF;
        int b2 = array[ offset + 1 ] & 0xFF;
 
        return b1 << 8 | b2;
    }

    public void close() throws IOException {
        m_file.close();
    }

}