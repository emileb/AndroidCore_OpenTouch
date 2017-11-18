package com.opentouchgaming.androidcore.controls;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.R;

import java.io.IOException;
import java.util.ArrayList;

import static com.opentouchgaming.androidcore.DebugLog.Level.D;
import static com.opentouchgaming.androidcore.DebugLog.Level.I;


public class GamePadFragment extends Fragment
{

    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.CONTROLS, "GamePadFragment");
    }

    ListView listView;
    ControlListAdapter adapter;

    TextView info;

    ControlConfig config;

    //This is a bit shit, set this before instantiat the fragment
    public static ArrayList<ActionInput> gamepadActions;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        config = new ControlConfig(AppInfo.currentEngine.gamepadDefiniton);

        try
        {
            log.log(I, "Trying to load condig from file...");
            config.loadControls();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            //e.printStackTrace();

            //Failed to load, so save the default
            log.log(I, "..file not found");
            try
            {
                config.saveControls();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }

        } catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            log.log(I, "Error in serialization.. " + e.toString());
        }

    }


    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
    }


    @Override
    public void onPause()
    {
        super.onPause();

    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View mainView = inflater.inflate(R.layout.fragment_gamepad, null);


        CheckBox enableCb = (CheckBox) mainView.findViewById(R.id.gamepad_enable_checkbox);
        enableCb.setChecked(TouchSettings.gamePadEnabled);

        enableCb.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                TouchSettings.setBoolOption(getActivity(), "gamepad_enabled", isChecked);
                TouchSettings.gamePadEnabled = isChecked;
                setListViewEnabled(TouchSettings.gamePadEnabled);

            }
        });


        CheckBox hideCtrlCb = (CheckBox) mainView.findViewById(R.id.gamepad_hide_touch_checkbox);
        hideCtrlCb.setChecked(TouchSettings.hideTouchControls);

        hideCtrlCb.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                TouchSettings.setBoolOption(getActivity(), "hide_touch_controls", isChecked);
                TouchSettings.hideTouchControls = isChecked;
            }
        });


        Button help = (Button) mainView.findViewById(R.id.gamepad_help_button);
        help.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                //NoticeDialog.show(getActivity(),"Gamepad Help", R.raw.gamepad);
            }
        });

        listView = (ListView) mainView.findViewById(R.id.gamepad_listview);
        adapter = new ControlListAdapter(getActivity());
        listView.setAdapter(adapter);

        setListViewEnabled(TouchSettings.gamePadEnabled);


        //listView.setSelector(R.drawable.layout_sel_background);
        listView.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int pos,
                                    long id)
            {
                config.startMonitor(getActivity(), pos);
                adapter.notifyDataSetChanged();
            }
        });

        listView.setOnItemLongClickListener(new OnItemLongClickListener()
        {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos,
                                           long id)
            {
                return config.showExtraOptions(getActivity(), pos);
            }
        });

        adapter.notifyDataSetChanged();

        info = (TextView) mainView.findViewById(R.id.gamepad_info_textview);
        info.setText("Select Action");
        info.setTextColor(getResources().getColor(android.R.color.holo_blue_light));

        config.setTextView(getActivity(), info);

        return mainView;
    }

    private void setListViewEnabled(boolean v)
    {

        listView.setEnabled(v);
        if (v)
        {
            listView.setAlpha(1);
        } else
        {
            listView.setAlpha(0.3f);
            //listView.setBackgroundColor(Color.GRAY);
        }
    }

    Dpad mDpad = new Dpad();

    public boolean onGenericMotionEvent(MotionEvent event)
    {

        log.log(D, "onGenericMotionEvent: event = " + event.toString());
/*
        if (Dpad.isDpadDevice(event)) {

            int press = mDpad.getDirectionPressed(event);
            switch (press) {
                case Dpad.LEFT:
                    log.log(D, "LEFT" );
                    return true;
                case Dpad.RIGHT:
                    log.log(D, "RIGHT" );
                    return true;
                case Dpad.UP:
                    log.log(D, "UP" );
                    return true;
                case Dpad.DOWN:
                    log.log(D, "DOWN" );
                    return true;
            }
        }
*/

        if (config.onGenericMotionEvent(event))
            adapter.notifyDataSetChanged();

        //return config.isMonitoring(); //This does not work, mouse appears anyway
        return true; //If gamepas tab visible always steal
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        log.log(D, "onKeyDown: keyCode = " + keyCode + " event = " + event.toString());
        if (config.onKeyDown(keyCode, event))
        {
            adapter.notifyDataSetChanged();
            return true;
        }
        adapter.notifyDataSetChanged();
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        log.log(D, "onKeyUp: keyCode = " + keyCode + " event = " + event.toString());
        if (config.onKeyUp(keyCode, event))
        {
            adapter.notifyDataSetChanged();
            return true;
        }
        adapter.notifyDataSetChanged();
        return false;
    }

    class ControlListAdapter extends BaseAdapter
    {
        private Activity context;

        public ControlListAdapter(Activity context)
        {
            this.context = context;

        }

        public void add(String string)
        {

        }

        public int getCount()
        {
            return config.getSize();
        }

        public Object getItem(int arg0)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public long getItemId(int arg0)
        {
            // TODO Auto-generated method stub
            return 0;
        }


        public View getView(int position, View convertView, ViewGroup list)
        {
            View v = config.getView(getActivity(), position);
            return v;
        }

    }

}
