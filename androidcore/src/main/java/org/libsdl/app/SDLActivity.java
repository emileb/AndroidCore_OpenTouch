package org.libsdl.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.AppSettings;
import com.opentouchgaming.androidcore.AssetFileAccess;
import com.opentouchgaming.androidcore.GamepadActivity;
import com.opentouchgaming.androidcore.Utils;
import com.opentouchgaming.androidcore.controls.ControlConfig;
import com.opentouchgaming.androidcore.controls.ControlInterpreter;
import com.opentouchgaming.androidcore.controls.GamepadDefinitions;
import com.opentouchgaming.androidcore.controls.TouchSettings;
import com.opentouchgaming.androidcore.ui.GyroDialog;
import com.opentouchgaming.androidcore.ui.TouchSettingsSaveLoad;
import com.opentouchgaming.saffal.UtilsSAF;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;

import static android.content.Context.SENSOR_SERVICE;
import static android.media.AudioTrack.PLAYSTATE_PAUSED;
import static android.media.AudioTrack.PLAYSTATE_PLAYING;
import static org.libsdl.app.SDLActivity.internalFiles;

/**
 * SDL Activity
 */
public class SDLActivity extends Activity implements Handler.Callback
{
    private static final String TAG = "SDL";

    // Keep track of the paused state
    public static boolean mIsPaused, mIsSurfaceReady, mHasFocus;
    public static boolean mExitCalledFromJava;

    /**
     * If shared libraries (e.g. SDL or the native application) could not be loaded.
     */
    public static boolean mBrokenLibraries;

    public static boolean useMouse;

    public static String userFiles;

    // If we want to separate mouse and touch events.
    //  This is only toggled in native code when a hint is set!
    public static boolean mSeparateMouseAndTouch;

    public  static boolean enableVibrate;

    // Main components
    protected static SDLActivity mSingleton;
    protected static SDLSurface mSurface;
    protected static View mTextEdit;
    protected static ViewGroup mLayout;

    // This is what SDL runs in. It invokes SDL_main(), eventually
    protected static Thread mSDLThread;

    // Audio
    protected static AudioTrack mAudioTrack;
    protected static AudioRecord mAudioRecord;

    static Handler handlerUI;

    static String internalFiles;

    /**
     * This method is called by SDL before loading the native shared libraries.
     * It can be overridden to provide names of shared libraries to be loaded.
     * The default implementation returns the defaults. It never returns null.
     * An array returned by a new implementation must at least contain "SDL2".
     * Also keep in mind that the order the libraries are loaded may matter.
     *
     * @return names of shared libraries to be loaded (e.g. "SDL2", "main").
     */
    protected String[] getLibraries()
    {
        return new String[]{
                "SDL2",
                "SDL2_mixer",
                "SDL2_image",
        };
    }

    // Load the .so
    public void loadLibraries()
    {
        System.loadLibrary("core_shared");
        for (String lib : getLibraries())
        {
            System.loadLibrary(lib);
        }
    }

    /**
     * This method is called by SDL before starting the native application thread.
     * It can be overridden to provide the arguments after the application name.
     * The default implementation returns an empty array. It never returns null.
     *
     * @return arguments for the native application.
     */
    protected String[] getArguments()
    {
        return new String[0];
    }

    public static void initialize()
    {
        Log.d("SDLActivity","initialize");
        // The static nature of the singleton and Android quirkyness force us to initialize everything here
        // Otherwise, when exiting the app and returning to it, these variables *keep* their pre exit values
        mSingleton = null;
        mSurface = null;
        mTextEdit = null;
        mLayout = null;
        mSDLThread = null;
        mAudioTrack = null;
        mAudioRecord = null;
        mExitCalledFromJava = false;
        mBrokenLibraries = false;
        mIsPaused = false;
        mIsSurfaceReady = false;
        mHasFocus = true;
    }

    // Setup
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.v(TAG, "Device: " + android.os.Build.DEVICE);
        Log.v(TAG, "Model: " + android.os.Build.MODEL);
        Log.v(TAG, "onCreate(): " + mSingleton);

        super.onCreate(savedInstanceState);

        AppSettings.reloadSettings(getApplicationContext());

        AppInfo.Apps app = AppInfo.Apps.valueOf(getIntent().getStringExtra("app"));

        AppInfo.setContext(this);
        AppInfo.setApp(app);

        enableVibrate =  AppSettings.getBoolOption(this,"enable_vibrate", true);

        handlerUI = new Handler(this);

        SDLActivity.initialize();
        // So we can call stuff from static callbacks
        mSingleton = this;

        UtilsSAF.setContext(getApplicationContext());
        UtilsSAF.loadTreeRoot(getApplicationContext());

        org.fmod.FMOD.init(this);

        Log.v(TAG, "FMOD loaded");

        // Load shared libraries
        String errorMsgBrokenLib = "";
        try
        {
            //Load SDL
            loadLibraries();

            // Load game libs
            String[] loadLibs = getIntent().getStringArrayExtra("load_libs");
            for (String lib : loadLibs)
            {
                System.loadLibrary(lib);
            }

        } catch (UnsatisfiedLinkError e)
        {
            System.err.println(e.getMessage());
            mBrokenLibraries = true;
            errorMsgBrokenLib = e.getMessage();
        } catch (Exception e)
        {
            System.err.println(e.getMessage());
            mBrokenLibraries = true;
            errorMsgBrokenLib = e.getMessage();
        }
        Log.v(TAG, "Libraries loaded");

        internalFiles =  getFilesDir().getAbsolutePath();

        AssetFileAccess.setAssetManager(mSingleton.getAssets());
        Utils.copyPNGAssets(getApplicationContext(), internalFiles);
        NativeConsoleBox.init(this);

        if (mBrokenLibraries)
        {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage("An error occurred while trying to start the application. Please try again and/or reinstall."
                    + System.getProperty("line.separator")
                    + System.getProperty("line.separator")
                    + "Error: " + errorMsgBrokenLib);
            dlgAlert.setTitle("SDL Error");
            dlgAlert.setPositiveButton("Exit",
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            // if this button is clicked, close current activity
                            SDLActivity.mSingleton.finish();
                        }
                    });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();

            return;
        }

        int resDiv = getIntent().getIntExtra("res_div", 1);
        // Set up the surface
        mSurface = new SDLSurface(getApplication(), resDiv);

        useMouse =   AppSettings.getBoolOption(this, "use_mouse", true);

        userFiles =   getIntent().getStringExtra("user_files");

        // fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // keep screen on
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Utils.setImmersionMode(this);
        Utils.expandToCutout(this);

        mLayout = new RelativeLayout(this);
        //mLayout.addView(mSurface);

        setContentView(mSurface);

        // Get filename from "Open with" of another application
        Intent intent = getIntent();

        if (intent != null && intent.getData() != null)
        {
            String filename = intent.getData().getPath();
            if (filename != null)
            {
                Log.v(TAG, "Got filename: " + filename);
                SDLActivity.onNativeDropFile(filename);
            }
        }

        Log.v(TAG, "onCreate finished");
    }


    // Events
    @Override
    protected void onPause()
    {
        Log.v(TAG, "onPause()");
        super.onPause();

        if (SDLActivity.mBrokenLibraries)
        {
            return;
        }

        SDLActivity.handlePause();
    }

    @Override
    protected void onResume()
    {
        Log.v(TAG, "onResume()");
        super.onResume();

        if (SDLActivity.mBrokenLibraries)
        {
            return;
        }

        SDLActivity.handleResume();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        Log.v(TAG, "onWindowFocusChanged(): " + hasFocus);

        Utils.onWindowFocusChanged(this, hasFocus);

        if (SDLActivity.mBrokenLibraries)
        {
            return;
        }

        SDLActivity.mHasFocus = hasFocus;
        if (hasFocus)
        {
            SDLActivity.handleResume();
        }
    }

    @Override
    public void onLowMemory()
    {
        Log.v(TAG, "onLowMemory()");
        super.onLowMemory();

        if (SDLActivity.mBrokenLibraries)
        {
            return;
        }

        SDLActivity.nativeLowMemory();
    }

    @Override
    protected void onDestroy()
    {
        Log.v(TAG, "onDestroy()");

        super.onDestroy();
/*
        // Send a quit message to the application
        SDLActivity.mExitCalledFromJava = true;
        SDLActivity.nativeQuit();

        // Now wait for the SDL thread to quit
        if (SDLActivity.mSDLThread != null)
        {
            try
            {
                SDLActivity.mSDLThread.join();
            } catch (Exception e)
            {
                Log.v(TAG, "Problem stopping thread: " + e);
            }
            SDLActivity.mSDLThread = null;

            //Log.v(TAG, "Finished waiting for SDL thread");
        }
*/
        System.exit(0);
    }
/*
    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        Log.d("SDL", "dispatchKeyEvent " +  event.toString());
        if (SDLActivity.mBrokenLibraries)
        {
            return false;
        }

        int keyCode = event.getKeyCode();
        // Ignore certain special keys so they're handled by Android
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                keyCode == KeyEvent.KEYCODE_CAMERA ||
                keyCode == 168 || // API 11: KeyEvent.KEYCODE_ZOOM_IN
                keyCode == 169 // API 11: KeyEvent.KEYCODE_ZOOM_OUT
                )
        {
            // Actually want to handle volume so can be mapped
            //return false;
        }
        return super.dispatchKeyEvent(event);
    }
*/
    /**
     * Called by onPause or surfaceDestroyed. Even if surfaceDestroyed
     * is the first to be called, mIsSurfaceReady should still be set
     * to 'true' during the call to onPause (in a usual scenario).
     */
    public static void handlePause()
    {
        if (!SDLActivity.mIsPaused && SDLActivity.mIsSurfaceReady)
        {
            SDLActivity.mIsPaused = true;
            SDLActivity.nativePause();
            mSurface.handlePause();
        }

        if (mAudioTrack != null)
        {
            if( mAudioTrack.getPlayState() == PLAYSTATE_PLAYING)
            {
                Log.d("mAudioTrack","Pausing");
                mAudioTrack.pause();
            }
        }
    }

    /**
     * Called by onResume or surfaceCreated. An actual resume should be done only when the surface is ready.
     * Note: Some Android variants may send multiple surfaceChanged events, so we don't need to resume
     * every time we get one of those events, only if it comes after surfaceDestroyed
     */
    public static void handleResume()
    {
        if (SDLActivity.mIsPaused && SDLActivity.mIsSurfaceReady && SDLActivity.mHasFocus)
        {
            SDLActivity.mIsPaused = false;
            SDLActivity.nativeResume();
            mSurface.handleResume();
        }
        if (mAudioTrack != null)
        {
            if( mAudioTrack.getPlayState() == PLAYSTATE_PAUSED)
            {
                Log.d("mAudioTrack","Resuming");
                mAudioTrack.play();
            }
        }
    }

    /* The native thread has finished */
    public static void handleNativeExit()
    {
        Log.d("SDL", "handleNativeExit");
        SDLActivity.mSDLThread = null;
        mSingleton.finish();
    }


    // Messages from the SDLMain thread
    static final int COMMAND_CHANGE_TITLE = 1;
    static final int COMMAND_UNUSED = 2;
    static final int COMMAND_TEXTEDIT_HIDE = 3;
    static final int COMMAND_SET_KEEP_SCREEN_ON = 5;

    protected static final int COMMAND_USER = 0x8000;
    protected static final int COMMAND_SET_BACKLIGHT = 0x8001;
    protected static final int COMMAND_SHOW_GYRO_OPTIONS = 0x8002;
    protected static final int COMMAND_SHOW_KEYBOARD = 0x8003;
    protected static final int COMMAND_SHOW_GAMEPAD = 0x8004;
    protected static final int COMMAND_VIBRATE = 0x8005;
    protected static final int COMMAND_LOAD_SAVE_CONTROLS = 0x8006;
    protected static final int COMMAND_EXIT_APP = 0x8007;


    /**
     * This method is called by SDL if SDL did not handle a message itself.
     * This happens if a received message contains an unsupported command.
     * Method can be overwritten to handle Messages in a different class.
     *
     * @param command the command of the message.
     * @param param   the parameter of the message. May be null.
     * @return if the message was handled in overridden method.
     */
    protected boolean onUnhandledMessage(int command, Object param)
    {
        return false;
    }

    @Override
    public boolean handleMessage(Message msg)
    {
        return false;
    }

    private static void vibrate( int duration)
    {
        if( enableVibrate ) {
            Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            if (v != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    v.vibrate(duration);
                }
            }
        }
    }
    /**
     * A Handler class for Messages from native SDL applications.
     * It uses current Activities as target (e.g. for the title).
     * static to prevent implicit references to enclosing object.
     */
    protected static class SDLCommandHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            Context context = getContext();
            if (context == null)
            {
                Log.e(TAG, "error handling message, getContext() returned null");
                return;
            }
            switch (msg.arg1)
            {
                case COMMAND_CHANGE_TITLE:
                    if (context instanceof Activity)
                    {
                        ((Activity) context).setTitle((String) msg.obj);
                    } else
                    {
                        Log.e(TAG, "error handling message, getContext() returned no Activity");
                    }
                    break;
                case COMMAND_TEXTEDIT_HIDE:
                    if (mTextEdit != null)
                    {
                        // Note: On some devices setting view to GONE creates a flicker in landscape.
                        // Setting the View's sizes to 0 is similar to GONE but without the flicker.
                        // The sizes will be set to useful values when the keyboard is shown again.
                        mTextEdit.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mTextEdit.getWindowToken(), 0);
                    }
                    break;
                case COMMAND_SET_KEEP_SCREEN_ON:
                {
                    Window window = ((Activity) context).getWindow();
                    if (window != null)
                    {
                        if ((msg.obj instanceof Integer) && (((Integer) msg.obj).intValue() != 0))
                        {
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        } else
                        {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                    }
                    break;
                }
                case COMMAND_SET_BACKLIGHT:
                {
                    Integer value = (Integer) msg.obj;
                    Log.d(TAG, "Set backlight " + value);
                    String text = "";
                    float brightness = (float) value / 255.f;

                    //Toast.makeText(SDLActivity.getContext(), text, Toast.LENGTH_SHORT).show();

                    //nt curBrightnessValue = android.provider.Settings.System.getInt(SDLActivity.getContext().getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
                    WindowManager.LayoutParams layoutParams = SDLActivity.mSingleton.getWindow().getAttributes();
                    layoutParams.screenBrightness = brightness;
                    SDLActivity.mSingleton.getWindow().setAttributes(layoutParams);

                    break;
                }
                case COMMAND_SHOW_GYRO_OPTIONS:
                {
                    new GyroDialog(SDLActivity.mSingleton, SDLActivity.mSurface.getRotationSensor()){
                        public void dismiss()
                        {
                            SDLActivity.mSurface.setGyroMode();
                        }
                    };

                    break;
                }
                case COMMAND_SHOW_GAMEPAD:
                {
                    Intent intent = new Intent(getContext(), GamepadActivity.class);
                    intent.putExtra("app", AppInfo.app.name());
                    SDLActivity.mSingleton.startActivity(intent);
                    break;
                }
                case COMMAND_SHOW_KEYBOARD:
                {
                    //showTextInput(0,0,100,10);

                    InputMethodManager imm = (InputMethodManager)
                            SDLActivity.mSingleton.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null){
                        //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                        SDLActivity.mSurface.clearFocus();
                        SDLActivity.mSurface.requestFocus();
                        //imm.showSoftInput(SDLActivity.mSurface,0);
                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
                    }

                    break;
                }
                case COMMAND_VIBRATE:
                {
                    Integer value = (Integer) msg.obj;
                    vibrate(value);
                    break;
                }
                case COMMAND_LOAD_SAVE_CONTROLS: {
                    new TouchSettingsSaveLoad(SDLActivity.mSingleton, SDLActivity.userFiles, SDLActivity.mSurface.engine);
                    break;
                }
                case COMMAND_EXIT_APP:
                {
                    SDLActivity.mSingleton.finish();
                    break;
                }
                default:
                    if ((context instanceof SDLActivity) && !((SDLActivity) context).onUnhandledMessage(msg.arg1, msg.obj))
                    {
                        Log.e(TAG, "error handling message, command is " + msg.arg1);
                    }
            }
        }
    }

    // Handler for the messages
    Handler commandHandler = new SDLCommandHandler();

    // Send a message from the SDLMain thread
    boolean sendCommand(int command, Object data)
    {
        Message msg = commandHandler.obtainMessage();
        msg.arg1 = command;
        msg.obj = data;
        return commandHandler.sendMessage(msg);
    }

    // C functions we call
    public static native int nativeInit(Object arguments);

    public static native void nativeLowMemory();

    public static native void nativeQuit();

    public static native void nativePause();

    public static native void nativeResume();

    public static native void onNativeDropFile(String filename);

    public static native void onNativeResize(int x, int y, int format, float rate);

    public static native int onNativePadDown(int device_id, int keycode);

    public static native int onNativePadUp(int device_id, int keycode);

    public static native void onNativeJoy(int device_id, int axis,
                                          float value);

    public static native void onNativeHat(int device_id, int hat_id,
                                          int x, int y);

    public static native void onNativeKeyDown(int keycode,int unicode);

    public static native void onNativeKeyUp(int keycode);

    public static native void onNativeKeyboardFocusLost();

    public static native void onNativeMouse(int button, int action, float x, float y);

    public static native void onNativeTouch(int touchDevId, int pointerFingerId,
                                            int action, float x,
                                            float y, float p);

    public static native void onNativeAccel(float x, float y, float z);

    public static native void onNativeSurfaceChanged();

    public static native void onNativeSurfaceDestroyed();

    public static native int nativeAddJoystick(int device_id, String name,
                                               int is_accelerometer, int nbuttons,
                                               int naxes, int nhats, int nballs);

    public static native int nativeRemoveJoystick(int device_id);

    public static native String nativeGetHint(String name);

    /**
     * This method is called by SDL using JNI.
     */
    public static boolean setActivityTitle(String title)
    {
        // Called from SDLMain() thread and can't directly affect the view
        //return mSingleton.sendCommand(COMMAND_CHANGE_TITLE, title);
        return true;
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static boolean sendMessage(int command, int param)
    {
        return mSingleton.sendCommand(command, Integer.valueOf(param));
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static Context getContext()
    {
        return mSingleton;
    }

    /**
     * This method is called by SDL using JNI.
     *
     * @return result of getSystemService(name) but executed on UI thread.
     */
    public Object getSystemServiceFromUiThread(final String name)
    {
        final Object lock = new Object();
        final Object[] results = new Object[2]; // array for writable variables
        synchronized (lock)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    synchronized (lock)
                    {
                        results[0] = getSystemService(name);
                        results[1] = Boolean.TRUE;
                        lock.notify();
                    }
                }
            });
            if (results[1] == null)
            {
                try
                {
                    lock.wait();
                } catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        return results[0];
    }

    static class ShowTextInputTask implements Runnable
    {
        /*
         * This is used to regulate the pan&scan method to have some offset from
         * the bottom edge of the input region and the top edge of an input
         * method (soft keyboard)
         */
        static final int HEIGHT_PADDING = 15;

        public int x, y, w, h;

        public ShowTextInputTask(int x, int y, int w, int h)
        {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override
        public void run()
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h + HEIGHT_PADDING);
            params.leftMargin = x;
            params.topMargin = y;

            if (mTextEdit == null)
            {
                mTextEdit = new DummyEdit(getContext());

                mLayout.addView(mTextEdit, params);
            } else
            {
                mTextEdit.setLayoutParams(params);
            }

            mTextEdit.setVisibility(View.VISIBLE);
            mTextEdit.requestFocus();

            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mTextEdit, InputMethodManager.SHOW_FORCED);
        }
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static boolean showTextInput(int x, int y, int w, int h)
    {
        // Transfer the task to the main thread as a Runnable
        return mSingleton.commandHandler.post(new ShowTextInputTask(x, y, w, h));
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static Surface getNativeSurface()
    {
        return SDLActivity.mSurface.getNativeSurface();
    }

    // Audio

    /**
     * This method is called by SDL using JNI.
     */
    public static int audioOpen(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames)
    {
        int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
        int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

        int nativeRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
        Log.v(TAG, "SDL nativeRate = " + nativeRate);

        Log.v(TAG, "SDL audio: wanted " + (isStereo ? "stereo" : "mono") + " " + (is16Bit ? "16-bit" : "8-bit") + " " + (sampleRate / 1000f) + "kHz, " + desiredFrames + " frames buffer");

        // Let the user pick a larger buffer if they really want -- but ye
        // gods they probably shouldn't, the minimums are horrifyingly high
        // latency already
        desiredFrames = Math.max(desiredFrames, (AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) + frameSize - 1) / frameSize);

        if (mAudioTrack == null)
        {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                   channelConfig, audioFormat, desiredFrames * frameSize, AudioTrack.MODE_STREAM);
/*
            AudioFormat myFormat = new AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(audioFormat)
                    .build();

            mAudioTrack = new AudioTrack(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    myFormat, desiredFrames * frameSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
*/
            // Instantiating AudioTrack can "succeed" without an exception and the track may still be invalid
            // Ref: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/media/java/android/media/AudioTrack.java
            // Ref: http://developer.android.com/reference/android/media/AudioTrack.html#getState()

            if (mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED)
            {
                Log.e(TAG, "Failed during initialization of Audio Track");
                mAudioTrack = null;
                return -1;
            }

            mAudioTrack.play();
        }

        Log.v(TAG, "SDL audio: got " + ((mAudioTrack.getChannelCount() >= 2) ? "stereo" : "mono") + " " + ((mAudioTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit" : "8-bit") + " " + (mAudioTrack.getSampleRate() / 1000f) + "kHz, " + desiredFrames + " frames buffer");

        return 0;
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void audioWriteShortBuffer(short[] buffer)
    {
        //Log.w(TAG, "audioWriteShortBuffer " +  buffer.length);
        for (int i = 0; i < buffer.length; )
        {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            //Log.w(TAG, "result " + result);
            if (result > 0)
            {
                i += result;
            } else if (result == 0)
            {
                try
                {
                    Thread.sleep(10);
                } catch (InterruptedException e)
                {
                    // Nom nom
                }
            } else
            {
                Log.w(TAG, "SDL audio: error return from write(short)");
                return;
            }
        }
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void audioWriteByteBuffer(byte[] buffer)
    {
        for (int i = 0; i < buffer.length; )
        {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0)
            {
                i += result;
            } else if (result == 0)
            {
                try
                {
                    Thread.sleep(1);
                } catch (InterruptedException e)
                {
                    // Nom nom
                }
            } else
            {
                Log.w(TAG, "SDL audio: error return from write(byte)");
                return;
            }
        }
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static int captureOpen(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames)
    {
        int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
        int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

        Log.v(TAG, "SDL capture: wanted " + (isStereo ? "stereo" : "mono") + " " + (is16Bit ? "16-bit" : "8-bit") + " " + (sampleRate / 1000f) + "kHz, " + desiredFrames + " frames buffer");

        // Let the user pick a larger buffer if they really want -- but ye
        // gods they probably shouldn't, the minimums are horrifyingly high
        // latency already
        desiredFrames = Math.max(desiredFrames, (AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) + frameSize - 1) / frameSize);

        if (mAudioRecord == null)
        {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate,
                    channelConfig, audioFormat, desiredFrames * frameSize);

            // see notes about AudioTrack state in audioOpen(), above. Probably also applies here.
            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED)
            {
                Log.e(TAG, "Failed during initialization of AudioRecord");
                mAudioRecord.release();
                mAudioRecord = null;
                return -1;
            }

            mAudioRecord.startRecording();
        }

        Log.v(TAG, "SDL capture: got " + ((mAudioRecord.getChannelCount() >= 2) ? "stereo" : "mono") + " " + ((mAudioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit" : "8-bit") + " " + (mAudioRecord.getSampleRate() / 1000f) + "kHz, " + desiredFrames + " frames buffer");

        return 0;
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static int captureReadShortBuffer(short[] buffer, boolean blocking)
    {
        // !!! FIXME: this is available in API Level 23. Until then, we always block.  :(
        //return mAudioRecord.read(buffer, 0, buffer.length, blocking ? AudioRecord.READ_BLOCKING : AudioRecord.READ_NON_BLOCKING);
        return mAudioRecord.read(buffer, 0, buffer.length);
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static int captureReadByteBuffer(byte[] buffer, boolean blocking)
    {
        // !!! FIXME: this is available in API Level 23. Until then, we always block.  :(
        //return mAudioRecord.read(buffer, 0, buffer.length, blocking ? AudioRecord.READ_BLOCKING : AudioRecord.READ_NON_BLOCKING);
        return mAudioRecord.read(buffer, 0, buffer.length);
    }


    /**
     * This method is called by SDL using JNI.
     */
    public static void audioClose()
    {
        if (mAudioTrack != null)
        {
            Log.d("mAudioTrack","Closing");
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void captureClose()
    {
        if (mAudioRecord != null)
        {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }


    // Input

    /**
     * This method is called by SDL using JNI.
     *
     * @return an array which may be empty but is never null.
     */
    public static int[] inputGetInputDeviceIds(int sources)
    {
        int[] ids = InputDevice.getDeviceIds();
        int[] filtered = new int[ids.length];
        int used = 0;
        for (int i = 0; i < ids.length; ++i)
        {
            InputDevice device = InputDevice.getDevice(ids[i]);
            if ((device != null) && ((device.getSources() & sources) != 0))
            {
                filtered[used++] = device.getId();
            }
        }
        return Arrays.copyOf(filtered, used);
    }


    /**
     * This method is called by SDL using JNI.
     */
    public static void pollInputDevices()
    {

    }

    // Check if a given device is considered a possible SDL joystick
    public static boolean isDeviceSDLJoystick(int deviceId)
    {
        InputDevice device = InputDevice.getDevice(deviceId);
        // We cannot use InputDevice.isVirtual before API 16, so let's accept
        // only nonnegative device ids (VIRTUAL_KEYBOARD equals -1)
        if ((device == null) || (deviceId < 0))
        {
            return false;
        }
        int sources = device.getSources();
        return (((sources & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK) ||
                ((sources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) ||
                ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
        );
    }

    // APK expansion files support

    /**
     * com.android.vending.expansion.zipfile.ZipResourceFile object or null.
     */
    private Object expansionFile;

    /**
     * com.android.vending.expansion.zipfile.ZipResourceFile's getInputStream() or null.
     */
    private Method expansionFileMethod;

    /**
     * This method is called by SDL using JNI.
     *
     * @return an InputStream on success or null if no expansion file was used.
     * @throws IOException on errors. Message is set for the SDL error message.
     */
    public InputStream openAPKExpansionInputStream(String fileName) throws IOException
    {
        // Get a ZipResourceFile representing a merger of both the main and patch files
        if (expansionFile == null)
        {
            String mainHint = nativeGetHint("SDL_ANDROID_APK_EXPANSION_MAIN_FILE_VERSION");
            if (mainHint == null)
            {
                return null; // no expansion use if no main version was set
            }
            String patchHint = nativeGetHint("SDL_ANDROID_APK_EXPANSION_PATCH_FILE_VERSION");
            if (patchHint == null)
            {
                return null; // no expansion use if no patch version was set
            }

            Integer mainVersion;
            Integer patchVersion;
            try
            {
                mainVersion = Integer.valueOf(mainHint);
                patchVersion = Integer.valueOf(patchHint);
            } catch (NumberFormatException ex)
            {
                ex.printStackTrace();
                throw new IOException("No valid file versions set for APK expansion files", ex);
            }

            try
            {
                // To avoid direct dependency on Google APK expansion library that is
                // not a part of Android SDK we access it using reflection
                expansionFile = Class.forName("com.android.vending.expansion.zipfile.APKExpansionSupport")
                        .getMethod("getAPKExpansionZipFile", Context.class, int.class, int.class)
                        .invoke(null, this, mainVersion, patchVersion);

                expansionFileMethod = expansionFile.getClass()
                        .getMethod("getInputStream", String.class);
            } catch (Exception ex)
            {
                ex.printStackTrace();
                expansionFile = null;
                expansionFileMethod = null;
                throw new IOException("Could not access APK expansion support library", ex);
            }
        }

        // Get an input stream for a known file inside the expansion file ZIPs
        InputStream fileStream;
        try
        {
            fileStream = (InputStream) expansionFileMethod.invoke(expansionFile, fileName);
        } catch (Exception ex)
        {
            // calling "getInputStream" failed
            ex.printStackTrace();
            throw new IOException("Could not open stream from APK expansion file", ex);
        }

        if (fileStream == null)
        {
            // calling "getInputStream" was successful but null was returned
            throw new IOException("Could not find path in APK expansion file");
        }

        return fileStream;
    }

    // Messagebox

    /**
     * Result of current messagebox. Also used for blocking the calling thread.
     */
    protected final int[] messageboxSelection = new int[1];

    /**
     * Id of current dialog.
     */
    protected int dialogs = 0;

    /**
     * This method is called by SDL using JNI.
     * Shows the messagebox from UI thread and block calling thread.
     * buttonFlags, buttonIds and buttonTexts must have same length.
     *
     * @param buttonFlags array containing flags for every button.
     * @param buttonIds   array containing id for every button.
     * @param buttonTexts array containing text for every button.
     * @param colors      null for default or array of length 5 containing colors.
     * @return button id or -1.
     */
    public int messageboxShowMessageBox(
            final int flags,
            final String title,
            final String message,
            final int[] buttonFlags,
            final int[] buttonIds,
            final String[] buttonTexts,
            final int[] colors)
    {

        messageboxSelection[0] = -1;

        // sanity checks

        if ((buttonFlags.length != buttonIds.length) && (buttonIds.length != buttonTexts.length))
        {
            return -1; // implementation broken
        }

        // collect arguments for Dialog

        final Bundle args = new Bundle();
        args.putInt("flags", flags);
        args.putString("title", title);
        args.putString("message", message);
        args.putIntArray("buttonFlags", buttonFlags);
        args.putIntArray("buttonIds", buttonIds);
        args.putStringArray("buttonTexts", buttonTexts);
        args.putIntArray("colors", colors);

        // trigger Dialog creation on UI thread

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                showDialog(dialogs++, args);
            }
        });

        // block the calling thread

        synchronized (messageboxSelection)
        {
            try
            {
                messageboxSelection.wait();
            } catch (InterruptedException ex)
            {
                ex.printStackTrace();
                return -1;
            }
        }

        // return selected value

        return messageboxSelection[0];
    }

    @Override
    protected Dialog onCreateDialog(int ignore, Bundle args)
    {

        // TODO set values from "flags" to messagebox dialog

        // get colors

        int[] colors = args.getIntArray("colors");
        int backgroundColor;
        int textColor;
        int buttonBorderColor;
        int buttonBackgroundColor;
        int buttonSelectedColor;
        if (colors != null)
        {
            int i = -1;
            backgroundColor = colors[++i];
            textColor = colors[++i];
            buttonBorderColor = colors[++i];
            buttonBackgroundColor = colors[++i];
            buttonSelectedColor = colors[++i];
        } else
        {
            backgroundColor = Color.TRANSPARENT;
            textColor = Color.TRANSPARENT;
            buttonBorderColor = Color.TRANSPARENT;
            buttonBackgroundColor = Color.TRANSPARENT;
            buttonSelectedColor = Color.TRANSPARENT;
        }

        // create dialog with title and a listener to wake up calling thread

        final Dialog dialog = new Dialog(this);
        dialog.setTitle(args.getString("title"));
        dialog.setCancelable(false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface unused)
            {
                synchronized (messageboxSelection)
                {
                    messageboxSelection.notify();
                }
            }
        });

        // create text

        TextView message = new TextView(this);
        message.setGravity(Gravity.CENTER);
        message.setText(args.getString("message"));
        if (textColor != Color.TRANSPARENT)
        {
            message.setTextColor(textColor);
        }

        // create buttons

        int[] buttonFlags = args.getIntArray("buttonFlags");
        int[] buttonIds = args.getIntArray("buttonIds");
        String[] buttonTexts = args.getStringArray("buttonTexts");

        final SparseArray<Button> mapping = new SparseArray<Button>();

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        for (int i = 0; i < buttonTexts.length; ++i)
        {
            Button button = new Button(this);
            final int id = buttonIds[i];
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    messageboxSelection[0] = id;
                    dialog.dismiss();
                }
            });
            if (buttonFlags[i] != 0)
            {
                // see SDL_messagebox.h
                if ((buttonFlags[i] & 0x00000001) != 0)
                {
                    mapping.put(KeyEvent.KEYCODE_ENTER, button);
                }
                if ((buttonFlags[i] & 0x00000002) != 0)
                {
                    mapping.put(111, button); /* API 11: KeyEvent.KEYCODE_ESCAPE */
                }
            }
            button.setText(buttonTexts[i]);
            if (textColor != Color.TRANSPARENT)
            {
                button.setTextColor(textColor);
            }
            if (buttonBorderColor != Color.TRANSPARENT)
            {
                // TODO set color for border of messagebox button
            }
            if (buttonBackgroundColor != Color.TRANSPARENT)
            {
                Drawable drawable = button.getBackground();
                if (drawable == null)
                {
                    // setting the color this way removes the style
                    button.setBackgroundColor(buttonBackgroundColor);
                } else
                {
                    // setting the color this way keeps the style (gradient, padding, etc.)
                    drawable.setColorFilter(buttonBackgroundColor, PorterDuff.Mode.MULTIPLY);
                }
            }
            if (buttonSelectedColor != Color.TRANSPARENT)
            {
                // TODO set color for selected messagebox button
            }
            buttons.addView(button);
        }

        // create content

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.addView(message);
        content.addView(buttons);
        if (backgroundColor != Color.TRANSPARENT)
        {
            content.setBackgroundColor(backgroundColor);
        }

        // add content to dialog and return

        dialog.setContentView(content);
        dialog.setOnKeyListener(new Dialog.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface d, int keyCode, KeyEvent event)
            {
                Button button = mapping.get(keyCode);
                if (button != null)
                {
                    if (event.getAction() == KeyEvent.ACTION_UP)
                    {
                        button.performClick();
                    }
                    return true; // also for ignored actions
                }
                return false;
            }
        });

        return dialog;
    }
}

/**
 * Simple nativeInit() runnable
 */
class SDLMain implements Runnable
{
    @Override
    public void run()
    {
        // Runs SDL_main()
        SDLActivity.nativeInit(SDLActivity.mSingleton.getArguments());

        String args = SDLActivity.mSingleton.getIntent().getStringExtra("args");

        args = args.replace("$W2", Integer.toString(SDLSurface.mWidth/2));
        args = args.replace("$H2", Integer.toString(SDLSurface.mHeight/2));
        args = args.replace("$W3", Integer.toString(SDLSurface.mWidth/3));
        args = args.replace("$H3", Integer.toString(SDLSurface.mHeight/3));
        args = args.replace("$W4", Integer.toString(SDLSurface.mWidth/4));
        args = args.replace("$H4", Integer.toString(SDLSurface.mHeight/4));

        args = args.replace("$W", Integer.toString(SDLSurface.mWidth));
        args = args.replace("$H", Integer.toString(SDLSurface.mHeight));

        String[] args_array = Utils.creatArgs(args);

        String gamePath = SDLActivity.mSingleton.getIntent().getStringExtra("game_path");

        int options = 0;
        if (TouchSettings.gamepadHidetouch)
            options |= TouchSettings.GAME_OPTION_AUTO_HIDE_GAMEPAD;

        if (TouchSettings.hideGameAndMenuTouch)
            options |= TouchSettings.GAME_OPTION_HIDE_MENU_AND_GAME;

        if (TouchSettings.useSystemKeyboard)
            options |= TouchSettings.GAME_OPTION_USE_SYSTEM_KEYBOARD;

        int gles_version = SDLActivity.mSingleton.getIntent().getIntExtra("gles_version", 1);

        if( gles_version == 2 )
            options |= TouchSettings.GAME_OPTION_GLES2;

        if( gles_version == 3 )
            options |= TouchSettings.GAME_OPTION_GLES3;

        int freq =  SDLActivity.mSingleton.getIntent().getIntExtra("audio_freq", 0);
        int samples = SDLActivity.mSingleton.getIntent().getIntExtra("audio_samples", 0);

        NativeLib.audioOverride(freq,samples);

        int gameType = SDLActivity.mSingleton.getIntent().getIntExtra("game_type", 0);
        int wheelNbr = SDLActivity.mSingleton.getIntent().getIntExtra("wheel_nbr", 10);

        //NativeLib.setScreenSize(1920,1104);
        //NativeLib.setScreenSize(1280,736);
        String logFilename = SDLActivity.mSingleton.getIntent().getStringExtra("log_filename");
        String userFiles = SDLActivity.mSingleton.getIntent().getStringExtra("user_files");
        String tmpFiles = SDLActivity.mSingleton.getCacheDir().getAbsolutePath();
        String sourceDir = SDLActivity.mSingleton.getApplicationContext().getApplicationInfo().sourceDir;

        String nativeSoPath = SDLActivity.mSingleton.getApplicationInfo().nativeLibraryDir;

        File folder = new File(nativeSoPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                Log.v("SDL","File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                Log.v("SDL","Directory " + listOfFiles[i].getName());
            }
        }
        Log.v("SDL", "Native .so path = " + nativeSoPath);
        Log.v("SDL", "gamePath = " + gamePath);
        Log.v("SDL", "logFilename = " + logFilename);

        int ret = NativeLib.init(internalFiles + "/", options, wheelNbr, args_array, gameType, gamePath, logFilename, nativeSoPath, userFiles, tmpFiles, sourceDir);

        Log.v("SDL", "SDL thread terminated");
        SDLActivity.mSingleton.finish();
    }
}


/**
 * SDLSurface. This is what we draw on, so we need to know when it's created
 * in order to do anything useful.
 * <p>
 * Because of this, that's where we set up the SDL thread
 */
class SDLSurface extends SurfaceView implements SurfaceHolder.Callback,
        View.OnKeyListener, View.OnTouchListener, SensorEventListener
{

    int resDiv = 1;
    boolean divDone = false;

    float gyroXSens = 1;
    float gyroYSens = 1;

    boolean gyroInvertX = false;
    boolean gyroInvertY = false;
    boolean gyroSwapXY = false;

    float gyroXOffset = 0;
    float gyroYOffset = 0;

    // Sensors
    protected static SensorManager mSensorManager;
    protected static Display mDisplay;

    // Keep track of the surface size to normalize touch events
    protected static int mWidth, mHeight;

    private ControlInterpreter controlInterp;
    NativeLib engine;
    // Startup
    public SDLSurface(Context context, int div)
    {
        super(context);

        getHolder().addCallback(this);

        resDiv = div;

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(this);

        mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);


        // Some arbitrary defaults to avoid a potential division by zero
        mWidth = 1;
        mHeight = 1;
    }

    // Sensor events
    private void enableSensor( Sensor sensor , boolean enabled)
    {
        if (enabled)
        {
            //Sensor sensor = mSensorManager.getDefaultSensor(sensortype);
            if( sensor != null )
            {
                Log.d("sen","sensor is: " + sensor.getName());
            }
            else
            {
                Log.d("sen","sensor is NULL!");
            }
            mSensorManager.registerListener(SDLActivity.mSurface,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME);

        } else
        {
            mSensorManager.unregisterListener(SDLActivity.mSurface,
                    sensor);
        }
    }

    Sensor getRotationSensor()
    {
        Sensor ret = null;
        /*
        // List of Sensors Available
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_GAME_ROTATION_VECTOR);

        for (Sensor s : sensorList)
        {
            Log.v("SDL", "Game Rotation sensor: " + s.getName());
        }

        if (sensorList.size() > 0)
        {
            ret = sensorList.get(0);
        } else
        {
            sensorList = mSensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);

            for (Sensor s : sensorList)
            {
                Log.v("SDL", "Rotation sensor: " + s.getName());
            }

            if (sensorList.size() > 0)
            {
                ret = sensorList.get(0);
            }
        }

        // Must have a gyro to be good enough
        if( mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null )
        {
            ret = null;
        }
        */
        ret = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        return ret;
    }

    public void setGyroMode()
    {
        boolean enableGyro = AppSettings.getBoolOption(SDLActivity.mSingleton,"gyro_enable", false);

        gyroXSens = AppSettings.getFloatOption(SDLActivity.mSingleton,"gyro_x_sens",1);
        gyroYSens = AppSettings.getFloatOption(SDLActivity.mSingleton,"gyro_y_sens",1);
        gyroInvertX = AppSettings.getBoolOption(SDLActivity.mSingleton,"gyro_invert_x", false);
        gyroInvertY = AppSettings.getBoolOption(SDLActivity.mSingleton,"gyro_invert_y", false);
        gyroSwapXY = AppSettings.getBoolOption(SDLActivity.mSingleton,"gyro_swap_xy", false);
        gyroXOffset = AppSettings.getFloatOption(SDLActivity.mSingleton,"gyro_x_offset",0);
        gyroYOffset = AppSettings.getFloatOption(SDLActivity.mSingleton,"gyro_y_offset",0);


        mSensorManager = (SensorManager) SDLActivity.mSingleton.getSystemService(SENSOR_SERVICE);

        Sensor sensor = getRotationSensor();

        if( sensor != null )
        {
            enableSensor(sensor, enableGyro);
        }
    }

    public void handlePause()
    {

    }

    public void handleResume()
    {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(this);
        if( controlInterp != null )
        {
            controlInterp.loadGameControlsFile();
        }
        sendWeaponWheelSettings();
    }

    void sendWeaponWheelSettings()
    {
        int useMoveStick = AppSettings.getBoolOption(getContext(), "weapon_wheel_move_stick", true)? 1 : 0;
        int mode = AppSettings.getIntOption(getContext(), "weapon_wheel_button_mode", 0);
        int autoTimeout = AppSettings.getIntOption(getContext(), "weapon_wheel_auto_timeout", 0);
        NativeLib.weaponWheelSettings(useMoveStick,mode,autoTimeout);
    }

    public Surface getNativeSurface()
    {
        return getHolder().getSurface();
    }

    // Called when we have a valid drawing surface
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.v("SDL", "surfaceCreated()");
        holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
    }

    // Called when we lose the surface
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.v("SDL", "surfaceDestroyed()");
        // Call this *before* setting mIsSurfaceReady to 'false'
        SDLActivity.handlePause();
        SDLActivity.mIsSurfaceReady = false;
        SDLActivity.onNativeSurfaceDestroyed();
    }


    // Called when the surface is resized
    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height)
    {
        Log.v("SDL", "surfaceChanged() w=" + width + " h=" + height);

        if (SDLActivity.useMouse)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                requestPointerCapture();
            }
        }

        mWidth = width;
        mHeight = height;

        if (resDiv != 1 && !divDone)
        {
            getHolder().setFixedSize(mWidth / resDiv, mHeight / resDiv);
            //getHolder().setFixedSize(640, 480);
            divDone = true;
            return;
        }

        NativeLib.setScreenSize(width, height);

        format = PixelFormat.RGBA_8888;

        int sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565 by default
        switch (format)
        {
            case PixelFormat.A_8:
                Log.v("SDL", "pixel format A_8");
                break;
            case PixelFormat.LA_88:
                Log.v("SDL", "pixel format LA_88");
                break;
            case PixelFormat.L_8:
                Log.v("SDL", "pixel format L_8");
                break;
            case PixelFormat.RGBA_4444:
                Log.v("SDL", "pixel format RGBA_4444");
                sdlFormat = 0x15421002; // SDL_PIXELFORMAT_RGBA4444
                break;
            case PixelFormat.RGBA_5551:
                Log.v("SDL", "pixel format RGBA_5551");
                sdlFormat = 0x15441002; // SDL_PIXELFORMAT_RGBA5551
                break;
            case PixelFormat.RGBA_8888:
                Log.v("SDL", "pixel format RGBA_8888");
                sdlFormat = 0x16462004; // SDL_PIXELFORMAT_RGBA8888
                break;
            case PixelFormat.RGBX_8888:
                Log.v("SDL", "pixel format RGBX_8888");
                sdlFormat = 0x16261804; // SDL_PIXELFORMAT_RGBX8888
                break;
            case PixelFormat.RGB_332:
                Log.v("SDL", "pixel format RGB_332");
                sdlFormat = 0x14110801; // SDL_PIXELFORMAT_RGB332
                break;
            case PixelFormat.RGB_565:
                Log.v("SDL", "pixel format RGB_565");
                sdlFormat = 0x15151002; // SDL_PIXELFORMAT_RGB565
                break;
            case PixelFormat.RGB_888:
                Log.v("SDL", "pixel format RGB_888");
                // Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
                sdlFormat = 0x16161804; // SDL_PIXELFORMAT_RGB888
                break;
            default:
                Log.v("SDL", "pixel format unknown " + format);
                break;
        }

        engine = new NativeLib();

        controlInterp = new ControlInterpreter(getContext(), engine, GamepadDefinitions.getDefinition(AppInfo.app), TouchSettings.gamePadEnabled, TouchSettings.altTouchCode);

        controlInterp.setScreenSize(width * resDiv, height * resDiv);

        SDLActivity.onNativeResize(width, height, sdlFormat, mDisplay.getRefreshRate());
        Log.v("SDL", "Window size: " + width + "x" + height);

        setGyroMode();

        boolean skip = false;
        int requestedOrientation = SDLActivity.mSingleton.getRequestedOrientation();

        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        {
            // Accept any
        } else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        {
            if (mWidth > mHeight)
            {
                skip = true;
            }
        } else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        {
            if (mWidth < mHeight)
            {
                skip = true;
            }
        }

        // Special Patch for Square Resolution: Black Berry Passport
        if (skip)
        {
            double min = Math.min(mWidth, mHeight);
            double max = Math.max(mWidth, mHeight);

            if (max / min < 1.20)
            {
                Log.v("SDL", "Don't skip on such aspect-ratio. Could be a square resolution.");
                skip = false;
            }
        }

        if (skip)
        {
            Log.v("SDL", "Skip .. Surface is not ready.");
            return;
        }


        // Set mIsSurfaceReady to 'true' *before* making a call to handleResume
        SDLActivity.mIsSurfaceReady = true;
        SDLActivity.onNativeSurfaceChanged();


        if (SDLActivity.mSDLThread == null)
        {
            // This is the entry point to the C app.
            // Start up the C app thread and enable sensor input for the first time

            final Thread sdlThread = new Thread(new SDLMain(), "SDLThread");

            sdlThread.start();

            // Set up a listener thread to catch when the native thread ends
            SDLActivity.mSDLThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        sdlThread.join();
                    } catch (Exception e)
                    {
                    } finally
                    {
                        // Native thread has finished
                        if (!SDLActivity.mExitCalledFromJava)
                        {
                            SDLActivity.handleNativeExit();
                        }
                    }
                }
            }, "SDLThreadListener");
            SDLActivity.mSDLThread.start();
        }

        if (SDLActivity.mHasFocus)
        {
            SDLActivity.handleResume();
        }

        sendWeaponWheelSettings();
    }

    // Key events
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event)
    {
        Log.v("SDL", "SDLSurface::onKey: " + keyCode + " Action = " + event.getAction() + " " + event.toString());

        int source = event.getSource();
        // Stop right mouse button being backbutton
        if ((source == InputDevice.SOURCE_MOUSE)
                || (source == InputDevice.SOURCE_MOUSE_RELATIVE))
        {
            Log.v("SDL", "SDLSurface::onKey: is mouse");
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
        } else if (event.getAction() == KeyEvent.ACTION_UP)
        {
            return controlInterp.onKeyUp(keyCode, event);
        }
/*
        // Dispatch the different events depending on where they come from
        // Some SOURCE_JOYSTICK, SOURCE_DPAD or SOURCE_GAMEPAD are also SOURCE_KEYBOARD
        // So, we try to process them as JOYSTICK/DPAD/GAMEPAD events first, if that fails we try them as KEYBOARD
        //
        // Furthermore, it's possible a game controller has SOURCE_KEYBOARD and
        // SOURCE_JOYSTICK, while its key events arrive from the keyboard source
        // So, retrieve the device itself and check all of its sources
        if (SDLActivity.isDeviceSDLJoystick(event.getDeviceId()))
        {
            // Note that we process events with specific key codes here
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                if (SDLActivity.onNativePadDown(event.getDeviceId(), keyCode) == 0)
                {
                    return true;
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP)
            {
                if (SDLActivity.onNativePadUp(event.getDeviceId(), keyCode) == 0)
                {
                    return true;
                }
            }
        }

        if ((event.getSource() & InputDevice.SOURCE_KEYBOARD) != 0)
        {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                Log.v("SDL", "key down: " + keyCode);
                SDLActivity.onNativeKeyDown(keyCode);
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP)
            {
                Log.v("SDL", "key up: " + keyCode);
                SDLActivity.onNativeKeyUp(keyCode);
                return true;
            }
        }
*/
        return false;
    }

    // Touch events
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        //Log.d("SDL touch", event.toString());

        return controlInterp.onTouchEvent(event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event)
    {
        //Log.d("SDL gen", event.toString());

        if (SDLActivity.useMouse)
        {
            if (event.getSource() == InputDevice.SOURCE_MOUSE)
            {
                //Log.v("SDL", "SDLSurface::onGenericMotionEvent: is mouse");
                // If this happens it measn the mouse has lost capture somehow
                if (event.getAction() == MotionEvent.ACTION_HOVER_MOVE)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        requestPointerCapture();
                    }
                }
                return true;
            }
        }

        return controlInterp.onGenericMotionEvent(event);
    }


    @Override
    public boolean onCapturedPointerEvent(MotionEvent event)
    {
        //Log.d("SDL cap", event.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            //Log.d("SDL", " dx = " + xOffset + " dt = " + yOffset + "Action = " + motionEvent.getAction() + " button" + motionEvent.getButtonState());

            if (event.getAction() == MotionEvent.ACTION_SCROLL)
            {
                SDLActivity.onNativeMouse(0, 8, event.getAxisValue(MotionEvent.AXIS_HSCROLL), event.getAxisValue(MotionEvent.AXIS_VSCROLL));
            } else if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS)
            {
                SDLActivity.onNativeMouse(event.getActionButton(), 0, 0, 0);
            } else if (event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE)
            {
                SDLActivity.onNativeMouse(event.getActionButton(), 1, 0, 0);
            } else if (event.getAction() == MotionEvent.ACTION_MOVE)
            {
                SDLActivity.onNativeMouse(0, 3, event.getX(), event.getY());
            }
            // Use the coordinates to update your view and return true if the event was
            // successfully processed
            return true;
        } else
            return false;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // TODO
    }

    // Dont create these each time
    float[] rotationMatrix = new float[9];
    float[] orientation = new float[3];
    float[] orientationLast = new float[3];
    float[] adjustedRotationMatrix = new float[9];

    private void updateOrientation(float[] rotationVector)
    {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        final int worldAxisForDeviceAxisX;
        final int worldAxisForDeviceAxisY;

        // Remap the axes as if the device screen was the instrument panel,
        // and adjust the rotation matrix for the device orientation.
        switch (SDLActivity.mSingleton.getWindowManager().getDefaultDisplay().getRotation())
        {
            case Surface.ROTATION_0:
            default:
                worldAxisForDeviceAxisX = SensorManager.AXIS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
                break;
            case Surface.ROTATION_90:
                worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
                break;
            case Surface.ROTATION_270:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_X;
                break;
        }

        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                worldAxisForDeviceAxisY, adjustedRotationMatrix);

        // Transform rotation matrix into azimuth/pitch/roll

        SensorManager.getOrientation(adjustedRotationMatrix, orientation);


        float yawDx = orientationLast[0] - orientation[0];
        // Handle when going 3.14 to -3.14
        // If very big we probably crossed the boundry
        if( (yawDx > 5) || ( yawDx < -5 ))
        {
            yawDx = orientationLast[0] - (-orientation[0]);
        }


        float pitchDx = orientationLast[1] - orientation[1];
        yawDx = yawDx * (float) (((Math.PI / 2) - Math.abs(orientationLast[1])) / Math.PI);
        //Log.d("gyro","pith = " + orientation[1] + " roll ="+ orientation[2] + "yaw = " + orientation[0] + " ydx =" + yawDx );
        NativeLib.analogYaw(ControlConfig.LOOK_MODE_MOUSE, (yawDx / 2) * gyroXSens,0);
        NativeLib.analogPitch(ControlConfig.LOOK_MODE_MOUSE, (pitchDx / 5) * gyroYSens,0);

        orientationLast[0] = orientation[0];
        orientationLast[1] = orientation[1];
        orientationLast[2] = orientation[2];

        //long alloc = Debug.getNativeHeapAllocatedSize();
        //long free = Debug.getNativeHeapSize();
        //Log.d("MEM","alloc = " + alloc + "   free = " + free);
    }

    long lastGyroTime;

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        //Log.d("sen"," type = "+ event.sensor.getType());
        int type = event.sensor.getType();
        if ((type ==  Sensor.TYPE_GAME_ROTATION_VECTOR) || (type == Sensor.TYPE_ROTATION_VECTOR))
        {
            //Log.d("GYRP", "1 = " + event.values[0]+ " 2 = " + event.values[1]+" 3 = " + event.values[2]);
            updateOrientation(event.values);
        }
        else if (type == Sensor.TYPE_GYROSCOPE)
        {
            long timeDiff = event.timestamp - lastGyroTime;
            lastGyroTime = event.timestamp;

            timeDiff /= 1000000; // Convert to milliseconds

            float yawDx;
            float pitchDx;

            if( !gyroSwapXY )
            {
                yawDx = event.values[0] - gyroXOffset;
                pitchDx = event.values[1]- gyroYOffset;
            }
            else
            {
                yawDx = event.values[1]- gyroYOffset;
                pitchDx = event.values[0]- gyroXOffset;
            }

            if( gyroInvertX )
                yawDx = -yawDx;

            if( gyroInvertY )
                pitchDx = -pitchDx;

            yawDx = yawDx  * timeDiff / 6000.f;
            pitchDx = pitchDx * timeDiff / 7000.f;

            int rotation = SDLActivity.mSingleton.getWindowManager().getDefaultDisplay().getRotation();
            if( rotation == Surface.ROTATION_90)
            {
                pitchDx = -pitchDx;
            }
            else
            {
                yawDx = -yawDx;
            }

            yawDx *= gyroXSens;
            pitchDx *= gyroYSens;

            if(yawDx > -2 && yawDx < 2) // Quick range check, and check for NaN
                NativeLib.analogYaw(ControlConfig.LOOK_MODE_MOUSE, (yawDx) * gyroXSens,0);

            if(pitchDx > -2 && pitchDx < 2) // Quick range check, and check for NaN
                NativeLib.analogPitch(ControlConfig.LOOK_MODE_MOUSE, (pitchDx) * gyroYSens,0);
        }
    }
}

/* This is a fake invisible editor view that receives the input and defines the
 * pan&scan region
 */
class DummyEdit extends View implements View.OnKeyListener
{
    InputConnection ic;

    public DummyEdit(Context context)
    {
        super(context);
        setFocusableInTouchMode(true);
        setFocusable(true);
        setOnKeyListener(this);
    }

    @Override
    public boolean onCheckIsTextEditor()
    {
        return true;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event)
    {

        // We always want the back button to do an escape
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                SDLActivity.onNativeKeyDown(KeyEvent.KEYCODE_ESCAPE, 0);
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP)
            {
                SDLActivity.onNativeKeyUp(KeyEvent.KEYCODE_ESCAPE);
                return true;
            }
        }
/*
        // This handles the hardware keyboard input
        if (event.isPrintingKey() || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                ic.commitText(String.valueOf((char) event.getUnicodeChar()), 1);
            }
            return true;
        }
*/
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            Log.v("SDL", "onKey down: " + keyCode);
            SDLActivity.onNativeKeyDown(keyCode, event.getUnicodeChar());
            return true;
        } else if (event.getAction() == KeyEvent.ACTION_UP)
        {
            Log.v("SDL", "onKey up: " + keyCode);
            SDLActivity.onNativeKeyUp(keyCode);
            return true;
        }

        return false;
    }

    //
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
        // As seen on StackOverflow: http://stackoverflow.com/questions/7634346/keyboard-hide-event
        // FIXME: Discussion at http://bugzilla.libsdl.org/show_bug.cgi?id=1639
        // FIXME: This is not a 100% effective solution to the problem of detecting if the keyboard is showing or not
        // FIXME: A more effective solution would be to assume our Layout to be RelativeLayout or LinearLayout
        // FIXME: And determine the keyboard presence doing this: http://stackoverflow.com/questions/2150078/how-to-check-visibility-of-software-keyboard-in-android
        // FIXME: An even more effective way would be if Android provided this out of the box, but where would the fun be in that :)
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (SDLActivity.mTextEdit != null && SDLActivity.mTextEdit.getVisibility() == View.VISIBLE)
            {
                SDLActivity.onNativeKeyboardFocusLost();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        ic = new SDLInputConnection(this, true);

        outAttrs.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
                | 33554432 /* API 11: EditorInfo.IME_FLAG_NO_FULLSCREEN */;

        return ic;
    }
}

class SDLInputConnection extends BaseInputConnection
{

    public SDLInputConnection(View targetView, boolean fullEditor)
    {
        super(targetView, fullEditor);

    }

    @Override
    public boolean sendKeyEvent(KeyEvent event)
    {
        /*
         * This handles the keycodes from soft keyboard (and IME-translated
         * input from hardkeyboard)
         */
        int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            if (event.isPrintingKey() || keyCode == KeyEvent.KEYCODE_SPACE)
            {
                commitText(String.valueOf((char) event.getUnicodeChar()), 1);
            }
            Log.v("SDL", "sendKeyEvent down: " + keyCode);
            SDLActivity.onNativeKeyDown(keyCode, 0);
            return true;
        } else if (event.getAction() == KeyEvent.ACTION_UP)
        {
            Log.v("SDL", "sendKeyEvent up: " + keyCode);
            SDLActivity.onNativeKeyUp(keyCode);
            return true;
        }
        return super.sendKeyEvent(event);
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition)
    {
        Log.v("SDL", "commitText: " + text);
        nativeCommitText(text.toString(), newCursorPosition);

        return super.commitText(text, newCursorPosition);
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition)
    {
        Log.v("SDL", "setComposingText: " + text);
        nativeSetComposingText(text.toString(), newCursorPosition);

        return super.setComposingText(text, newCursorPosition);
    }

    public native void nativeCommitText(String text, int newCursorPosition);

    public native void nativeSetComposingText(String text, int newCursorPosition);

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength)
    {
        // Workaround to capture backspace key. Ref: http://stackoverflow.com/questions/14560344/android-backspace-in-webview-baseinputconnection
        if (beforeLength == 1 && afterLength == 0)
        {
            // backspace
            return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                    && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
        }

        return super.deleteSurroundingText(beforeLength, afterLength);
    }
}
