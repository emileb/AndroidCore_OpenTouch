package com.opentouchgaming.androidcore;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;


public class SubGameRecyclerViewAdapter extends RecyclerView.Adapter<SubGameRecyclerViewAdapter.ViewHolder>
{

    private final ArrayList<SubGame> mValues;

    public SubGameRecyclerViewAdapter(ArrayList<SubGame> items)
    {
        mValues = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_subgame, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        holder.mItem = mValues.get(position);

        holder.mIdView.setText(holder.mItem.title);
        holder.mDetail1.setText(holder.mItem.detail1);
        holder.mDetail2.setText(holder.mItem.detail2);
        if( holder.mItem.getImagePng() != null )
        {
            Glide.with(holder.mImage)
                    .load(holder.mItem.getImagePng())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                    .into(holder.mImage);

          //  holder.mImage.setScaleType(ImageView.ScaleType.FIT_XY);
           // holder.mImage.setImageURI(Uri.fromFile( new File(holder.mItem.getImagePng()) ));
        }
        else
            holder.mImage.setImageResource(holder.mItem.image);

        if (holder.mItem.selected)
        {
            holder.mView.setBackgroundResource(R.drawable.subgame_selected);
        } else
        {
            holder.mView.setBackgroundResource(0);
        }
    }

    @Override
    public int getItemCount()
    {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final ImageView mImage;
        public final ImageView mGameTypeImage;
        public final TextView mIdView;
        public final TextView mDetail1;
        public final TextView mDetail2;

        public SubGame mItem;

        public ViewHolder(View view)
        {
            super(view);
            mView          = view;
            mIdView        =  view.findViewById(R.id.textview_iwad_title);
            mDetail1       =  view.findViewById(R.id.textview_iwad_detail1);
            mDetail2       =  view.findViewById(R.id.textview_iwad_detail2);
            mImage         =  view.findViewById(R.id.imageview);
            mGameTypeImage = view.findViewById(R.id.game_type_imageview);
        }
    }
}
