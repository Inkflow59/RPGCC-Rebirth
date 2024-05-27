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

import android.Manifest.permission;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.input.InputManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Actions.CActExtension;
import Application.CRunApp;
import Application.CRunApp.MenuEntry;
import Conditions.CCndExtension;
import Expressions.CValue;
import OpenGL.GLRenderer;
import RunLoop.CCreateObjectInfo;
import Runtime.MMFRuntime;
import Runtime.PermissionsResultAction;
import Runtime.SurfaceView;
import Services.CBinaryFile;
import Services.CFile;
import Services.CServices;


public class CRunAndroid extends CRunExtension
{
	String deviceID = "";
	String logTag;

	String google_email = "";

	Intent intentOut;

	private KeyguardManager keyguardManager = null;
	@SuppressWarnings("deprecation")
	private KeyguardManager.KeyguardLock lock = null;

	Map<String, BroadcastReceiver> intentsIn;
	Intent intentIn;

	private boolean enabled_read;
	private boolean enabled_account;
	private boolean expression_request;

	private boolean IsControllerGamepad;
	private boolean IsControllerDPAD;
	private boolean IsControllerJoystick;
	private int controllerQty = 0;
	public int lastKeyPressed;

	private int lastId;

	private InputManager inputManager;
	private InputManager.InputDeviceListener inputDevListener = new InputManager.InputDeviceListener() {
		@Override
		public void onInputDeviceAdded(int id) {
			CheckForInputDevices(id, 1);
		}

		@Override
		public void onInputDeviceRemoved(int id) {
			CheckForInputDevices(id, -1);
		}

		@Override
		public void onInputDeviceChanged(int id) {
			CheckForInputDevices(id, 0);
		}
	};

	public TelephonyManager getTelephonyManager()
	{
		return (TelephonyManager) MMFRuntime.inst.getSystemService(Context.TELEPHONY_SERVICE);
	}

	public ConnectivityManager getConnectivityManager()
	{
		return (ConnectivityManager) MMFRuntime.inst.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public Vibrator getVibrator ()
	{
		return (Vibrator) MMFRuntime.inst.getSystemService(Context.VIBRATOR_SERVICE);
	}

	@SuppressLint("MissingPermission")
	public NetworkInfo getActiveNetworkInfo()
	{
		return getConnectivityManager().getActiveNetworkInfo();
	}

	private CValue expRet;

	public CRunAndroid()
	{
		expRet = new CValue(0);
	}
	@Override
	public int getNumberOfConditions()
	{
		return 34;
	}

	@Override
	public boolean createRunObject(CBinaryFile file, CCreateObjectInfo cob, int version)
	{
		//attrs = MMFRuntime.inst.getWindow().getAttributes();

		intentOut = new Intent();
		intentsIn = new HashMap<String, BroadcastReceiver>();

		logTag = ho.getApplication().appName;

		ho.getApplication().androidObjects.add (this);

		enabled_read = false;
		enabled_account = false;

		// do something
		if(Build.VERSION.SDK_INT > 22) {
			enabled_read = MMFRuntime.inst.hasPermissionGranted(permission.READ_PHONE_STATE);
		}
		else if (MMFRuntime.inst.hasManifestPermission(permission.READ_PHONE_STATE))
			enabled_read = true;

		// do something
		if(Build.VERSION.SDK_INT > 22) {
			enabled_account = MMFRuntime.inst.hasPermissionGranted(permission.GET_ACCOUNTS);
		}
		else if (MMFRuntime.inst.hasManifestPermission(permission.GET_ACCOUNTS))
			enabled_account = true;

		IsControllerGamepad = false;
		IsControllerJoystick = false;
		IsControllerDPAD = false;

		inputManager = (InputManager) MMFRuntime.inst.getSystemService( Context.INPUT_SERVICE );
		inputManager.registerInputDeviceListener(inputDevListener, null);


		google_email = "";

		deviceID = "";
		lastId=-1;

		return true;
	}

	@Override
	public void destroyRunObject(boolean bFast)
	{
		if(MMFRuntime.inst.keyBoardOn)
			MMFRuntime.inst.HideKeyboard(null, true);

		ho.getApplication().androidObjects.remove (this);

		for(BroadcastReceiver receiver : intentsIn.values())
			MMFRuntime.inst.unregisterReceiver (receiver);

		intentsIn.clear();

		if(inputManager != null)
			inputManager.unregisterInputDeviceListener(inputDevListener);
	}

	@Override
	public int handleRunObject() {
		List<String> perms = new ArrayList<String>();
		if (MMFRuntime.inst.hasManifestPermission(permission.READ_PHONE_STATE))
			perms.add(permission.READ_PHONE_STATE);
		if (MMFRuntime.inst.hasManifestPermission(permission.GET_ACCOUNTS))
			perms.add(permission.GET_ACCOUNTS);

		String[] permissions = new String[perms.size()];
		perms.toArray(permissions);

		MMFRuntime.inst.askForPermission(
				permissions,
				new PermissionsResultAction() {

					@Override
					public void onGranted() {
						if (MMFRuntime.inst.hasManifestPermission(permission.READ_PHONE_STATE))
							getPhoneID();
						if (MMFRuntime.inst.hasManifestPermission(permission.GET_ACCOUNTS))
							getAccountEmail();
					}

					@Override
					public void onDenied(String permission) {
						Log.d(logTag, "Phone Status permission non granted...");
					}
				});
		
		CheckForInputDevices(-1, 0);
		return REFLAG_ONESHOT;
	}

	@Override
	public void onNewIntent(Intent intent)
	{
		if(intent != null)
		{
			intentIn = intent;

			intentEvent = ho.getEventCount();
			ho.generateEvent(18, 0);

			anyIntentEvent = ho.getEventCount();
			ho.generateEvent(19, 0);
		}
	}

	private void getPhoneID()
	{
		enabled_read = true;
		deviceID = getThisPhoneId();
	}

	private void getAccountEmail()
	{
		enabled_account = true;
		google_email = getEmail();
	}

	@Override
	public void continueRunObject() {

	}


	public int menuButtonEvent = -1;
	public int backButtonEvent = -1;
	public int intentEvent = -1;
	public int anyIntentEvent = -1;

	public int menuItemEvent = -1;
	public int anyMenuItemEvent = -1;
	public String menuItem;

	public int asyncLoadCompleteEvent = -1;
	public int asyncLoadFailedEvent = -1;

	@SuppressLint("MissingPermission")
	@Override
	public boolean condition(int num, CCndExtension cnd)
	{
		switch (num)
		{
			case 0: /* Device has a GPU? */
				return GLRenderer.inst.gpuVendor.compareTo("Android") != 0;

			case 1: /* User is roaming? */
			{
				TelephonyManager tm = getTelephonyManager();

				if(tm == null)
					return false;

				return tm.isNetworkRoaming();
			}

			case 2: /* On extension exception */
				return false;

			case 3: /* Network is connected? */

				try
				{
					return getActiveNetworkInfo().isConnected();
				}
				catch (Throwable t)
				{
					return false;
				}
				//return isOnWIFI();

			case 4: /* Device is plugged in? */

				if(!MMFRuntime.inst.batteryReceived)
					return false;

				return MMFRuntime.inst.batteryPlugged == BatteryManager.BATTERY_PLUGGED_AC ||
						MMFRuntime.inst.batteryPlugged == BatteryManager.BATTERY_PLUGGED_USB;

			case 5: /* Device is plugged in to an AC adapter? */

				if(!MMFRuntime.inst.batteryReceived)
					return false;

				return MMFRuntime.inst.batteryPlugged == BatteryManager.BATTERY_PLUGGED_AC;

			case 6: /* Device is plugged in to a USB port? */

				if(!MMFRuntime.inst.batteryReceived)
					return false;

				return MMFRuntime.inst.batteryPlugged == BatteryManager.BATTERY_PLUGGED_USB;

			case 7: /* Battery is charging? */

				if(!MMFRuntime.inst.batteryReceived)
					return false;

				return MMFRuntime.inst.batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING;

			case 8: /* Battery is discharging? */

				if(!MMFRuntime.inst.batteryReceived)
					return false;

				return MMFRuntime.inst.batteryStatus == BatteryManager.BATTERY_STATUS_DISCHARGING;

			case 9: /* Battery is full? */

				if(!MMFRuntime.inst.batteryReceived)
					return false;

				return MMFRuntime.inst.batteryStatus == BatteryManager.BATTERY_STATUS_FULL;

			case 10: /* On back button pressed */

				return ho.getEventCount() == backButtonEvent;

			case 11: /* On home button pressed */

				return false;

			case 12: /* On menu button pressed */

				return (ho.getEventCount() == menuButtonEvent);

			case 13: /* Device is rooted? */

				return MMFRuntime.rooted;

			case 14: /* Bluetooth enabled? */
			{
				BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

				if (adapter == null || !adapter.isEnabled ())
					return false;

				return true;
			}

			case 15: /* Back button down? */

				return rh.rhApp.keyBuffer[KeyEvent.KEYCODE_BACK] != 0;

			case 16: /* Menu button down? */

				return rh.rhApp.keyBuffer[KeyEvent.KEYCODE_MENU] != 0;

			case 17: /* Button %0 down? */

				int keyCode = cnd.getParamExpression(rh, 0);

				if (keyCode < 0 || keyCode >= rh.rhApp.keyBuffer.length)
					return false;

				return rh.rhApp.keyBuffer[keyCode] != 0;

			case 18: /* On incoming intent with action %0 */

				return intentIn != null && intentIn.getAction() != null && intentIn.getAction().contentEquals(cnd.getParamExpString(rh, 0))
						&& ho.getEventCount() == intentEvent;

			case 19: /* On any incoming intent */

				return intentIn != null && ho.getEventCount() == anyIntentEvent;

			case 20: /* On options menu item selected */

				return ho.getEventCount() == menuItemEvent &&
						menuItem.compareToIgnoreCase(cnd.getParamExpString(rh, 0)) == 0;

			case 21: /* On any options menu item selected */

				return ho.getEventCount() == anyMenuItemEvent;

			case 22: /* On Visible Keyboard */

				return true;

			case 23: /* On Invisible Keyboard */

				return true;

			case 24: /* Is Keyboard Visible? */

				return MMFRuntime.inst.keyBoardOn;

			case 25: /* Is Permission Granted */
			{
				String p0 = cnd.getParamExpString(rh, 0);
				return MMFRuntime.inst.hasPermissionGranted(p0);
			}

			case 26: /* Is Controller with gamepad? */

				return IsControllerGamepad;

			case 27: /* Is controller with joystick? */

				return IsControllerJoystick;

			case 28: /* Is controller with DPAD? */
				return IsControllerDPAD;

			case 29: /* Is Permission denied? */
				String p1 = cnd.getParamExpString(rh, 0);
				return MMFRuntime.inst.hasPermissionDenied(p1);

			case 30: /* Is Permission blocked */
				String p2 = cnd.getParamExpString(rh, 0);
				return MMFRuntime.inst.hasPermissionBlocked(p2);

			case 31: /* Is Storage Manager Permission granted */
				return MMFRuntime.inst.hasManagerStoragePermission();

			case 32: /* Is Last permission requested granted */
			case 33: /* Is Last permission requested denied */
			{
				int Id = cnd.getParamExpression(rh, 0);
				if(Id == lastId)
					return true;

				return false;
			}

		}

		return false;
	}

	private static boolean isOnWIFI() {

		ConnectivityManager cm = (ConnectivityManager) MMFRuntime.inst.getSystemService(Context.CONNECTIVITY_SERVICE);

		if(cm != null){

			if(Build.VERSION.SDK_INT < 28)
			{
				@SuppressLint("MissingPermission")
				NetworkInfo networkInfo = cm.getActiveNetworkInfo();
				return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
			}
			else
			{
				@SuppressLint("MissingPermission")
				NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
				if (capabilities != null)
					return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
			}

		}
		//cm is null
		return false;

	}

	private void galleryAddPic(String file) {
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(file);
		Uri contentUri;
		if(Build.VERSION.SDK_INT < 24)
			contentUri = Uri.fromFile (f);
		else
		{
			Context context = MMFRuntime.inst.getApplicationContext();
			contentUri = FileProvider.getUriForFile(context,
					context.getApplicationContext().getPackageName() + ".provider",
					f);
		}
		mediaScanIntent.setData(contentUri);
		mediaScanIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		MMFRuntime.inst.sendBroadcast(mediaScanIntent);	}

	@SuppressLint({"NewApi", "MissingPermission"})
	@SuppressWarnings("deprecation")
	@Override
	public void action(int num, CActExtension act)
	{

		switch (num)
		{
			/* Log actions */

			case 0:
				Log.d(logTag, act.getParamExpString(rh, 0));
				break;
			case 1:
				Log.e(logTag, act.getParamExpString(rh, 0));
				break;
			case 2:
				Log.i(logTag, act.getParamExpString(rh, 0));
				break;
			case 3:
				Log.v(logTag, act.getParamExpString(rh, 0));
				break;
			case 4:
				Log.w(logTag, act.getParamExpString(rh, 0));
				break;

			case 5: /* Set log tag */
				logTag = act.getParamExpString(rh, 0);
				break;

			case 6: /* Start sleep prevention */

				MMFRuntime.inst.runOnUiThread (new Runnable ()
				{   @Override
				public void run ()
				{   MMFRuntime.inst.getWindow().addFlags
						(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
				});

				break;

			case 7: /* Stop sleep prevention */

				MMFRuntime.inst.runOnUiThread (new Runnable ()
				{   @Override
				public void run ()
				{   MMFRuntime.inst.getWindow().clearFlags
						(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
				});

				break;

			case 8: /* Hide status bar */
				MMFRuntime.inst.ToggleStatusBar(false);
				break;

			case 9: /* Show status bar */
				MMFRuntime.inst.ToggleStatusBar(true);
				break;

			case 10: /* Open URL */
			{
				try
				{
					String url = act.getParamExpString(rh, 0);

					if(url.indexOf("://") == -1)
						url = "http://" + url;

					Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
					MMFRuntime.inst.startActivity(intent);
				}
				catch (Throwable e)
				{
				}

				break;
			}

			case 11: /* Start intent */
			{
				try
				{
					String action = act.getParamExpString(rh, 0);
					if(action.length() > 0)
						intentOut.setAction(action);

					String data = act.getParamExpString(rh, 1);
					if(data.length() > 0)
						intentOut.setData(Uri.parse (data));

					MMFRuntime.inst.startActivity(intentOut);

				}
				catch (Throwable e)
				{
					Log.e("MMFRuntime", "Error starting intent: " + e.toString());
				}

				intentOut = null;
				intentOut = new Intent();

				break;
			}

			case 12: /* Vibrate */

				Vibrator vi = getVibrator();
				if(vi == null || !MMFRuntime.inst.hasManifestPermission(permission.VIBRATE))
					return;

				vi.vibrate (act.getParamExpression (rh, 0));

				break;

			case 13: /* Show keyboard */

				MMFRuntime.inst.HideKeyboard(null, false);
				break;

			case 14: /* Hide keyboard */

				MMFRuntime.inst.HideKeyboard(null, true);
				break;

			case 15: /* Add intent category */

				intentOut.addCategory (act.getParamExpString(rh, 0));
				break;

			case 16: /* Add intent data (string) */

				intentOut.putExtra (act.getParamExpString(rh, 0), act.getParamExpString(rh, 1));
				break;

			case 17: /* Add intent data (boolean) */

				intentOut.putExtra(act.getParamExpString(rh, 0), act.getParamExpression(rh, 1) != 0);
				break;

			case 18: /* Add intent data (long) */

				intentOut.putExtra(act.getParamExpString(rh, 0), (long) act.getParamExpression(rh, 1));
				break;

			case 19: /* Subscribe to action */

				try
				{
					String action = act.getParamExpString(rh,  0);
					if (intentsIn.get(action) != null)
						break;

					BroadcastReceiver receiver = new BroadcastReceiver()
					{
						@Override
						public void onReceive(Context context, Intent intent)
						{
							intentIn = intent;

							intentEvent = ho.getEventCount();
							ho.generateEvent(18, 0);

							anyIntentEvent = ho.getEventCount();
							ho.generateEvent(19, 0);

							intentIn = null;
						}
					};

					MMFRuntime.inst.registerReceiver
							(receiver, new IntentFilter(action));

					intentsIn.put(action, receiver);
				}
				catch(Exception e)
				{
				}

				break;

			case 20: /* Unsubscribe from action */

				BroadcastReceiver removed = intentsIn.remove(act.getParamExpString(rh, 0));

				if (removed != null)
					MMFRuntime.inst.unregisterReceiver(removed);

				break;

			case 21: /* Start intent with chooser */
			{
				try
				{
					String action = act.getParamExpString(rh, 0);
					if(action.length() > 0)
						intentOut.setAction(action);

					String data = act.getParamExpString(rh, 1);
					if(data.length() > 0)
						intentOut.setData(Uri.parse (data));

					MMFRuntime.inst.startActivity(Intent.createChooser (intentOut, act.getParamExpString(rh, 2)));

				}
				catch (Throwable e)
				{
					Log.e("MMFRuntime", "Error starting intent: " + e.toString());
				}

				intentOut = null;
				intentOut = new Intent();

				break;
			}

			case 22: /* Enable options menu item */
			{
				String id = act.getParamExpString(rh, 0);

				if(rh.rhApp.androidMenu == null)
					break;

				for(MenuEntry item : rh.rhApp.androidMenu)
				{
					if(item.id.equalsIgnoreCase(id))
					{
						item.disabled = false;
						break;
					}
				}

				MMFRuntime.inst.invalidateOptionsMenu();
				break;
			}

			case 23: /* Disable options menu item */
			{
				String id = act.getParamExpString(rh, 0);

				if(rh.rhApp.androidMenu == null)
					break;

				for(MenuEntry item : rh.rhApp.androidMenu)
				{
					if(item.id.equalsIgnoreCase(id))
					{
						item.disabled = true;
						break;
					}
				}
				MMFRuntime.inst.invalidateOptionsMenu();
				break;
			}

			case 24: /* Show action bar */
				MMFRuntime.inst.runOnUiThread(new Runnable () {

					@Override
					public void run() {
						synchronized(MMFRuntime.inst) {
							MMFRuntime.inst.toggleActionBar(true);
						}
					}

				});

				break;

			case 25: /* Hide Action bar */

				MMFRuntime.inst.runOnUiThread(new Runnable () {

					@Override
					public void run() {
						synchronized(MMFRuntime.inst) {
							MMFRuntime.inst.toggleActionBar(false);
						}
					}

				});
				break;

			case 26: /* Force Landscape layout */

				MMFRuntime.inst.runOnUiThread(new Runnable () {

					@SuppressLint("SourceLockedOrientationActivity")
					@Override
					public void run() {
						synchronized(MMFRuntime.inst) {
							MMFRuntime.isShortOut = true;
							MMFRuntime.inst.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
						}
					}

				});
				break;

			case 27: /* Force portrait layout */


				MMFRuntime.inst.runOnUiThread(new Runnable () {

					@SuppressLint("SourceLockedOrientationActivity")
					@Override
					public void run() {
						synchronized(MMFRuntime.inst) {
							MMFRuntime.isShortOut = true;
							MMFRuntime.inst.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
						}
					}

				});
				break;


			case 28: /* Save Screenshot to %0*/
				String filename = act.getParamExpString(rh, 0);
				if(filename.length() > 0) {
					if(CServices.allowWriteFileMode(filename))
						CaptureFromScreen(filename, 0, 0, -1, -1);
					else {
						MMFRuntime.inst.askForPermission(
								new String[]{permission.READ_EXTERNAL_STORAGE,
										permission.WRITE_EXTERNAL_STORAGE},
								new PermissionsResultAction() {

									@Override
									public void onGranted() {
										CaptureFromScreen(filename, 0, 0, -1, -1);
									}

									@Override
									public void onDenied(String permission) {
										Log.d("MMFRuntime","Storage permissions non granted...");
									}
								});
					}

				}
				break;


			case 29: /* Open Menu via action */
				MMFRuntime.inst.runOnUiThread(new Runnable () {

					@Override
					public void run() {
						if (Build.VERSION.SDK_INT >= 11)
							MMFRuntime.inst.invalidateOptionsMenu();
						SystemClock.sleep(100);
						MMFRuntime.inst.openOptionsMenu();
					}

				});

				break;

			case 30: /* Disable hardware keys for old devices api 11 and below */

				if(!MMFRuntime.inst.hasManifestPermission(permission.DISABLE_KEYGUARD))
					break;

				if(keyguardManager == null) {
					keyguardManager = (KeyguardManager) MMFRuntime.inst.getSystemService(Context.KEYGUARD_SERVICE);
					if(lock == null)
						lock = keyguardManager.newKeyguardLock("ALARM_RECEIVE");
				}

				if(lock != null)
					lock.disableKeyguard();

				break;

			case 31: /* Enable hardware keys for old devices api 11 and below */

				if(!MMFRuntime.inst.hasManifestPermission(permission.DISABLE_KEYGUARD))
					break;

				if(keyguardManager == null) {
					keyguardManager = (KeyguardManager) MMFRuntime.inst.getSystemService(Context.KEYGUARD_SERVICE);
					if(lock == null)
						lock = keyguardManager.newKeyguardLock("ALARM_RECEIVE");
				}

				if(lock != null)
					lock.reenableKeyguard();

				break;

			case 32: /* Send Application to back */
				MMFRuntime.inst.runOnUiThread(new Runnable () {

					@Override
					public void run() {
						synchronized(MMFRuntime.inst) {
							MMFRuntime.inst.sendBack();
						}
					}

				});
				break;


			//  256 View.SYSTEM_UI_FLAG_LAYOUT_STABLE			api 16
			//  512 View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION	api 16
			// 1024 View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN		api 16
			//	  2 View.SYSTEM_UI_FLAG_HIDE_NAVIGATION			api 14
			//	  1 View.SYSTEM_UI_FLAG_LOW_PROFILE				api 14
			//    4 View.SYSTEM_UI_FLAG_FULLSCREEN				api 16
			// 4096 View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY		api 19
			// 2048 View.SYSTEM_UI_FLAG_IMMERSIVE				api 19

			case 33: /* Show Navigation bar */
				MMFRuntime.inst.hideBar=false;
				MMFRuntime.inst.toggleNavigationBar(true, 1);
				break;

			case 34: /* Dim Navigation bar */
				MMFRuntime.inst.hideBar=false;
				MMFRuntime.inst.toggleNavigationBar(false, 1);
				break;

			case 35: /* Hide Navigation Bar */
				MMFRuntime.inst.hideBar=true;
				MMFRuntime.inst.toggleNavigationBar(false, View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
				break;


			case 36: /* Add intent setType */

				intentOut.setType(act.getParamExpString(rh, 0));
				break;

			case 37: /* Disable AutoEnd */

				MMFRuntime.inst.app.hdr2Options &= ~ CRunApp.AH2OPT_AUTOEND;
				break;

			case 38: /* Enable AutoEnd */

				MMFRuntime.inst.app.hdr2Options |= CRunApp.AH2OPT_AUTOEND;
				break;

			case 39: /* Set Character Set */

				if(act.getParamExpression(rh, 0) > 0)
					MMFRuntime.inst.charSet = CFile.charset ;
				else
					MMFRuntime.inst.charSet = CFile.charsetU8 ;

				break;

			case 40: /* Add intent data (Uri) */

				intentOut.putExtra (act.getParamExpString(rh, 0), Uri.parse(act.getParamExpString(rh, 1)));
				break;

			case 41: /* Capture portion of screen at x,y with size w and h */
			{
				String filename2 = act.getParamExpString(rh, 0);
				int x = act.getParamExpression(rh, 1);
				int y = act.getParamExpression(rh, 2);
				int w = act.getParamExpression(rh, 3);
				int h = act.getParamExpression(rh, 4);

				if(w+x > ho.hoAdRunHeader.rhLevelSx || w < 0 || x < 0)
					return;
				if(h+y > ho.hoAdRunHeader.rhLevelSy || h < 0 || y < 0)
					return;

				if(filename2.length() > 0) {
					if(CServices.allowWriteFileMode(filename2))
						CaptureFromScreen(filename2, x, y, w, h);
					else {
						MMFRuntime.inst.askForPermission(
								new String[]{permission.READ_EXTERNAL_STORAGE,
										permission.WRITE_EXTERNAL_STORAGE},
								new PermissionsResultAction() {

									@Override
									public void onGranted() {
										CaptureFromScreen(filename2, x, y, w, h);
									}

									@Override
									public void onDenied(String permission) {
										Log.d("MMFRuntime","Storage permissions non granted...");
									}
								});
					}

				}


				break;
			}
			case 42: /* Check for InputDevices */

				IsControllerGamepad = false;
				IsControllerJoystick = false;
				IsControllerDPAD = false;

				CheckForInputDevices(-1, 0);
				break;
			case 43: /* Disable menu behavior */

				MMFRuntime.inst.disableMenuBehavior = true;
				break;
			case 44: /* Enable menu behavior */

				MMFRuntime.inst.disableMenuBehavior = false;
				break;
			case 45:
			{
				final int var = act.getParamExpression(rh, 0);
				if (Build.VERSION.SDK_INT >= 19) {
					MMFRuntime.inst.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							synchronized (MMFRuntime.inst) {
								if (var != 0)
									MMFRuntime.inst.hideSystemUI(0);
								else
									MMFRuntime.inst.showSystemUI();
							}
						}

					});
				}
				break;
			}
			case 46:
			{
				MMFRuntime.inst.getManagerStoragePermission();
				break;
			}
			case 47:
			{
				String permission = act.getParamExpString(rh, 0);
				int id = act.getParamExpression(rh, 1);
				final String[] permissions = permission.split("\\|");
				if(permissions.length > 0 && MMFRuntime.inst.hasManifestPermissions(permissions)) {
					final int askedId = id;
					MMFRuntime.inst.askForPermission(
							permissions,
							new PermissionsResultAction() {
								@Override
								public void onGranted() {
									MMFRuntime.inst.runOnUiThread(new Runnable () {
										@Override
										public void run() {
											lastId = askedId;
											ho.pushEvent(32,0);
										}
									});
								}

								@Override
								public void onDenied(String permission) {
									MMFRuntime.inst.runOnUiThread(new Runnable () {
										@Override
										public void run() {
											lastId = askedId;
											ho.pushEvent(33,0);
										}
									});
								}
							});
				}
				break;
			}
			case 48: /* Load image async */

			/*String url = act.getParamExpString(rh, 0);
            	CObject object = act.getParamObject(rh, 1);

            	if(! (object instanceof CExtension))
            	    return;

            	if (((CExtension) object).ext instanceof CRunkcpica)
                {
                    final CRunkcpica ext = (CRunkcpica) ((CExtension) object).ext;

                    ho.retrieveHFile(url, new CRunApp.FileRetrievedHandler()
                    {
                        @Override
                        public void onRetrieved(CRunApp.HFile file, java.io.InputStream stream)
                        {
                            try
                            {
                                Log.Log("Android object: Image retrieved, " + stream.available() + " bytes available");
                            }
                            catch(IOException e)
                            {
                            }

                            ext.load(stream);

                            asyncLoadCompleteEvent = ho.getEventCount();
                            ho.generateEvent(25, 0);
                        }

                        @Override
                        public void onFailure()
                        {
                            Log.Log("Android object: Failure w/ async image download");

                            asyncLoadFailedEvent = ho.getEventCount();
                            ho.generateEvent(26, 0);
                        }
                    });
                }                   */

				break;
		}
	}





	private static String getManufacturerSerialNumber() {
		String serial = "";
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class, String.class);
			serial = (String) get.invoke(c, "ril.serialnumber", "unknown");
		} catch (Exception ignored) {}

		return serial;
	}


	private String getEmail() {
		String email = null;
		try {
			AccountManager accountManager = AccountManager.get(MMFRuntime.inst.getApplicationContext());
			Account account = getAccount(accountManager);

			if (account != null)
				email = account.name;
		} catch (Exception e)
		{

		}
		return email == null ? "" : email;
	}

	private Account getAccount(AccountManager accountManager) throws SecurityException {
		Account account = null;

		if (enabled_account)
		{

			Log.d("AndroidRuntime", "permission granted for accounts");
			@SuppressLint("MissingPermission") Account[] accounts = accountManager.getAccountsByType("com.google");
			if (accounts.length > 0)
			{
				account = accounts[0];
			}
			else
			{
				account = null;
			}
		}
		return account;
	}

	@Override
	public CValue expression(int num)
	{
		String key;

		switch (num)
		{
			case 0: /* GPU_Name$ */
				expRet.forceString(GLRenderer.inst.gpu);
				return expRet;
			case 1: /* GPU_Vendor$ */
				expRet.forceString(GLRenderer.inst.gpuVendor);
				return expRet;
			case 2: /* DeviceID$ */
			{
				expRet.forceString(deviceID);
				return expRet;
			}

			case 3: /* Operator$ */
			{
				TelephonyManager tm = getTelephonyManager();

				if(tm == null)
					expRet.forceString("");
				else
					expRet.forceString(tm.getNetworkOperatorName().trim());

				return expRet;
			}

			case 4: /* StackTrace$ */
				expRet.forceString("");
				return expRet;
			case 5: /* AppTitle$() */
				expRet.forceString(ho.getApplication().appName);
				return expRet;
			case 6: /* BatteryPercentage() */

				if(!MMFRuntime.inst.batteryReceived)
					expRet.forceInt(-1);
				else
					expRet.forceDouble(100 * ((float) MMFRuntime.inst.batteryLevel / (float) MMFRuntime.inst.batteryScale));

				return expRet;
			case 7:
				expRet.forceInt(ho.getApplication().gaCxWin);
			return expRet;
			case 8:
				expRet.forceInt(ho.getApplication().gaCyWin);
			return expRet;
			case 9:
				expRet.forceString (MMFRuntime.inst.getFilesDir ().toString ());
				return expRet;
			case 10:
				expRet.forceString (rh.getTempPath ());
				return expRet;
			case 11:
				expRet.forceString (CServices.getAndroidID ());
				return expRet;
			case 12:
				expRet.forceString (MMFRuntime.inst.getClass().getName());
				return expRet;
			case 13:
				expRet.forceString (Environment.getExternalStorageDirectory().toString());
				return expRet;
			case 14:
				expRet.forceString (MMFRuntime.appVersion);
				return expRet;
			case 15:
				expRet.forceString (MMFRuntime.version);
				return expRet;
			case 16:
				//if (MMFRuntime.inst.adView == null)
				expRet.forceInt (0);
				return expRet;
			//expRet.forceString (MMFRuntime.inst.adView.getHeight ());
			case 17:
				expRet.forceString (GLRenderer.inst.glVersion);
				return expRet;
			case 18: /* IntentAction$ */

				if (intentIn == null || intentIn.getAction() == null)
					expRet.forceString("");
				else
					expRet.forceString(intentIn.getAction());

				return expRet;
			case 19: /* IntentData$ */

				if (intentIn == null || intentIn.getDataString() == null)
					expRet.forceString("");
				else
					expRet.forceString(intentIn.getDataString());

				return expRet;
			case 20: /* IntentExtra_String$ */

				key = ho.getExpParam().getString();

				String extra =null;
				if (intentIn != null)
					extra = intentIn.getStringExtra(key);

				expRet.forceString(extra == null ? "" : extra);
				return expRet;
			case 21: /* IntentExtra_Boolean */

				key = ho.getExpParam().getString();

				if (intentIn == null)
					expRet.forceString("");
				else
				expRet.forceInt(intentIn.getBooleanExtra(key, false) ? 1 : 0);

				return expRet;
			case 22: /* IntentExtra_Long */

				key = ho.getExpParam().getString();

				if (intentIn == null)
					expRet.forceInt(0);
				else
					expRet.forceInt(intentIn.getIntExtra(key, 0));

				return expRet;
			case 23: /* MenuItem$ */
				expRet.forceString(menuItem);
				return expRet;
			case 24: /* Android Api version */
				expRet.forceInt(Build.VERSION.SDK_INT);
				return expRet;
			case 25: /* Manufacturer$ */
				expRet.forceString(Build.MANUFACTURER);
				return expRet;
			case 26: /* Model$ */
				expRet.forceString(Build.MODEL);
				return expRet;
			case 27: /* Product$ */
				expRet.forceString(Build.PRODUCT);
				return expRet;
			case 28: /* PublicStorageDirectory$ */
				// Pictures, Movies, DCIM, Documents, Videos, Downloads, Music
				String type = ho.getExpParam().getString();
				String Directory;
				if(type.equalsIgnoreCase("pictures"))
					Directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
				else if (type.equalsIgnoreCase("movies"))
					Directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
				else if (type.equalsIgnoreCase("dcim"))
					Directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
				else if (type.equalsIgnoreCase("documents"))
					Directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
				else if (type.equalsIgnoreCase("videos"))
					Directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
				else if (CServices.containsIgnoreCase(type, "download"))
					Directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
				else if (type.equalsIgnoreCase("music"))
					Directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
				else
					Directory = "";
				expRet.forceString (Directory);
				return expRet;
			case 29: /* DeviceSerialNumber$ */
				expRet.forceString(getManufacturerSerialNumber());
				return expRet;
			case 30: /* AccountEMail$ */
				expRet.forceString(google_email);
				return expRet;
			case 31: /* SecondaryExternalStorage$ */
				String dir = System.getenv("SECONDARY_STORAGE"); //Environment.getExternalStorageDirectory ().getAbsolutePath()
				if ((null == dir) || (dir.length() == 0)) {
					dir = System.getenv("EXTERNAL_SDCARD_STORAGE"); //only work in Samsung devices
					if(null == dir)
						dir = "";
				}
				expRet.forceString(dir);
				return expRet;
			case 32: /* LastButtonPressed */
				expRet.forceInt(lastKeyPressed);
				return expRet;
			case 33: /* PublicScopedStorageDirectory$ */
			{
				// Pictures, Movies, DCIM, Documents, Videos, Downloads, Music
				String typeQ = ho.getExpParam().getString();
				String DirectoryQ;
				try {
					if (typeQ.equalsIgnoreCase("pictures"))
						DirectoryQ = MMFRuntime.inst.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
					else if (typeQ.equalsIgnoreCase("movies"))
						DirectoryQ = MMFRuntime.inst.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();
					else if (typeQ.equalsIgnoreCase("dcim"))
						DirectoryQ = MMFRuntime.inst.getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath();
					else if (typeQ.equalsIgnoreCase("documents"))
						DirectoryQ = MMFRuntime.inst.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
					else if (typeQ.equalsIgnoreCase("videos"))
						DirectoryQ = MMFRuntime.inst.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();
					else if (CServices.containsIgnoreCase(typeQ, "download"))
						DirectoryQ = MMFRuntime.inst.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
					else if (typeQ.equalsIgnoreCase("music"))
						DirectoryQ = MMFRuntime.inst.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
					else
						DirectoryQ = MMFRuntime.inst.getExternalFilesDir(null).getAbsolutePath();
				}
				catch(Exception e)
				{
					DirectoryQ = "";
				}
				expRet.forceString(DirectoryQ);
				return expRet;
			}
			case 34: /* Get Device Display Frame Rate */
			{
				double dfps = ((WindowManager) MMFRuntime.inst
						.getSystemService(Context.WINDOW_SERVICE))
						.getDefaultDisplay()
						.getRefreshRate();
				expRet.forceDouble(dfps);
				return expRet;
			}
		}
		expRet.forceInt(0);
		return expRet;
	}

	private void CaptureFromScreen(String filename, int x, int y, int w, int h) {

		int typeImg = 0;
		int vx = MMFRuntime.inst.viewportX + x;
		int vy = MMFRuntime.inst.viewportY + y;
		Paint paint = new Paint();
		paint.setFilterBitmap(true);

		//  Got Control Layer
		View mView = MMFRuntime.inst.mainView;
		mView.setDrawingCacheEnabled(true);
		Bitmap bmp2 = Bitmap.createBitmap(mView.getDrawingCache());
		mView.setDrawingCacheEnabled(false);

		if(w == -1) {
			w = (int) ((MMFRuntime.inst.currentWidth - 2 * MMFRuntime.inst.viewportX) / MMFRuntime.inst.scaleX);
		}
		if(h == -1) {
			h = (int) ((MMFRuntime.inst.currentHeight - 2 * MMFRuntime.inst.viewportY) / MMFRuntime.inst.scaleY);
		}

		// Got Surface View
		Bitmap bmp1 = SurfaceView.inst.drawBitmap(x,y,w,h);

		// Create Overlay Bitmap
		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());

		Canvas canvas = new Canvas(bmOverlay);

		canvas.drawBitmap(bmp1, 0, 0, paint);
		canvas.drawBitmap(bmp2, -vx, -vy, paint);

		String filenameArray[] = filename.split("\\.");
		String extension = filenameArray[filenameArray.length-1];

		if(extension == null)
			filename += ".jpg";
		else if(extension.toLowerCase(Locale.US).contains("png"))
			typeImg = 1;

		File mPath = new File(filename);
		FileOutputStream fileos = null;

		try {
			CServices.saveImage(MMFRuntime.inst, bmOverlay, filename);
			galleryAddPic(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}

		bmp1.recycle();
		bmp2.recycle();

		bmOverlay.recycle();
	}

	@SuppressLint("MissingPermission")
	private String getThisPhoneId()
	{
		String id = null;
		try {
			TelephonyManager tm = getTelephonyManager();
			if(enabled_read && tm != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					id = tm.getImei();
				} else {
					id = tm.getDeviceId();
				}
			}
		} catch (Exception e)
		{
			Log.e("MMFRuntime", "Error: " + e.toString());
		}

		return (id == null ? "" : id);
	}

	private void CheckForInputDevices(int id, int mode) {

		int nGamepad = 0;
		int nJoystick = 0;
		int nDPAD = 0;

		int[] deviceIds = InputDevice.getDeviceIds();

		for (int deviceId : deviceIds) {
			InputDevice dev = InputDevice.getDevice(deviceId);
			if(dev == null || dev.isVirtual())
				continue;

			String controllerName = dev.getName().toLowerCase(Locale.ENGLISH);
			Log.v("MMFRuntime", "name: " + controllerName+" hasSources: "+Integer.toHexString(dev.getSources()));
			if (controllerName.contains("roller") || // Cover some misspelled controller name
					controllerName.contains("pad") ||	 // covers Gamepad, pad and dpad
					controllerName.contains("joystick") ||
					controllerName.contains("virtual") ||
					controllerName.contains("razer") ||
					controllerName.contains("nintendo")) {

				int sources = dev.getSources();

				// Verify that the device has gamepad buttons, control sticks, or both.
				if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
					nGamepad++;

				if ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
					nJoystick++;

				if ((sources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD)
					nDPAD++;
			}

		}
		IsControllerGamepad = (nGamepad > 0 ? true : false);
		IsControllerJoystick= (nJoystick > 0 ? true : false);
		IsControllerDPAD = (nDPAD > 0 ? true : false);
		if(!(MMFRuntime.FIRETV || MMFRuntime.OUYA) && !MMFRuntime.NEXUSTV &&
				(IsControllerJoystick || IsControllerJoystick || IsControllerDPAD )) {
			MMFRuntime.NEXUSTV = true;
			CRunNEXUSTV.init(MMFRuntime.inst);
		}

	}

}
