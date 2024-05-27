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

package Extensions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.io.BufferedReader;

import Actions.CActExtension;
import Application.CKeyConvert;
import Application.CRunApp;
import Conditions.CCndExtension;
import Expressions.CValue;
import OI.CObjectCommon;
import Params.CPositionInfo;
import RunLoop.CCreateObjectInfo;
import Runtime.MMFRuntime;
import Runtime.PermissionsResultAction;
import Services.CBinaryFile;
import Services.CFontInfo;
import Services.CFuncVal;
import Services.CRect;
import Services.CServices;
import Services.UnicodeReader;

public class CRunkcedit extends CRunViewExtension
{
	@Override
	public int getNumberOfConditions()
	{   return 7;
	}

	private boolean modified;
	private CFontInfo font;

	private int fColor = 0xff000000;
	private int bColor = 0xffffffff;
	private int lColor = 0xff333333;

	private int InputTypeKB = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
	private int IMEOptions  = EditorInfo.IME_ACTION_UNSPECIFIED;
	private int flags = 0;
	private int fitfontsize;
	private boolean bHasFocus = false;
	private boolean bReadOnly = false;
	private int kbCount = 0;
	private int lastkey = 0;
	private boolean firstTime;

	private int caretPosition;
	
	private InputMethodManager imm = null;

	private TextWatcher mWatch = null;

	private EditText field = null;
	
    float nDensity = 0.0f;
    
    private CValue expRet;

	private boolean enabled_perms;

	private Runnable onCleanBuffer=new Runnable() {

		@Override
		public void run() {
			if(lastkey > 0) {
				MMFRuntime.inst.app.keyUp(lastkey);
				lastkey = 0;
			}
		}
	};

	public void ResizeToFit(CRect rect, boolean forceSize)
	{
		String text ="fFwqjWQJ";

		Paint paint = new Paint();
		Rect bounds = new Rect();

		int text_height = rect.bottom - rect.top;
		int text_check_h = 0;

		int incr_text_size = 4;
		boolean found_desired_size = true;

        int additionalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, MMFRuntime.inst.getResources().getDisplayMetrics());
        // text is moved up since cursor does require more space than text size, added to cover this in all situation and api
		
		while (found_desired_size && (flags & 0x0020) == 0)
		{			
			paint.setTextSize(incr_text_size);	// have this the same as your text size

			paint.getTextBounds(text, 0, text.length(), bounds);

			text_check_h =  bounds.height();
			incr_text_size++;
			
			if (text_height <= text_check_h )
			{
				found_desired_size = false;
				if(view != null && field != null)
				{
				   	if ((rh.rhApp.hdr2Options & CRunApp.AH2OPT_SYSTEMFONT) == 0 &&  Build.VERSION.SDK_INT  < 18)
				   	{
				   		field.setPadding(2, 2, 1, 2);
				   	}
				   	else
				   		field.setPadding(2, 0, 2, 0);
				   		
					if(forceSize)
					{
						int spacer =  2 + field.getPaddingBottom() + field.getPaddingTop();
						this.font.lfHeight = text_check_h - spacer;
						this.updateFont(this.font);
					}

					field.setMinWidth(rect.right - rect.left);
					field.setMinHeight(rect.bottom - rect.top + additionalPadding);
					field.setHeight(rect.bottom - rect.top + additionalPadding);
					
					this.setViewX(rect.left);
					this.setViewY(rect.top);
					this.setViewWidth(rect.right - rect.left);
					this.setViewHeight(rect.bottom - rect.top ); 
					fitfontsize = (int) pixelsToSp(field.getTextSize());
				}
			}
		}

	}
	
	public void SetHintColor(int color)
	{
		if(field != null)
			field.setHintTextColor(color);
	}
	
	public void SetHintText(String s)
	{
		if(field != null && s != null)
		{
			field.setText(null);
			field.setHint(s);
		}
	}
	private float pixelsToSp(float px) {
	    float scaledDensity = ho.getControlsContext().getResources().getDisplayMetrics().scaledDensity;
	    return px/scaledDensity;
	}
	
	public int getFitFontSize()
	{
		return fitfontsize;
	}
	
	public void SetKeyboard(int keyb) {
		InputTypeKB = keyb;

		if ((flags & 0x0020) != 0) { // Multiline
			InputTypeKB |= InputType.TYPE_TEXT_FLAG_MULTI_LINE;
		}

		if ((flags & 0x0040) != 0) { // Password
			InputTypeKB |= InputType.TYPE_TEXT_VARIATION_PASSWORD;
		}

		if(view != null)
			((EditText)view).setInputType(InputTypeKB);
	}

	public void SetIMEOptions(int IMEkeyb) {
		IMEOptions = IMEkeyb;
		if(view != null)
			((EditText)view).setImeOptions(IMEOptions);
	}

	
	private static Callback fieldCallback;


	@SuppressLint("AppCustomWidgetView")
	@Override
	public void createRunView(final CBinaryFile file, CCreateObjectInfo cob, int version)
	{
		expRet = new CValue(0);

		this.ho.hoOEFlags |= CObjectCommon.OEFLAG_NEVERKILL;

        ho.hoImgWidth = file.readShort();
		ho.hoImgHeight = file.readShort();

		if(rh.rhApp.bUnicode)
			font = file.readLogFont();
		else
			font = file.readLogFont16();

		file.skipBytes(4 * 16); // Custom colors
		//file.skipBytes(8); // Foreground/background colors
		fColor = (0xff << 24) | file.readColor(); // Foreground color
		bColor = (0xff << 24) | file.readColor(); // Background color

		if(rh.rhApp.bUnicode)
			file.skipBytes(80);
		else
			file.skipBytes(40);

		flags = file.readInt();
		
    	if ((rh.rhApp.hdr2Options & CRunApp.AH2OPT_SYSTEMFONT) != 0) {
    		font.font = Typeface.DEFAULT;
     	}

		field = new androidx.appcompat.widget.AppCompatEditText(ho.getControlsContext())
		{
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				lastkey = keyCode;
				MMFRuntime.inst.app.keyDown(lastkey);
				if(MMFRuntime.inst.keyBoardOn)
					view.postDelayed(onCleanBuffer, 50);
				if (keyCode == KeyEvent.KEYCODE_TAB) {
					focusNext();
					return true;
				}
				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
					caretPosition = field.getSelectionStart() - 1;
					caretPosition = caretPosition < 0 ? 0 : caretPosition;
					caretPosition = caretPosition > field.getText().length() ? field.getText().length() : caretPosition;
					field.setSelection(caretPosition, caretPosition);
					return true;
				}
				if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
					caretPosition = field.getSelectionStart() + 1;
					caretPosition = caretPosition < 0 ? 0 : caretPosition;
					caretPosition = caretPosition > field.getText().length() ? field.getText().length() : caretPosition;
					field.setSelection(caretPosition, caretPosition);
					return true;
				}
				return super.onKeyDown(keyCode, event);
			}
			@Override
			public boolean onKeyUp(int keyCode, KeyEvent event) {
				if(field != null)
					caretPosition = field.getSelectionStart();

				if(!MMFRuntime.inst.keyBoardOn)
					MMFRuntime.inst.app.keyUp(lastkey);
				return super.onKeyUp(keyCode, event);
			}
			@Override
			public boolean onKeyPreIme(int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
					if(field != null && field.hasFocus()) {
						field.clearFocus();
					}
					return true;
				}
				return super.onKeyPreIme(keyCode, event);
			}

		};

        InputTypeKB = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;

        fieldCallback = new Callback() {

            @Override
            public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1) {
                return false;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        };

		
		field.setPadding(2, 3, 2, 3);

		if ((flags & 0x0010) != 0) { // Read only
			field.setTextIsSelectable(false);
			field.setFocusable(false);
			field.setCustomSelectionActionModeCallback(fieldCallback);
		}
		else {
			field.setTextIsSelectable(true);
			field.setFocusable(true);
            field.setCustomSelectionActionModeCallback(null);
		}

		int vert_gravity = 0;
		if ((flags & 0x0020) != 0) {// Multiline
			InputTypeKB |= InputType.TYPE_TEXT_FLAG_MULTI_LINE;
			vert_gravity = Gravity.START | Gravity.TOP;
		}
		else {
			field.setSingleLine();
			vert_gravity = Gravity.CENTER_VERTICAL;
		}

		
		if ((flags & 0x0040) != 0) // Password
			InputTypeKB |= InputType.TYPE_TEXT_VARIATION_PASSWORD;
		//field.setTransformationMethod(new PasswordTransformationMethod());

		if ((flags & 0x0100) != 0) // Hide on start
			field.setVisibility(View.INVISIBLE);

		if ((flags & 0x4000) != 0) // Transparent
			field.setBackgroundDrawable(null);

		int horiz_gravity = Gravity.START;
		if ((flags & 0x00010000) != 0) // Align center
			horiz_gravity = Gravity.CENTER_HORIZONTAL;
		if ((flags & 0x00020000) != 0) // Align right
			horiz_gravity = Gravity.RIGHT;

		field.setGravity(vert_gravity | horiz_gravity);

		//0x0001 horizontal scroll
		//0x0002 horizontal autoscroll
		if ((flags & 0x0001) != 0 || (flags & 0x0002) != 0) {// Horizontal Scroll
			field.setHorizontallyScrolling(true);
			field.setHorizontalScrollBarEnabled(true);
		}
		else {
			field.setHorizontallyScrolling(false);
			field.setHorizontalScrollBarEnabled(false);			
		}

		//0x0004 vertical scroll
		//0x0008 vertical autoscroll
		// Stop any vertical scrolling
		if ((flags & 0x0004) != 0 || (flags & 0x0008) != 0) {// Vertical Scroll
			field.setVerticalScrollBarEnabled(true);
		}
		else {
			field.setMovementMethod(ArrowKeyMovementMethod.getInstance());			
		}

		if ((flags & 0x1000) == 0) {       	// Not System Color
			field.setTextColor (new ColorStateList (
					new int [] [] {
							new int [] {-android.R.attr.state_pressed},
							new int [] {-android.R.attr.state_focused},
							new int [] {}
					},
					new int [] {
							fColor,
							fColor,
							fColor
					}
					));

			if ((flags & 0x4000) == 0)
				field.setBackgroundColor((0xFF << 24 | bColor));
			
		}

		if((flags & 0x0080) != 0) {	//Adding border 294.1
			GradientDrawable gradientDrawableDefault = new GradientDrawable();
			if ((flags & 0x1000) == 0 && (flags & 0x4000) == 0)
				gradientDrawableDefault.setColor((0xFF << 24 | bColor));
			else if((flags & 0x1000) != 0)
				gradientDrawableDefault.setColor(Color.TRANSPARENT);
			gradientDrawableDefault.setCornerRadius(2.0f);
			gradientDrawableDefault.setStroke( 1, Color.BLACK);
			field.setBackground(gradientDrawableDefault);
		}

		if ((flags & 0x2000) != 0) // 3D look
			field.setShadowLayer(1.5f, 1, 1, Color.DKGRAY);

		field.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(v == field) {
					if (hasFocus) {
						bHasFocus = true;
						caretPosition = ((EditText) v).getSelectionStart();
						MMFRuntime.inst.HideKeyboard(v, false);
					} else {
						bHasFocus = false;
						MMFRuntime.inst.HideKeyboard(v, true);
						v.clearFocus();
					}
				}
			}
		}); 

		field.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (v == field && (actionId == EditorInfo.IME_ACTION_DONE 			||
						actionId == EditorInfo.IME_ACTION_SEND 		||
						actionId == EditorInfo.IME_ACTION_SEARCH 	||
						actionId == EditorInfo.IME_ACTION_GO 		||
						actionId == EditorInfo.IME_ACTION_NEXT )) {
					if(bHasFocus)
						modified = true;
					// do your stuff here
					lastkey = KeyEvent.KEYCODE_ENTER;
					MMFRuntime.inst.app.keyDown(lastkey);
					field.postDelayed(onCleanBuffer, 75);
					if(view != null)
						view.clearFocus();
				}
				return false;
			}

		});

		mWatch = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if(bHasFocus)
					modified = true;
				if(MMFRuntime.inst.keyBoardOn) {
					if(s.length() > kbCount) {
						int a = s.toString().substring(s.length()-1).toUpperCase().charAt(0);
						if(a >= 'A' && a <= 'Z' || a == ' ') {
							lastkey = CKeyConvert.getJavaKey(a);
							MMFRuntime.inst.app.keyDown(lastkey);
							view.postDelayed(onCleanBuffer, 75);
						}
						if(MMFRuntime.inst.keyBoardOn && a == '\n') {
							lastkey = KeyEvent.KEYCODE_ENTER;
							MMFRuntime.inst.app.keyDown(lastkey);
							view.postDelayed(onCleanBuffer, 75);
						}
					}

					kbCount = s.length();
					if(view instanceof EditText)
						caretPosition = ((EditText) view).getSelectionStart();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(view != null && view instanceof EditText)
					caretPosition = ((EditText) view).getSelectionStart();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}
		};

		field.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				caretPosition = ((EditText) v).getSelectionStart();
				
			}
		});
		
		field.addTextChangedListener(mWatch);

		field.setInputType(InputTypeKB);
		field.setCursorVisible(true);

		modified = false;
		firstTime = true;

		setView (field);
		updateFont (font);
		if((flags & 0x0020) == 0)
		{
	    	if ((rh.rhApp.hdr2Options & CRunApp.AH2OPT_SYSTEMFONT) == 0)
	    	{
	    		CRect rect = new CRect();
	    		rect.left = ho.hoX;
	    		rect.top  = ho.hoY;
	    		rect.right =ho.hoX + ho.hoImgWidth;
	    		rect.bottom=ho.hoY + ho.hoImgHeight;
	    		ResizeToFit(rect, false);
	    	}
	    	
	    	else
	    	{
	    		int height = (int)(field.getTextSize() + 0.5f) + 6; //def_height ;
	    		ho.hoImgHeight = (height > ho.hoImgHeight) ? height : ho.hoImgHeight;
	    		
	    		field.setHeight(ho.hoImgHeight);
	    		setViewHeight(ho.hoImgHeight);
	
	    	}
		}
		// define the Input method and create a new IMMResult
		//imm = (InputMethodManager) MMFRuntime.inst.getSystemService(Context.INPUT_METHOD_SERVICE);		
		
        enabled_perms = false;

		if(Build.VERSION.SDK_INT  > 22)
			enabled_perms = MMFRuntime.inst.hasAllPermissionsGranted(
					new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
		else
			enabled_perms = true;

		
		if(view != null)
		{
        	view.invalidate();
        	view.requestLayout();
		}
	}    

	@Override
	public int handleRunObject()
	{
		super.handleRunObject ();

		if (view != null) {
			if((flags & 0x0100) == 0 && firstTime) {
				
				if(rh.rhApp.parentApp != null)
				{
					int x = ho.hoX + rh.rhApp.absoluteX;
					int y = ho.hoY + rh.rhApp.absoluteY;
					if((x > rh.rhApp.widthSetting
						|| x + ho.hoImgWidth < 0
						|| y > rh.rhApp.heightSetting
						|| y + ho.hoImgHeight < 0))
					{
						view.setVisibility(View.INVISIBLE);		
					}
				}
				else
				{
					view.setVisibility(View.VISIBLE);				
				}
				firstTime = false;
			}
		}
		return 0;
	}
	
	
	@Override
	public void destroyRunObject(boolean bFast) {
		field.removeTextChangedListener(mWatch);
		if(view != null)
			view.setVisibility(View.GONE);
		setView (null);
	}

	@Override
	public CFontInfo getRunObjectFont()
	{
		return font;
	}

	@Override
	public void setRunObjectFont(CFontInfo font, CRect rc)
	{
			this.font = font;
	
			if(rc != null) {
				setViewWidth(rc.right - rc.left);
				if ((flags & 0x0020) == 0) {// Not Multiline
					setViewHeight(rc.bottom - rc.top);
				}
			}
		updateFont(this.font);
		updateLayout();
	}

	@Override
	public void setRunObjectTextColor(int rgb) 
	{
		this.fColor = 0xFF << 24 | rgb;
		if(view != null) {
			if ((flags & 0x1000) == 0) {       	// Not System Color
				field.setTextColor (new ColorStateList (
						new int [] [] {
							new int [] {-android.R.attr.state_pressed},
							new int [] {-android.R.attr.state_focused},
							new int [] {}
						},
						new int [] {
								fColor,
								fColor,
								fColor
						}
						));

				if ((flags & 0x4000) == 0)
					field.setBackgroundColor((0xFF << 24 | bColor));

			}
			updateLayout();
		}

	}
	
	@Override
	public boolean condition(int num, CCndExtension cnd)
	{
		switch (num)
		{
		case 0: // Is visible?

			return view.getVisibility() == View.VISIBLE;

		case 1: // Is enabled?

			return view.isEnabled();

		case 2: // Can undo?

			return false;

		case 3: // Just been modified?

			// Doesn't make sense, but this is how flash does it..

			boolean temp = modified;
			modified = false;
			return temp;

		case 4: // Has focus?

			return bHasFocus;

		case 5: // Is number?

			String text = ((EditText) view).getText().toString();

			if (text.length() == 0)
				return false;

			int index = 0;
			char first = text.charAt(index);

			for (; first == ' ' || first == '	'; first = text.charAt(index))
			{
			}

			return (first >= '0' && first <= '9');

		case 6: // Is selected?

			return ((EditText) view).getSelectionStart() != -1;

		};

		return false;
	}

	private void replaceSelection(String replacement)
	{
		final EditText field = (EditText) view;

		String text = field.getText().toString();

		int selectionStart = field.getSelectionStart();
		int selectionEnd = field.getSelectionEnd();

		final String newText = text.substring(0, selectionStart) + replacement
				+ text.substring(selectionEnd, text.length());

		field.setText(newText);
	}

	private void LoadEdit(final String filename)
	{
		String text = "";
		//Log.Log("Starting ...");
		try
		{
			CRunApp.HFile file = null;
			file = ho.openHFile(filename);

			//Log.Log("Opened ...");
			if(file != null) {
				UnicodeReader ur = new UnicodeReader(file.stream, MMFRuntime.inst.charSet);
				BufferedReader reader = new BufferedReader(ur);
				//Log.Log("Verified ...");

				try {
					int a = 0;
					StringBuilder sb = new StringBuilder();
					String line = reader.readLine();

					while (line != null) {
						if ( a != 0 )
							sb.append('\n');
						a = 1;
						sb.append(line);
						line = reader.readLine();
					}
					text = sb.toString();
				} finally {
					reader.close();
				}
				//Log.Log("About to set text ...");
				field.setText(text);
				field.notifyAll();
				file.close();
				//Log.Log("Ended ...");

			}
		}
		catch(Exception e)
		{
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void action(int num, CActExtension act)
	{
		final EditText field = (EditText) view;

		switch (num)
		{
		case 0: // Load text from file
		{
			final String filename = act.getParamFilename (rh, 0);
//			boolean lread_perm = enabled_perms;
//
//			if(Build.VERSION.SDK_INT  > 18)
//				lread_perm = MMFRuntime.inst.hasPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE);
//
//			if(!lread_perm)
//			{
//				if (!filename.contains("/")
//						|| filename.contains(MMFRuntime.packageName)
//						|| URLUtil.isNetworkUrl(filename))
//					lread_perm = true;
//			}


			if(CServices.canFusionRead(filename))
				LoadEdit(filename);
			else
				MMFRuntime.inst.askForPermission(
						new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
						new PermissionsResultAction() {
							@Override
							public void onGranted() {
								LoadEdit(filename);
							}

							@Override
							public void onDenied(String permission) {

							}
						}
				);

			break;
		}

		case 1: // Load text from file via a file selector

			break;

		case 2: // Save text to file
		{
			final String savename = act.getParamFilename (rh, 0);

			if(!savename.contains(MMFRuntime.packageName))
			{
				if (CServices.allowWriteFileMode(savename))
					CServices.saveFile (MMFRuntime.inst, savename, field.getText().toString());
				else
					MMFRuntime.inst.askForPermission(
							new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
							new PermissionsResultAction() {
								@Override
								public void onGranted() {
									CServices.saveFile (MMFRuntime.inst, savename, field.getText().toString());
								}

								@Override
								public void onDenied(String permission) {
									if(Build.VERSION.SDK_INT > 28)
										CServices.saveFile (MMFRuntime.inst, savename, field.getText().toString());

								}
							}
					);
			}
			else
			{
				CServices.saveFile (MMFRuntime.inst, savename, field.getText().toString());
			}
			break;
		}

		case 3: // Save text to file via a file selector

			break;

		case 4: // Set text
		{
			if(field != null) {
				boolean enabled = field.isEnabled();
				if(enabled)
					field.setEnabled(false);
				field.setText(act.getParamExpString(rh, 0));
				if(enabled)
					field.setEnabled(true);
			}
			break;
		}

		case 5: // Replace selection

			replaceSelection(act.getParamExpString(rh, 0));
			field.setSelection(act.getParamExpString(rh, 0).length());
			break;

		case 6: // Cut
		{
			rh.rhApp.clipboard
			(field.getText().toString().substring(field.getSelectionStart(), field.getSelectionEnd()));

			replaceSelection("");

			break;
		}

		case 7: // Copy
		{
			rh.rhApp.clipboard
			(field.getText().toString().substring(field.getSelectionStart(), field.getSelectionEnd()));

			break;
		}

		case 8: // Paste
		{
			replaceSelection(rh.rhApp.clipboard ());

			break;
		}

		case 9: // Clear

			if(field != null)
				field.setText ("");
			break;

		case 10: // Undo

			break;

		case 11: // Clear undo buffer

			break;

		case 12: // Show

			if(field != null)
				field.setVisibility(View.VISIBLE);
			firstTime = false;
			break;

		case 13: // Hide

			if(field != null)
				field.setVisibility(View.INVISIBLE);
			break;

		case 14: // Set font via font selector

			break;

		case 15: // Set font color via color selector

			break;

		case 16: // Activate

			MMFRuntime.inst.runOnRuntimeThread( new Runnable() {

				@Override
				public void run() {
					if(view != null)
						view.requestFocus();
				}
			});

			break;

		case 20: // Set read only off
			if(field != null) {
				field.setTextIsSelectable(true);
				field.setFocusableInTouchMode(true);
				field.setFocusable(true);
				field.setCustomSelectionActionModeCallback(null);
			}
			bReadOnly = false;
			break;

		case 17: // Enable

			if(view != null)
				view.setEnabled (true);

			break;

		case 19: // Set read only on
			if(field != null) {
				field.setTextIsSelectable(false);
				field.setFocusableInTouchMode(false);
				field.setFocusable(false);
				field.setCustomSelectionActionModeCallback(fieldCallback);
			}
			bReadOnly = true;
			break;

		case 18: // Disable

			if(view != null)
				view.setEnabled (false);

			break;

		case 21: // Force text modified on

			modified = true;
			break;

		case 22: // Force text modified off

			modified = false;
			break;

		case 23: // Limit text size

			int length = act.getParamExpression (rh, 0);

			InputFilter[] filters = new InputFilter [1];
			filters [0] = new InputFilter.LengthFilter (length);

			((TextView) view).setFilters (filters);

			break;

		case 24: // Set position

			CPositionInfo position = act.getParamPosition(rh, 0);

			ho.hoX = position.x;
			ho.hoY = position.y;

			break;

		case 25: // Set X position

			ho.hoX = act.getParamExpression(rh, 0);
			break;

		case 26: // Set Y position

			ho.hoY = act.getParamExpression(rh, 0);
			break;

		case 27: // Set size

			ho.setWidth (act.getParamExpression(rh, 0));
			ho.setHeight(act.getParamExpression(rh, 1));

			break;

		case 28: // Set X size

			ho.setWidth(act.getParamExpression(rh, 0));
			break;

		case 29: // Set Y size

			ho.setHeight(act.getParamExpression(rh, 0));
			break;

		case 30: // Deactivate

			if(view != null)
				view.clearFocus();

			break;

		case 31: // Scroll to top

			field.scrollTo(1, 1);
			break;

		case 32: // Scroll to line

			break;

		case 33: // Scroll to end

			field.scrollTo(1, field.getLineCount()*field.getLineHeight()+20);
			break;

		case 34: // Set color
			fColor = act.getParamColour(rh, 0);
	        flags = flags | ~ 0x1000;
			setRunObjectTextColor(fColor);  
			break;

		case 35: // Set background color
			bColor = act.getParamColour(rh, 0);
	        flags = flags &(~( 0x1000 | 0x4000));
			if ((flags & 0x4000) == 0)
				field.setBackgroundColor((0xFF << 24 | bColor));
			break;
		case 36: // Insert text at caret position
			String textAdd = act.getParamExpString(rh, 0);
			int pos = act.getParamExpression(rh, 1);
			if(field != null && textAdd.length() > 0)
			{
				String fieldText = field.getText().toString();
				String textBefore = fieldText.substring(0, pos);
				String textAfter = fieldText.substring(pos, fieldText.length());
				field.setText(textBefore+textAdd+textAfter);
			}
			break;
		case 37: // Set caret position
			int posx = act.getParamExpression(rh, 0);
			if(field != null && field.getVisibility() == View.VISIBLE)
			{
				if(posx <= field.getText().length())
					field.setSelection(posx, posx);
				caretPosition = field.getSelectionStart();				
			}
			break;
		}
		;
	}

	@Override
	public CValue expression(int num)
	{
		//final EditText field = (EditText) view;
		if(field == null)
		{
			expRet.forceInt(0);
			return expRet;
		}

		switch (num)
		{
		case 0: // Get text
		{
			expRet.forceString(field.getText().toString());
			return expRet;
		}
		case 1: // Get selection
		{
			expRet.forceString(field.getText().toString().substring(field.getSelectionStart(), field.getSelectionEnd()));
			return expRet;
		}
		case 2: // Get X position
		{
			expRet.forceInt(ho.hoX);
			return expRet;
		}
		case 3: // Get Y position
		{
			expRet.forceInt(ho.hoY);
			return expRet;
		}
		case 4: // Get X size
		{
			expRet.forceInt(ho.hoImgWidth);
			return expRet;
		}
		case 5: // Get Y size
		{
			expRet.forceInt(ho.hoImgHeight);
			return expRet;
		}
		case 6: // Get value
		{
			//expRet.forceInt(CServices.parseInt(field.getText().toString()));
			CFuncVal val = new CFuncVal();
			switch (val.parse(field.getText().toString())) {
				case 0:
					expRet.forceInt(val.intValue);
					break;
				case 1:
					expRet.forceDouble(val.doubleValue);
					break;
			}
			return expRet;
		}
		case 7: // Get first line
		{
			String text = field.getText().toString();
			expRet.forceString(text.substring(0, Math.min(text.indexOf('\r'), text.indexOf('\n'))));
			return expRet;
		}
		case 8: // Get line count
		{
			expRet.forceInt(field.getLineCount());
			return expRet;
		}
		case 9: // Get color
		{
			expRet.forceInt(0);
			return expRet;
		}
		case 10: // Get background color
		{
			expRet.forceInt(0);
			return expRet;
		}
		case 11: // Get Caret Position
		{
			expRet.forceInt(-1);
			if (field != null)
				expRet.forceInt(caretPosition);
			return expRet;
		}
		}


		expRet.forceInt(0);
		return expRet;


	}
}