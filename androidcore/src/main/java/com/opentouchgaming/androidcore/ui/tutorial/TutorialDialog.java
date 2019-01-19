package com.opentouchgaming.androidcore.ui.tutorial;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.R;

import java.util.ArrayList;
import java.util.List;

public class TutorialDialog
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.GAMEFRAGMENT, "TutorialDialog");
    }

    final Dialog dialog;
    Activity activity;

    ViewPager pager;
    ViewPagerAdapter pagerAdapter;

    private RelativeLayout tutorialLayout;

    private ListView mainList;

    int tutorialActive = -1;

    ArrayList<Tutorial> tutorials;

    Tutorial activeTutorial;

    public TutorialDialog(final Activity act, ArrayList<Tutorial> tut)
    {
        activity = act;
        this.tutorials = tut;

        dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        {
            @Override
            public void onBackPressed()
            {
                if (activeTutorial != null)
                {
                    showTutorial(null);
                } else
                {
                    dialog.dismiss();
                }
            }
        };

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_tutorial);
        dialog.setCancelable(true);

        dialog.setOnKeyListener(new Dialog.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event)
            {

                return false;
            }
        });

        tutorialLayout = dialog.findViewById(R.id.tutorial_layout);
        pager = dialog.findViewById(R.id.pager);
        mainList = dialog.findViewById(R.id.listView);

        pagerAdapter = new ViewPagerAdapter(activity);

        pager.setAdapter(pagerAdapter);



        ListAdapter customAdapter = new ListAdapter(act, R.layout.list_item_tutorial, tutorials);
        mainList.setAdapter(customAdapter);

        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                showTutorial(tutorials.get(position));
            }
        });
/*
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        try
        {
            String json = ow.writeValueAsString(tutorials);
            log.log(D, "json = " + json);
        } catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }
*/

        showTutorial(null);

        dialog.show();
    }


    private void showTutorial(Tutorial tutorial)
    {
        activeTutorial = tutorial;

        if (tutorial == null)
        {
            // Show main list
            tutorialLayout.setVisibility(View.GONE);

            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new AccelerateInterpolator());
            fadeIn.setDuration(500);
            mainList.startAnimation(fadeIn);
            mainList.setVisibility(View.VISIBLE);

        } else
        {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new AccelerateInterpolator());
            fadeIn.setDuration(500);
            tutorialLayout.startAnimation(fadeIn);

            // Show tutorial screens
            tutorialLayout.setVisibility(View.VISIBLE);
            mainList.setVisibility(View.GONE);

            pagerAdapter.setTutorial(activeTutorial);
            pagerAdapter.notifyDataSetChanged();
            pager.setAdapter(pagerAdapter);
            pager.setCurrentItem(0, false);
        }
    }

    public static class FragmentForInstruction1 extends Fragment
    {
        @Override
        @Nullable
        public View onCreateView(LayoutInflater inflater,
                                 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.tutorial_slider_view, container, false);
            return view;
        }
    }

    public class ViewPagerAdapter extends PagerAdapter
    {
        private Context mContext;
        private Tutorial tutorial;

        public ViewPagerAdapter(Context context)
        {
            mContext = context;
        }

        public void setTutorial(Tutorial tut)
        {
            tutorial = tut;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView((View) object);
        }

        @Override
        public int getCount()
        {
            if (tutorial != null)
                return tutorial.getScreens().size();
            else
                return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object)
        {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position)
        {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup view = (ViewGroup) inflater.inflate(R.layout.tutorial_slider_view, container, false);

            final TextView textView = view.findViewById(R.id.textView);
            final TextView pageTextView = view.findViewById(R.id.page_textView);
            final ImageView imageView = view.findViewById(R.id.imageView);


            if( textView != null)
                textView.setText(tutorial.getScreens().get(position).title);
            else
                textView.setVisibility(View.GONE);


            pageTextView.setText((position + 1) + "/" + getCount());

            Glide.with(activity)
                    .load(tutorial.getScreens().get(position).image)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .apply(new RequestOptions()
                            //.placeholder(R.drawable.ic_tut_cloud_download)
                            .placeholder( android.R.drawable.stat_sys_download)
                            .fitCenter())
                    .into(imageView);

            container.addView(view);
            return view;
        }
    }


    public class ListAdapter extends ArrayAdapter<Tutorial>
    {
        private int resourceLayout;
        private Context mContext;

        public ListAdapter(Context context, int resource, List<Tutorial> items)
        {
            super(context, resource, items);
            this.resourceLayout = resource;
            this.mContext = context;
        }

        public int getResourceId(String pVariableName, String pResourcename)
        {
            try
            {
                return activity.getResources().getIdentifier(pVariableName, pResourcename, activity.getPackageName());
            } catch (Exception e)
            {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v = convertView;

            if (v == null)
            {
                LayoutInflater vi;
                vi = LayoutInflater.from(mContext);
                v = vi.inflate(resourceLayout, null);
            }

            Tutorial tut = getItem(position);

            if (tut.title != null)
            {
                TextView text = (TextView) v.findViewById(R.id.textView);
                text.setText(tut.title);
            }

            if (tut.icon != null)
            {
                AppCompatImageView icon = v.findViewById(R.id.icon_imageView);
                icon.setImageResource(getResourceId(tut.icon, "drawable"));
            }

            return v;
        }
    }
}
