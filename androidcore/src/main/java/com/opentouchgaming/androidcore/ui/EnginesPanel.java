package com.opentouchgaming.androidcore.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.opentouchgaming.androidcore.GameEngine;
import com.opentouchgaming.androidcore.R;
import com.opentouchgaming.androidcore.Utils;

/**
 * Created by Emile on 25/07/2017.
 */

public class EnginesPanel
{
    public interface Listener
    {
        void engineSelected(GameEngine engine);

        void enginePanelStateChange(boolean open);
    }

    GameEngine[] gameEngines;

    GameEngine currentEngine;

    SlidePanel leftSlidePanel;

    EnginesPanel.Listener listener;

    public EnginesPanel(Context context, View topView, GameEngine[] engines, final Listener listener)
    {
        this.listener = listener;

        gameEngines = engines;

        int leftPanelSlideAmmount;

        View leftPanel = topView.findViewById(R.id.linear_left_hidden_panel);
        {  // Sets the size of panel so the buttons are square
            Configuration configuration = context.getResources().getConfiguration();
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) leftPanel.getLayoutParams();
            lp.width = Utils.dpToPx(context.getResources(), configuration.screenHeightDp / gameEngines.length);
            leftPanel.setLayoutParams(lp);
            leftPanelSlideAmmount = leftPanel.getLayoutParams().width;
        }

        View leftPanelTopView;
        leftPanelTopView = topView.findViewById(R.id.relative_top_left_panel);
        leftSlidePanel = new SlidePanel(leftPanelTopView, SlidePanel.SlideSide.LEFT, leftPanelSlideAmmount, 300);


        // Button to open panel
        ImageButton leftPanelButton = (ImageButton) topView.findViewById(R.id.imagebutton_entry_open_left);
        leftPanelButton.setFocusable(false);
        leftPanelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(isOpen())
                    close() ;
                else
                    open();
                updateFocus();
            }
        });

        LinearLayout leftPanelLayout = (LinearLayout) topView.findViewById(R.id.linear_left_hidden_panel);

        for (int n = 0; n < gameEngines.length; n++)
        {
            ImageButton button = new ImageButton(context);
            button.setTag(new Integer(n)); // Used for the click listener callback
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
            params.weight = 1;
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.height = 0;

            int margin = 5;
            params.leftMargin = Utils.dpToPx(context.getResources(), margin);
            params.rightMargin = Utils.dpToPx(context.getResources(), margin);
            params.topMargin = Utils.dpToPx(context.getResources(), margin);
            params.bottomMargin = Utils.dpToPx(context.getResources(), margin);

            button.setLayoutParams(params);
            button.setImageResource(gameEngines[n].iconRes);
            button.setScaleType(ImageView.ScaleType.FIT_CENTER);
            leftPanelLayout.addView(button);

            button.setBackgroundResource(R.drawable.focusable);
            button.setFocusableInTouchMode(true);
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    int selected = (Integer) view.getTag();
                    selectEngine(gameEngines[selected]);
                    close();
                }
            });


            gameEngines[n].imageButton = button;
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
                // Set focus to current engine
                if (currentEngine == button)
                {
                    button.imageButton.requestFocus();
                }
            } else
            {
                button.imageButton.setFocusable(false);
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
        updateFocus();
    }

    public void open()
    {
        leftSlidePanel.open();
        listener.enginePanelStateChange(true);
        updateFocus();
    }

    public void closeIfOpen()
    {
        leftSlidePanel.closeIfOpen();
        listener.enginePanelStateChange(false);
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
            } else
            {
                button.imageButton.setActivated(false);
            }
        }

        currentEngine = engine;
        listener.engineSelected(currentEngine);
        //  wadsFragment.selectEngine(currentEngine);
    }
}
