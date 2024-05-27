package Extensions;

import java.util.Calendar;

import Actions.CActExtension;
import Conditions.CCndExtension;
import Expressions.CValue;
import RunLoop.CCreateObjectInfo;
import Runtime.Log;
import Runtime.MMFRuntime;
import Services.CBinaryFile;
import Services.CFontInfo;
import android.R;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class CRunAndroidDialogs extends CRunExtension
{
	public static final int CND0_OBJ_ALERTDIALOGBUTTONONEPRESSED = 0;
    public static final int CND1_OBJ_ALERTDIALOGBUTTONTWOPRESSED = 1;
    public static final int CND2_OBJ_ALERTDIALOGBUTTONTHREEPRESSED = 2;
    public static final int CND3_OBJ_PROGRESSDIALOGONSCREEN = 3;
    public static final int CND4_OBJ_QUICKMESSAGESHOWN = 4;
    public static final int CND5_OBJ_LISTDIALOGONSCREEN = 5;
    public static final int CND6_OBJ_LISTCLOSEDWITHCHOICE = 6;
    public static final int CND7_OBJ_NEWDATESET = 7;
    public static final int CND8_OBJ_NEWTIMESET = 8;
    public static final int CND9_OBJ_NOTIFICATIONSENT = 9;
    public static final int CND10_OBJ_DATEPICKERBUTTON1PRESSED = 10;
    public static final int CND11_OBJ_DATEPICKERBUTTON2PRESSED = 11;
    public static final int CND12_OBJ_TIMEPICKERBUTTON1PRESSED = 12;
    public static final int CND13_OBJ_TIMEPICKERBUTTON2PRESSED = 13;
    public static final int CND14_OBJ_INPUTDIALOGBUTTON1PRESSED = 14;
    public static final int CND15_OBJ_INPUTDIALOGBUTTON2PRESSED = 15;
    public static final int CND_LAST = 16;

    public static final int ACT0_SHOWONEBUTTONALERTDIALOG0123 = 0;
    public static final int ACT1_SHOWTWOBUTTONALERTDIALOG01234 = 1;
    public static final int ACT2_SHOWTHREEBUTTONALERTDIALOG012345 = 2;
    public static final int ACT3_CREATEPROGRESSDIALOG012 = 3;
    public static final int ACT4_CREATEQUICKMESSAGE01234 = 4;
    public static final int ACT5_CREATETWOOPTIONLIST012 = 5;
    public static final int ACT6_CREATETHREEOPTIONLIST0123 = 6;
    public static final int ACT7_CREATEFOUROPTIONLIST01234 = 7;
    public static final int ACT8_CREATEFIVEOPTIONLIST012345 = 8;
    public static final int ACT9_CREATEDATEPICKER0123456 = 9;
    public static final int ACT10_CREATETIMEPICKER0123456 = 10;
    public static final int ACT11_SENDPUSHNOTIFICATION01234 = 11;
    public static final int ACT12_CREATEINPUTDIALOG0123456 = 12;
    public static final int ACT13_DISMISSPROGRESSDIALOG = 13;
    
    public static final int EXP0_GETLASTCHOICE = 0;
    public static final int EXP1_GETMONTH = 1;
    public static final int EXP2_GETDAY = 2;
    public static final int EXP3_GETYEAR = 3;
    public static final int EXP4_GETHOUR = 4;
    public static final int EXP5_GETMINUTE = 5;
    public static final int EXP6_GETINPUTTEXT = 6;
	
	public int year;
	public int month;
	public int day;
	public boolean button = false;
	
	public int hour;
	public int minute;
	public int newHour;
	public int newMinute;
	
	public int tpNewTimeSet = 0;
	public int tpButton1Pressed = 0;
	public int tpButton2Pressed = 0;
	
	public DatePickerDialog dialog = null;
	public TimePickerDialog dialog2 = null;
	
    //Extension Variables
    public int buttonOnePressed = 0; //Buttons
    public int buttonTwoPressed = 0;
    public int buttonThreePressed = 0;
    public int progressOnScreen = 0; //Progress dialog is on the screen
    public int quickMessageShown = 0; //Quick message is on the screen
    public int listLastChoice = 0; //Get the user choice from the list
    public int listOnScreen = 0; //List is on the screen
    public int listClosedWithChoice = 0; //List was closed due to a user choice
    public int timePickerOK = 0; //time picker was closed due to a user choice
	public int pHour;
	public int pMinute;
	public int dpNewDateSet = 0;
	public int dpButton1Pressed = 0;
	public int dpButton2Pressed = 0;
	public int pTime;
	public int dpButtonCheck = 0;
	public int newDay = 0;
	public int newMonth = 0;
	public int newYear = 0;
	
	public int notifySent = 0;
	
	public int idButton1Pressed = 0;
	public int idButton2Pressed = 0;
	public String idText = "";
	
	public int pos = 0;

    public @Override int getNumberOfConditions()
    {
	    return CND_LAST;
    }
    public @Override boolean createRunObject(CBinaryFile file, CCreateObjectInfo cob, int version)
    {
        return false;
    }
    
    public @Override int handleRunObject()
    {
        return REFLAG_ONESHOT;
    }
    
    public @Override CFontInfo getRunObjectFont()
    {
        return null;
    }
    
    public @Override int getRunObjectTextColor()
    {
        return 0;
    }

    // Conditions
    // -------------------------------------------------
    public @Override boolean condition(int num, CCndExtension cnd)
    {
        switch (num)
        {
        case CND0_OBJ_ALERTDIALOGBUTTONONEPRESSED:
            return cnd0_obj_AlertDialogButtonOnePressed(cnd);
        case CND1_OBJ_ALERTDIALOGBUTTONTWOPRESSED:
            return cnd1_obj_AlertDialogButtonTwoPressed(cnd);
        case CND2_OBJ_ALERTDIALOGBUTTONTHREEPRESSED:
            return cnd2_obj_AlertDialogButtonThreePressed(cnd);
        case CND3_OBJ_PROGRESSDIALOGONSCREEN:
            return cnd3_obj_ProgressDialogOnScreen(cnd);
        case CND4_OBJ_QUICKMESSAGESHOWN:
            return cnd4_obj_QuickMessageShown(cnd);
        case CND5_OBJ_LISTDIALOGONSCREEN:
            return cnd5_obj_ListDialogOnScreen(cnd);
        case CND6_OBJ_LISTCLOSEDWITHCHOICE:
            return cnd6_obj_ListClosedWithChoice(cnd);
        case CND7_OBJ_NEWDATESET:
            return cnd7_obj_NewDateSet(cnd);
        case CND8_OBJ_NEWTIMESET:
            return cnd8_obj_NewTimeSet(cnd);
        case CND9_OBJ_NOTIFICATIONSENT:
            return cnd9_obj_NotificationSent(cnd);
        case CND10_OBJ_DATEPICKERBUTTON1PRESSED:
            return cnd10_obj_DatePickerButton1Pressed(cnd);
        case CND11_OBJ_DATEPICKERBUTTON2PRESSED:
            return cnd11_obj_DatePickerButton2Pressed(cnd);
        case CND12_OBJ_TIMEPICKERBUTTON1PRESSED:
            return cnd12_obj_TimePickerButton1Pressed(cnd);
        case CND13_OBJ_TIMEPICKERBUTTON2PRESSED:
            return cnd13_obj_TimePickerButton2Pressed(cnd);
        case CND14_OBJ_INPUTDIALOGBUTTON1PRESSED:
            return cnd14_obj_InputDialogButton1Pressed(cnd);
        case CND15_OBJ_INPUTDIALOGBUTTON2PRESSED:
            return cnd15_obj_InputDialogButton2Pressed(cnd);
        }
        return false;
    }

    // Actions
    // -------------------------------------------------
    public @Override void action(int num, CActExtension act)
    {
        switch (num)
        {
        case ACT0_SHOWONEBUTTONALERTDIALOG0123:
            act0_ShowOneButtonAlertDialog0123(act);
            break;
        case ACT1_SHOWTWOBUTTONALERTDIALOG01234:
            act1_ShowTwoButtonAlertDialog01234(act);
            break;
        case ACT2_SHOWTHREEBUTTONALERTDIALOG012345:
            act2_ShowThreeButtonAlertDialog012345(act);
            break;
        case ACT3_CREATEPROGRESSDIALOG012:
            act3_CreateProgressDialog012(act);
            break;
        case ACT4_CREATEQUICKMESSAGE01234:
            act4_CreateQuickMessage01234(act);
            break;
        case ACT5_CREATETWOOPTIONLIST012:
            act5_CreateTwoOptionList012(act);
            break;
        case ACT6_CREATETHREEOPTIONLIST0123:
            act6_CreateThreeOptionList0123(act);
            break;
        case ACT7_CREATEFOUROPTIONLIST01234:
            act7_CreateFourOptionList01234(act);
            break;
        case ACT8_CREATEFIVEOPTIONLIST012345:
            act8_CreateFiveOptionList012345(act);
            break;
        case ACT9_CREATEDATEPICKER0123456:
            act9_CreateDatePicker0123456(act);
            break;
        case ACT10_CREATETIMEPICKER0123456:
            act10_CreateTimePicker0123456(act);
            break;
        case ACT11_SENDPUSHNOTIFICATION01234:
            act11_SendPushNotification01234(act);
            break;
        case ACT12_CREATEINPUTDIALOG0123456:
            act12_CreateInputDialog0123456(act);
            break;
        case ACT13_DISMISSPROGRESSDIALOG:
            act13_DismissProgressDialog(act);
            break;
        }
    }

    // Expressions
    // -------------------------------------------------
    public @Override CValue expression(int num)
    {
        switch (num)
        {
        case EXP0_GETLASTCHOICE:
            return exp0_GetLastChoice();
        case EXP1_GETMONTH:
            return exp1_GetMonth();
        case EXP2_GETDAY:
            return exp2_GetDay();
        case EXP3_GETYEAR:
            return exp3_GetYear();
        case EXP4_GETHOUR:
            return exp4_GetHour();
        case EXP5_GETMINUTE:
            return exp5_GetMinute();
        case EXP6_GETINPUTTEXT:
            return exp6_GetInputText();
        }
        return null;
    }
    
    //Conditions Start----------------------------------------------------
    private boolean cnd0_obj_AlertDialogButtonOnePressed(CCndExtension cnd)
    {
    	if(buttonOnePressed == 1)
    	{
    		buttonOnePressed = 0;
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }

    private boolean cnd1_obj_AlertDialogButtonTwoPressed(CCndExtension cnd)
    {
    	if(buttonTwoPressed == 1)
    	{
    		buttonTwoPressed = 0;
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }

    private boolean cnd2_obj_AlertDialogButtonThreePressed(CCndExtension cnd)
    {
    	if(buttonThreePressed == 1)
    	{
    		buttonThreePressed = 0;
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    private boolean cnd3_obj_ProgressDialogOnScreen(CCndExtension cnd)
    {
    	if(progressOnScreen == 1)
    	{
    		progressOnScreen = 0;
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    private boolean cnd4_obj_QuickMessageShown(CCndExtension cnd)
    {
    	if(quickMessageShown == 1)
    	{
    		quickMessageShown = 0;
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    private boolean cnd5_obj_ListDialogOnScreen(CCndExtension cnd)
    {
    	if(listOnScreen == 1)
    	{
    		listOnScreen = 0;
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    private boolean cnd6_obj_ListClosedWithChoice(CCndExtension cnd)
    {
    	if(listClosedWithChoice == 1)
    	{
    		listClosedWithChoice = 0;
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    private boolean cnd7_obj_NewDateSet(CCndExtension cnd)
    {
    	if (dpNewDateSet == 1)
    	{
    		dpNewDateSet = 0;
    		return true;
    	}
        return false;
    }
    
    private boolean cnd8_obj_NewTimeSet(CCndExtension cnd)
    {
    	if (tpNewTimeSet == 1)
    	{
    		tpNewTimeSet = 0;
    		return true;
    	}
        return false;
    }

    private boolean cnd9_obj_NotificationSent(CCndExtension cnd)
    {    	
    	if (notifySent == 1)
    	{
    		notifySent = 0;
    		return true;
    	}
        return false;
    }
    
    private boolean cnd10_obj_DatePickerButton1Pressed(CCndExtension cnd)
    {
    	if (dpButton1Pressed == 1)
    	{
    		dpButton1Pressed = 0;
    		return true;
    	}
        return false;
    }

    private boolean cnd11_obj_DatePickerButton2Pressed(CCndExtension cnd)
    {
    	if (dpButton1Pressed == 1)
    	{
    		dpButton1Pressed = 0;
    		return true;
    	}
        return false;
    }
    
    private boolean cnd12_obj_TimePickerButton1Pressed(CCndExtension cnd)
    {
    	if (tpButton1Pressed == 1)
    	{
    		tpButton1Pressed = 0;
    		return true;
    	}
        return false;
    }

    private boolean cnd13_obj_TimePickerButton2Pressed(CCndExtension cnd)
    {
    	if (tpButton2Pressed == 1)
    	{
    		tpButton2Pressed = 0;
    		return true;
    	}
        return false;
    }
    
    private boolean cnd14_obj_InputDialogButton1Pressed(CCndExtension cnd)
    {
    	if (idButton1Pressed == 1)
    	{
    		idButton1Pressed = 0;
    		return true;
    	}
        return false;
    }

    private boolean cnd15_obj_InputDialogButton2Pressed(CCndExtension cnd)
    {
    	if (idButton2Pressed == 1)
    	{
    		idButton2Pressed = 0;
    		return true;
    	}
        return false;
    }

    //Actions Start-------------------------------------------------
    private void act0_ShowOneButtonAlertDialog0123(CActExtension act)
    {
    	final String param0 = act.getParamExpString(rh,0); //Title
    	final String param1 = act.getParamExpString(rh,1); //Message
    	final String param2 = act.getParamExpString(rh,2); //Button 1
    	final int param3 = act.getParamExpression(rh,3); //Cancel
                
        	final AlertDialog alertDialog = new AlertDialog.Builder(ho.getControlsContext()).create(); //Create dialog
        	alertDialog.setTitle(param0); //Title Text
        	alertDialog.setMessage(param1); //Message Text
        	alertDialog.setIcon(R.drawable.ic_dialog_info);
        	
        	alertDialog.setButton(param2, new DialogInterface.OnClickListener() { //Button 1 Text
        	      public void onClick(DialogInterface dialog, int which) {
        	 
        	      //Button 1 was clicked?	  
        	      alertDialog.dismiss(); //Kill the dialog
        	      buttonOnePressed = 1;
        	      ho.generateEvent(CND0_OBJ_ALERTDIALOGBUTTONONEPRESSED, 0);
        	 
        	    } });
        	
        	if(param3 == 1)
        	{
        		alertDialog.setCancelable(true); //User can cancel the dialog?
        	}
        	else
        	{
        		alertDialog.setCancelable(false);
        	}
        	
        	alertDialog.show(); //Show the dialog
    }
    
    private void act1_ShowTwoButtonAlertDialog01234(CActExtension act)
    {
    	final String param0 = act.getParamExpString(rh,0);
    	final String param1 = act.getParamExpString(rh,1);
    	final String param2 = act.getParamExpString(rh,2);
    	final String param3 = act.getParamExpString(rh,3);
    	final int param4 = act.getParamExpression(rh,4);
       
        	final AlertDialog alertDialog = new AlertDialog.Builder(ho.getControlsContext()).create(); //Create dialog
        	alertDialog.setTitle(param0); //Title Text
        	alertDialog.setMessage(param1); //Message Text
        	alertDialog.setIcon(R.drawable.ic_dialog_info);
        	
        	alertDialog.setButton(param2, new DialogInterface.OnClickListener() { //Button 1 Text
        	      public void onClick(DialogInterface dialog, int which) {
        	 
        	      //Button 1 was clicked?	  
        	      alertDialog.dismiss(); //Kill the dialog
        	      buttonOnePressed = 1;
        	      ho.generateEvent(CND0_OBJ_ALERTDIALOGBUTTONONEPRESSED, 0);
        	 
        	    } });
        	
        	alertDialog.setButton2(param3, new DialogInterface.OnClickListener() { //Button 2 Text
      	      public void onClick(DialogInterface dialog, int which) {
      	 
          	      //Button 2 was clicked?	  
          	      alertDialog.dismiss(); //Kill the dialog
          	      buttonTwoPressed = 1;
          	      ho.generateEvent(CND1_OBJ_ALERTDIALOGBUTTONTWOPRESSED, 0);
      	 
      	    } });
        	
        	if(param4 == 1)
        	{
        		alertDialog.setCancelable(true); //User can cancel the dialog?
        	}
        	else
        	{
        		alertDialog.setCancelable(false);
        	}
        	
        	alertDialog.show(); //Show the dialog
    }

    private void act2_ShowThreeButtonAlertDialog012345(CActExtension act)
    {
        final String param0 = act.getParamExpString(rh,0);
        final String param1 = act.getParamExpString(rh,1);
        final String param2 = act.getParamExpString(rh,2);
        final String param3 = act.getParamExpString(rh,3);
        final String param4 = act.getParamExpString(rh,4);
        final int param5 = act.getParamExpression(rh,5);
        
        	final AlertDialog alertDialog = new AlertDialog.Builder(ho.getControlsContext()).create(); //Create dialog
        	alertDialog.setTitle(param0); //Title Text
        	alertDialog.setMessage(param1); //Message Text
        	alertDialog.setIcon(R.drawable.ic_dialog_info);
        	
        	alertDialog.setButton(param2, new DialogInterface.OnClickListener() { //Button 1 Text
        	      public void onClick(DialogInterface dialog, int which) {
        	 
        	      //Button 1 was clicked?	  
        	      alertDialog.dismiss(); //Kill the dialog
        	      buttonOnePressed = 1;
        	      ho.generateEvent(CND0_OBJ_ALERTDIALOGBUTTONONEPRESSED, 0);
        	 
        	    } });
        	
        	alertDialog.setButton2(param4, new DialogInterface.OnClickListener() { //Button 2 Text
      	      public void onClick(DialogInterface dialog, int which) {
      	 
          	      //Button 2 was clicked?	  
          	      alertDialog.dismiss(); //Kill the dialog
          	      buttonTwoPressed = 1;
          	      ho.generateEvent(CND1_OBJ_ALERTDIALOGBUTTONTWOPRESSED, 0);
      	 
      	    } });
        	
        	alertDialog.setButton3(param3, new DialogInterface.OnClickListener() { //Button 3 Text
        	      public void onClick(DialogInterface dialog, int which) {
        	 
          	      //Button 3 was clicked?	  
          	      alertDialog.dismiss(); //Kill the dialog
          	      buttonThreePressed = 1;
          	      ho.generateEvent(CND2_OBJ_ALERTDIALOGBUTTONTHREEPRESSED, 0);
        	 
        	    } });
        	
        	if(param5 == 1)
        	{
        		alertDialog.setCancelable(true); //User can cancel the dialog?
        	}
        	else
        	{
        		alertDialog.setCancelable(false);
        	}
        	
        	alertDialog.show(); //Show the dialog
    }
    
    private void act3_CreateProgressDialog012(CActExtension act)
    {
        final String param0 = act.getParamExpString(rh,0); //Title
        final String param1 = act.getParamExpString(rh,1); //Message
        final int param2 = act.getParamExpression(rh,2); //Show Timer Milliseconds
        
        	final ProgressDialog dialog = ProgressDialog.show(ho.getControlsContext(), param0, param1, true);
        	dialog.setIcon(R.drawable.ic_dialog_info);
        	pos = 0;
        	
        	progressOnScreen = 1;
        	ho.generateEvent(CND3_OBJ_PROGRESSDIALOGONSCREEN, 0);
        	
        	long timer = Long.valueOf(param2);
        	
        	new CountDownTimer(timer, 1000)
        	{
        	     public void onTick(long millisUntilFinished)
        	     {
        	         //Update Here
        	    	 if(pos == 1)
        	    	 {
        	    		 pos = 0;
        	    		 dialog.dismiss();
        	    	 }
        	     }

        	     public void onFinish()
        	     {
        	         //Finish
        	    	 dialog.dismiss();
        	     }
        	  }.start();
    }
    
    private void act4_CreateQuickMessage01234(CActExtension act)
    {
        final String param0 = act.getParamExpString(rh,0);
        final String param1 = act.getParamExpString(rh,1);
        final int param2 = act.getParamExpression(rh,2);
        final int param3 = act.getParamExpression(rh,3);
        final int param4 = act.getParamExpression(rh,4);
             
        	Toast toast = Toast.makeText(ho.getControlsContext(), param0, param4);
        	
	        if(param1.equals("Top"))
	        {
	        	toast.setGravity(Gravity.TOP, param2, param3);
	        }
	        else if(param1.equals("Bottom"))
	        {
	        	toast.setGravity(Gravity.BOTTOM, param2, param3);
	        }
	        else
	        {
	        	toast.setGravity(Gravity.CENTER, param2, param3);
	        }
	        
	        toast.show();
	        
	        quickMessageShown = 1;
		    ho.generateEvent(CND4_OBJ_QUICKMESSAGESHOWN, 0);
    }
    
    private void act5_CreateTwoOptionList012(CActExtension act)
    {
        final String param0 = act.getParamExpString(rh,0);
        final String param1 = act.getParamExpString(rh,1);
        final String param2 = act.getParamExpString(rh,2);
            
        	final CharSequence[] items = {param1, param2};

        	AlertDialog.Builder builder = new AlertDialog.Builder(ho.getControlsContext());
        	builder.setTitle(param0);
        	builder.setIcon(R.drawable.ic_dialog_info);
        	builder.setItems(items, new DialogInterface.OnClickListener()
        	{
        	    public void onClick(DialogInterface dialog, int item)
        	    {
        	    	if(items[item].equals(param1))
        	    	{
        	    		listLastChoice = 0;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    	else
        	    	{
        	    		listLastChoice = 1;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    }
        	});
        	AlertDialog alert = builder.create();
        	alert.show();
	        
        	listOnScreen = 1;
		    ho.generateEvent(CND5_OBJ_LISTDIALOGONSCREEN, 0);
    }

    private void act6_CreateThreeOptionList0123(CActExtension act)
    {
        final String param0 = act.getParamExpString(rh,0);
        final String param1 = act.getParamExpString(rh,1);
        final String param2 = act.getParamExpString(rh,2);
        final String param3 = act.getParamExpString(rh,3);
            
        	final CharSequence[] items = {param1, param2, param3};

        	AlertDialog.Builder builder = new AlertDialog.Builder(ho.getControlsContext());
        	builder.setTitle(param0);
        	builder.setIcon(R.drawable.ic_dialog_info);
        	builder.setItems(items, new DialogInterface.OnClickListener()
        	{
        	    public void onClick(DialogInterface dialog, int item)
        	    {
        	    	if(items[item].equals(param1))
        	    	{
        	    		listLastChoice = 0;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    	else if(items[item].equals(param2))
        	    	{
        	    		listLastChoice = 1;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    	else
        	    	{
        	    		listLastChoice = 2;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    }
        	});
        	AlertDialog alert = builder.create();
        	alert.show();
	        
        	listOnScreen = 1;
		    ho.generateEvent(CND5_OBJ_LISTDIALOGONSCREEN, 0);
    }

    private void act7_CreateFourOptionList01234(CActExtension act)
    {
        final String param0 = act.getParamExpString(rh,0);
        final String param1 = act.getParamExpString(rh,1);
        final String param2 = act.getParamExpString(rh,2);
        final String param3 = act.getParamExpString(rh,3);
        final String param4 = act.getParamExpString(rh,4);
        
        	final CharSequence[] items = {param1, param2, param3, param4};

        	AlertDialog.Builder builder = new AlertDialog.Builder(ho.getControlsContext());
        	builder.setTitle(param0);
        	builder.setIcon(R.drawable.ic_dialog_info);
        	builder.setItems(items, new DialogInterface.OnClickListener()
        	{
        	    public void onClick(DialogInterface dialog, int item)
        	    {
        	    	if(items[item].equals(param1))
        	    	{
        	    		listLastChoice = 0;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    	else if(items[item].equals(param2))
        	    	{
        	    		listLastChoice = 1;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    	else if(items[item].equals(param3))
        	    	{
        	    		listLastChoice = 2;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    	else
        	    	{
        	    		listLastChoice = 3;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    }
        	});
        	AlertDialog alert = builder.create();
        	alert.show();
	        
        	listOnScreen = 1;
		    ho.generateEvent(CND5_OBJ_LISTDIALOGONSCREEN, 0);
    }

    private void act8_CreateFiveOptionList012345(CActExtension act)
    {
        final String param0 = act.getParamExpString(rh,0);
        final String param1 = act.getParamExpString(rh,1);
        final String param2 = act.getParamExpString(rh,2);
        final String param3 = act.getParamExpString(rh,3);
        final String param4 = act.getParamExpString(rh,4);
        final String param5 = act.getParamExpString(rh,5);
         
        	final CharSequence[] items = {param1, param2, param3, param4, param5};

        	AlertDialog.Builder builder = new AlertDialog.Builder(ho.getControlsContext());
        	builder.setTitle(param0);
        	builder.setIcon(R.drawable.ic_dialog_info);
        	builder.setItems(items, new DialogInterface.OnClickListener()
        	{
        	    public void onClick(DialogInterface dialog, int item)
        	    {
        	    	if(items[item].equals(param1))
        	    	{
        	    		listLastChoice = 0;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    	else if(items[item].equals(param2))
        	    	{
        	    		listLastChoice = 1;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    	else if(items[item].equals(param3))
        	    	{
        	    		listLastChoice = 2;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    	else if(items[item].equals(param4))
        	    	{
        	    		listLastChoice = 3;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    	else
        	    	{
        	    		listLastChoice = 4;
        	    		listClosedWithChoice = 1;
        			    ho.generateEvent(CND6_OBJ_LISTCLOSEDWITHCHOICE, 0);
        	    	}
        	    }
        	});
        	AlertDialog alert = builder.create();
        	alert.show();
	        
        	listOnScreen = 1;
		    ho.generateEvent(CND5_OBJ_LISTDIALOGONSCREEN, 0);
    }
    
    private void act9_CreateDatePicker0123456(CActExtension act)
    {
        final String param0 = act.getParamExpString(rh,0);
        final String param1 = act.getParamExpString(rh,1);
        final String param2 = act.getParamExpString(rh,2);
        final String param3 = act.getParamExpString(rh,3);
        int param4 = act.getParamExpression(rh,4);
        int param5 = act.getParamExpression(rh,5);
        int param6 = act.getParamExpression(rh,6);
                      	
        //Context context = ho.getControlsContext();
        final Calendar c = Calendar.getInstance();//To initialize with the current date 
        
        if(param4 == 0)
        {
        	year = c.get(Calendar.YEAR);
        }
        else if (param4 != 0)
        {
        	//year = param4;
        	c.set(Calendar.YEAR, param4);
        	year = c.get(Calendar.YEAR);
        }
        
        if(param5 == 0)
        {
        	month = c.get(Calendar.MONTH);
        }
        else if (param5 != 0)
        {
        	c.set(Calendar.MONTH, param5 - 1);
        	month = c.get(Calendar.MONTH);
        }
        
        if(param6 == 0)
        {
        	day = c.get(Calendar.DAY_OF_MONTH);
        }   
        else if (param4 != 0)
        {
        	c.set(Calendar.DAY_OF_MONTH, param6);
        	day = c.get(Calendar.DAY_OF_MONTH);
        }

        Context context = ho.getControlsContext();
        
        DatePickerDialog.OnDateSetListener dateListen = new DatePickerDialog.OnDateSetListener()
    	{
    	    public void onDateSet(DatePicker view, int Myear, int MmonthOfYear, int MdayOfMonth)
    	    {             	    	
	        	newYear = Myear;
	        	newMonth = MmonthOfYear +1;
	        	newDay = MdayOfMonth;
	        	
	        	dialog.updateDate(newYear, newMonth, newDay);
	        	
    			dpNewDateSet = 1;
    			ho.pushEvent(CND7_OBJ_NEWDATESET, 0);
    			
    			dpButton1Pressed = 1;
		    	ho.pushEvent(CND10_OBJ_DATEPICKERBUTTON1PRESSED, 0);
    	    }
    	};
    	
		dialog = new DatePickerDialog(context , dateListen, year, month, day);
		dialog.setTitle(param0);
		dialog.setMessage(param1);	
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, param2, dialog);
		dialog.setIcon(R.drawable.ic_dialog_info);
		//dialog.setButton(DialogInterface.BUTTON_NEGATIVE, param3, dialog);
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, param3, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
		       if (which == DialogInterface.BUTTON_NEGATIVE)
		       {   
		    	   dpButton2Pressed = 1;
		    	   ho.pushEvent(CND11_OBJ_DATEPICKERBUTTON2PRESSED, 0);
		       }
		    }
		});
		
		dialog.show(); 					
    }
    
    private void act10_CreateTimePicker0123456(CActExtension act)
    {
        final String param0 = act.getParamExpString(rh,0);
        final String param1 = act.getParamExpString(rh,1);
        final String param2 = act.getParamExpString(rh,2);
        final String param3 = act.getParamExpString(rh,3);
        final int param4 = act.getParamExpression(rh,4);
        int param5 = act.getParamExpression(rh,5);
        int param6 = act.getParamExpression(rh,6);
        
        final Calendar d = Calendar.getInstance();//To initialize with the current date 
        
        if(param5 == 0)
        {
        	hour = d.get(Calendar.HOUR);
        }
        else if (param5 != 0)
        {
        	d.set(Calendar.HOUR, param5);
        	hour = d.get(Calendar.HOUR);
        }
        
        if(param6 == 0)
        {
        	minute = d.get(Calendar.MINUTE);
        }
        else if (param6 != 0)
        {
        	d.set(Calendar.MINUTE, param6);
        	minute = param6;
        }

        Context context = ho.getControlsContext();
        
        OnTimeSetListener timeListen = new TimePickerDialog.OnTimeSetListener()
    	{
    	    public void onTimeSet(TimePicker view, int MHour, int MMinute)
    	    {             	    	
	        	newHour = MHour;
	        	newMinute = MMinute;
	        	
	        	dialog2.updateTime(newHour, newMinute);
	        	
    			tpNewTimeSet = 1;
    			ho.pushEvent(CND8_OBJ_NEWTIMESET, 0);
    			
    			tpButton1Pressed = 1;
		    	ho.pushEvent(CND12_OBJ_TIMEPICKERBUTTON1PRESSED, 0);
    	    }
    	};
    	
    	if(param4 == 1)
    	{
    		dialog2 = new TimePickerDialog(context , timeListen, hour, minute, false);
    	}
    	else
    	{
    		dialog2 = new TimePickerDialog(context , timeListen, hour, minute, true);
    	}
    	
		//dialog2 = new TimePickerDialog(context , timeListen, hour, minute, true);
		dialog2.setTitle(param0);
		dialog2.setMessage(param1);	
		dialog2.setButton(DialogInterface.BUTTON_POSITIVE, param2, dialog2);
		dialog2.setIcon(R.drawable.ic_dialog_info);
		//dialog.setButton(DialogInterface.BUTTON_NEGATIVE, param3, dialog);
		
		dialog2.setButton(DialogInterface.BUTTON_NEGATIVE, param3, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
		       if (which == DialogInterface.BUTTON_NEGATIVE)
		       {   
		    	   	tpButton2Pressed = 1;
			    	ho.pushEvent(CND13_OBJ_TIMEPICKERBUTTON2PRESSED, 0);
		       }
		    }
		});
		
		dialog2.show();           	
    }

    private void act11_SendPushNotification01234(CActExtension act)
    {
        final String param0 = act.getParamExpString(rh,0);
        final String param1 = act.getParamExpString(rh,1);
        final String param2 = act.getParamExpString(rh,2);
        final int param3 = act.getParamExpression(rh,3);
        final int param4 = act.getParamExpression(rh,4);
    	
        //Context context = ho.getControlsContext();
    	
        triggerNotification(param0, param1, param2, param3, param4);        
    }
    
    @SuppressWarnings("deprecation")
	private void triggerNotification(String titleText, String messageText, String tickerText, int playSound, int vibrateDevice)
    {
    	Context context = ho.getControlsContext();
    	
        CharSequence title = titleText;
        CharSequence message = messageText;
        long whenTo = System.currentTimeMillis();
        
        Intent intent = new Intent(MMFRuntime.inst, MMFRuntime.inst.getClass());

        PendingIntent pendingIntent = PendingIntent.getActivity(MMFRuntime.inst, 101101, intent, 0);

        Notification.Builder builder = new Notification.Builder(MMFRuntime.inst);

        if(playSound == 1)
        {
        	Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        	builder.setSound(uri);
        }
        
        if(vibrateDevice == 1)
        {
        	long[] v = {500,1000};
        	builder.setVibrate(v);
        }

        builder.setAutoCancel(true);
        builder.setTicker(tickerText);
        builder.setContentTitle(title);               
        builder.setContentText(message);
        builder.setSmallIcon(MMFRuntime.inst.getResourceID("drawable/launcher"));
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setWhen(whenTo);
        builder.build();

        Notification notication = builder.build();
        NotificationManager notificationManager;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        try
        {
        	notificationManager.notify(10109955, notication);
			notifySent = 1;
			ho.pushEvent(CND9_OBJ_NOTIFICATIONSENT, 0);
		}
        catch (Exception e)
        {
			Log.Log("An unkown error occurred while sending push notification on line 928.");
		}
    }
    
    private void act12_CreateInputDialog0123456(CActExtension act)
    {
        String param0 = act.getParamExpString(rh,0); //title
        String param1 = act.getParamExpString(rh,1); //message
        String param2 = act.getParamExpString(rh,2); //button 1
        String param3 = act.getParamExpString(rh,3); //button 2
        int param4 = act.getParamExpression(rh,4); //limit chars
        String param5 = act.getParamExpString(rh,5); //default text
        int param6 = act.getParamExpression(rh,6); //password field?
        
        AlertDialog.Builder alert = new AlertDialog.Builder(ho.getControlsContext());

        alert.setTitle(param0);
        alert.setMessage(param1);
        alert.setIcon(R.drawable.ic_dialog_info);

        // Set an EditText view to get user input 
        final EditText input = new EditText(ho.getControlsContext());
        input.setText(param5);
        
        if(param6 == 1)
        {
        	input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        
        if(param4 >0)
        {
        	InputFilter[] FilterArray = new InputFilter[1];  
        	FilterArray[0] = new InputFilter.LengthFilter(param4); 
        	input.setFilters(FilterArray);
        }
        
        alert.setView(input);

        alert.setPositiveButton(param2, new DialogInterface.OnClickListener()
        {
        	public void onClick(DialogInterface dialog, int whichButton)
        	{
        		idText = input.getText().toString();
        		
        		idButton1Pressed = 1;
    		    ho.generateEvent(CND14_OBJ_INPUTDIALOGBUTTON1PRESSED, 0);
        	}
        });

        alert.setNegativeButton(param3, new DialogInterface.OnClickListener()
        {
        	public void onClick(DialogInterface dialog, int whichButton)
        	{
        		idText = "";
        		
        		idButton2Pressed = 1;
    		    ho.generateEvent(CND15_OBJ_INPUTDIALOGBUTTON2PRESSED, 0);
        	}
        });

        alert.show();
    }
    
    private void act13_DismissProgressDialog(CActExtension act)
    {
    	pos = 1;
    }
    
	//Expressions Start----------------
    private CValue exp0_GetLastChoice()
    {
    	return new CValue(listLastChoice);
    }
    
    private CValue exp1_GetMonth()
    {
        return new CValue(newMonth);
    }

    private CValue exp2_GetDay()
    {
        return new CValue(newDay);
    }

    private CValue exp3_GetYear()
    {
        return new CValue(newYear);
    }
    
    private CValue exp4_GetHour()
    {
        return new CValue(newHour);
    }

    private CValue exp5_GetMinute()
    {
        return new CValue(newMinute);
    }
    
    private CValue exp6_GetInputText()
    {
        return new CValue(idText);
    }
}
