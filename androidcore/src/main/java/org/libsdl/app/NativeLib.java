package org.libsdl.app;


import android.util.Log;
import android.view.KeyEvent;

import com.opentouchgaming.androidcore.controls.ControlInterface;

public class NativeLib implements ControlInterface {

	public static native int init(String graphics_dir,int mem,String[] args,int game,String path, String filesPath, String nativeLibs);

	public static native void setScreenSize( int width, int height );

	public static native int frame();

	public static native boolean touchEvent( int action, int pid, float x, float y);
	public static native void keypress(int down, int qkey, int unicode);
	public static native void doAction(int state, int action);
	public static native void backButton();
	public static native void analogFwd(float v);
	public static native void analogSide(float v);
	public static native void analogPitch(int mode,float v);
	public static native void analogYaw(int mode,float v);
	public static native void setTouchSettings(float alpha,float strafe,float fwd,float pitch,float yaw,int other);


	@Override
	public void initTouchControls_if(String pngPath, int width, int height) {

	}

	@Override
	public boolean touchEvent_if(int action, int pid, float x, float y) {
		return touchEvent(  action,  pid,  x,  y);
	}
	@Override
	public void keyPress_if(int down, int qkey, int unicode) {
		keypress(down,qkey,unicode);

	}
	@Override
	public void doAction_if(int state, int action) {
		doAction(state,action);
	}

	@Override
	public void backButton_if()
	{
		backButton();
	}

	@Override
	public void analogFwd_if(float v) {
		analogFwd(v);
	}
	@Override
	public void analogSide_if(float v) {
		analogSide(v);
	}
	@Override
	public void  analogPitch_if(int mode,float v)
	{
		analogPitch(mode,v);
	}
	@Override
	public void  analogYaw_if(int mode,float v)
	{
		analogYaw(mode,v);
	}

}
