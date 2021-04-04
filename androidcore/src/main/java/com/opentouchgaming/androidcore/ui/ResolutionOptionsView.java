package com.opentouchgaming.androidcore.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class ResolutionOptionsView
{

    static List<ResolutionOptions> resolutionList = new ArrayList<>();

    static
    {
        resolutionList.add(new ResolutionOptions("Screen (100%)", ResolutionType.FULL, "$W", "$H"));
        resolutionList.add(new ResolutionOptions("Screen / 2 (50%)", ResolutionType.FULL_0_5, "$W2", "$H2"));
        resolutionList.add(new ResolutionOptions("Screen / 3 (33%)", ResolutionType.FULL_0_3, "$W3", "$H3"));
        resolutionList.add(new ResolutionOptions("Screen / 4 (25%)", ResolutionType.FULL_0_25, "$W4", "$H4"));
        resolutionList.add(new ResolutionOptions("568 x 320 (16:9)", ResolutionType.SET, "568", "320"));
        resolutionList.add(new ResolutionOptions("850 x 480 (16:9)", ResolutionType.SET, "850", "480"));
        resolutionList.add(new ResolutionOptions("769 x 480 (16:10)", ResolutionType.SET, "769", "480"));
        resolutionList.add(new ResolutionOptions("1280 x 675 (16:9)", ResolutionType.SET, "1280", "675"));
        resolutionList.add(new ResolutionOptions("Custom", ResolutionType.CUSTOM, "0", "0"));
    }

    String prefix;
    EditText widthEdit;
    EditText heightEdit;
    public ResolutionOptionsView(Context context, Dialog view, String prefix)
    {

        this.prefix = prefix;

        Spinner resSpinner = view.findViewById(com.opentouchgaming.androidcore.R.id.resolution_spinner);
        widthEdit = view.findViewById(com.opentouchgaming.androidcore.R.id.resolution_width_editText);
        heightEdit = view.findViewById(com.opentouchgaming.androidcore.R.id.resolution_height_editText);

        ArrayAdapter<ResolutionOptions> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, resolutionList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resSpinner.setAdapter(dataAdapter);
        resSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                AppSettings.setIntOption(context, prefix + "_resolution_spinner", position);
                ResolutionOptions option = getResOption(prefix);
                widthEdit.setText("" + option.w);
                heightEdit.setText("" + option.h);
                if (option.type == ResolutionType.CUSTOM)
                {
                    widthEdit.setEnabled(true);
                    heightEdit.setEnabled(true);
                }
                else
                {
                    widthEdit.setEnabled(false);
                    heightEdit.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        int selected = AppSettings.getIntOption(context, prefix + "_resolution_spinner", 0);
        resSpinner.setSelection(selected);
    }

    public static ResolutionOptions getResOption(String prefix)
    {
        int selected = AppSettings.getIntOption(AppInfo.getContext(), prefix + "_resolution_spinner", 0);
        ResolutionOptions option = resolutionList.get(selected);
        if (option.type == ResolutionType.CUSTOM)
        {
            option.w = AppSettings.getStringOption(AppInfo.getContext(), prefix + "_resolution_cust_w", "640");
            option.h = AppSettings.getStringOption(AppInfo.getContext(), prefix + "_resolution_cust_h", "480");
        }
        return option;
    }

    public void save()
    {
        if (getResOption(prefix).type == ResolutionType.CUSTOM)
        {
            AppSettings.setStringOption(AppInfo.getContext(), prefix + "_resolution_cust_w", widthEdit.getText().toString());
            AppSettings.setStringOption(AppInfo.getContext(), prefix + "_resolution_cust_h", heightEdit.getText().toString());
        }
    }

    enum ResolutionType
    {
        FULL, FULL_0_5, FULL_0_3, FULL_0_25, SET, CUSTOM
    }

    public static class ResolutionOptions
    {
        public String w;
        public String h;
        String title;
        ResolutionType type;
        ResolutionOptions(String title, ResolutionType t, String w, String h)
        {
            this.title = title;
            this.type = t;
            this.w = w;
            this.h = h;
        }

        //Override
        public String toString()
        {
            return title;
        }
    }

}
