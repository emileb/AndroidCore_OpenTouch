package com.opentouchgaming.androidcore.controls;

public interface ControlInterface {
	
	public void initTouchControls_if(String pngPath,int width,int height);
	
	public boolean touchEvent_if( int action, int pid, float x, float y);
	public void    keyPress_if(int down, int qkey, int unicode);
	public void    doAction_if(int state, int action);
	public void    backButton_if();
	public void    analogFwd_if(float v,float raw);
	public void    analogSide_if(float v,float raw);
	public void    analogPitch_if(int mode,float v,float raw);
	public void    analogYaw_if(int mode,float v,float raw);
	public void    weaponWheelSettings_if(int useMoveStick,int mode,int autoTimeout);

}
 