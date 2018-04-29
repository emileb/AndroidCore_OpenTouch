package com.opentouchgaming.androidcore.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.AppCompatImageButton;
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
        void engineConfig(GameEngine engine);

        void enginePanelStateChange(boolean open);
    }

    GameEngine[] gameEngines;

    GameEngine currentEngine;

    SlidePanel leftSlidePanel;

    EnginesPanel.Listener listener;

    ImageButton leftPanelButton;

    public EnginesPanel(Context context, View topView, GameEngine[] engines, final Listener listener)
    {
        this.listener = listener;

        gameEngines = engines;

        int leftPanelSlideAmmount;

        View leftPanel = topView.findViewById(R.id.linear_left_hidden_panel);
        {  // Sets the size of panel so the buttons are square
            Configuration configuration = context.getResources().getConfiguration();
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) leftPanel.getLayoutParams();
            lp.width = (int)(Utils.dpToPx(context.getResources(), configuration.screenHeightDp / gameEngines.length) * 1.5);
            leftPanel.setLayoutParams(lp);
            leftPanelSlideAmmount = leftPanel.getLayoutParams().width;
        }

        View leftPanelTopView;
        leftPanelTopView = topView.findViewById(R.id.relative_top_left_panel);
        leftSlidePanel = new SlidePanel(leftPanelTopView, SlidePanel.SlideSide.LEFT, leftPanelSlideAmmount, 300);


        // Button to open panel
        leftPanelButton = topView.findViewById(R.id.imagebutton_entry_open_left);
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

        LinearLayout leftPanelLayout = topView.findViewById(R.id.linear_left_hidden_panel);

        for (int n = 0; n < gameEngines.length; n++)
        {

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
            params.weight = 1;
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.height = 0;

            int margin = 5;
            params.leftMargin = Utils.dpToPx(context.getResources(), margin);
            params.rightMargin = Utils.dpToPx(context.getResources(), margin);
            params.topMargin = Utils.dpToPx(context.getResources(), margin);
            params.bottomMargin = Utils.dpToPx(context.getResources(), margin);

            LinearLayout groupLayout = new LinearLayout(context);
            groupLayout.setLayoutParams(params);

            // button.setLayoutParams(params);
            ImageButton button = new ImageButton(context);
            button.setTag(new Integer(n)); // Used for the click listener callback
            button.setImageResource(gameEngines[n].iconRes);
            button.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams paramsB = new LinearLayout.LayoutParams(0, 0);
            paramsB.weight = 2f;
            paramsB.width = 0;
            paramsB.height = LinearLayout.LayoutParams.MATCH_PARENT;
            button.setLayoutParams(paramsB);
            button.setBackgroundResource(R.drawable.focusable);
            button.setFocusableInTouchMode(true);

            AppCompatImageButton buttonCfg = new AppCompatImageButton(context);
            buttonCfg.setTag(new Integer(n)); // Used for the click listener callback
            buttonCfg.setImageResource(R.drawable.ic_build_black_24dp);
            buttonCfg.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams paramsC = new LinearLayout.LayoutParams(0, 0);
            paramsC.weight = 1;
            paramsC.width = 0;
            paramsC.height = LinearLayout.LayoutParams.MATCH_PARENT;
            buttonCfg.setLayoutParams(paramsC);
            buttonCfg.setBackgroundResource(R.drawable.focusable);
            buttonCfg.setFocusableInTouchMode(true);

            groupLayout.addView(button);
            groupLayout.addView(buttonCfg);
            leftPanelLayout.addView(groupLayout);


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


            buttonCfg.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    int selected = (Integer) view.getTag();
                    listener.engineConfig(gameEngines[selected]);
                }
            });

            gameEngines[n].imageButton = button;
            gameEngines[n].imageButtonCfg = buttonCfg;
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


    private void fadeHandleButton(boolean out)
    {
        if (out)
        {
            leftPanelButton.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            super.onAnimationEnd(animation);
                            leftPanelButton.clearAnimation();
                            leftPanelButton.setVisibility(View.GONE);
                        }
                    });
        } else
        {
            leftPanelButton.setVisibility(View.VISIBLE);
            leftPanelButton.animate()
                    .alpha(1.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            super.onAnimationEnd(animation);
                            leftPanelButton.clearAnimation();

                        }
                    });
        }
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
