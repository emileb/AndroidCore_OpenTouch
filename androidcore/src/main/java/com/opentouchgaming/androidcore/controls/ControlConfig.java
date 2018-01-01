package com.opentouchgaming.androidcore.controls;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.R;
import com.opentouchgaming.androidcore.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import static com.opentouchgaming.androidcore.DebugLog.Level.D;

public class ControlConfig implements Serializable
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.CONTROLS, "ControlConfig");
    }

    public interface Listener
    {
        void startMonitoring( ActionInput action );
        void finishedMonitoring();
    }

    private static final long serialVersionUID = 1L;

    public static final int LOOK_MODE_MOUSE = 0;
    public static final int LOOK_MODE_ABSOLUTE = 1;
    public static final int LOOK_MODE_JOYSTICK = 2;

    String filename;

    Listener listener;

    public ControlConfig(ActionInputDefinition gamepadDefinition, Listener listener)
    {
        actions.addAll(gamepadDefinition.actions);
        filename = AppInfo.internalFiles + "/" + gamepadDefinition.filename;
        this.listener = listener;
    }

    void saveControls() throws IOException
    {
        saveControls(new File(filename));
    }

    void saveControls(File file) throws IOException
    {
        log.log(D, "saveControls, file = " + file.toString());

        FileOutputStream fos = null;
        ObjectOutputStream out = null;

        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(actions);
        out.close();
    }

    public void loadControls() throws IOException, ClassNotFoundException
    {
        loadControls(new File(filename));
    }

    public void loadControls(File file) throws IOException, ClassNotFoundException
    {
        log.log(D, "loadControls, file = " + file.toString());

        InputStream fis = null;
        ObjectInputStream in = null;

        fis = new FileInputStream(file);

        in = new ObjectInputStream(fis);
        ArrayList<ActionInput> cd = (ArrayList<ActionInput>) in.readObject();

        log.log(D, "loadControls, file loaded OK");

        in.close();

        for (ActionInput d : cd)
        {
            for (ActionInput a : actions)
            {
                if (d.tag.contentEquals(a.tag))
                {
                    a.invert = d.invert;
                    a.source = d.source;
                    a.sourceType = d.sourceType;
                    a.sourcePositive = d.sourcePositive;
                    a.scale = d.scale;
                    if (a.scale == 0) a.scale = 1;
                }
            }
        }

        //Now check no buttons are also assigned to analog, if it is, clear the buttons
        //This is because n00bs keep assigning movment analog AND buttons!
        for (ActionInput a : actions)
        {
            if ((a.source != -1) && (a.sourceType == ActionInput.SourceType.AXIS) && (a.actionType == ActionInput.ActionType.BUTTON))
            {
                for (ActionInput a_check : actions)
                {
                    if ((a_check.sourceType == ActionInput.SourceType.AXIS) && (a_check.actionType == ActionInput.ActionType.ANALOG))
                    {
                        if (a.source == a_check.source)
                        {
                            a.source = -1;
                            break;
                        }
                    }
                }
            }
        }

        fis.close();
    }


    void updated()
    {
        try
        {
            saveControls(new File(filename));
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    ArrayList<ActionInput> actions = new ArrayList<ActionInput>();

    ActionInput actionMonitor = null;

    boolean monitoring = false;
    boolean gotInput = false;

    public boolean showExtraOptions(Activity act, int pos)
    {
        final ActionInput in = actions.get(pos);

        if (in.actionType == ActionInput.ActionType.ANALOG)
        {
            Dialog dialog = new Dialog(act);
            dialog.setTitle("Axis Sensitivity Setting");
            dialog.setCancelable(true);

            final LinearLayout l = new LinearLayout(act);
            l.setOrientation(LinearLayout.VERTICAL);
            l.setMinimumWidth((int)Utils.convertDpToPixel(500,act));

            final SeekBar sb = new SeekBar(act);
            l.addView(sb);


            sb.setMax(100);
            sb.setProgress((int) (in.scale * 50));

            final CheckBox invert = new CheckBox(act);
            invert.setText("Invert");
            invert.setChecked(in.invert);

            l.addView(invert);

            dialog.setOnDismissListener(new OnDismissListener()
            {

                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    in.scale = (float) sb.getProgress() / (float) 50;
                    in.invert = invert.isChecked();
                    updated();
                }
            });

            dialog.setContentView(l);

            dialog.show();
            return true;
        }
        return false;
    }

    public void startMonitor(Activity act, int pos)
    {
        actionMonitor = actions.get(pos);
        monitoring = true;
        gotInput = false;
        if(listener != null)
            listener.startMonitoring( actionMonitor );
    }

    private void stopMonitor()
    {
        monitoring = false;
        if(listener != null)
            listener.finishedMonitoring();
    }

    int[] axisTest = {
            /*
            MotionEvent.AXIS_GENERIC_1,
			MotionEvent.AXIS_GENERIC_2,
			MotionEvent.AXIS_GENERIC_3,
			MotionEvent.AXIS_GENERIC_4,
			MotionEvent.AXIS_GENERIC_5,
			MotionEvent.AXIS_GENERIC_6,
			MotionEvent.AXIS_GENERIC_7,
			MotionEvent.AXIS_GENERIC_8,
			MotionEvent.AXIS_GENERIC_9,
			MotionEvent.AXIS_GENERIC_10,
			MotionEvent.AXIS_GENERIC_11,
			MotionEvent.AXIS_GENERIC_12,
			MotionEvent.AXIS_GENERIC_13,
			MotionEvent.AXIS_GENERIC_14,
			MotionEvent.AXIS_GENERIC_15,
			MotionEvent.AXIS_GENERIC_16,
			 */
            MotionEvent.AXIS_HAT_X,
            MotionEvent.AXIS_HAT_Y,
            MotionEvent.AXIS_LTRIGGER,
            MotionEvent.AXIS_RTRIGGER,
            MotionEvent.AXIS_RUDDER,
            MotionEvent.AXIS_RX,
            MotionEvent.AXIS_RY,
            MotionEvent.AXIS_RZ,
            MotionEvent.AXIS_THROTTLE,
            MotionEvent.AXIS_X,
            MotionEvent.AXIS_Y,
            MotionEvent.AXIS_Z,
            MotionEvent.AXIS_BRAKE,
            MotionEvent.AXIS_GAS,
    };

    public boolean onGenericMotionEvent(MotionEvent event)
    {
        log.log(D, "onGenericMotionEvent");

        if (monitoring)
        {
            if (actionMonitor != null && gotInput == false)
            {
                for (int a : axisTest)
                {
                    if (Math.abs(event.getAxisValue(a)) > 0.6)
                    {
                        actionMonitor.source = a;
                        actionMonitor.sourceType = ActionInput.SourceType.AXIS;
                        //Used for button actions
                        if (event.getAxisValue(a) > 0)
                            actionMonitor.sourcePositive = true;
                        else
                            actionMonitor.sourcePositive = false;

                        //monitoring = false;
                        gotInput = true;

                        log.log(D, actionMonitor.description + " = Analog (" + actionMonitor.source + ")");

                        updated();
                        return true;
                    }
                }
            } else // Keep monitoring until all the axis are back in the centre
            {
                boolean allCentre = true;
                for (int a : axisTest)
                {
                    if (Math.abs(event.getAxisValue(a)) > 0.2)
                    {
                        allCentre = false;
                    }
                }
                if( allCentre )
                {
                    stopMonitor();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isMonitoring()
    {
        return monitoring;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        log.log(D, "onKeyDown " + keyCode);

        if (monitoring)
        {
            if (keyCode == KeyEvent.KEYCODE_BACK) //Cancel and clear button assignment
            {
                actionMonitor.source = -1;
                actionMonitor.sourceType = ActionInput.SourceType.BUTTON;

                stopMonitor();
                updated();
                return true;
            } else
            {
                if (actionMonitor != null)
                {
                    if (actionMonitor.actionType != ActionInput.ActionType.ANALOG)
                    {
                        actionMonitor.source = keyCode;
                        actionMonitor.sourceType = ActionInput.SourceType.BUTTON;

                        stopMonitor();
                        updated();
                        return true;
                    }
                }
            }
            return true;
        }

        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        return monitoring;
    }


    public int getSize()
    {
        return actions.size();
    }

    public View getView(final Activity ctx, final int nbr)
    {
        View view = ctx.getLayoutInflater().inflate(R.layout.controls_listview_item, null);
        ImageView image = (ImageView) view.findViewById(R.id.imageView);
        TextView name = (TextView) view.findViewById(R.id.name_textview);
        TextView binding = (TextView) view.findViewById(R.id.binding_textview);
        ImageView setting_image = (ImageView) view.findViewById(R.id.settings_imageview);

        ActionInput ai = actions.get(nbr);

        if ((ai.actionType == ActionInput.ActionType.BUTTON) || (ai.actionType == ActionInput.ActionType.MENU))
        {
            setting_image.setVisibility(View.GONE);

            if ((ai.actionType == ActionInput.ActionType.MENU))
            {
                name.setTextColor(0xFF00aeef); //BLUEY
                image.setImageResource(R.drawable.gamepad_menu);
            } else
            {
                name.setTextColor(0xFF02ad2a); //GREEN
                image.setImageResource(R.drawable.gamepad);
            }
        } else if (ai.actionType == ActionInput.ActionType.ANALOG)
        {
            binding.setText(MotionEvent.axisToString(ai.source));
            setting_image.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    showExtraOptions(ctx, nbr);
                }
            });
            name.setTextColor(0xFFf7941d); //ORANGE
        }

        if (ai.source == -1)
        {
            binding.setText("not set");
        } else
        {
            if (ai.sourceType == ActionInput.SourceType.AXIS)
                binding.setText(MotionEvent.axisToString(ai.source));
            else
                binding.setText(KeyEvent.keyCodeToString(ai.source));
        }

        if (actionMonitor != null && actionMonitor == ai && monitoring)
        {
            view.setBackgroundResource(R.drawable.layout_sel_background);
        } else
        {
            view.setBackgroundResource(0);
        }

        //view.setBackgroundResource(R.drawable.focusable);
        /*
		if (ai.actionType == ActionType.BUTTON)
		{
			image.setImageResource(R.drawable.gamepad);
			if (ai.sourceType == ActionType.ANALOG)
				binding.setText(MotionEvent.axisToString(ai.source));
			else
				binding.setText(KeyEvent.keyCodeToString(ai.source));

			setting_image.setVisibility(View.GONE);
		}
		else //Analog
		{
			binding.setText(MotionEvent.axisToString(ai.source));
			setting_image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showExtraOptions(ctx,nbr);
				}
			});
			name.setTextColor(0xFFf7941d);
		}
*/
        name.setText(ai.description);

        return view;
    }
}
