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
// CCND: une condition
//
//----------------------------------------------------------------------------------
package Conditions;

import java.util.ArrayList;

import Application.CRunApp;
import Events.CEvent;
import Events.CEventGroup;
import Events.CQualToOiList;
import Expressions.CValue;
import Expressions.EXP_STRING;
import OI.COI;
import Objects.CObject;
import Params.CParam;
import Params.CParamExpression;
import Params.PARAM_SHORT;
import Params.PARAM_OBJECT;
import RunLoop.CRun;
import Runtime.Log;

public abstract class CCnd extends CEvent
{
    public static final int NUM_ONEVENT = 6;
	public static short[] oiOneObjectList = new short[2];

    public short evtIdentifier;
    
    public CCnd() 
    {
    }
    
    public static CCnd create(CRunApp app)
    {
		boolean bCompareG = false;
        long debut = app.file.getFilePointer();

        short size = app.file.readAShort();          // evtSize
        CCnd cnd = null;
        int c = app.file.readAInt();
        switch (c)
        {
			case ((-43 << 16) | 0xFFFF):
				//d = new CND_STARTCHILDEVENT();
				break;
			case ((-42 << 16) | 0xFFFF):
				//d = new CND_NEVER();
				break;
            case ((-40 << 16) | 0xFFFF):
//              cnd = new CND_RUNNINGAS();
                break;
            case ((-39 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGDBL_GT();
				bCompareG = true;
                break;
            case ((-38 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGDBL_GE();
				bCompareG = true;
                break;
            case ((-37 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGDBL_LT();
				bCompareG = true;
                break;
            case ((-36 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGDBL_LE();
				bCompareG = true;
                break;
            case ((-35 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGDBL_NE();
				bCompareG = true;
                break;
            case ((-34 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGDBL_EQ();
				bCompareG = true;
                break;
            case ((-33 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGINT_GT();
				bCompareG = true;
                break;
            case ((-32 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGINT_GE();
				bCompareG = true;
                break;
            case ((-31 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGINT_LT();
				bCompareG = true;
                break;
            case ((-30 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGINT_LE();
				bCompareG = true;
                break;
            case ((-29 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGINT_NE();
				bCompareG = true;
                break;
            case ((-28 << 16) | 0xFFFF):
                cnd = new CND_COMPAREGINT_EQ();
				bCompareG = true;
                break;
            case ((-26 << 16) | 0xFFFF):	// #define CND_CHANCE	
//              cnd = new CND_CHANCE();
                break;
            case ((-25 << 16) | 0xFFFF):	// #define CND_ORLOGICAL		((-25<<16)|0xFFFF)
//              cnd = new CND_ORLOGICAL();
                break;
            case ((-24 << 16) | 0xFFFF):	// #define CND_OR				
//              cnd = new CND_OR();
                break;
            case ((-23 << 16) | 0xFFFF):	// #define CND_GROUPSTART		
//              cnd = new CND_GROUPSTART();
                break;
            case ((-22 << 16) | 0xFFFF):	// #define CND_CLIPBOARD
//              cnd = new CND_CLIPBOARD();
                break;
            case ((-21 << 16) | 0xFFFF):	// CND_ONCLOSE			
//              cnd = new CND_ONCLOSE();
                break;
            case ((-20 << 16) | 0xFFFF):	// CND_COMPAREGSTRING	
//              cnd = new CND_COMPAREGSTRING();
                break;
            case ((-16 << 16) | 0xFFFF):	// CND_ONLOOP
//              cnd = new CND_ONLOOP();
                break;
            case ((-13 << 16) | 0xFFFF):	// CND_RECORDKEY		
//              cnd = new CND_RECORDKEY();
                break;
            case ((-12 << 16) | 0xFFFF):	// CND_GROUPACTIVATED	
//              cnd = new CND_GROUPACTIVATED();
                break;
            case ((-11 << 16) | 0xFFFF):	// CND_ENDGROUP		
                cnd = new CND_ENDGROUP();
                break;
            case ((-10 << 16) | 0xFFFF):	// CND_GROUP			
                cnd = new CND_GROUP();
                break;
            case ((-9<<16)|0xFFFF):			// CND_REMARK			
//              cnd=new CND_REMARK();
                break;
            case ((-8 << 16) | 0xFFFF):		// CND_COMPAREG		
//              cnd = new CND_COMPAREG();
                break;
            case ((-7 << 16) | 0xFFFF):	// CND_NOTALWAYS		
//              cnd = new CND_NOTALWAYS();
                break;
            case ((-6 << 16) | 0xFFFF):	// CND_ONCE			
//              cnd = new CND_ONCE();
                break;
            case ((-5 << 16) | 0xFFFF):	// CND_REPEAT			
//              cnd = new CND_REPEAT();
                break;
            case ((-4 << 16) | 0xFFFF):	// CND_NOMORE			
//              cnd = new CND_NOMORE();
                break;
            case ((-3 << 16) | 0xFFFF):	// CND_COMPARE			
//              cnd = new CND_COMPARE();
                break;
            case ((-2 << 16) | 0xFFFF):	// CND_NEVER			
//              cnd = new CND_NEVER();
                break;
            case ((-1 << 16) | 0xFFFF):	// CND_ALWAYS			
                cnd = new CND_ALWAYS();
                break;
            case ((-9<<16)|0xFFFE):	 // CND_SPCHANNELPAUSED 
//              cnd=new CND_SPCHANNELPAUSED();
                break;
            case ((-8 << 16) | 0xFFFE):	// CND_NOSPCHANNELPLAYING 
//              cnd = new CND_NOSPCHANNELPLAYING();
                break;
            case ((-7 << 16) | 0xFFFE):	// CND_MUSPAUSED		
//              cnd = new CND_MUSPAUSED();
                break;
            case ((-6 << 16) | 0xFFFE):	// CND_SPSAMPAUSED		
//              cnd = new CND_SPSAMPAUSED();
                break;
            case ((-5 << 16) | 0xFFFE):	// CND_MUSICENDS		
//              cnd = new CND_MUSICENDS();
                break;
            case ((-4 << 16) | 0xFFFE):	// CND_NOMUSPLAYING	
//              cnd = new CND_NOMUSPLAYING();
                break;
            case ((-3 << 16) | 0xFFFE):	// CND_NOSAMPLAYING	
//              cnd = new CND_NOSAMPLAYING();
                break;
            case ((-2 << 16) | 0xFFFE):	// CND_NOSPMUSPLAYING	
//              cnd = new CND_NOSPMUSPLAYING();
                break;
            case ((-1 << 16) | 0xFFFE):	// CND_NOSPSAMPLAYING	
//              cnd = new CND_NOSPSAMPLAYING();
                break;
            case ((-10 << 16) | 0xFFFD):	// CND_FRAMESAVED
//              cnd = new CND_FRAMESAVED();
                break;
            case ((-9 << 16) | 0xFFFD):	// CND_FRAMELOADED		
//              cnd = new CND_FRAMELOADED();
                break;
            case ((-8 << 16) | 0xFFFD):	// CND_ENDOFPAUSE		
//              cnd = new CND_ENDOFPAUSE();
                break;
            case ((-6 << 16) | 0xFFFD):	// CND_ISLADDER		
//              cnd = new CND_ISLADDER();
                break;
            case ((-5 << 16) | 0xFFFD):	// CND_ISOBSTACLE		
//              cnd = new CND_ISOBSTACLE();
                break;
            case ((-4 << 16) | 0xFFFD):	// CND_QUITAPPLICATION	
//              cnd = new CND_QUITAPPLICATION();
                break;
            case ((-3 << 16) | 0xFFFD):	// CND_LEVEL			
//              cnd = new CND_LEVEL();
                break;
            case ((-2 << 16) | 0xFFFD):	// CND_END				
//              cnd = new CND_END();
                break;
            case ((-1 << 16) | 0xFFFD):	// CND_START			
                cnd = new CND_START();
                break;
            case ((-8 << 16) | 0xFFFC):		// CND_EVERY2
//              cnd = new CND_EVERY2();
                break;
            case ((-7 << 16) | 0xFFFC):		// CND_TIMEREQUALS
                cnd = new CND_TIMEREQUALS();
                break;
            case ((-6 << 16) | 0xFFFC):		// CND_ONEVENT
//              cnd = new CND_ONEVENT();
                break;
            case ((-5 << 16) | 0xFFFC):	// CND_TIMEOUT
//              cnd = new CND_TIMEOUT();
                break;
            case ((-4 << 16) | 0xFFFC):	// CND_EVERY       	
//              cnd = new CND_EVERY();
                break;
            case ((-3 << 16) | 0xFFFC):	// CND_TIMER       	
//              cnd = new CND_TIMER();
                break;
            case ((-2 << 16) | 0xFFFC):	// CND_TIMERINF       	
//              cnd = new CND_TIMERINF();
                break;
            case ((-1 << 16) | 0xFFFC):	// CND_TIMERSUP       	
//              cnd = new CND_TIMERSUP();
                break;
            case ((-10 << 16) | 0xFFFA):	// CND_MOUSEON		   	
//              cnd = new CND_MOUSEON();
                break;
            case ((-9 << 16) | 0xFFFA):	// CND_ANYKEY			
//              cnd = new CND_ANYKEY();
                break;
            case ((-8 << 16) | 0xFFFA):	// CND_MKEYDEPRESSED	
//              cnd = new CND_MKEYDEPRESSED();
                break;
            case ((-7 << 16) | 0xFFFA):	// CND_MCLICKONOBJECT	
                cnd = new CND_MCLICKONOBJECT();
                break;
            case ((-6 << 16) | 0xFFFA):	// CND_MCLICKINZONE 	
//              cnd = new CND_MCLICKINZONE();
                break;
            case ((-5 << 16) | 0xFFFA):	// CND_MCLICK	 		
                cnd = new CND_MCLICK();
                break;
            case ((-4 << 16) | 0xFFFA):	// CND_MONOBJECT		
//              cnd = new CND_MONOBJECT();
                break;
            case ((-3 << 16) | 0xFFFA):	// CND_MINZONE			
//              cnd = new CND_MINZONE();
                break;
            case ((-2 << 16) | 0xFFFA):	// CND_KBKEYDEPRESSED 	
//              cnd = new CND_KBKEYDEPRESSED();
                break;
            case ((-1 << 16) | 0xFFFA):	// CND_KBPRESSKEY   	
//              cnd = new CND_KBPRESSKEY();
                break;
            case ((-6 << 16) | 0xFFF9):	// CND_JOYPUSHED		
//              cnd = new CND_JOYPUSHED();
                break;
            case ((-5 << 16) | 0xFFF9):	// CND_NOMORELIVE		
//              cnd = new CND_NOMORELIVE();
                break;
            case ((-4 << 16) | 0xFFF9):	// CND_JOYPRESSED		
//              cnd = new CND_JOYPRESSED();
                break;
            case ((-3 << 16) | 0xFFF9):	// CND_LIVE	        
//              cnd = new CND_LIVE();
                break;
            case ((-2 << 16) | 0xFFF9):	// CND_SCORE		    
//              cnd = new CND_SCORE();
                break;
            case ((-1 << 16) | 0xFFF9):	// CND_PLAYERPLAYING   
//              cnd = new CND_PLAYERPLAYING();
                break;
            case ((-23 << 16) | 0xFFFB):	// CND_CHOOSEALLINLINE	
//              cnd = new CND_CHOOSEALLINLINE();
                break;
            case ((-22 << 16) | 0xFFFB):	// CND_CHOOSEFLAGRESET	
//              cnd = new CND_CHOOSEFLAGRESET();
                break;
            case ((-21 << 16) | 0xFFFB):	// CND_CHOOSEFLAGSET 	
//              cnd = new CND_CHOOSEFLAGSET();
                break;
            case ((-20 << 16) | 0xFFFB):	// CND_CHOOSEVALUE 	
//              cnd = new CND_CHOOSEVALUE();
                break;
            case ((-19 << 16) | 0xFFFB):	// CND_PICKFROMID		
//              cnd = new CND_PICKFROMID();
                break;
            case ((-18 << 16) | 0xFFFB):	// CND_CHOOSEALLINZONE 
//              cnd = new CND_CHOOSEALLINZONE();
                break;
            case ((-17 << 16) | 0xFFFB):	// CND_CHOOSEALL       
//              cnd = new CND_CHOOSEALL();
                break;
            case ((-16 << 16) | 0xFFFB):	// CND_CHOOSEZONE      
//              cnd = new CND_CHOOSEZONE();
                break;
            case ((-15 << 16) | 0xFFFB):	// CND_NUMOFALLOBJECT  
//              cnd = new CND_NUMOFALLOBJECT();
                break;
            case ((-14 << 16) | 0xFFFB):	// CND_NUMOFALLZONE    
//              cnd = new CND_NUMOFALLZONE();
                break;
            case ((-13 << 16) | 0xFFFB):	// CND_NOMOREALLZONE   
//              cnd = new CND_NOMOREALLZONE();
                break;
            case ((-12 << 16) | 0xFFFB):	// CND_CHOOSEFLAGRESET_OLD	
//              cnd = new CND_CHOOSEFLAGRESET_OLD();
                break;
            case ((-11 << 16) | 0xFFFB):	// CND_CHOOSEFLAGSET_OLD 	
//              cnd = new CND_CHOOSEFLAGSET_OLD();
                break;
            case ((-8 << 16) | 0xFFFB):	// CND_CHOOSEVALUE_OLD 	
//              cnd = new CND_CHOOSEVALUE_OLD();
                break;
            case ((-7 << 16) | 0xFFFB):	// CND_PICKFROMID_OLD		
//              cnd = new CND_PICKFROMID_OLD();
                break;
            case ((-6 << 16) | 0xFFFB):	// CND_CHOOSEALLINZONE_OLD 
//              cnd = new CND_CHOOSEALLINZONE_OLD();
                break;
            case ((-5 << 16) | 0xFFFB):	// CND_CHOOSEALL_OLD       
//              cnd = new CND_CHOOSEALL_OLD();
                break;
            case ((-4 << 16) | 0xFFFB):	// CND_CHOOSEZONE_OLD      
//              cnd = new CND_CHOOSEZONE_OLD();
                break;
            case ((-3 << 16) | 0xFFFB):	// CND_NUMOFALLOBJECT_OLD  
//              cnd = new CND_NUMOFALLOBJECT_OLD();
                break;
            case ((-2 << 16) | 0xFFFB):	// CND_NUMOFALLZONE_OLD    
//              cnd = new CND_NUMOFALLZONE_OLD();
                break;
            case ((-1 << 16) | 0xFFFB):		// CND_NOMOREALLZONE_OLD   
//              cnd = new CND_NOMOREALLZONE_OLD();
                break;
	        case (((-80 - 4) << 16) | 2):
				//d = new CND_CMPSCALEY();
				break;
			case (((-80 - 3) << 16) | 2):
				//d = new CND_CMPSCALEX();
				break;
			case (((-80 - 2) << 16) | 2):
				//d = new CND_CMPANGLE();
				break;
            case (((-80 - 1) << 16) | 2):		// CND_SPRCLICK	   			
//              cnd = new CND_SPRCLICK();
                break;
            case (((-80 - 1) << 16) | 7):		// CND_CCOUNTER				
                cnd = new CND_CCOUNTER();
                break;
            case (((-80 - 3) << 16) | 4):		// CND_QEQUAL					
//              cnd = new CND_QEQUAL();
                break;
            case (((-80 - 2) << 16) | 4):		// CND_QFALSE					
//              cnd = new CND_QFALSE();
                break;
            case (((-80 - 1) << 16) | 4):		// CND_QEXACT					
//              cnd = new CND_QEXACT();
                break;
            case (((-80 - 4) << 16) | (9 & 0x00FF)):		// CND_CCAISPAUSED
//              cnd = new CND_CCAISPAUSED();
                break;
            case (((-80 - 3) << 16) | (9 & 0x00FF)):		// CND_CCAISVISIBLE
            	//d = new CND_CCAISVISIBLE();
                break;
            case (((-80 - 2) << 16) | (9 & 0x00FF)):		// CND_CCAAPPFINISHED
            	//d = new CND_CCAAPPFINISHED();
                break;
            case (((-80 - 1) << 16) | (9 & 0x00FF)):		// CND_CCAFRAMECHANGED
            	//d = new CND_CCAFRAMECHANGED();
                break;
            default:
                switch (c & 0xFFFF0000)
                {
					case (-49 << 16):
						//d = new CND_EXTCMPINSTANCEDATA();
						break;
					case (-48 << 16):
						//d = new CND_EXTPICKMAXVALUE();
						break;
					case (-47 << 16):
						//d = new CND_EXTPICKMINVALUE();
						break;
					case (-46 << 16):
						//d = new CND_EXTCMPLAYER();
						break;
					case (-45 << 16):
						cnd = new CND_EXTCOMPARE();
						break;
					case (-44 << 16):
						//d = new CND_EXTPICKCLOSEST();
						break;
                    case (-43 << 16):				// CND_EXTCMPVARDBL
                        cnd = new CND_EXTCMPVARDBL();
                        break;
                    case (-42 << 16):				// CND_EXTCMPVARINT
                        cnd = new CND_EXTCMPVARINT();
                        break;
                    case (-41 << 16):				// CND_EXTONLOOP
//                      cnd = new CND_EXTONLOOP();
                        break;
                    case (-40 << 16):				// CND_EXTISSTRIKEOUT
//                      cnd = new CND_EXTISSTRIKEOUT();
                        break;
                    case (-39 << 16):				// CND_EXTISUNDERLINE			
//                      cnd = new CND_EXTISUNDERLINE();
                        break;
                    case (-38 << 16):				// CND_EXTISITALIC				
//                      cnd = new CND_EXTISITALIC();
                        break;
                    case (-37 << 16):				// CND_EXTISBOLD				
//                      cnd = new CND_EXTISBOLD();
                        break;
                    case (-36 << 16):				// CND_EXTCMPVARSTRING			
//                      cnd = new CND_EXTCMPVARSTRING();
                        break;
                    case (-35 << 16):				// CND_EXTPATHNODENAME			
//                      cnd = new CND_EXTPATHNODENAME();
                        break;
                    case (-34 << 16):				// CND_EXTCHOOSE				
//                      cnd = new CND_EXTCHOOSE();
                        break;
                    case (-33 << 16):				// CND_EXTNOMOREOBJECT			
//                      cnd = new CND_EXTNOMOREOBJECT();
                        break;
                    case (-32 << 16):				// CND_EXTNUMOFOBJECT			
//                      cnd = new CND_EXTNUMOFOBJECT();
                        break;
                    case (-31 << 16):				// CND_EXTNOMOREZONE			
//                      cnd = new CND_EXTNOMOREZONE();
                        break;
                    case (-30 << 16):				// CND_EXTNUMBERZONE			
//                      cnd = new CND_EXTNUMBERZONE();
                        break;
                    case (-29 << 16):				// CND_EXTSHOWN				
//                      cnd = new CND_EXTSHOWN();
                        break;
                    case (-28 << 16):				// CND_EXTHIDDEN				
//                      cnd = new CND_EXTHIDDEN();
                        break;
                    case (-27 << 16):				// CND_EXTCMPVAR				
//                      cnd = new CND_EXTCMPVAR();
                        break;
                    case (-26 << 16):				// CND_EXTCMPVARFIXED			
//                      cnd = new CND_EXTCMPVARFIXED();
                        break;
                    case (-25 << 16):				// CND_EXTFLAGSET				
//                      cnd = new CND_EXTFLAGSET();
                        break;
                    case (-24 << 16):				// CND_EXTFLAGRESET			
//                      cnd = new CND_EXTFLAGRESET();
                        break;
                    case (-23 << 16):				// CND_EXTISCOLBACK	        
//                      cnd = new CND_EXTISCOLBACK();
                        break;
                    case (-22 << 16):				// CND_EXTNEARBORDERS	        
//                      cnd = new CND_EXTNEARBORDERS();
                        break;
                    case (-21 << 16):				// CND_EXTENDPATH	  	        
//                      cnd = new CND_EXTENDPATH();
                        break;
                    case (-20 << 16):				// CND_EXTPATHNODE    	        
//                      cnd = new CND_EXTPATHNODE();
                        break;
                    case (-19 << 16):				// CND_EXTCMPACC	            
//                      cnd = new CND_EXTCMPACC();
                        break;
                    case (-18 << 16):				// CND_EXTCMPDEC	 	        
//                      cnd = new CND_EXTCMPDEC();
                        break;
                    case (-17 << 16):				// CND_EXTCMPX	 	  	        
//                      cnd = new CND_EXTCMPX();
                        break;
                    case (-16 << 16):				// CND_EXTCMPY   		        
//                      cnd = new CND_EXTCMPY();
                        break;
                    case (-15 << 16):				// CND_EXTCMPSPEED             
//                      cnd = new CND_EXTCMPSPEED();
                        break;
                    case (-14 << 16):				// CND_EXTCOLLISION   	        
//                      cnd = new CND_EXTCOLLISION();
                        break;
                    case (-13 << 16):				// CND_EXTCOLBACK              
//                      cnd = new CND_EXTCOLBACK();
                        break;
                    case (-12 << 16):				// CND_EXTOUTPLAYFIELD         
//                      cnd = new CND_EXTOUTPLAYFIELD();
                        break;
                    case (-11 << 16):				// CND_EXTINPLAYFIELD          
//                      cnd = new CND_EXTINPLAYFIELD();
                        break;
                    case (-10 << 16):				// CND_EXTISOUT	            
//                      cnd = new CND_EXTISOUT();
                        break;
                    case (-9 << 16):				// CND_EXTISIN                 
//                      cnd = new CND_EXTISIN();
                        break;
                    case (-8 << 16):				// CND_EXTFACING               
//                      cnd = new CND_EXTFACING();
                        break;
                    case (-7 << 16):				// CND_EXTSTOPPED              
//                      cnd = new CND_EXTSTOPPED();
                        break;
                    case (-6 << 16):				// CND_EXTBOUNCING	            
//                      cnd = new CND_EXTBOUNCING();
                        break;
                    case (-5 << 16):				// CND_EXTREVERSED             
//                      cnd = new CND_EXTREVERSED();
                        break;
                    case (-4 << 16):				// CND_EXTISCOLLIDING          
//                      cnd = new CND_EXTISCOLLIDING();
                        break;
                    case (-3 << 16):				// CND_EXTANIMPLAYING          
//                      cnd = new CND_EXTANIMPLAYING();
                        break;
                    case (-2 << 16):				// CND_EXTANIMENDOF        	
//                      cnd = new CND_EXTANIMENDOF();
                        break;
                    case (-1 << 16):				// CND_EXTCMPFRAME     		
//                      cnd = new CND_EXTCMPFRAME();
                        break;
                    default:                                    // EXTENSION
                        cnd = new CCndExtension();
                        break;
                }
        }
        if (cnd != null)
        {
            cnd.evtCode = c;
            cnd.evtOi = app.file.readAShort();
            cnd.evtOiList = app.file.readAShort();
            cnd.evtFlags = app.file.readByte();
            cnd.evtFlags2 = app.file.readByte();
            cnd.evtNParams = app.file.readByte();
            cnd.evtDefType = app.file.readByte();
            cnd.evtIdentifier = app.file.readAShort();

            // Lis les parametres
            if (cnd.evtNParams > 0)
            {
                cnd.evtParams = new CParam[cnd.evtNParams];
                int n;
                for (n = 0; n < cnd.evtNParams; n++)
                {
                    cnd.evtParams[n] = CParam.create(app);
                }
            }

			

			

			

			// Optimization of "compare global value" for constant values
            if (bCompareG)
            {
	            CParam pParam = (CParam)cnd.evtParams[0];
				int num = ((PARAM_SHORT)pParam).value;
				CParamExpression pExp1 = (CParamExpression)cnd.evtParams[1];
				switch (c) {
				case ((-39 << 16) | 0xFFFF):
					((CND_COMPAREGDBL_GT)cnd).varNum = num;
					((CND_COMPAREGDBL_GT)cnd).value = pExp1.tokens[0].getDouble();
					break;
				case ((-38 << 16) | 0xFFFF):
					((CND_COMPAREGDBL_GE)cnd).varNum = num;
					((CND_COMPAREGDBL_GE)cnd).value = pExp1.tokens[0].getDouble();
					break;
				case ((-37 << 16) | 0xFFFF):
					((CND_COMPAREGDBL_LT)cnd).varNum = num;
					((CND_COMPAREGDBL_LT)cnd).value = pExp1.tokens[0].getDouble();
					break;
				case ((-36 << 16) | 0xFFFF):
					((CND_COMPAREGDBL_LE)cnd).varNum = num;
					((CND_COMPAREGDBL_LE)cnd).value = pExp1.tokens[0].getDouble();
					break;
				case ((-35 << 16) | 0xFFFF):
					((CND_COMPAREGDBL_NE)cnd).varNum = num;
					((CND_COMPAREGDBL_NE)cnd).value = pExp1.tokens[0].getDouble();
					break;
				case ((-34 << 16) | 0xFFFF):
					((CND_COMPAREGDBL_EQ)cnd).varNum = num;
					((CND_COMPAREGDBL_EQ)cnd).value = pExp1.tokens[0].getDouble();
					break;
				case ((-33 << 16) | 0xFFFF):
					((CND_COMPAREGINT_GT)cnd).varNum = num;
					((CND_COMPAREGINT_GT)cnd).value = pExp1.tokens[0].getInt();
					break;
				case ((-32 << 16) | 0xFFFF):
					((CND_COMPAREGINT_GE)cnd).varNum = num;
					((CND_COMPAREGINT_GE)cnd).value = pExp1.tokens[0].getInt();
					break;
				case ((-31 << 16) | 0xFFFF):
					((CND_COMPAREGINT_LT)cnd).varNum = num;
					((CND_COMPAREGINT_LT)cnd).value = pExp1.tokens[0].getInt();
					break;
				case ((-30 << 16) | 0xFFFF):
					((CND_COMPAREGINT_LE)cnd).varNum = num;
					((CND_COMPAREGINT_LE)cnd).value = pExp1.tokens[0].getInt();
					break;
				case ((-29 << 16) | 0xFFFF):
					((CND_COMPAREGINT_NE)cnd).varNum = num;
					((CND_COMPAREGINT_NE)cnd).value = pExp1.tokens[0].getInt();
					break;
				case ((-28 << 16) | 0xFFFF):
					((CND_COMPAREGINT_EQ)cnd).varNum = num;
					((CND_COMPAREGINT_EQ)cnd).value = pExp1.tokens[0].getInt();
					break;
				}
            }
        }
        else
        {
            Log.Log("Missing condition: " + Integer.toHexString(c));
        }

        // Positionne a la fin de la condition
        app.file.seek(debut + size);

        return cnd;
    }

    public boolean negaTRUE()
    {
        if ((evtFlags2 & EVFLAG2_NOT) != 0)
        {
            return false;
        }
        return true;
    }

    public boolean negaFALSE()
    {
        if ((evtFlags2 & EVFLAG2_NOT) != 0)
        {
            return true;
        }
        return false;
    }

    // Empeche les evenements one-shot GLOBAUX de se reproduire
    public boolean compute_GlobalNoRepeat(CRun rhPtr)
    {
        CEventGroup evgPtr = rhPtr.rhEvtProg.rhEventGroup;
        int inhibit = evgPtr.evgInhibit;
        evgPtr.evgInhibit = rhPtr.rhLoopCount;
        int loopCount = rhPtr.rhLoopCount;
        if (loopCount == inhibit)
        {
            return false;
        }
        loopCount--;
        if (loopCount == inhibit)
        {
            return false;
        }
        return true;
    }

    // ------------------------------------------------
    // Empeche les evenements one-shot de se reproduire
    // ------------------------------------------------
    boolean compute_NoRepeatCol(int identifier, CObject pHo)
    {
        // Stocke dans la table actuelle
        Integer id;
        int n;
    	int pArray_size = 0;

        ArrayList<Integer> pArray = pHo.hoBaseNoRepeat;
        if (pArray == null)
        {
            pArray = new ArrayList<Integer>();
            pHo.hoBaseNoRepeat = pArray;
        }
        else
        {
            // Evenement deja appele dans cette boucle?
        	pArray_size = pArray.size();
            for (n = 0; n < pArray_size; n++)
            {
                id = pArray.get(n);
                if (id.intValue() == identifier)
                {
                    return false;
                }
            }
        }
        id = Integer.valueOf(identifier);
        pArray.add(id);

        // Regarde au cycle precedent
        pArray = pHo.hoPrevNoRepeat;					// Au cycle precedent
        if (pArray == null)
        {
            return true;
        }
    	pArray_size = pArray.size();
        for (n = 0; n < pArray_size; n++)
        {
            id = pArray.get(n);
            if (id.intValue() == identifier)
            {
                return false;
            }
        }
        return true;
    }

    boolean compute_NoRepeat(CObject pHo)
    {
        return compute_NoRepeatCol(evtIdentifier, pHo);			//; L'identificateur
    }

    // ------------------------------------------------------------
    // CONDITION: Selection des objets actifs ayant une value donne
    // ------------------------------------------------------------
    public boolean evaChooseValueOld(CRun rhPtr, IChooseValue1 pRoutine)
    {
        int cpt = 0;
        CParamExpression p = (CParamExpression) evtParams[0];
        CObject pHo = rhPtr.rhEvtProg.evt_FirstObjectFromType(COI.OBJ_SPR);

		// Simple constant expression? evaluate it before the loop
		if ( p.tokens.length == 2 && p.tokens[0].isNumericalConstant() )
		{
			int value = p.tokens[0].getInt();
			while (pHo != null)
			{
				cpt++;
				if (pRoutine.evaluate(pHo, value) == false)
				{
					cpt--;
					rhPtr.rhEvtProg.evt_DeleteCurrentObject();
				}
				pHo = rhPtr.rhEvtProg.evt_NextObjectFromType();
			}
		}
		else
		{
			while (pHo != null)
			{
				cpt++;
				int value = rhPtr.get_EventExpressionInt(p);
				if (pRoutine.evaluate(pHo, value) == false)
				{
					cpt--;
					rhPtr.rhEvtProg.evt_DeleteCurrentObject();
				}
				pHo = rhPtr.rhEvtProg.evt_NextObjectFromType();
			}
		}
        // Vrai / Faux?
        if (cpt != 0)
        {
            return true;
        }
        return false;
    }

    public boolean evaChooseValue(CRun rhPtr, IChooseValue1 pRoutine)
    {
        int cpt = 0;
        CParamExpression p = (CParamExpression) evtParams[0];
        CObject pHo = rhPtr.rhEvtProg.evt_FirstObjectFromType((short) -1);

		// Simple constant expression? evaluate it before the loop
		if ( p.tokens.length == 2 && p.tokens[0].isNumericalConstant() )
		{
			int value = p.tokens[0].getInt();
			while (pHo != null)
			{
				cpt++;
				if (pRoutine.evaluate(pHo, value) == false)
				{
					cpt--;
					rhPtr.rhEvtProg.evt_DeleteCurrentObject();
				}
				pHo = rhPtr.rhEvtProg.evt_NextObjectFromType();
			}
		}
		else
		{
			while (pHo != null)
			{
				cpt++;
				int value = rhPtr.get_EventExpressionInt(p);
				if (pRoutine.evaluate(pHo, value) == false)
				{
					cpt--;
					rhPtr.rhEvtProg.evt_DeleteCurrentObject();
				}
				pHo = rhPtr.rhEvtProg.evt_NextObjectFromType();
			}
		}
        // Vrai / Faux?
        if (cpt != 0)
        {
            return true;
        }
        return false;
    }

    public boolean evaExpObject(CRun rhPtr, IEvaExpObject pRoutine)
    {
        CObject pHo = rhPtr.rhEvtProg.evt_FirstObject(evtOiList);
        int cpt = rhPtr.rhEvtProg.evtNSelectedObjects;
        CParamExpression p = (CParamExpression) evtParams[0];

		// No interdependence between event object and parameter objects? evaluate expression before the loop
        if ((evtFlags2 & CEvent.EVFLAG2_NOOBJECTINTERDEPENDENCE) != 0) {
			int value;
			if ( p.tokens.length == 2 && p.tokens[0].isNumericalConstant() )	// Simple constant expression? avoid evaluating expression
				value = p.tokens[0].getInt();
			else
				value = rhPtr.get_EventExpressionInt(p);
            while (pHo != null) {
                if (pRoutine.evaExpRoutine(pHo, value, p.comparaison) == false) {
                    cpt--;
                    rhPtr.rhEvtProg.evt_DeleteCurrentObject();
                }
                pHo = rhPtr.rhEvtProg.evt_NextObject();
            }
		}
        else {
            int value;
            while (pHo != null) {
                value = rhPtr.get_EventExpressionInt(p);
                if (pRoutine.evaExpRoutine(pHo, value, p.comparaison) == false) {
					cpt--;
					rhPtr.rhEvtProg.evt_DeleteCurrentObject();
				}
				pHo = rhPtr.rhEvtProg.evt_NextObject();
			}
        }
        if (cpt != 0)
        {
            return true;
        }
        return false;
    }

    public boolean evaExpObjectDouble(CRun rhPtr, IEvaExpObjectDouble pRoutine)
    {
        CObject pHo = rhPtr.rhEvtProg.evt_FirstObject(evtOiList);
        int cpt = rhPtr.rhEvtProg.evtNSelectedObjects;
        CParamExpression p = (CParamExpression) evtParams[0];

		// No interdependence between event object and parameter objects? evaluate expression before the loop
        if ((evtFlags2 & CEvent.EVFLAG2_NOOBJECTINTERDEPENDENCE) != 0) {
			double value;
			if ( p.tokens.length == 2 && p.tokens[0].isNumericalConstant() )	// Simple constant expression? avoid evaluating expression
				value = p.tokens[0].getDouble();
			else
				value = rhPtr.get_EventExpressionDouble(p);
            while (pHo != null)
            {
                if (pRoutine.evaExpRoutineDouble(pHo, value, p.comparaison) == false)
                {
                    cpt--;
                    rhPtr.rhEvtProg.evt_DeleteCurrentObject();
                }
                pHo = rhPtr.rhEvtProg.evt_NextObject();
            }
		}
		else
		{
			double value;
            while (pHo != null) {
				value = rhPtr.get_EventExpressionDouble(p);
                if (pRoutine.evaExpRoutineDouble(pHo, value, p.comparaison) == false) {
					cpt--;
					rhPtr.rhEvtProg.evt_DeleteCurrentObject();
				}
				pHo = rhPtr.rhEvtProg.evt_NextObject();
			}
        }
        if (cpt != 0)
        {
            return true;
        }
        return false;
    }

    public boolean evaObject(CRun rhPtr, IEvaObject pRoutine)
    {
        // Boucle d'exploration
        CObject pHo = rhPtr.rhEvtProg.evt_FirstObject(evtOiList);
        int cpt = rhPtr.rhEvtProg.evtNSelectedObjects;
        while (pHo != null)
        {
            if (pRoutine.evaObjectRoutine(pHo) == false)
            {
                cpt--;
                rhPtr.rhEvtProg.evt_DeleteCurrentObject();			// On le vire!
            }
            pHo = rhPtr.rhEvtProg.evt_NextObject();
        }
        // Vrai / Faux?
        if (cpt != 0)
        {
            return true;
        }
        return false;
    }

    public boolean compareCondition(CRun rhPtr, int param, int v)
    {
        // Le parametre
        CValue value2 = rhPtr.get_EventExpressionAny_WithoutNewValue((CParamExpression) evtParams[param]);
        short comp = ((CParamExpression) evtParams[param]).comparaison;
		return CRun.compareIntTo(v, value2, comp);
    }
    // Verifie une checkmark

    public boolean checkMark(CRun rhPtr, int mark)
    {
        if (mark == 0)
        {
            return false;				// Pas la premiere boucle
        }
        if (mark == rhPtr.rhLoopCount)
        {
            return true;
        }
        if (mark == rhPtr.rhLoopCount - 1)
        {
            return true;
        }
        return false;
    }

    // IS COLLIDING
    public boolean isColliding(CRun rhPtr)
    {
        // Cas particulier lors de conditions OU, selectionne les deux listes d'objet
        if (rhPtr.rhEvtProg.rh4ConditionsFalse)
        {
            rhPtr.rhEvtProg.evt_FirstObject(evtOiList);
            rhPtr.rhEvtProg.evt_FirstObject(((PARAM_OBJECT) evtParams[0]).oiList);
            return false;
        }

        // Positionne le flag negate
        boolean negate = false;
        if ((evtFlags2 & EVFLAG2_NOT) != 0)
        {
            negate = true;
        }

        // Un objet a voir?
        CObject pHo = rhPtr.rhEvtProg.evt_FirstObject(evtOiList);
        if (pHo == null)
        {
            return negaFALSE();
        }
        int cpt = rhPtr.rhEvtProg.evtNSelectedObjects;
        int cptTotal = cpt;

        short[] oi2List;
        short oi = ((PARAM_OBJECT) evtParams[0]).oi;
        CQualToOiList qoil = null;
        if (oi < 0)					//; Le deuxieme objet
        {
            // Qualifier
            qoil = rhPtr.rhEvtProg.qualToOiList[((PARAM_OBJECT) evtParams[0]).oiList & 0x7FFF];		// Pointe l'OILIST
            oi2List = qoil.qoiList;
        }
        else
        {
            // Normal object
            oi2List = oiOneObjectList;	// new short[2];		// todo: static?
            oi2List[0] = oi;
            oi2List[1] = ((PARAM_OBJECT) evtParams[0]).oiList;
        }

        // Boucle d'exploration
        boolean bFlag = false;
        ArrayList<CObject> list = null;
        ArrayList<CObject> list2 = new ArrayList<CObject>();
        int index;
        CObject pHo2;
        do
        {
            // Test at a specific position?
            int hox = pHo.hoX;
            int hoy = pHo.hoY;
            if (evtNParams >= 3) {
                hox = rhPtr.get_EventExpressionInt((CParamExpression)evtParams[1]);
                hoy = rhPtr.get_EventExpressionInt((CParamExpression)evtParams[2]);
            }

            list = rhPtr.objectAllCol_IXY(pHo, pHo.roc.rcImage, pHo.roc.rcAngle, pHo.roc.rcScaleX, pHo.roc.rcScaleY, hox, hoy, oi2List);
            if (list == null)
            {
                if (negate == false)
                {
                    cpt--;
                    rhPtr.rhEvtProg.evt_DeleteCurrentObject();
                }
            }
            else
            {
                // Explore la liste des sprites en collision a la recherche du deuxieme objet
                bFlag=false;
                for (index=0; index<list.size(); index++)
                {
                    pHo2=(CObject)list.get(index);
                    if ((pHo2.hoFlags&CObject.HOF_DESTROYED)==0)    // Detruit au cycle precedent?
                    {
                        list2.add(pHo2);
                        bFlag=true;
                    }
                }
                list.clear();

                // Vire le sprite?
                if (negate==true)
                {
                    if (bFlag==true)
                    {
                        cpt--;
                        rhPtr.rhEvtProg.evt_DeleteCurrentObject();
                    }
                }
                else
                {
                    if (bFlag==false)
                    {
                        cpt--;
                        rhPtr.rhEvtProg.evt_DeleteCurrentObject();
                    }
                }
            }
            pHo = rhPtr.rhEvtProg.evt_NextObject();
        } while (pHo != null);

        if (negate==false)
        {
            if (cpt==0)
            {
                list2.clear();
                return false;
            }
        }
        else
        {
            if (cpt<cptTotal)
            {
                list2.clear();
                return false;
            }
        }

        // Fabrique la liste du sprite II
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        pHo = rhPtr.rhEvtProg.evt_FirstObject(((PARAM_OBJECT) evtParams[0]).oiList);
        if (pHo == null)
        {
            return false;
        }
        cpt = rhPtr.rhEvtProg.evtNSelectedObjects;
        int list2_size = 0;
        if ( list2 != null )
        {
            list2_size = list2.size();

            if (negate == false)
            {
                do
                {
                    for (index = 0; index < list2_size; index++)
                    {
                        pHo2 = list2.get(index);
                        if (pHo == pHo2)
                        {
                            break;
                        }
                    }
                    if (index == list2_size)
                    {
                        cpt--;
                        rhPtr.rhEvtProg.evt_DeleteCurrentObject();
                    }
                    pHo = rhPtr.rhEvtProg.evt_NextObject();
                } while (pHo != null);
                if (cpt != 0)
                {
                    return true;
                }
                return false;
            }

            // Exploration avec negation
            do
            {
                for (index = 0; index < list2_size; index++)
                {
                    pHo2 = list2.get(index);
                    if (pHo == pHo2)
                    {
                        cpt--;
                        rhPtr.rhEvtProg.evt_DeleteCurrentObject();
                        break;
                    }
                }
                pHo = rhPtr.rhEvtProg.evt_NextObject();
            } while (pHo != null);
            if (cpt != 0)
            {
                return true;
            }
        }
        return false;
    }
    
    /** Abstract evaluation method, with an object.
     */
    public abstract boolean eva1(CRun rhPtr, CObject hoPtr);
    /** Abstract evaluation method, without object.
     */
    public abstract boolean eva2(CRun rhPtr);    
}
