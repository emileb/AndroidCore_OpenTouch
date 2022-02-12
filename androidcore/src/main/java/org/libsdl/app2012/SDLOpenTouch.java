package org.libsdl.app2012;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.AppSettings;
import com.opentouchgaming.androidcore.GamepadActivity;
import com.opentouchgaming.androidcore.Utils;
import com.opentouchgaming.androidcore.controls.ControlInterpreter;
import com.opentouchgaming.androidcore.controls.GamepadDefinitions;
import com.opentouchgaming.androidcore.controls.TouchSettings;
import com.opentouchgaming.androidcore.ui.GyroDialog;
import com.opentouchgaming.androidcore.ui.QuickCommandDialog;
import com.opentouchgaming.androidcore.ui.TouchSettingsSaveLoad;
import com.opentouchgaming.saffal.UtilsSAF;

import org.libsdl.app.NativeConsoleBox;
import org.libsdl.app.NativeLib;

import java.io.File;

public class SDLOpenTouch
{

    static final String TAG = "SDLOpenTouch";

    static float resDiv = 1.0f;
    static boolean divDone = false;
    public static boolean enableVibrate;

    static String userFiles;

    static NativeLib engine;

    static ControlInterpreter controlInterp;

    static SDLOpenTouchGyro gyro;

    static QuickCommandDialog quickCommandDialog;
    static String quickCommandMainPath;
    static String quickCommandModPath;

    static void onPause(Context context)
    {
        SDLAudioManager.onPause();
    }

    static void onResume(Context context)
    {
        if (controlInterp != null)
        {
            controlInterp.loadGameControlsFile();
        }
        sendWeaponWheelSettings(context);
        SDLAudioManager.onResume();
    }

    static void Setup(Activity activity, Intent intent)
    {
        AppSettings.reloadSettings(activity);

        AppInfo.Apps app = AppInfo.Apps.valueOf(intent.getStringExtra("app"));

        AppInfo.setContext(activity);
        AppInfo.setApp(app);

        UtilsSAF.setContext(activity.getApplicationContext());
        UtilsSAF.loadTreeRoot(activity.getApplicationContext());

        org.fmod.FMOD.init(activity);
        NativeConsoleBox.init(activity);

        // Load libraries
        try
        {
            // Load game libs
            String[] loadLibs = intent.getStringArrayExtra("load_libs");
            for (String lib : loadLibs)
            {
                Log.d(TAG, "Loading: " + lib);
                System.loadLibrary(lib);
            }

        } catch (UnsatisfiedLinkError e)
        {
            System.err.println(e.getMessage());
        } catch (Exception e)
        {
            System.err.println(e.getMessage());
        }

        // fullscreen
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // keep screen on
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Utils.setImmersionMode(activity);
        Utils.expandToCutout(activity);

        gyro = new SDLOpenTouchGyro(activity, activity.getWindowManager().getDefaultDisplay().getRotation());

        engine = new NativeLib();
        controlInterp = new ControlInterpreter(activity, engine, GamepadDefinitions.getDefinition(AppInfo.app), TouchSettings.gamePadEnabled, TouchSettings.altTouchCode);

        enableVibrate = AppSettings.getBoolOption(activity, "enable_vibrate", true);
        resDiv = intent.getFloatExtra("res_div_float", 1.0f);
    }

    static String ReplaceDisplaySize(String test, float width, float height)
    {
        test = test.replace("$W2", Integer.toString((int) (width / 2)));
        test = test.replace("$H2", Integer.toString((int) (height / 2)));
        test = test.replace("$W3", Integer.toString((int) (width / 3)));
        test = test.replace("$H3", Integer.toString((int) (height / 3)));
        test = test.replace("$W4", Integer.toString((int) (width / 4)));
        test = test.replace("$H4", Integer.toString((int) (height / 4)));

        test = test.replace("$W", Integer.toString((int) (width)));
        test = test.replace("$H", Integer.toString((int) (height)));

        return test;
    }

    static void RunApplication(Activity activity, Intent intent, float displayWidth, float displayHeight)
    {

        {
            int fbWidth = 0;
            int fbHeight = 0;

            String frameBufferWidth = intent.getStringExtra("framebuffer_width");
            String frameBufferHeight = intent.getStringExtra("framebuffer_height");

            if (frameBufferWidth != null && frameBufferHeight != null)
            {
                frameBufferWidth = ReplaceDisplaySize(frameBufferWidth, displayWidth, displayHeight);
                frameBufferHeight = ReplaceDisplaySize(frameBufferHeight, displayWidth, displayHeight);
                try
                {
                    fbWidth = Integer.decode(frameBufferWidth);
                    fbHeight = Integer.decode(frameBufferHeight);
                } catch (Exception e)
                {

                }
            }
            NativeLib.setFramebufferSize(fbWidth, fbHeight);
        }

        String args = intent.getStringExtra("args");

        args = ReplaceDisplaySize(args, displayWidth, displayHeight);

        String[] args_array = Utils.creatArgs(args);

        String gamePath = intent.getStringExtra("game_path");

        int options = 0;
        if (TouchSettings.gamepadHidetouch)
            options |= TouchSettings.GAME_OPTION_AUTO_HIDE_GAMEPAD;

        if (TouchSettings.hideGameAndMenuTouch)
            options |= TouchSettings.GAME_OPTION_HIDE_MENU_AND_GAME;

        if (TouchSettings.useSystemKeyboard)
            options |= TouchSettings.GAME_OPTION_USE_SYSTEM_KEYBOARD;

        int gles_version = intent.getIntExtra("gles_version", 1);

        if (gles_version == 2)
            options |= TouchSettings.GAME_OPTION_GLES2;

        if (gles_version == 3)
            options |= TouchSettings.GAME_OPTION_GLES3;

        int audioBackend = intent.getIntExtra("audio_backend",0);
        if(audioBackend == 1)
            options |= TouchSettings.GAME_OPTION_SDL_OLD_AUDIO;
        else if(audioBackend == 2)
            options |= TouchSettings.GAME_OPTION_SDL_AAUDIO_AUDIO;

        if (intent.getBooleanExtra("use_gl4es", false))
            options |= TouchSettings.GAME_OPTION_GL4ES;

        int freq = intent.getIntExtra("audio_freq", 0);
        int samples = intent.getIntExtra("audio_samples", 0);

        NativeLib.audioOverride(freq, samples);

        int gameType = intent.getIntExtra("game_type", 0);
        int wheelNbr = intent.getIntExtra("wheel_nbr", 10);

        quickCommandMainPath = intent.getStringExtra("quick_command_main_path");
        quickCommandModPath = intent.getStringExtra("quick_command_mod_path");

        //NativeLib.setScreenSize(1920,1104);
        //NativeLib.setScreenSize(1280,736);
        userFiles = intent.getStringExtra("user_files");
        String logFilename = intent.getStringExtra("log_filename");
        String tmpFiles = activity.getCacheDir().getAbsolutePath();
        String sourceDir = activity.getApplicationContext().getApplicationInfo().sourceDir;
        String nativeSoPath = activity.getApplicationInfo().nativeLibraryDir;
        String pngFiles = activity.getFilesDir().getAbsolutePath();

        Utils.copyPNGAssets(activity, pngFiles);

        File folder = new File(nativeSoPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++)
        {
            if (listOfFiles[i].isFile())
            {
                Log.v(TAG, "File " + listOfFiles[i].getName());
            }
            else if (listOfFiles[i].isDirectory())
            {
                Log.v(TAG, "Directory " + listOfFiles[i].getName());
            }
        }
        Log.v(TAG, "Native .so path = " + nativeSoPath);
        Log.v(TAG, "gamePath = " + gamePath);
        Log.v(TAG, "logFilename = " + logFilename);

        int ret = NativeLib.init(pngFiles + "/", options, wheelNbr, args_array, gameType, gamePath, logFilename, nativeSoPath, userFiles, tmpFiles, sourceDir);

        Log.v(TAG, "SDL thread terminated");
        //context.finish();
    }


    static public boolean surfaceChanged(Context context, SurfaceHolder holder, int width, int height)
    {
        Log.v(TAG, "surfaceChanged: " + width + " x " + height);

        if (resDiv != 1.0 && !divDone)
        {
            holder.setFixedSize((int)(width * resDiv), (int)(height * resDiv));
            divDone = true;
            return true;
        }

        NativeLib.setScreenSize(width, height);

        controlInterp.setScreenSize((int)(width / resDiv), (int)(height / resDiv));

        gyro.reload(context);

        SDLActivity.getMotionListener().setRelativeMouseEnabled(true);

        return false;
    }

    static public boolean onTouchEvent(MotionEvent event)
    {
        return controlInterp.onTouchEvent(event);
    }

    static public boolean onKey(int keyCode, KeyEvent event)
    {

        int source = event.getSource();
        // Stop right mouse button being backbutton
        if ((source == InputDevice.SOURCE_MOUSE) || (source == InputDevice.SOURCE_MOUSE_RELATIVE))
        {
            Log.v(TAG, "SDLSurface::onKey: is mouse");
            return true;
        }

        // We always want the back button to do an escape
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                controlInterp.onBackButton();
            }
            return true;
        }


        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            return controlInterp.onKeyDown(keyCode, event);
        }
        else if (event.getAction() == KeyEvent.ACTION_UP)
        {
            return controlInterp.onKeyUp(keyCode, event);
        }

        return false;
    }

    @SuppressLint("MissingPermission")
    private static void vibrate(Context context, int duration)
    {
        if (enableVibrate)
        {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            if (v != null)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                else
                {
                    //deprecated in API 26
                    v.vibrate(duration);
                }
            }
        }
    }

    protected static final int COMMAND_SET_BACKLIGHT = 0x8001;
    protected static final int COMMAND_SHOW_GYRO_OPTIONS = 0x8002;
    protected static final int COMMAND_SHOW_KEYBOARD = 0x8003;
    protected static final int COMMAND_SHOW_GAMEPAD = 0x8004;
    protected static final int COMMAND_VIBRATE = 0x8005;
    protected static final int COMMAND_LOAD_SAVE_CONTROLS = 0x8006;
    protected static final int COMMAND_EXIT_APP = 0x8007;
    protected static final int COMMAND_SHOW_QUICK_COMMANDS = 0x8008;

    static public boolean CommandHandler(Activity activity, Message msg)
    {
        boolean handled = true;

        switch (msg.arg1)
        {
            case COMMAND_SET_BACKLIGHT:
            {
                Integer value = (Integer) msg.obj;
                Log.d(TAG, "Set backlight " + value);
                String text = "";
                float brightness = (float) value / 255.f;

                WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
                layoutParams.screenBrightness = brightness;
                activity.getWindow().setAttributes(layoutParams);

                break;
            }
            case COMMAND_SHOW_GYRO_OPTIONS:
            {
                new GyroDialog(SDLActivity.mSingleton, gyro.getRotationSensor())
                {
                    public void dismiss()
                    {
                        gyro.reload(activity);
                    }
                };
                break;
            }
            case COMMAND_SHOW_GAMEPAD:
            {
                Intent intent = new Intent(activity, GamepadActivity.class);
                intent.putExtra("app", AppInfo.app.name());
                activity.startActivity(intent);
                break;
            }
            case COMMAND_SHOW_KEYBOARD:
            {
                //showTextInput(0,0,100,10);

                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                {
                    //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                    SDLActivity.mSurface.clearFocus();
                    SDLActivity.mSurface.requestFocus();
                    //imm.showSoftInput(SDLActivity.mSurface,0);
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                }

                break;
            }
            case COMMAND_VIBRATE:
            {
                Integer value = (Integer) msg.obj;
                vibrate(activity, value);
                break;
            }
            case COMMAND_LOAD_SAVE_CONTROLS:
            {
                new TouchSettingsSaveLoad(activity, userFiles, engine);
                break;
            }
            case COMMAND_SHOW_QUICK_COMMANDS:
            {
                if(quickCommandDialog == null)
                {
                    quickCommandDialog = new QuickCommandDialog(activity, quickCommandMainPath, quickCommandModPath, input ->
                    {
                        NativeLib.executeCommand(input);
                        return null;
                    });
                }

                quickCommandDialog.show();
            break;
            }
            case COMMAND_EXIT_APP:
            {
                activity.finish();
                break;
            }
            default:
                handled = false;
        }

        return handled;
    }

    static void sendWeaponWheelSettings(Context context)
    {
        int useMoveStick = AppSettings.getBoolOption(context, "weapon_wheel_move_stick", true) ? 1 : 0;
        int mode = AppSettings.getIntOption(context, "weapon_wheel_button_mode", 0);
        int autoTimeout = AppSettings.getIntOption(context, "weapon_wheel_auto_timeout", 0);
        NativeLib.weaponWheelSettings(useMoveStick, mode, autoTimeout);
    }

}
