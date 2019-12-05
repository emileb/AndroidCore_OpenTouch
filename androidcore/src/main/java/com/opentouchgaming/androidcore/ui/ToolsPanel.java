package com.opentouchgaming.androidcore.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opentouchgaming.androidcore.R;
import com.opentouchgaming.androidcore.Utils;


/**
 * Created by Emile on 26/07/2017.
 */

public class ToolsPanel
{
    public interface Listener
    {
        void toolsOnClick(int code);
        void toolsPanelStateChange(boolean open);
    }

    public static class ToolsPanelButton
    {
        public ToolsPanelButton(int code,String label,int image)
        {
            this.label = label;
            this.imageRes = image;
            this.code = code;
        }

        public int imageRes;
        public AppCompatImageButton imageButton;
        public String label;
        int code;
    };

    SlidePanel slidePanel;

    final ToolsPanelButton[] buttons;

    Listener listener;

    public ToolsPanel(Context context, View topView, final ToolsPanelButton[] buttons, final Listener listener)
    {
        this.buttons = buttons;
        this.listener = listener;

        int slideAmmount;

        int buttonSize;

        LinearLayout leftPanel = topView.findViewById(R.id.linear_right_hidden_panel);
        {  // Sets the size of panel so the buttons are square
            Configuration configuration = context.getResources().getConfiguration();
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) leftPanel.getLayoutParams();

            buttonSize =  (int)(Utils.dpToPx(context.getResources(), configuration.screenHeightDp / buttons.length));
            lp.width = (int)(Utils.dpToPx(context.getResources(), configuration.screenHeightDp / buttons.length) * 1.5);
            lp.width += Utils.dpToPx(context.getResources(),100);

            leftPanel.setLayoutParams(lp);
            slideAmmount = leftPanel.getLayoutParams().width;
        }

        View relativeView;
        relativeView = topView.findViewById(R.id.relative_top_right_panel);
        slidePanel = new SlidePanel(relativeView, SlidePanel.SlideSide.RIGHT, slideAmmount, 300);


        // Button to open panel
        AppCompatImageButton slideButton = (AppCompatImageButton) relativeView.findViewById(R.id.imagebutton_entry_open_right);
        slideButton.setFocusable(false);
        slideButton.setOnClickListener(new View.OnClickListener()
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

        for (int n = 0; n < buttons.length; n++)
        {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.tool_panel_item,null);

            AppCompatImageButton button = view.findViewById(R.id.image_button);
            TextView lableTextView = view.findViewById(R.id.label_textView);
            lableTextView.setText(buttons[n].label);

            buttons[n].imageButton = button;
            button.setTag(new Integer(n)); // Used for the click listener callback


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
            params.weight = 1;
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.height = 0;

            int margin = 3;
            params.leftMargin = Utils.dpToPx(context.getResources(), margin);
            params.rightMargin = Utils.dpToPx(context.getResources(), margin);
            params.topMargin = Utils.dpToPx(context.getResources(), margin);
            params.bottomMargin = Utils.dpToPx(context.getResources(), margin);

            view.setLayoutParams(params);

            //RelativeLayout.LayoutParams p = new  RelativeLayout.LayoutParams(0,0);// (RelativeLayout.LayoutParams)button.getLayoutParams();
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)button.getLayoutParams();
            p.width  = buttonSize;
            p.height = buttonSize;
            button.setLayoutParams(p);

            leftPanel.addView(view);

            button.setImageResource(buttons[n].imageRes);
            button.setScaleType(ImageView.ScaleType.FIT_CENTER);
            button.setBackgroundResource(R.drawable.focusable);
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    int selected = (Integer) view.getTag();
                    listener.toolsOnClick( buttons[selected].code);
                    close();
                }
            });

        }
        updateFocus();
    }

    private void updateFocus()
    {
        for (int n = 0; n < buttons.length; n++)
        {
            if( slidePanel.isOpen() )
            {
                buttons[n].imageButton.setFocusable(true);
            }
            else
            {
                buttons[n].imageButton.setFocusable(false);
            }
        }

        buttons[0].imageButton.requestFocus(); // Try to set focus on the top item
    }

    public boolean isOpen()
    {
        return slidePanel.isOpen();
    }

    public void open()
    {
        slidePanel.open();
        listener.toolsPanelStateChange(true);
        updateFocus();
    }

    public void close()
    {
        slidePanel.close();
        listener.toolsPanelStateChange(false);
        updateFocus();
    }

    public void closeIfOpen()
    {
        slidePanel.closeIfOpen();
        listener.toolsPanelStateChange(false);
        updateFocus();
    }
}