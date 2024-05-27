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
package Runtime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.google.android.play.core.assetpacks.AssetPackLocation;
import com.google.android.play.core.assetpacks.AssetPackManager;
import com.google.android.play.core.assetpacks.AssetPackManagerFactory;
import com.google.android.play.core.assetpacks.AssetPackState;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import Application.CRunApp;
import Application.CRunApp.MenuEntry;
import Application.CRunTimerTask;
import Extensions.CRunAndroid;
import Objects.CExtension;
import Objects.CObject;
import OpenGL.GLRenderer;
import RunLoop.CRun;
import Services.CFile;
import Services.CServices;
import Services.FontUtils;


@SuppressLint({"NewApi", "DefaultLocale", "Assert"})
public abstract class MMFRuntime extends Activity {
	public static String version = "Fusion 293.0";
	public static String sub_version = "beta 06";
	public static int debugLevel = 0;
	public static String appVersion = "";
	public static boolean rooted;
	public static int targetApi = 0;
	public static boolean isDebuggable = false;

	public static boolean fromPause = false;
	public static boolean isShortOut = false;
	public boolean isScreenOn = false;
	public boolean isOnPause = false;
	public boolean isBackPressed = false;

	private boolean splashScreen = false;
	public boolean splashDismiss = false;
	private int splashTimeout;

	public boolean obbExpansion;
	public boolean obbAvailable;
	public long obbMainSize;
	public int obbMainVersion;
	public long obbPatchSize;
	public int obbPatchVersion;
	public Object adMob_Global;
	public AssetPackManager assetPackManager;
	private AssetPackState assetPackState;
	public boolean assetsAvailable;
	public int assetsSize;
	public int eventLine;

	public String ABI;

	public static MMFRuntime inst;
	public static ContentResolver resolver;
	public static WeakReference<Context> appContext;
	public static String packageName;
	public static String authProvider;
	public CTouchManager touchManager;

	public FontUtils fontUtils = null;

	public int OuyaTouchPad = 0;

	public String charSet = CFile.charsetU8;

	public InputMethodManager inputMethodManager = null;
	public static IMMResult result = null;

	protected Dialog mSplashDialog;

	public boolean askToDie = false;

	private final static int PERMISSIONS_REQUEST_API23 = 12344456;

	public String lastACE = "";
	public boolean hideBar;
	public boolean disableMenuBehavior;
	private boolean disableAutoEnd;

	// ActionBar variables
	private View decorView;
	private int timeBar;
	protected int nav_flag;
	private int colorActionBar = 0xCCCCCC;
	private int colorActionText = 0xFFFFFF;

	// StatusBar
	private int colorStatusBar = 0;

	// Menu extra info & colors
	private int colorMenuBack = 0xFFCCCC;
	public int colorMenuItem = 0xFF4060;
	public boolean menuMode;

	public String IconActionBar;
	public String LogoActionBar;
	public String HomeActionBar;
	public String MenuActionBar;

	private final static int PERMISSIONS_OBB_REQUEST = 15577777;
	private HashMap<String, String> permissionsApi23;
	private boolean enabled_obb;
	private boolean no_obb;

	public int countMe = 0;


	// 
	private BroadcastReceiver ouyareceiver;
	private Intent batteryReceiver;

	public void die() {
		Log.Log("MMFRuntime/die");
		// Going to a clean close of application

		if (OUYA && ouyareceiver != null && ouyareceiver instanceof BroadcastReceiver) {
			unregisterReceiver(ouyareceiver);
			ouyareceiver = null;
		}

		if (hasManifestPermission(Manifest.permission.CLEAR_APP_CACHE))
			ClearAppCache(this);

		if (timerTask != null)
			timerTask.dead = true;

		if (enableLoggerReporting) {
			Log.GenerateLogcat();
		}
		Log.ResetAtStart(this);
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				Runtime.getRuntime().exit(0);
			}
		}, 100);
	}

	public void sendBack() {
		Log.Log("MMFRuntime/sendback");
		// send it back
		moveTaskToBack(true);
	}

	public boolean enableCrashReporting = false;
	public boolean enableLoggerReporting = false;

	public static void installCrashReporter() {
		if (MMFRuntime.debugLevel == 0) /* not in development mode? */
			Thread.currentThread().
					setUncaughtExceptionHandler(new CrashReporter());
	}

	//public AdView adView;

//	public void setAdMob (final boolean enabled, final boolean displayAtBottom, final boolean displayOverFrame)
//	{
//	}

	public String adMobID = "";
	public String adMobTestDeviceID = "";

	public boolean DEBUG;
	public boolean SECURE;

	public MainView mainView = null;
	public RelativeLayout container = null;

	public int orientation = 0;
	public int keyBoardCount = 0;
	public boolean keyBoardOn = false;
	public boolean bHardKey = false;

	public int viewportX, viewportY, viewportWidth, viewportHeight;
	public float scaleX;
	public float scaleY;

	public int currentWidth, currentHeight;
	public int immersive_mode = 0;

	public int devOrientation;

	public CRunTimerTask timerTask;

	public static boolean OUYA = false;
	public static boolean FIRETV = false;
	public static boolean NEXUSTV = false;
	public static boolean ADMOB = false;
	public static boolean FONTPACK = false;
	// Emulation of joystick via android plus
	public static boolean joystickEmul = false;

	public static boolean GPGObject = false;
	public static boolean GPGMultiPlayer = false;
	public static boolean GPGCloud = false;
	public static boolean GPGEmail = false;

	@Override
	public void onConfigurationChanged(Configuration c) {
		// Checks whether a hardware keyboard is available
		if (c.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
			keyBoardOn = true;
		} else if (c.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
			keyBoardOn = false;
		}

		super.onConfigurationChanged(c);

		orientation = c.orientation;

		devOrientation = CServices.getActualOrientation();
		bHardKey = getResources().getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY;

		Log.Log("Configuration Change ...");

	}

	public boolean onceup = false;

	public boolean batteryReceived;

	public int batteryLevel;
	public int batteryStatus;
	public int batteryPlugged;
	public int batteryHealth;
	public int batteryScale;

	public CRunApp app;

	public static LinkedList<String> nativeExtensions;
	public static LinkedList<String> otherObjects;

	public BroadcastReceiver receiver;

	public boolean inputStreamToFile(InputStream input, String filename) {
		try {
			byte[] buffer = new byte[1024 * 64];
			FileOutputStream output = this.openFileOutput(filename, 0);
			BufferedOutputStream bos = new BufferedOutputStream(output, buffer.length);

			int total = 0;
			int read;
			while ((read = input.read(buffer, 0, buffer.length)) != -1) {
				bos.write(buffer, 0, read);
				total += read;
			}
			//Close up shop..
			bos.flush();
			bos.close();

			output.flush();
			output.close();

			Log.Log("inputStreamToFile: " + total + " bytes");
		} catch (Exception e) {
			Log.Log("inputStreamToFile: FAILED: " + e);
			return false;
		}

		return true;
	}

	public boolean inputAssetToFile(String assetname, String filename) {
		try {
			AssetManager assetManager = getAssets();
			InputStream input = assetManager.open(assetname);

			byte[] buffer = new byte[1024 * 64];
			FileOutputStream output = this.openFileOutput(filename, 0);
			BufferedOutputStream bos = new BufferedOutputStream(output, buffer.length);

			int total = 0;
			int read;
			while ((read = input.read(buffer, 0, buffer.length)) != -1) {
				bos.write(buffer, 0, read);
				total += read;
			}
			//Close up shop..
			bos.flush();
			bos.close();

			output.flush();
			output.close();

			Log.Log("inputAssetToFile: " + total + " bytes");
		} catch (Exception e) {
			Log.Log("inputAssetToFile: FAILED: " + e);
			return false;
		}

		return true;
	}

	public boolean inputAssetToCacheFile(String assetname, File file) {
		try {
			AssetManager assetManager = getAssets();
			InputStream input = assetManager.open(assetname);

			byte[] buffer = new byte[1024 * 64];
			FileOutputStream output = new FileOutputStream(file.getAbsolutePath());
			BufferedOutputStream bos = new BufferedOutputStream(output, buffer.length);

			int total = 0;
			int read;
			while ((read = input.read(buffer, 0, buffer.length)) != -1) {
				bos.write(buffer, 0, read);
				total += read;
			}
			//Close up shop..
			bos.flush();
			bos.close();

			output.flush();
			output.close();

			Log.Log("inputAssetToFile: " + total + " bytes");
		} catch (Exception e) {
			Log.Log("inputAssetToFile: FAILED: " + e);
			return false;
		}

		return true;
	}


	private String getAbsoluteAssetPath(String assetPack, String relativeAssetPath) {

		if (assetPackManager == null)
			initAssetPackManager();

		AssetPackLocation assetPackPath = assetPackManager.getPackLocation(assetPack);

		if (assetPackPath == null) {
			// asset pack is not ready
			return null;
		}

		String assetsFolderPath = assetPackPath.assetsPath();
		String assetPath = FilenameUtils.concat(assetsFolderPath, relativeAssetPath);
		return assetPath;
	}


	private void initAssetPackManager() {
		if (CServices.isInternetConnected(getApplicationContext())) {
			if (assetPackManager == null) {
				assetPackManager = AssetPackManagerFactory.getInstance(getApplicationContext());
			}
		} else {
			Log.Log("NO Internet available ...");
		}
	}

	private void assetToFile(String asset) {
		try {
			inputStreamToFile(getResources().getAssets().open("mmf/" + ABI + "/" + asset), asset);
		} catch (Exception e) {
		}
	}

	private void setWindowFlag(final int bits, boolean on) {
		Window window = getWindow();
		WindowManager.LayoutParams winParams = window.getAttributes();
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		window.setAttributes(winParams);
	}

	private static boolean checkRooted() {

		try {
			File file = new File("/system/app/Superuser.apk");
			if (file.exists()) {
				return true;
			}
		} catch (Exception e0) {
			// nothing to do here
		}

		String buildTags = Build.TAGS;
		if (buildTags != null && buildTags.contains("test-keys")) {
			return true;
		}

		// try executing possible location for su
		String[] commands = {
				"/sbin/su",
				"/system/bin/su",
				"/system/xbin/su",
				"/data/local/xbin/su",
				"/data/local/bin/su",
				"/system/sd/xbin/su",
				"/system/bin/failsafe/su",
				"/data/local/su"
				//"/system/xbin/which su",
				//"/system/bin/which su",
				//"which su",
				//"su"
		};
		Process process = null;
		for (String command : commands) {
			try {
				process = Runtime.getRuntime().exec(command);
				if (process != null) {
					process.destroy();
					return true;
				}
			} catch (Exception e1) {
				if (process != null)
					process.destroy();
			}
		}
		Log.Log("Non Rooted");
		return false;
	}

	Thread mainThread;

	boolean created = false;
	ApplicationInfo ai;
	int splashFlag;

	public Thread getMainThread() {
		return mainThread;
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//StrictMode.enableDefaults();

		ouyareceiver = null;
		receiver = null;
		isDebuggable = (0 != (this.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
		enableLoggerReporting = Log.isEnabledReport(this);
		NEXUSTV = OUYA = FIRETV = false;
		DEBUG = this.getResources().getBoolean(this.getResourceID("bool/APP_DEBUG"));
		SECURE = this.getResources().getBoolean(this.getResourceID("bool/APP_SECURE"));
		packageName = getApplicationContext().getPackageName();
		authProvider = packageName + ".provider";

		Log.Log("onCreate, runtime version: " + MMFRuntime.version);
		Log.AboutToStart(this);

		try {

			ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			splashFlag = ai.metaData.getInt("SPLASH_SCREEN_FLAG");
			if((splashFlag & 0x0001)!= 0) {
				splashScreen = true;
				splashDismiss = (splashFlag & 0x002) == 0;
				splashTimeout = ai.metaData.getInt("SPLASH_SCREEN_TIME");
			}

			long obbmsize = ai.metaData.getInt("OBB_EXPMAIN_SIZE");
			if(obbmsize > 0) {
				obbMainSize = obbmsize;
				obbMainVersion = ai.metaData.getInt("OBB_EXPMAIN_VERSION");
			}
			long obbpsize = ai.metaData.getInt("OBB_EXPPATCH_SIZE");
			if(obbpsize > 0) {
				obbPatchSize = obbpsize;
				obbPatchVersion = ai.metaData.getInt("OBB_EXPPATCH_VERSION");
			}

			assetsAvailable = ai.metaData.getBoolean("ASSETS_INSTALL", false);

			if(obbmsize + obbpsize == 0)
				no_obb = true;

			/////  Reading GPG_EMAIL /////
			GPGEmail = ai.metaData.getInt("GPG_EMAIL") != 0;

		} catch (NameNotFoundException e1) {
			Log.Log("onCreate, Meta Data empty");
		}

		/* the su binary should only be present on rooted devices */

		try
		{
			rooted = checkRooted();
		}
		catch (Throwable e)
		{
			Log.Log("Other Problem with rooted verification");
		}

		try
		{
			String line = Build.DEVICE;

			if(line.indexOf("ouya") != -1 || line.indexOf("cardhu") != -1 || line.indexOf("pearlyn") != -1)
			{
				OUYA = true;
				IntentFilter intent = new IntentFilter();
				intent.addAction("tv.ouya.intent.action.OUYA_MENU_APPEARING");
				intent.addAction("tv.ouya.intent.category.GAME");
				ouyareceiver = new BroadcastReceiver()
				{
					// Receiver from OUYA controller to detect system double tapped
					@Override
					public void onReceive(Context context, Intent intent)
					{
						Log.Log("Receiving a menu appearing "+intent.getAction());
						if (OUYA && intent.getAction().equals("tv.ouya.intent.action.OUYA_MENU_APPEARING")) {
							if(app.ouyaObjects.size() != 0 ) {
								for (Iterator <Extensions.CRunOUYA> it = app.ouyaObjects.iterator (); it.hasNext (); )
								{
									it.next().OnSystemTapped(0);
								}

							}
							else
							if(app != null && app.run != null)
								app.run.rhQuit= CRun.LOOPEXIT_ENDGAME;

						}
						if (OUYA && intent.getAction().equals("tv.ouya.intent.category.GAME")) {
							if(app.ouyaObjects.size() != 0 ) {
								for (Iterator <Extensions.CRunOUYA> it = app.ouyaObjects.iterator (); it.hasNext (); )
								{
									it.next().OnSystemTapped(1);
								}

							}
						}
					}
				};
				registerReceiver(ouyareceiver, intent);
			}
			else
				ouyareceiver = null;

			final String AMAZON_FEATURE_FIRE_TV = "amazon.hardware.fire_tv";

			if (getPackageManager().hasSystemFeature(AMAZON_FEATURE_FIRE_TV))
			{
				FIRETV = true;
			}
			else {
				UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
				if ((uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION
						|| line.indexOf("molly") != -1
						|| line.indexOf("pearlyn") != -1
						|| line.indexOf("foster") != -1) && !OUYA) {
					NEXUSTV = true;
				}
			}
		}
		catch (Throwable t)
		{
			Log.Log ("Error retrieving device: " + t);
		}

		Log.Log("MMFRuntime/OUYA: " + OUYA);

		CrashReporter.addInfo ("OUYA", Boolean.toString(OUYA));

		mainThread = Thread.currentThread ();

		try
		{
			PackageInfo manager=getPackageManager().getPackageInfo(getPackageName(), 0);
			appVersion = manager.versionName;
			targetApi = manager.applicationInfo.targetSdkVersion;
		}
		catch (Throwable t)
		{
			Log.Log ("Error retrieving app version: " + t);

			appVersion = "";
		}

		Log.Log ("appVersion: " + appVersion);

		MMFRuntime.installCrashReporter ();

		CrashReporter.addInfo ("Package", this.getClass ().getName ());

		MMFRuntime.inst = this;
		appContext = new WeakReference<> (this);
		resolver = getApplicationContext().getContentResolver();

		viewportX = viewportY = 0;
		scaleX = scaleY = 1.0f;

		Native.init (this.getPackageName(),//this.getClass().getPackage().getName(),
				getApplicationInfo ().dataDir + "/libs");

		ABI = Native.getABI ();
		Log.Log ("ABI is " + ABI);

		nativeExtensions = new LinkedList <String> ();
		otherObjects = new LinkedList <String> ();

		try
		{
			String assets [] = getResources().getAssets().list("mmf/" + ABI);

			for (String asset: assets)
			{
				String fasset = asset.replaceAll(".so", "");

				if (fasset.startsWith("CRun"))
				{
					nativeExtensions.add (fasset);
				}
				else
				{
					otherObjects.add (fasset);
				}
			}

			Iterator <String> it = otherObjects.iterator ();

			while (it.hasNext())
			{
				String object = it.next() + ".so";

				assetToFile (object);

				System.load(this.getFilesDir() + "/" + object);

				this.deleteFile(object);
			}

			it = nativeExtensions.iterator ();

			while (it.hasNext())
			{
				String extension = it.next();

				assetToFile (extension + ".so");

				Native.load(extension,
						this.getFilesDir() + "/" + extension + ".so");

				this.deleteFile(extension + ".so");
			}

			it = otherObjects.iterator ();

			while (it.hasNext())
			{
				this.deleteFile(it.next() + ".so");
			}
		}
		catch(Exception e)
		{
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		super.onCreate(savedInstanceState);

		if(!OUYA && splashScreen)
		{
			Log.Log("onCreate, about to splash screen ");
			showSplashScreen();
		}

		PermissionsHelper.getInstance().readManifestPermissions(this);

        if(Build.VERSION.SDK_INT < 23 || hasPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE))
            enabled_obb = true;

		if(obbMainVersion > 0 || obbPatchVersion > 0)
		{
			this.obbAvailable = expansionFileDelivered(true, obbMainVersion, obbMainSize)
					|| expansionFileDelivered(false, obbPatchVersion, obbPatchSize);
		}
		Log.Log("Obb Expansion Pack available?: "+(this.obbAvailable ? "yes": "no"));

		app = new CRunApp();
		if(no_obb || enabled_obb)
			app.load(null);

		if((app.gaFlags  & CRunApp.GA_NOHEADING) == 0 || (app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) != 0)
			setTheme(getResourceID("style/Theme.Fusion_actionbar"));

		container = new RelativeLayout (this);
		mainView = new MainView(this);

		LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		container.addView (mainView, params);

		//mainView.setId (1);

		app.createControlView ();

		app.updateWindowDimensions(app.widthSetting, app.heightSetting);

		decorView  = this.getWindow().getDecorView();
		nav_flag = 0;

		if(SECURE)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

		if (Build.VERSION.SDK_INT >= 28) {
			Resources res = getResources();
			int inset = res.getInteger(this.getResourceID("integer/insets_Mode"));
			switch (inset) {
				case 3: {
					if(Build.VERSION.SDK_INT > 29)
						this.getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
					else
						this.getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
					break;
				}
				case 2:
					this.getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
					break;
				case 1:
					this.getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
				default:
					this.getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
					break;
			}
		}

		if(!OUYA && !NEXUSTV && !FIRETV && (app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) == 0)
		{
			// Hide the status bar.
			nav_flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			if(Build.VERSION.SDK_INT >= 16)
				nav_flag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
		}

		if((app.gaFlags  & CRunApp.GA_NOHEADING) != 0 || (app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) == 0) {
			toggleActionBar(false);
		}
		else {
			toggleActionBar(true);
		}

		if((app.hdr2Options & CRunApp.AH2OPT_KEYBOVERAPPWINDOW) != 0)
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		else
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setContentView(container);

		if(no_obb || enabled_obb)
			app.startApplication();

		// result will handle the keyboard globally
		result = new IMMResult();

		// Adding FontUtils for internal and packed fonts
		fontUtils = new FontUtils();

		Log.Log("Cloud object used: "+(isClass("Extensions.CRunGPGCloud")?"yes":"no"));

		if(GPGObject) {
			if(isClass("Extensions.CRunGPGCloud"))
				startGoogleApiClient(true, GPGEmail);
			else
				startGoogleApiClient(false, GPGEmail);
		}

		timeBar = 1500;

		this.container.requestFocus();

		created = true;
		hideBar = false;
		disableAutoEnd = false;
		isScreenOn = true;

		Log.Log(" onCreated nav Flags: "+nav_flag);
		decorView.setOnSystemUiVisibilityChangeListener
				(new View.OnSystemUiVisibilityChangeListener() {
					@Override
					public void onSystemUiVisibilityChange(int visibility) {
						if((visibility | nav_flag) != nav_flag)
							VisibilityUiChange(1500);
						Log.Log("Visibility value: "+visibility + " nav Flags: "+nav_flag);
					}
				});
		setDisplayUIMode();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				onNewIntent(getIntent());
			}
		}, 2000);

	}


	public void setFrameRate (int frameRate)
	{
		Log.Log ("setFrameRate to " + frameRate);

		long interval = ((frameRate != 0) ? (long) (1000 / frameRate) : 0);

		if (timerTask != null)
		{
			if ((!timerTask.dead) && timerTask.interval == interval)
				return;

			synchronized (timerTask)
			{
				timerTask.dead = true;
				timerTask = null;
			}
		}

		if (interval != 0)
		{
			Log.Log("Interval setting...");
			Handler handler = new Handler ();
			timerTask = new CRunTimerTask(handler, interval);

			handler.post(timerTask);
		}
	}

	public boolean initialUpdateDone = false;

	public void updateViewport ()
	{
		if (!created || isOnPause)
			return;

		if (SurfaceView.inst != null)
			SurfaceView.inst.makeCurrent();

		Log.Log("Thread " + Thread.currentThread() + " updating viewport");

		currentWidth = MainView.currentWidth;
		currentHeight = MainView.currentHeight;

		Log.Log("Android.MMFRuntime updating viewport - width "
				+ currentWidth + ", height " + currentHeight);

		boolean stretchWindowToViewport = false;

		scaleX = scaleY = 1.0f;

		if(app != null && app.run != null)
			app.run.rhFrame.initSize();

		float appAspect = ((float) app.gaCxWin) / ((float) app.heightSetting);
		float screenAspect = ((float) currentWidth) / ((float) (currentHeight));

		if(currentWidth != 0  && currentHeight != 0) {

			switch (app.viewMode) {
				/* resize window to screen size (no scaling) */
				case CRunApp.VIEWMODE_ADJUSTWINDOW:

					stretchWindowToViewport = false;

					viewportWidth = currentWidth;
					viewportHeight = currentHeight;

					if (app.frame != null) {
						if (viewportWidth > app.frame.leWidth)
							viewportWidth = app.frame.leWidth;

						if (viewportHeight > app.frame.leHeight)
							viewportHeight = app.frame.leHeight;
					}


					viewportX = (currentWidth / 2) - (viewportWidth / 2);
					viewportY = (currentHeight / 2) - (viewportHeight / 2);

					app.gaCxWin = viewportWidth;
					app.gaCyWin = viewportHeight;

					app.updateWindowDimensions(viewportWidth, viewportHeight);
					//Log.Log ("ADJWND: Vw:"+viewportWidth+" Vh:"+viewportHeight+" Scx:"+scaleX+" Scy:"+scaleY+" Cw:"+currentWidth+" Ch:"+currentHeight);

					break;

				/* center unscaled app in screen */
				case CRunApp.VIEWMODE_CENTER:

					stretchWindowToViewport = false;

					viewportWidth = app.gaCxWin;
					viewportHeight = app.gaCyWin;

					viewportX = (currentWidth / 2) - (viewportWidth / 2);
					viewportY = (currentHeight / 2) - (viewportHeight / 2);

					app.updateWindowDimensions(app.gaCxWin, app.gaCyWin);
					//Log.Log ("CENTER: Vw:"+viewportWidth+" Vh:"+viewportHeight+" Scx:"+scaleX+" Scy:"+scaleY+" Cw:"+currentWidth+" Ch:"+currentHeight);

					break;

				/* scale, keep aspect ratio, add borders to match screen ratio */
				case CRunApp.VIEWMODE_FITINSIDE_BORDERS:

					/* grow window to match screen ratio, scale to screen size */
				case CRunApp.VIEWMODE_FITINSIDE_ADJUSTWINDOW:

					stretchWindowToViewport = true;

					scaleX = scaleY = Math.min(((float) currentWidth) / app.gaCxWin,
							((float) currentHeight) / app.gaCyWin);

					viewportWidth = (int) Math.ceil(app.gaCxWin * scaleX);
					viewportHeight = (int) Math.ceil(app.gaCyWin * scaleY);

					if (app.viewMode == CRunApp.VIEWMODE_FITINSIDE_ADJUSTWINDOW) {
						if (viewportWidth < currentWidth)
							app.gaCxWin = (int) Math.ceil(currentWidth / scaleX);

						if (viewportHeight < currentHeight)
							app.gaCyWin = (int) Math.ceil(currentHeight / scaleY);

						viewportWidth = (int) Math.ceil(app.gaCxWin * scaleX);
						viewportHeight = (int) Math.ceil(app.gaCyWin * scaleY);
					}

					viewportX = (currentWidth / 2) - (viewportWidth / 2);
					viewportY = (currentHeight / 2) - (viewportHeight / 2);

					app.updateWindowDimensions(app.gaCxWin, app.gaCyWin);
					//Log.Log ("FITINSIDEADJUST: Vw:"+viewportWidth+" Vh:"+viewportHeight+" Scx:"+scaleX+" Scy:"+scaleY+" Cw:"+currentWidth+" Ch:"+currentHeight);

					break;

				/* scale, keep aspect ratio, allow chopping off to fill screen */
				case CRunApp.VIEWMODE_FITOUTSIDE:

					if (appAspect < screenAspect)
						app.gaCyWin = (int) (app.gaCxWin / screenAspect);
					else
						app.gaCxWin = (int) (app.gaCyWin * screenAspect);

					scaleX = scaleY = Math.min(((float) currentWidth) / app.gaCxWin,
							((float) currentHeight) / app.gaCyWin);

					stretchWindowToViewport = true;

					viewportWidth = (int) Math.ceil(app.gaCxWin * scaleX);
					viewportHeight = (int) Math.ceil(app.gaCyWin * scaleY);

					viewportX = (currentWidth / 2) - (viewportWidth / 2);
					viewportY = (currentHeight / 2) - (viewportHeight / 2);

					app.updateWindowDimensions(app.gaCxWin, app.gaCyWin);
					//Log.Log ("FITOUTSIDE: Vw:"+viewportWidth+" Vh:"+viewportHeight+" Scx:"+scaleX+" Scy:"+scaleY+" Cw:"+currentWidth+" Ch:"+currentHeight);

					break;

				/* stretch game window, ignore aspect ratio */
				case CRunApp.VIEWMODE_STRETCH:

					stretchWindowToViewport = true;

					scaleX = ((float) currentWidth) / app.gaCxWin;
					scaleY = ((float) currentHeight) / app.gaCyWin;

					viewportWidth = (int) Math.ceil(app.gaCxWin * scaleX);
					viewportHeight = (int) Math.ceil(app.gaCyWin * scaleY);

					viewportX = (currentWidth / 2) - (viewportWidth / 2);
					viewportY = (currentHeight / 2) - (viewportHeight / 2);

					app.updateWindowDimensions(app.gaCxWin, app.gaCyWin);
					//Log.Log ("STRETCH: Vw:"+viewportWidth+" Vh:"+viewportHeight+" Scx:"+scaleX+" Scy:"+scaleY+" Cw:"+currentWidth+" Ch:"+currentHeight);

					break;

				/* scale set to 1 */
				case CRunApp.VIEWMODE_SQUARE:
				case CRunApp.VIEWMODE_SQUARE_ADJUSTWINDOW:

					stretchWindowToViewport = true;

					scaleX = scaleY = 1.0f;

					viewportWidth = (int) Math.ceil(app.gaCxWin * scaleX);
					viewportHeight = (int) Math.ceil(app.gaCyWin * scaleY);

					if (app.viewMode == CRunApp.VIEWMODE_SQUARE_ADJUSTWINDOW)
					{
						if (viewportWidth < currentWidth || viewportHeight < currentHeight)
						{
							app.gaCxWin = (int) Math.ceil(currentWidth / scaleX);
							app.gaCyWin = (int) Math.ceil(currentHeight / scaleY);
						}

						viewportWidth = (int) Math.ceil(app.gaCxWin * scaleX);
						viewportHeight = (int) Math.ceil(app.gaCyWin * scaleY);
					}

					viewportX = (currentWidth / 2) - (viewportWidth / 2);
					viewportY = (currentHeight / 2) - (viewportHeight / 2);

					app.updateWindowDimensions(app.gaCxWin, app.gaCyWin);
					//Log.Log ("FITINSIDEADJUST: Vw:"+viewportWidth+" Vh:"+viewportHeight+" Scx:"+scaleX+" Scy:"+scaleY+" Cw:"+currentWidth+" Ch:"+currentHeight);

					break;

			}

		}

		Log.Log ("uV: initialUpdateDone " + initialUpdateDone + ", GLRenderer is " + GLRenderer.inst);

		if (!initialUpdateDone)
		{
			initialUpdateDone = true;

			/* nb. onSurfaceCreated calls setFrameRate, which starts the timer */

			app.setSurfaceEnabled (true); /* for now */
		}
		else
		{
			if (GLRenderer.inst != null)
			{
				Log.Log("Setting renderer limits...");

				GLRenderer.limitX = MMFRuntime.inst.viewportX + MMFRuntime.inst.app.gaCxWin;
				GLRenderer.inst.setLimitX(GLRenderer.limitX);

				GLRenderer.limitY = MMFRuntime.inst.viewportY + MMFRuntime.inst.app.gaCyWin;
				GLRenderer.inst.setLimitY(GLRenderer.limitY);

				GLRenderer.inst.updateViewport(stretchWindowToViewport);

				GLRenderer.inst.setCurrentView(MMFRuntime.inst.currentWidth, MMFRuntime.inst.currentHeight, MMFRuntime.inst.scaleX, MMFRuntime.inst.scaleY);
			}

			if(app != null)
			{
				/* Update the frame size */

				if(app.frame != null)
					app.frame.updateSize();

				/* Redraw everything (some objects may now be visible etc.) */

				if(app.controlView != null)
					app.controlView.invalidate();

				if(app.run != null)
					app.run.redrawLevel(CRun.DLF_DRAWOBJECTS| CRun.DLF_REDRAWLAYER);

				if(splashScreen && splashDismiss) {
					removeSplashScreen();
				}

				if(app.run != null)
					app.run.updateBckgEffectTexture();
			}
		}
	}


	@Override
	protected void onStart() {

		Log.Log("Entering OnStart ...");
		super.onStart();

		CRun run = app.run;

		if (run != null && run.rhNObjects != 0)
		{
			int cptObject = run.rhNObjects;

			CObject[] localObjectList = run.rhObjectList;
			for(CObject object : localObjectList) {
				if(object != null && object instanceof CExtension) {
					((CExtension) object).ext.onStart();
				}
				-- cptObject;
				if(cptObject == 0)
					break;
			}
		}

		Log.Log("onStart");
	}

	@Override
	protected void onStop()
	{
		Log.Log("onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		Log.Log("Ending Application");
		super.onDestroy();
		decorView.removeCallbacks(displaySystemUIBar);
	}

	@Override
	protected void onPause()
	{
		Log.Log("onPause");
		if(receiver instanceof BroadcastReceiver)
			unregisterReceiver(receiver);
		receiver = null;

		super.onPause();

		isScreenOn = isScreenXOn();
		isOnPause = true;

		if (app != null && app.run != null && isScreenOn && !disableAutoEnd) {
			if((app.hdr2Options & CRunApp.AH2OPT_AUTOEND) != 0 || isBackPressed) {
				Log.Log("Requesting application to end");
				app.run.rhQuit = CRun.LOOPEXIT_ENDGAME;
			}
		}

		// Let read the screen status before entered in pause mode
		Log.Log("Screen is: "+(isScreenOn ? "yes":"no"));

		if (app != null && app.run != null && !isBackPressed
				&& (!isScreenOn || disableAutoEnd || (app.hdr2Options & CRunApp.AH2OPT_AUTOEND) == 0)) {
			app.run.pause();
		}
		isScreenOn = isScreenXOn();

	}


	@Override
	protected void onResume()
	{
		Log.Log("onResume");

		bHardKey = getResources().getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY;

		super.onResume();
		if(receiver == null)
		{
			receiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					batteryLevel = intent.getIntExtra("level", 0);
					batteryStatus = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
					batteryPlugged = intent.getIntExtra("plugged", 0xDEADBEEF);
					batteryHealth = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
					batteryScale = intent.getIntExtra("scale", 1);

					batteryReceived = true;


				}
			};
		}
		if(receiver != null)
			batteryReceiver = registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		if(app != null && app.run != null) {
			app.run.reinitDisplay();
			if(app.run.bPaused) {
				if(isScreenOn) {
					if(app.run.rh2PauseCompteur > 1)
					{
						app.run.returnPausedEvent = app.run.rh4EventCount;
						app.run.rh2PauseCompteur=1;
					}
					app.run.resume();
				}
				else {
					app.run.resume();
				}
			}
			else {
				app.run.resume();
			}

			// Added for some Samsung devices which are not updating the layout when resuming
			if(app.controlView != null)
				app.controlView.requestLayout();
		}
		isScreenOn = isScreenXOn();
		isOnPause = false;
	}

	@Override
	public void onBackPressed() {
		Log.Log("OnBackPressed() ...");

		CRun run = app.run;

		boolean extBackPressed = true;
		isBackPressed = false;

		if (run == null) {
			super.onBackPressed();
			return;
		}

		if (run.rhNObjects != 0)
		{
			int cptObject = run.rhNObjects;

			CObject[] localObjectList = run.rhObjectList;
			for(CObject object : localObjectList) {
				if(object != null && object instanceof CExtension) {
					extBackPressed &= ((CExtension) object).ext.onBackPressed();
				}
				-- cptObject;
				if(cptObject == 0)
					break;
			}
		}

		if ((app.hdr2Options & CRunApp.AH2OPT_DISABLEBACKBUTTON) == 0 && extBackPressed) {
			isBackPressed = true;
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(MMFRuntime.inst != null)
								MMFRuntime.inst.backPressedDelayed();
						}
					});
				}
			}, 200);
		}

	}

	protected void backPressedDelayed()
	{
		Log.Log("BackPress delayed");
		super.onBackPressed();
		if(!isFinishing())
			super.onBackPressed();
	}

    public boolean isScreenXOn() {
        if (Build.VERSION.SDK_INT >= 20) {
             DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays ()) {
                if (display.getState () == Display.STATE_ON ||
                        display.getState () == Display.STATE_UNKNOWN) {
                    return true;
                }
            }
            return false;
        }
		else {
			PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
			return powerManager.isScreenOn();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		CRun run = app.run;
		disableAutoEnd = false;

		super.onActivityResult(requestCode, resultCode, data);

		if (run == null) {
			return;
		}


		if (run.rhNObjects != 0)
		{
			int cptObject = run.rhNObjects;

			CObject[] localObjectList = run.rhObjectList;
			for(CObject object : localObjectList) {
				if(object != null && object instanceof CExtension) {
					((CExtension) object).ext.onActivityResult(requestCode, resultCode, data);
				}
				-- cptObject;
				if(cptObject == 0)
					break;
			}
		}

	}

	@SuppressLint({ "NewApi", "Override" })
	@TargetApi(23)
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		disableAutoEnd = false;
		if(requestCode == PERMISSIONS_REQUEST_API23) {
			Log.Log("Starting by onrequest permission ...");

			Log.Log("On Request ");
			PermissionsHelper.getInstance().notifyPermissionsChange(this, permissions, grantResults);
		}
	}

	public void askForPermission(String[] permissions, @Nullable PermissionsResultAction action) {
		if(!hasAllPermissionsGranted(permissions))
			disableAutoEnd = true;
		PermissionsHelper.getInstance().requestPermissionsIfNecessaryForResult(this, PERMISSIONS_REQUEST_API23, permissions, action);
	}

	public boolean hasAllPermissionsGranted(String[] permissions) {
		return PermissionsHelper.getInstance().hasAllPermissions(this.getApplicationContext(), permissions);
	}

	public boolean hasPermissionGranted(String permission) {
		return PermissionsHelper.getInstance().hasPermission(this.getApplicationContext(), permission);
	}

	public boolean hasPermissionDenied(String permission) {
		return PermissionsHelper.getInstance().hasPermissionDenied(this.getApplicationContext(), permission);
	}

	public boolean hasPermissionBlocked(String permission) {
		return PermissionsHelper.getInstance().hasPermissionBlocked(this.getApplicationContext(), permission);
	}
	public boolean hasAnyPermissionBlocked(String[] permissions) {
		return PermissionsHelper.getInstance().hasAnyPermissionBlocked(this.getApplicationContext(), permissions);
	}


	public boolean hasManifestPermission(String permission) {
		return PermissionsHelper.getInstance().hasManifestPermission(this.getApplicationContext(), permission);
	}

	public boolean hasManifestPermissions(String[] permissions) {
		return PermissionsHelper.getInstance().hasManifestPermissions(this.getApplicationContext(), permissions);
	}

	public boolean hasAnyManifestPermissions(String[] permissions) {
		return PermissionsHelper.getInstance().hasAnyManifestPermissions(this.getApplicationContext(), permissions);
	}

	public void getManagerStoragePermission()
	{
		if(targetApi > 29) {
			disableAutoEnd = true;
			PermissionsHelper.getInstance().askScopedStorage(this.getApplicationContext());
		}
	}

	public boolean hasManagerStoragePermission()
	{
		return PermissionsHelper.getInstance().hasExternalStorageManager();
	}

	public static void doRestart() {
        try {
            PackageManager pm = inst.getPackageManager();
            if (pm != null) {
                Intent intent = pm.getLaunchIntentForPackage(inst.getPackageName());
                if (intent != null) {
                    ComponentName componentName = intent.getComponent();
                    Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    if(Build.VERSION.SDK_INT <=23)
                    	inst.startActivity(mainIntent);
                    else
					{
						int mPendingIntentId = 223344;
						PendingIntent mPendingIntent = PendingIntent
								.getActivity(inst, mPendingIntentId, mainIntent,
										PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_CANCEL_CURRENT);
						AlarmManager mgr = (AlarmManager) inst.getSystemService(Context.ALARM_SERVICE);
						mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
					}

                    System.exit(0);
                } else {
                    Log.Log("Was not able to restart application, intent null");
                }
            } else {
                Log.Log("Was not able to restart application, PM null");
            }
        } catch (Exception e) {
            Log.Log( "Was not able to restart application");
        }
    }

	private ContextWrapper getInstance() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onLowMemory() {
		Log.Log("Low memory detected ...");
		super.onLowMemory();
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		CRun run = app.run;
		if (run == null) {
			return;
		}

		if (run.rhNObjects != 0)
		{
			int cptObject = run.rhNObjects;

			CObject[] localObjectList = run.rhObjectList;
			for(CObject object : localObjectList) {
				if(object != null && object instanceof CExtension) {
					((CExtension) object).ext.onNewIntent(intent);
					-- cptObject;
				}
				if(cptObject == 0)
					break;
			}
		}
	}


	public static void ClearAppCache(Context context) {
		try {
			File dir = context.getCacheDir();
			if (dir != null && dir.isDirectory()) {
				if(deleteDir(dir))
					Log.Log("Cache destroyed ...");
			}
		} catch (Exception e) {
			Log.Log("Problem when destroying the Cache ...");
		}
	}

	public static boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	public int getResourceID (String name)
	{
		String p = getClass().getPackage().getName();

		int id = 0;
		id = getResources().getIdentifier(name, null, p);

		Log.Log ("getResourceID for " + name + ", package " + p + ": " + id);

		if(!obbAvailable && !assetsAvailable)
			assert (id > 0);

		return id;
	}

	public int getStringResourceByName(String string)
	{
		String packageName = getPackageName();

		int id = getResources().getIdentifier(string, "string", packageName);

		if(!obbAvailable && !assetsAvailable)
			assert (id > 0);

		return id;
	}

	public int getIDsByName(String name) {
		int id = -1;

		try {
			Class<?> classIDs = Class.forName(getPackageName()+".R$id");
			if (classIDs != null) {
				final Field field = classIDs.getField(name);
				if (field != null)
					id = field.getInt(null);
			}
		} catch (final Exception e) {
			Log.Log("Error "+ e.toString());
			id = 0;
		}
		return id;
	}

	public int getGenericIDsByName(String location, String name) {
		int id = -1;
		try {
			Class<?> classID = Class.forName(MMFRuntime.inst.getPackageName()+location);
			if (classID != null) {
				final Field field = classID.getField(name);
				if (field != null)
					id = field.getInt(null);
			}
		} catch (final Exception e) {
			Log.Log("Error "+ e.toString());
		}
		return id;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		final int keyCode = event.getKeyCode ();
		final int keyAction = event.getAction();
		boolean handled = false;

		if (app != null)
		{
			if (keyCode == KeyEvent.KEYCODE_BACK && keyAction == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0 )
			{
				if ((app.hdr2Options & CRunApp.AH2OPT_DISABLEBACKBUTTON) != 0) {
					runOnRuntimeThread(new Runnable()
					{
						@Override
						public void run()
						{
							app.keyDown(keyCode);
						}
					});

					return true;
				}
			}
		}

		return handled || super.dispatchKeyEvent (event);
	}

	public Queue <Runnable> toRun = new LinkedList <Runnable> ();

	public void runOnRuntimeThread (final Runnable r)
	{
		synchronized (toRun)
		{
			MMFRuntime.inst.toRun.add (r);
		}
	}

	private boolean fillOptionsMenu(Menu menu)
	{
		if (app.androidMenu != null && app.androidMenu.length > 0)
		{
			Log.Log("Options menu not handled by an extension - using app menu (" + app.androidMenu.length + " options)");
			SubMenu subMenu = null;
			if(Build.VERSION.SDK_INT > 10 && targetApi > 10 && (app.gaFlags & CRunApp.GA_NOHEADING) == 0)
			{
				if(!menuMode)
				{
					if(MenuActionBar == null)
						subMenu = menu.addSubMenu(0, 1, 0, "").setIcon(getResourceID("drawable/ic_more_vert_white")); //ic_more_vert_black/white
					else
						subMenu = menu.addSubMenu(0, 1, 0, "").setIcon(getSpecialID(MenuActionBar));
					subMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				}
			}

			for (int i = 0; i < app.androidMenu.length; ++ i)
			{
				MenuEntry entry = app.androidMenu[i];
				if(Build.VERSION.SDK_INT > 10 && targetApi > 10 && (app.gaFlags & CRunApp.GA_NOHEADING) == 0 && ! menuMode)
					entry.item = subMenu.add(Menu.NONE, i, Menu.NONE, entry.title);
				else
					entry.item = menu.add(entry.title);


				String resource = String.format(Locale.US, "drawable/optmenu%03d", i);
				int resID = getResourceID(resource);

				Log.Log("Menu icon resource " + resource + " -> res ID " + resID);


				if(Build.VERSION.SDK_INT > 10 && ((app.gaFlags & CRunApp.GA_NOHEADING) == 0 || menuMode))
				{
					entry.item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				}

				if (resID != -1)
					entry.item.setIcon(resID);

				if (entry.disabled)
					entry.item.setEnabled(false);

			}
			if(Build.VERSION.SDK_INT > 10 && targetApi > 10 && (app.gaFlags & CRunApp.GA_NOHEADING) == 0 && !menuMode)
			{
				for(int i = 0; i < subMenu.size(); i++)
				{
					MenuItem item = subMenu.getItem(i);
					SpannableString spanItem = new SpannableString(subMenu.getItem(i).getTitle().toString());
					spanItem.setSpan(new ForegroundColorSpan(0xFF << 24 | colorMenuItem), 0, spanItem.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE); //fix the color to colorMenuItem
					item.setTitle(spanItem);
				}
			}
			else
			{

				for(int i = 0; i < menu.size(); i++)
				{
					MenuItem item = menu.getItem(i);
					SpannableString spanItem = new SpannableString(menu.getItem(i).getTitle().toString());
					//spanItem.setSpan(new RelativeSizeSpan(1.5f),  0, spanItem.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					spanItem.setSpan(new ForegroundColorSpan(0xFF << 24 | colorMenuItem), 0, spanItem.length(), 0); //fix the color to colorMenuItem
					item.setTitle(spanItem);
				}

			}


			return true;
		}

		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{

		if(disableMenuBehavior)
			return false;

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{

		boolean display = false;

		CRun run = app.run;

		if (run == null) {
			if (targetApi > 10 && app.androidMenu != null && app.androidMenu.length > 0)
			{
				// Menu is Global make it from beginning
				// this only apply for api 11 to above
				fillOptionsMenu(menu);
				return super.onCreateOptionsMenu(menu);
			}
			else
				return false;
		}

		if (run.rhNObjects != 0)
		{
			int cptObject = run.rhNObjects;
			for(CObject object : run.rhObjectList) {
				if (object != null && object instanceof CExtension
						&& ((CExtension) object).ext.onCreateOptionsMenu (menu) == true
						&& display == false)
				{
					display = true;
				}
				-- cptObject;
				if(cptObject == 0)
					break;
			}
		}

		if (!display)
		{
			/* Not handled by an extension */
			/* 	this will only run after creation under API 11, before action bar
			 	in the other case the menu need to be created at the beginning and invalidate for any 
			 	future use/modify by extension.
			 */
			fillOptionsMenu(menu);
			return super.onCreateOptionsMenu(menu);
		}

		return display;
	}

	@Override
	public boolean onMenuItemSelected(int id, MenuItem item)
	{
		return super.onMenuItemSelected(id, item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		for(MenuEntry entry : app.androidMenu)
		{
			if(entry != null) {
				if(entry.item == item)
				{
					for(CRunAndroid obj : app.androidObjects)
					{
						obj.menuItem = entry.id;

						obj.menuItemEvent = obj.ho.getEventCount();
						obj.ho.generateEvent(20, 0);

						obj.anyMenuItemEvent = obj.ho.getEventCount();
						obj.ho.generateEvent(21, 0);
					}
				}
				else {
					for(CRunAndroid obj : app.androidObjects)
					{
						obj.menuButtonEvent = obj.ho.getEventCount();
						obj.ho.generateEvent (12, 0);
					}

				}
			}
		}

		if(item != null && item.getItemId() == android.R.id.home)
			this.onBackPressed();

		return super.onOptionsItemSelected(item);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//
	//
	//                             KEYBOARD LISTENER
	//
	//
	////////////////////////////////////////////////////////////////////////////////////

	/**
	 * To capture the result of IMM hide/show soft keyboard
	 */
	public class IMMResult extends ResultReceiver {
		//public int result = -1;
		public IMMResult() {
			super(null);
		}

		@Override
		public void onReceiveResult(int r, Bundle data) {
			//Log.Log("Received result is: "+r);
			//result = r;
			if (r == InputMethodManager.RESULT_UNCHANGED_SHOWN ||
					r == InputMethodManager.RESULT_SHOWN) {
				keyBoardOn = true;
				timeBar = 3000000;
				for (Iterator <CRunAndroid> it =
                     app.androidObjects.iterator(); it.hasNext (); )
				{
					it.next().ho.generateEvent(22, 0);
				}

			}
			if (r == InputMethodManager.RESULT_UNCHANGED_HIDDEN ||
					r == InputMethodManager.RESULT_HIDDEN) {
				keyBoardOn = false;
				timeBar = 1500;
				for (Iterator <CRunAndroid> it =
                     app.androidObjects.iterator(); it.hasNext (); )
				{
					it.next().ho.generateEvent(23, 0);
				}
			}

			decorView.removeCallbacks(displaySystemUIBar);
			decorView.postDelayed(displaySystemUIBar, timeBar);

		}

	}

	public void HideKeyboard(View view, boolean bHide) {

		View currentFocus = null;

		if(inputMethodManager == null)
			inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		if(inputMethodManager != null)
		{
			if(view != null)
				currentFocus = view;

			if(currentFocus == null) {
				app.controlView.requestFocus();
				currentFocus = getCurrentFocus();
			}

			if(bHide) {
				if(currentFocus == null)
					inputMethodManager.hideSoftInputFromWindow(app.controlView.getWindowToken(), 0, result);
				else {
					synchronized (currentFocus) {
						inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0, result);
					}
				}
			}
			else {
				if(currentFocus == null)
					inputMethodManager.toggleSoftInputFromWindow(app.controlView.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
				else {
					synchronized(currentFocus) {
						inputMethodManager.showSoftInput(currentFocus, 0, result);

					}
				}
			}

		}

	}

	public static Object getBuildConfigValue(Context context, String fieldName)
	{
		try
		{
			Class<?> clazz = Class.forName(context.getPackageName() + ".BuildConfig");
			Field field = clazz.getField(fieldName);
			return field.get(null);
		} catch (ClassNotFoundException e) {
			Log.Log( "Error: " + e.toString());
		} catch (NoSuchFieldException e) {
			Log.Log( "Error: " + e.toString());
		} catch (IllegalAccessException e) {
			Log.Log( "Error: " + e.toString());
		}
		return null;
	}
	////////////////////////////////////////////////////////////////////////////////////
	//
	//
	//                             SPLASH SCREEN
	//
	//
	////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Removes the Dialog that displays the splash screen
	 */
	public void removeSplashScreen() {
		if (mSplashDialog != null) {
			mSplashDialog.dismiss();
			mSplashDialog = null;
			splashScreen = false;

		}
	}

	/**
	 * Shows the splash screen over the full Activity
	 */
	@SuppressWarnings("deprecation")
	public void showSplashScreen() {
		Display display = MMFRuntime.inst.getWindowManager().getDefaultDisplay();
		//DisplayMetrics displayMetrics = MMFRuntime.inst.getResources().getDisplayMetrics();

		int width = 0 , height = 0;
		//int px = Math.round(48 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       

		if(Build.VERSION.SDK_INT > 12) {
			Point size = new Point();
			display.getSize(size);
			width = size.x;
			height = size.y;
		}
		else {
			//Below API 13
			width = display.getWidth();  // deprecated
			height = display.getHeight();  // deprecated
		}

		mSplashDialog = new Dialog(new ContextThemeWrapper(this,  android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen), getResourceID("values/splash_screen"));
		mSplashDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSplashDialog.setContentView(getResourceID("layout/fragment_splash_screen"));
		mSplashDialog.getWindow().setBackgroundDrawable(null);
		mSplashDialog.getWindow().setGravity(Gravity.CENTER);
		mSplashDialog.getWindow().setLayout(width+10, height+10);
		mSplashDialog.setCancelable(false);
		mSplashDialog.show();

		mSplashDialog.getWindow().setBackgroundDrawable(null);
		// Set Runnable to remove splash screen just in case
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				removeSplashScreen();
			}
		}, splashTimeout);
	}

	////////////////////////////////////////////////////////////////////////////////////
	//
	//
	//                       EXPANSION ROUTINES
	//
	//
	////////////////////////////////////////////////////////////////////////////////////
	public static final String EXP_PATH = File.separator + "Android"
			+ File.separator + "obb" + File.separator;

	public static String getExpansionAPKFileName(Context c, boolean mainFile, int versionCode) {
		return (mainFile ? "main." : "patch.") + versionCode + "." + c.getPackageName() + ".obb";
	}

	static public String generateSaveFileName(Context c, String fileName) {
		String path = getSaveFilePath(c)
				+ File.separator + fileName;
		return path;
	}

	static public String getSaveFilePath(Context c) {
		File root = inst.getApplicationContext().getExternalFilesDir(null);
		//String path = root.toString() + MMFRuntime.EXP_PATH + c.getPackageName();
		String path = c.getObbDir().getAbsolutePath();
		return path;
	}

	static public boolean doesFileExist(Context c, String fileName, long fileSize,
										boolean deleteFileOnMismatch) {
		// the file may have been delivered by Market --- let's make sure
		// it's the size we expect
		File fileForNewFile = new File(generateSaveFileName(c, fileName));
		if (fileForNewFile.exists()) {
			if (fileForNewFile.length() == fileSize) {
				return true;
			}
			if (deleteFileOnMismatch) {
				// delete the file --- we won't be able to resume
				// because we cannot confirm the integrity of the file
				fileForNewFile.delete();
			}
		}
		return false;
	}

	public boolean expansionFileDelivered(boolean mIsMain, int mFileVersion, long mFileSize) {
		if(mFileSize == 0)
			return false;

		if(enabled_obb)
		{
			String fileName = getExpansionAPKFileName(this, mIsMain, mFileVersion);
			return doesFileExist(this, fileName, mFileSize, false);
		}

		PermissionsHelper.getInstance().requestPermissionsIfNecessaryForResult(this, PERMISSIONS_REQUEST_API23,
				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
				new PermissionsResultAction() {

					@Override
					public void onGranted() {
						enabled_obb = true;
						if (app.run == null) {
							if(receiver != null && receiver instanceof BroadcastReceiver) {
								unregisterReceiver(receiver);
								receiver = null;
							}

							if(OUYA && ouyareceiver!=null && ouyareceiver instanceof BroadcastReceiver) {
								unregisterReceiver (ouyareceiver);
								ouyareceiver = null;
							}
							doRestart();
						}

					}

					@Override
					public void onDenied(String permission) {
						enabled_obb = false;

						if(Build.VERSION.SDK_INT >= 23 && !enabled_obb)
						{
							app.hdr2Options |= CRunApp.AH2OPT_AUTOEND;
							sendBack();
							finish();
							die();
						}
					}

				});

		return false;
	}

	public AssetFileDescriptor getAssetFromOBB(String resource)
	{
		if(obbAvailable) {
			try {
				com.android.vending.expansion.zipfile.ZipResourceFile expansionFile =
						APKExpansionSupport.getAPKExpansionZipFile(this,obbMainVersion,obbPatchVersion);

				if(expansionFile!=null){
					AssetFileDescriptor fd = expansionFile.getAssetFileDescriptor(resource);
					return fd;
				}

			} catch (Exception e) {
				if (MMFRuntime.isDebuggable)
					e.printStackTrace();
			}
		}
		return null;
	}

	public InputStream getInputStreamFromOBB(String resource)
	{
		if(obbAvailable) {
			try {
				com.android.vending.expansion.zipfile.ZipResourceFile expansionFile =
						APKExpansionSupport.getAPKExpansionZipFile(this,obbMainVersion,obbPatchVersion);

				if(expansionFile!=null){
					InputStream is = expansionFile.getInputStream(resource);
					return is;
				}

			} catch (Exception e) {
				if (MMFRuntime.isDebuggable)
					e.printStackTrace();
			}
		}
		return null;
	}

	public AssetFileDescriptor getSoundFromOBB(String resource) {
		AssetFileDescriptor fd;
		String snd_ext[]={".mp3",".wav",".ogg",".flac",".mid"};

		for(int i=0; i < snd_ext.length; i++) {
			fd = MMFRuntime.inst.getAssetFromOBB(resource+snd_ext[i]);
			if(fd != null)
				return fd;
		}
		return null;
	}

	public AssetFileDescriptor getSoundFromAssets(String resource ) {
		AssetFileDescriptor fd;
		String snd_ext[]={".mp3",".wav",".ogg",".flac",".mid"};

		for(int i=0; i < snd_ext.length; i++) {
			try {
				fd = MMFRuntime.inst.getResources().getAssets().openFd(resource+snd_ext[i]);
				if(fd != null)
					return fd;
			} catch (IOException e) {
				Log.Log("Problem getting a sound from assets ...");
			}
		}
		return null;
	}


	public void doFinish() {
		finish();
		Runtime.getRuntime().exit(0);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//				ActionBar, Nav Bar, StatusBar and Menus control and extras
	//
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public void toggleActionBar(boolean showbar) {
		if (!showbar) {
			if (Build.VERSION.SDK_INT > 10) {
				ActionBar actionBar = getActionBar();
				if (actionBar != null) {
					actionBar.hide();
					app.gaFlags |= CRunApp.GA_NOHEADING;
				}
			}
		} else {
			if (Build.VERSION.SDK_INT > 10 && (app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) != 0) {
				ActionBar actionBar = getActionBar();
				if (actionBar != null) {
					actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
					actionBar.show();
					app.gaFlags &= ~CRunApp.GA_NOHEADING;
				}
			}
		}
		updateViewport();
	}

	public void setActionBarColor(int color)
	{
		try
		{
			ActionBar bar = getActionBar();
			if(bar != null)
			{
				ColorDrawable colorDrawable = new ColorDrawable(0xFF << 24 | color);
				bar.setBackgroundDrawable(colorDrawable);

			}
			else
			{
				View titleBar = findViewById(android.R.id.title);
				if (titleBar != null)
				{
					// find parent view
					ViewParent parent = titleBar.getParent();
					if (parent != null && (parent instanceof View))
					{
						View view = (View)parent;
						view.setBackgroundColor(0xFF << 24 | color);
					}
				}
			}
			colorActionBar = color;
		}
		catch(Exception e)
		{
			Log.Log("Error "+e.toString());
		}
	}

	public int getActionBarColor()
	{
		int color = 0;

		try
		{
			ActionBar bar = getActionBar();
			if(bar == null)
			{
				View titleBar = findViewById(android.R.id.title);
				if (titleBar != null)
				{
					// find parent view
					ViewParent parent = titleBar.getParent();
					if (parent != null && (parent instanceof View))
					{
						View view = (View)parent;
						Drawable background = view.getBackground();
						if (background instanceof ColorDrawable) {
							color = (((ColorDrawable) background).getColor() & 0x00FFFFFF);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Log.Log("Error "+e.toString());
		}
		return color;
	}

	public void setActionBarTextColor(int color)
	{
		try
		{
			int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
			if(actionBarTitleId > 0)
			{
				TextView title = (TextView)findViewById(actionBarTitleId);
				if (title != null)
				{
					title.setTextColor(0xFF << 24 | color);
				}
				else
				{
					Spannable textTitle = new SpannableString(getActionBar().getTitle());
					textTitle.setSpan(new ForegroundColorSpan(0xFF << 24 | color), 0, textTitle.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
					getActionBar().setTitle(textTitle);
				}

			}
			else
			{
				View titleBar = findViewById(android.R.id.title);
				if (titleBar != null)
				{
					// set text color
					TextView textview = (TextView)titleBar;
					textview.setTextColor(0xFF << 24 | color);
				}
			}
			colorActionText = color;
		}
		catch(Exception e)
		{
			Log.Log("Error "+e.toString());
		}
	}

	@SuppressLint("NewApi")
	public void setStatusBarColor(int color)
	{
		if (Build.VERSION.SDK_INT >= 21)
		{
			setStatusBarColor(color, 0xFF);
		}
	}

	@SuppressLint("NewApi")
	public void setStatusBarColor(int color, int alpha)
	{
		if (Build.VERSION.SDK_INT >= 21)
		{
			Window window = getWindow();

			if(alpha != 0xFF) {
				setWindowFlag(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS, false);
				setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
			}
			else {
				setWindowFlag(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS, true);
				setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
			}

			if(alpha != 0) {
				window.setStatusBarColor((alpha << 24) | color);
			}
			else {
				window.setStatusBarColor(Color.TRANSPARENT);
			}

		}
	}

	public void setUILimitsMode(int flag)
	{
		try
		{
			setWindowFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, false);
			if(flag != 0)
				setWindowFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, true);
		}
		catch(Exception e)
		{
			Log.Log("Error "+e.toString());
		}
	}

	private int getActionBarHeight()
	{
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, MMFRuntime.inst.getResources().getDisplayMetrics());

		return actionBarHeight;
	}

	private int getStatusBarHeight()
	{
		int result = 0;
		int resourceId =getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0)
			result = getResources().getDimensionPixelSize(resourceId);

		return result;
	}

	public void setDisplayTitle(int flag)
	{
		try
		{
			ActionBar bar = getActionBar();
			if(bar != null)
			{
				bar.setDisplayShowTitleEnabled((flag == 0));
			}
		}
		catch(Exception e)
		{
			Log.Log("Error "+e.toString());
		}
	}

	public void setDisplayLogo(int flag)
	{
		try
		{
			ActionBar bar = getActionBar();
			if(bar != null)
			{
				if(flag > 0)
				{
					bar.setDisplayShowHomeEnabled(false);
				}
				else if (flag < 0)
				{
					if(LogoActionBar != null)
						bar.setLogo(getSpecialID(LogoActionBar));
					if(IconActionBar != null)
						bar.setLogo(getSpecialID(IconActionBar));

					if(LogoActionBar == null && IconActionBar == null)
						bar.setDisplayUseLogoEnabled(false);
					else
						bar.setDisplayUseLogoEnabled(true);

					if((bar.getDisplayOptions() & ActionBar.DISPLAY_USE_LOGO) != 0)
						bar.setDisplayShowHomeEnabled(true);

				}
				else
				{
					if(LogoActionBar != null)
						bar.setLogo(getSpecialID(LogoActionBar));
					if(IconActionBar != null)
						bar.setLogo(getSpecialID(IconActionBar));

					if(LogoActionBar == null && IconActionBar == null)
						bar.setDisplayUseLogoEnabled(false);
					else
						bar.setDisplayUseLogoEnabled(true);

					bar.setDisplayShowHomeEnabled(true);

				}

			}

		}
		catch(Exception e)
		{
			Log.Log("Error "+e.toString());
		}
	}

	@SuppressLint("NewApi")
	public void setDisplayHomeUp(int flag)
	{
		try
		{
			ActionBar bar = getActionBar();
			if(bar != null)
			{
				if(flag == 0)
				{
					bar.setHomeButtonEnabled(false);
					bar.setDisplayHomeAsUpEnabled(false);
				}
				else if(flag == 1)
				{
					bar.setHomeButtonEnabled(true);
					if(Build.VERSION.SDK_INT > 17)
					{
						if(HomeActionBar != null)
							SetHomeAsUpIndicator(bar, getSpecialID(HomeActionBar));
						else
							SetHomeAsUpIndicator(bar, null);
					}
					bar.setDisplayHomeAsUpEnabled(true);
				}
				else
				{
					if(Build.VERSION.SDK_INT > 17)
					{
						if(HomeActionBar != null)
							SetHomeAsUpIndicator(bar, getSpecialID(HomeActionBar));
						else
							SetHomeAsUpIndicator(bar, null);
					}
					if((bar.getDisplayOptions() & ActionBar.DISPLAY_SHOW_HOME) != 0)
						bar.setDisplayHomeAsUpEnabled(true);

				}
			}

		}
		catch(Exception e)
		{
			Log.Log("Error "+e.toString());
		}
	}


	void SetHomeAsUpIndicator(ActionBar bar, Object obj)
	{
		try
		{
			//String parameter
			Class[] params = new Class[1];
			params[0] = Integer.TYPE;
			Object args [] = new Object [1];

			Method[] methods = bar.getClass().getDeclaredMethods();

			for(Method m : methods)
			{
				if(m.getName().equalsIgnoreCase("setHomeAsUpIndicator"));
				{
					args[0] = obj;
					m.invoke(null, args);
				}
			}
		}
		catch (Exception e)
		{
			Log.Log( "Error: " + e.toString());
		}
	}

	public void setActionBarTitle(String title)
	{
		try
		{
			ActionBar bar = getActionBar();
			if(bar != null)
			{
				bar.setTitle(title);
				setActionBarTextColor(colorActionText);
			}

		}
		catch(Exception e)
		{
			Log.Log("Error "+e.toString());
		}
	}

	private int getSpecialID(String res)
	{
		int id = 0;
		if(res.contains("android.") && res.contains("drawable."))
		{
			try {
				Drawable d = null;
				String resource = res.substring(res.lastIndexOf(".")+1);
				id = MMFRuntime.inst.getResources().getIdentifier(resource, "drawable", "android");
				return id;
			}
			catch (Exception e)
			{
				return 0;
			}
		}
		else if(res.contains("drawable."))
		{
			try {
				Drawable d = null;
				String resource = res.substring(res.lastIndexOf(".")+1);
				id = MMFRuntime.inst.getResources().getIdentifier(resource, "drawable", MMFRuntime.inst.getClass().getPackage().getName());
				return id;
			}
			catch (Exception e)
			{
				return 0;
			}

		}
		else
			return getResourceID(res);

	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//
	//             Navigation Bar
	//
	//////////////////////////////////////////////////////////////////////////////////////////
	// The action bar above honeycomb is always present, so it does not need to be hidden only disabled
	// allowing to correctly updateViewport and maintain the focus, if the action bar is hidden, the first
	// focus will be for it.

	//    1 View.SYSTEM_UI_FLAG_LOW_PROFILE                 api 14
	//    2 View.SYSTEM_UI_FLAG_HIDE_NAVIGATION             api 14
	//    4 View.SYSTEM_UI_FLAG_FULLSCREEN                  api 16
	//  256 View.SYSTEM_UI_FLAG_LAYOUT_STABLE               api 16
	//  512 View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION      api 16
	// 1024 View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN	        api 16
	// 2048 View.SYSTEM_UI_FLAG_IMMERSIVE                   api 19
	// 4096 View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY            api 19

	protected Runnable displaySystemUIBar = new Runnable() {
		@Override
		public void run() {
			final int flag = nav_flag;

			final int old_flags= decorView.getSystemUiVisibility();

			if (!keyBoardOn) {
				decorView.setSystemUiVisibility(flag);
			}
			Log.Log("displaySystemUIBar was:"+old_flags+" with flags:"+nav_flag);

		}
	};

	public void VisibilityUiChange(final int time) {
		decorView.removeCallbacks(displaySystemUIBar);
		decorView.postDelayed(displaySystemUIBar, time);
	}

	public void resetSystemUIVisibility() {
		decorView.setSystemUiVisibility(0);
	}

	public void executeSystemUIVisibility() {
		decorView.setSystemUiVisibility(nav_flag);
		updateViewport();
	}

	public void setDisplayUIMode() {
		if((app.gaFlags & CRunApp.GA_NOHEADING) != 0 && (app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) == 0)
			hideSystemUI(0);
		else
			showSystemUI();
	}

	public int getDisplayUIMode() {
		return nav_flag;
	}

	public void hideSystemUI()
	{
		hideSystemUI(0);
	}
	// This snippet hides the system bars.
	public void hideSystemUI(int mode)
	{
		immersive_mode = mode;
		final UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
		//LEANBACK DEVICES
		if (uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
			nav_flag |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
			if(Build.VERSION.SDK_INT >= 14)
				nav_flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

			if(Build.VERSION.SDK_INT >=16)
			{
				nav_flag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
				nav_flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
				nav_flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			}
			if(Build.VERSION.SDK_INT >= 18)
			{
				nav_flag &= ~View.SYSTEM_UI_FLAG_IMMERSIVE;
				nav_flag &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			}
			executeSystemUIVisibility();
			return;
		}

		nav_flag |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
		if(Build.VERSION.SDK_INT >= 14)
			nav_flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		if(Build.VERSION.SDK_INT >=16)
		{
			if((app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) == 0)
			{
				nav_flag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
				nav_flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			}
			//nav_flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
		}
		if(Build.VERSION.SDK_INT >= 18) {
			if(immersive_mode == 0) {
				nav_flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			}
			else {
				nav_flag |= View.SYSTEM_UI_FLAG_IMMERSIVE;
			}
		}
		if(Build.VERSION.SDK_INT > 19)
			nav_flag |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

		if (Build.VERSION.SDK_INT >= 19) {
			setWindowFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, true);

			if (Build.VERSION.SDK_INT >= 21) {
				setWindowFlag(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS, true);
			}
		}
		executeSystemUIVisibility();
		Log.Log("hideSystemUI ...");
	}

	public void showSystemUI()
	{
		nav_flag &= ~View.SYSTEM_UI_FLAG_LOW_PROFILE;
		if(Build.VERSION.SDK_INT >= 14)
			nav_flag &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

		if(Build.VERSION.SDK_INT >= 16)
		{
			if ((app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) != 0)
				nav_flag &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;

			nav_flag &= ~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
			nav_flag &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			nav_flag &= ~View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		}
		if(Build.VERSION.SDK_INT >= 18) {
			nav_flag &= ~View.SYSTEM_UI_FLAG_IMMERSIVE;
			nav_flag &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}

		executeSystemUIVisibility();
		Log.Log("showSystemUI ...");
	}

	public void toggleNavigationBar(final boolean showBar, final int value)
	{
		nav_flag &= ~View.SYSTEM_UI_FLAG_LOW_PROFILE;
		if(Build.VERSION.SDK_INT >= 14)
			nav_flag &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		if (!showBar) {
			if (value > 1) {
				if(Build.VERSION.SDK_INT >= 14)
					nav_flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			}
			else {
				nav_flag |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
			}

		}
		executeSystemUIVisibility();
		Log.Log("toggleNavigationBar ...");
	}

	public void ToggleStatusBar(boolean show) {

		if(OUYA || NEXUSTV || FIRETV)
			return;

		// Toggle the status bar.
		if (Build.VERSION.SDK_INT < 16) {
			try {
				if (!show) {
					if((app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) != 0) {
						app.hdr2Options &= ~CRunApp.AH2OPT_STATUSLINE;
						getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
								WindowManager.LayoutParams.FLAG_FULLSCREEN);
					}
				} else {
					if((app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) == 0) {
						app.hdr2Options |= CRunApp.AH2OPT_STATUSLINE;
						getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
					}
				}
			}
			catch (Exception e)
			{
				Log.Log("ToggleStatusBar exception: "+e.toString());
			}
		}
		else {
			if (!show) {
				if((app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) != 0) {
					app.hdr2Options &= ~CRunApp.AH2OPT_STATUSLINE;
					nav_flag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
				}
			}
			else {
				if((app.hdr2Options & CRunApp.AH2OPT_STATUSLINE) == 0) {
					app.hdr2Options |= CRunApp.AH2OPT_STATUSLINE;
					nav_flag &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
				}
			}
			VisibilityUiChange(500);
			updateViewport();
		}
		Log.Log("toggleStatusBar ...");
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//				Google Api Client
	//
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	private boolean isClass(String className) {
		try  {
			Class.forName(className);
			return true;
		}  catch (ClassNotFoundException e) {
			return false;
		}
	}

	void startGoogleApiClient(boolean bEmail) {
		try
		{
			//String parameter
			Class[] params = new Class[2];
			params[0] = Activity.class;
			params[1] = boolean.class;

			Class<?> c = Class.forName("com.clickteam.special.CTSignInClient");
			if ( c != null) {
				Constructor<?> co = c.getConstructor(params);
				co.newInstance(this, bEmail);
			}
		}
		catch (Exception e)
		{
			Log.Log( "Error: " + e.toString());
		}
	}

	void startGoogleApiClient(boolean bCloudBased, boolean bEmail) {
		try
		{
			//String parameter
			Class[] params = new Class[3];
			params[0] = Activity.class;
			params[1] = boolean.class;
			params[2] = boolean.class;

			Class<?> c = Class.forName("com.clickteam.special.CTSignInClient");
			if ( c != null) {
				Constructor<?> co = c.getConstructor(params);
				co.newInstance(this, bCloudBased, bEmail);
			}
		}
		catch (Exception e)
		{
			Log.Log( "Error: " + e.toString());
		}
	}

	void disconnectGoogleApiClient() {
		try
		{
			Class<?> c = Class.forName("com.clickteam.special.CTSignInClient");
			if ( c != null) {
				Method m = c.getDeclaredMethod("disconnect", (Class[])null);
				m.invoke(null, (Object[])null);
			}
		}
		catch (Exception e)
		{
			Log.Log( "Error: " + e.toString());
		}
	}

	void connectGoogleApiClient() {
		try
		{
			Class<?> c = Class.forName("com.clickteam.special.CTSignInClient");
			if ( c != null) {
				Method m = c.getDeclaredMethod("connect", (Class[])null);
				m.invoke(null, (Object[])null);
			}
		}
		catch (Exception e)
		{
			Log.Log( "Error: " + e.toString());
		}
	}

	boolean isConnectGoogleApiClient() {
		try
		{
			Class<?> c = Class.forName("com.clickteam.special.CTSignInClient");
			if ( c != null) {
				Method m = c.getDeclaredMethod("isConnected", (Class[])null);
				Object o = m.invoke(null, (Object[])null);
				return (Boolean)o;
			}
		}
		catch (Exception e)
		{
			Log.Log( "Error: " + e.toString());
		}
		return false;
	}

}

