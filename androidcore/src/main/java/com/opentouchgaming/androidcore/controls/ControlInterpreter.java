package com.opentouchgaming.androidcore.controls;

import android.view.KeyEvent;
import android.view.MotionEvent;

import com.opentouchgaming.androidcore.DebugLog;

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

        config = new ControlConfig(gamepadDefinition);
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
        ;
        if (gamePadEnabled)
        {
            for (ActionInput ai : config.actions)
            {
                if (((ai.sourceType == ActionInput.SourceType.BUTTON)) && (ai.source == keyCode))
                {
                    controlInterface.doAction_if(1, ai.actionCode);
                    log.log(D, "key down intercept");
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
            int uc = 0;
            if (event != null)
                uc = event.getUnicodeChar();
            controlInterface.keyPress_if(1, controlInterface.mapKey(keyCode, uc), uc);
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
            int uc = 0;
            if (event != null)
                uc = event.getUnicodeChar();
            controlInterface.keyPress_if(0, controlInterface.mapKey(keyCode, uc), uc);
            return true;
        }

    }

    public void onBackButton( )
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
            //return v;
        }
    }

    static int lastMenuButton = -1;

    public boolean onGenericMotionEvent(MotionEvent event)
    {
        log.log(D, "onGenericMotionEvent");

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
                        return true;
                    case Dpad.RIGHT:
                        log.log(D, "RIGHT");
                        controlInterface.doAction_if(1, PortActDefs.PORT_ACT_MENU_RIGHT);
                        controlInterface.doAction_if(0, PortActDefs.PORT_ACT_MENU_RIGHT);
                        return true;
                    case Dpad.UP:
                        log.log(D, "UP");
                        controlInterface.doAction_if(1, PortActDefs.PORT_ACT_MENU_UP);
                        controlInterface.doAction_if(0, PortActDefs.PORT_ACT_MENU_UP);
                        return true;
                    case Dpad.DOWN:
                        log.log(D, "DOWN");
                        controlInterface.doAction_if(1, PortActDefs.PORT_ACT_MENU_DOWN);
                        controlInterface.doAction_if(0, PortActDefs.PORT_ACT_MENU_DOWN);
                        return true;
                }
            }
        }

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
                        log.log(D, "Analog as button, value = " + value);
                        log.log(D, ai.toString());

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


        return used;

    }
}
