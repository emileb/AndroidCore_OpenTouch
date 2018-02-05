package com.opentouchgaming.androidcore.controls;

import android.view.KeyEvent;
import android.view.MotionEvent;

import com.opentouchgaming.androidcore.DebugLog;

import org.libsdl.app.SDLActivity;

import java.io.IOException;
import java.util.HashMap;

import static com.opentouchgaming.androidcore.DebugLog.Level.D;
import static com.opentouchgaming.androidcore.DebugLog.Level.E;

public class ControlInterpreter
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.CONTROLS, "ControlConfig");
    }

    ControlInterface controlInterface;
    ControlConfig config;

    boolean gamePadEnabled;

    float screenWidth, screenHeight;

    HashMap<Integer, Boolean> analogButtonState = new HashMap<Integer, Boolean>(); //Saves current state of analog buttons so all sent each time

    Dpad mDpad = new Dpad();

    public ControlInterpreter(ControlInterface qif, ActionInputDefinition gamepadDefinition, boolean ctrlEn)
    {
        gamePadEnabled = ctrlEn;

        config = new ControlConfig(gamepadDefinition,null);
        try
        {
            config.loadControls();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        } catch (ClassNotFoundException e)
        {
            log.log(E, "Error loading gamepad file: " + e.toString());
        }

        for (ActionInput ai : config.actions)
        {
            if ((ai.sourceType == ActionInput.SourceType.AXIS) && ((ai.actionType == ActionInput.ActionType.MENU) || (ai.actionType == ActionInput.ActionType.BUTTON)))
            {
                analogButtonState.put(ai.actionCode, false);
            }
        }

        controlInterface = qif;
    }

    public void setScreenSize(int w, int h)
    {
        screenWidth = w;
        screenHeight = h;
    }

    public boolean onTouchEvent(MotionEvent event)
    {
/*
        // Get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // Get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // Get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        float x = event.getX(pointerIndex) / screenWidth;
        float y = event.getY(pointerIndex) / screenHeight;

        switch (maskedAction)
        {
            case MotionEvent.ACTION_MOVE:
            {
                int pointerCount = event.getPointerCount();

                for(int i = 0; i < pointerCount; ++i)
                {
                    // i is the pointer index but we'll update our pointerIndex variable for clarity
                    pointerIndex = i;

                    // To find out WHICH pointer moved we must compare pointer historical locations
                    if (event.getHistorySize() > 0)
                    {
                        // X or Y location for that pointer index moved?
                        // Corner-case: Pointer index changed (pointer up or down promoted or demoted pointer index while moving?)
                        // Fix: Track by pointer Id via sparse array as outlined in second potential solution below.
                        if ( (int)event.getX(i) != (int)event.getHistoricalX(i,0) || (int)event.getY(i) != (int)event.getHistoricalY(i, 0) )
                        {
                            pointerId = event.getPointerId(i);
                            // ...and update the circle's location.
                            x = event.getX(pointerIndex) / screenWidth;
                            y = event.getY(pointerIndex) / screenHeight;

                            controlInterface.touchEvent_if(3, pointerId, x, y);
                        }
                    }
                }

            }
            break;
            case MotionEvent.ACTION_DOWN:
            {
                controlInterface.touchEvent_if(1, pointerId, x, y);
            }
            break;
            case MotionEvent.ACTION_POINTER_DOWN:
            {
                controlInterface.touchEvent_if(1, pointerId, x, y);
            }
            break;
            case MotionEvent.ACTION_POINTER_UP:
            {
                controlInterface.touchEvent_if(2, pointerId, x, y);
            }
            break;
            case MotionEvent.ACTION_UP:
            {
                controlInterface.touchEvent_if(2, pointerId, x, y);
            }
            break;
        }
*/
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;

        if (actionCode == MotionEvent.ACTION_MOVE)
        {
            for (int i = 0; i < event.getPointerCount(); i++)
            {
                float x = event.getX(i) / screenWidth;
                float y = event.getY(i) / screenHeight;
                int pid = event.getPointerId(i);
                controlInterface.touchEvent_if(3, pid, x, y);
            }
        } else if (actionCode == MotionEvent.ACTION_DOWN)
        {
            float x = event.getX() / screenWidth;
            float y = event.getY() / screenHeight;
            controlInterface.touchEvent_if(1, 0, x, y);
        } else if (actionCode == MotionEvent.ACTION_POINTER_DOWN)
        {
            int index = event.getActionIndex();
            if (index != -1)
            {
                float x = event.getX(index) / screenWidth;
                float y = event.getY(index) / screenHeight;
                int pid = event.getPointerId(index);
                controlInterface.touchEvent_if(1, pid, x, y);
            }
        } else if (actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            int index = event.getActionIndex();
            if (index != -1)
            {
                float x = event.getX(index) / screenWidth;
                float y = event.getY(index) / screenHeight;
                int pid = event.getPointerId(index);
                controlInterface.touchEvent_if(2, pid, x, y);
            }
        } else if (actionCode == MotionEvent.ACTION_UP)
        {
            float x = event.getX() / screenWidth;
            float y = event.getY() / screenHeight;
            int index = event.getActionIndex();
            int pid = event.getPointerId(index);

            controlInterface.touchEvent_if(2, pid, x, y);
        }

        return true;
    }


    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean used = false;

        if (gamePadEnabled && event.getRepeatCount() == 0) //Don't want to send key repeates
        {
            for (ActionInput ai : config.actions)
            {
                if (((ai.sourceType == ActionInput.SourceType.BUTTON)) && (ai.source == keyCode))
                {
                    controlInterface.doAction_if(1, ai.actionCode);
                    //log.log(D, "key down intercept");
                    used = true;
                }
            }
        }

        if (used)
            return true;


        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || //If these were mapped it would have already returned
                (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))
            return false;
        else
        {
            SDLActivity.onNativeKeyDown(keyCode);
            return true;
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        boolean used = false;

        if (gamePadEnabled)
        {
            for (ActionInput ai : config.actions)
            {
                if (((ai.sourceType == ActionInput.SourceType.BUTTON)) && (ai.source == keyCode))
                {
                    controlInterface.doAction_if(0, ai.actionCode);
                    used = true;
                }
            }
        }

        if (used)
            return true;

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || //If these were mapped it would have already returned
                (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))
            return false;
        else
        {
            SDLActivity.onNativeKeyUp(keyCode);
            return true;
        }
    }

    public void onBackButton()
    {
        controlInterface.backButton_if();
    }

    float deadRegion = 0.2f;

    private float analogCalibrate(float v)
    {
        if ((v < deadRegion) && (v > -deadRegion))
            return 0;
        else
        {
            if (v > 0)
                return (v - deadRegion) / (1 - deadRegion);
            else
                return (v + deadRegion) / (1 - deadRegion);
        }
    }

    static int lastMenuButton = -1;

    public boolean onGenericMotionEvent(MotionEvent event)
    {
        //log.log(D, "onGenericMotionEvent");


        boolean used = false;
        if (gamePadEnabled)
        {
            for (ActionInput ai : config.actions)
            {
                if ((ai.sourceType == ActionInput.SourceType.AXIS) && (ai.source != -1))
                {
                    int invert;
                    invert = ai.invert ? -1 : 1;
                    if (ai.actionCode == PortActDefs.ACTION_ANALOG_PITCH)
                        controlInterface.analogPitch_if(ControlConfig.LOOK_MODE_JOYSTICK, analogCalibrate(event.getAxisValue(ai.source)) * invert * ai.scale);
                    else if (ai.actionCode == PortActDefs.ACTION_ANALOG_YAW)
                        controlInterface.analogYaw_if(ControlConfig.LOOK_MODE_JOYSTICK, -analogCalibrate(event.getAxisValue(ai.source)) * invert * ai.scale);
                    else if (ai.actionCode == PortActDefs.ACTION_ANALOG_FWD)
                        controlInterface.analogFwd_if(-analogCalibrate(event.getAxisValue(ai.source)) * invert * ai.scale);
                    else if (ai.actionCode == PortActDefs.ACTION_ANALOG_STRAFE)
                        controlInterface.analogSide_if(analogCalibrate(event.getAxisValue(ai.source)) * invert * ai.scale);
                    else //Must be using analog as a button
                    {
                        float value = event.getAxisValue(ai.source);
                        //log.log(D, "Analog as button, value = " + value);
                        //log.log(D, ai.toString());

                        if (((ai.sourcePositive) && (value) > 0.5) ||
                                ((!ai.sourcePositive) && (value) < -0.5))
                        {
                            if (!analogButtonState.get(ai.actionCode)) //Check internal state, only send if different
                            {
                                controlInterface.doAction_if(1, ai.actionCode); //press
                                analogButtonState.put(ai.actionCode, true);
                            }
                        } else
                        {
                            if (analogButtonState.get(ai.actionCode)) //Check internal state, only send if different
                            {
                                controlInterface.doAction_if(0, ai.actionCode); //un-press
                                analogButtonState.put(ai.actionCode, false);
                            }
                        }
                    }
                    used = true;
                }
            }
        }

        // Moved to below the above so GZDOOM gamepad custom buttons get registered before the arrows
        if (Dpad.isDpadDevice(event))
        {
            int menuButton = mDpad.getDirectionPressed(event);
            if (menuButton != lastMenuButton)
            {
                lastMenuButton = menuButton;
                switch (menuButton)
                {
                    case Dpad.LEFT:
                        log.log(D, "LEFT");
                        controlInterface.doAction_if(1, PortActDefs.PORT_ACT_MENU_LEFT);
                        controlInterface.doAction_if(0, PortActDefs.PORT_ACT_MENU_LEFT);
                        break;
                    case Dpad.RIGHT:
                        log.log(D, "RIGHT");
                        controlInterface.doAction_if(1, PortActDefs.PORT_ACT_MENU_RIGHT);
                        controlInterface.doAction_if(0, PortActDefs.PORT_ACT_MENU_RIGHT);
                        break;
                    case Dpad.UP:
                        log.log(D, "UP");
                        controlInterface.doAction_if(1, PortActDefs.PORT_ACT_MENU_UP);
                        controlInterface.doAction_if(0, PortActDefs.PORT_ACT_MENU_UP);
                        break;
                    case Dpad.DOWN:
                        log.log(D, "DOWN");
                        controlInterface.doAction_if(1, PortActDefs.PORT_ACT_MENU_DOWN);
                        controlInterface.doAction_if(0, PortActDefs.PORT_ACT_MENU_DOWN);
                        break;
                }
            }
        }

        return used;
    }
}
