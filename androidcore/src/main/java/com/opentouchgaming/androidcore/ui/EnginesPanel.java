package com.opentouchgaming.androidcore.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.GameEngine;
import com.opentouchgaming.androidcore.R;
import com.opentouchgaming.androidcore.Utils;

import java.util.ArrayList;

/**
 * Created by Emile on 25/07/2017.
 */

public class EnginesPanel
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.GAMEFRAGMENT, "EnginesPanel");
    }

    public interface Listener
    {
        void engineSelected(GameEngine engine);

        void engineConfig(GameEngine engine);

        void enginePanelStateChange(boolean open);
    }

    GameEngine[] gameEngines;

    GameEngine currentEngine;

    SlidePanel leftSlidePanel;

    EnginesPanel.Listener listener;

    boolean useGroups;

    private class EngineGroup
    {
        ArrayList<GameEngine> engines = new ArrayList<>();
        ImageButton button;
    }

    ArrayList<EngineGroup> engineGroups = new ArrayList<>();

    public EnginesPanel(Context context, View topView, GameEngine[] engines, boolean useGroups, final Listener listener)
    {
        this.listener = listener;
        this.useGroups = useGroups;

        gameEngines = engines;

        View         leftPanelTopView = topView.findViewById(R.id.relative_top_left_panel);
        LinearLayout leftPanelLayout  = topView.findViewById(R.id.linear_left_hidden_panel);
        ImageButton  leftPanelButton  = topView.findViewById(R.id.imagebutton_entry_open_left);


        // Collect the engines into the UI groups
        EngineGroup group = null;
        int uiGroup = -1;
        for (int n = 0; n < gameEngines.length; n++)
        {
            if( useGroups )
            {
                // New (or first ui group)
                if (uiGroup != gameEngines[n].uiGroup)
                {
                    group = new EngineGroup();
                    engineGroups.add(group);
                    uiGroup = gameEngines[n].uiGroup;
                }
            }
            else
            {
                group = new EngineGroup();
                engineGroups.add(group);
            }

            group.engines.add(gameEngines[n]);
        }

        // Find largest number of engines in a group
        int largestGroup = 1;
        for( EngineGroup g : engineGroups )
        {
            log.log(DebugLog.Level.D,"Group has " + g.engines.size());
            if( g.engines.size() > largestGroup)
                largestGroup =  g.engines.size();
        }

        // Button to open panel
        leftPanelButton.setFocusable(false);
        leftPanelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (isOpen())
                    close();
                else
                    open();
                updateFocus();
            }
        });


        int leftPanelSlideAmmount;

        // Get screen height in pixels
        Configuration configuration = context.getResources().getConfiguration();
        int screenHeightPx = Utils.dpToPx(context.getResources(), configuration.screenHeightDp);
        //screenHeightPx = leftPanelTopView.getLayoutParams().height;

        // Calculate square button size
        // Give equal size for each ui group
        int buttonSize = screenHeightPx / engineGroups.size();
        int buttonCfgSize = buttonSize / 3;
        int totalWidth ;
        if( useGroups )
        {
            buttonCfgSize = buttonSize / 3;
            totalWidth = buttonSize * largestGroup;
        }
        else
        {
            buttonCfgSize = buttonSize / 2;
            totalWidth = buttonSize + buttonCfgSize;
        }
        // Set the layout width, equal to the button size x largest group
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)leftPanelLayout.getLayoutParams();
        lp.width = totalWidth;
        leftPanelLayout.setLayoutParams(lp);
        leftPanelSlideAmmount = lp.width;


        leftSlidePanel = new SlidePanel(leftPanelTopView, SlidePanel.SlideSide.LEFT, leftPanelSlideAmmount, 300);


        for (int g = 0; g < engineGroups.size(); g++)
        {
            //log.log(DebugLog.Level.D,"g = " + g +  " engineGroups.size() = " + engineGroups.size());
            group = engineGroups.get(g);

            // Create new ui group layout
            int margin = 1;
            LinearLayout groupLayout = new LinearLayout(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
            params.weight = 1;
            params.setMargins(margin,margin,margin,margin);
            groupLayout.setLayoutParams(params);

            // Add buttons to the group
            for( int e = 0; e < group.engines.size(); e++ )
            {
                log.log(DebugLog.Level.D,"e = " + e);
                GameEngine engine = group.engines.get(e);

                // MAIN BUTTON
                AppCompatImageView button = new AppCompatImageView(context);
                params = new LinearLayout.LayoutParams(buttonSize, buttonSize);
                button.setTag(engine); // Used for the click listener callback
                button.setImageResource(engine.iconRes);
                button.setScaleType(ImageView.ScaleType.FIT_CENTER);
                button.setBackgroundResource(R.drawable.focusable);
                button.setFocusableInTouchMode(true);

                button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        GameEngine engine = (GameEngine) view.getTag();
                        selectEngine(engine);
                        close();
                    }
                });


                button.setLayoutParams(params);
                groupLayout.addView(button);

                // CFG BUTTON
                AppCompatImageView buttonCfg;
                buttonCfg = new AppCompatImageView(context);
                params = new LinearLayout.LayoutParams(buttonCfgSize, buttonCfgSize);
                if( useGroups )
                {
                    // Move to bottom right
                    params.setMargins( -buttonCfgSize, buttonSize - buttonCfgSize, 0 ,0 );
                }
                else
                {
                    // Put next to the button
                    params.setMargins( 0, (buttonCfgSize / 2), 0 ,0 );
                }
                buttonCfg.setTag(engine); // Used for the click listener callback
                buttonCfg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                buttonCfg.setBackgroundResource(R.drawable.focusable);
                buttonCfg.setFocusableInTouchMode(true);

                if (engine.engineOptions != null) // Only show if available
                    buttonCfg.setImageResource(R.drawable.ic_settings_black_24dp);

                buttonCfg.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        GameEngine engine = (GameEngine) view.getTag();
                        listener.engineConfig(engine);
                    }
                });


                buttonCfg.setLayoutParams(params);
                groupLayout.addView(buttonCfg);


                engine.imageButton = button;
                engine.imageButtonCfg = buttonCfg;
            }

            leftPanelLayout.addView(groupLayout);
        }
        updateFocus();
    }

    private void updateFocus()
    {
        for (int n = 0; n < gameEngines.length; n++)
        {
            GameEngine button = gameEngines[n];
            if (leftSlidePanel.isOpen())
            {
                button.imageButton.setFocusable(true);
                button.imageButtonCfg.setFocusable(true);
                // Set focus to current engine
                if (currentEngine == button)
                {
                    button.imageButton.requestFocus();
                }
            } else
            {
                button.imageButton.setFocusable(false);
                button.imageButtonCfg.setFocusable(false);
            }
        }
    }

    public boolean isOpen()
    {
        return leftSlidePanel.isOpen();
    }

    public void close()
    {
        leftSlidePanel.close();
        listener.enginePanelStateChange(false);
        //fadeHandleButton(false);
        updateFocus();
    }

    public void open()
    {
        leftSlidePanel.open();
        listener.enginePanelStateChange(true);
        // Fade out button
        //fadeHandleButton(true);
        updateFocus();
    }

    public void closeIfOpen()
    {
        leftSlidePanel.closeIfOpen();
        listener.enginePanelStateChange(false);
        //fadeHandleButton(false);
        updateFocus();
    }

    public void selectEngine(GameEngine engine)
    {
        // Update buttons so they are highlighted
        for (int n = 0; n < gameEngines.length; n++)
        {
            GameEngine button = gameEngines[n];
            if (engine == gameEngines[n]) // Set selected image
            {
                button.imageButton.setActivated(true);
                if(useGroups)
                    button.imageButtonCfg.setVisibility(View.VISIBLE);

            } else
            {
                button.imageButton.setActivated(false);
                if(useGroups)
                    button.imageButtonCfg.setVisibility(View.INVISIBLE);
            }
        }

        currentEngine = engine;
        listener.engineSelected(currentEngine);
    }
}
