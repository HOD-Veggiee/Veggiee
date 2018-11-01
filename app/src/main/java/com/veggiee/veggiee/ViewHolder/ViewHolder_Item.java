package com.veggiee.veggiee.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.veggiee.veggiee.Interface.ItemClickListener;
import com.veggiee.veggiee.R;

/*
public class ViewHolder_Item extends RecyclerView.ViewHolder implements View.OnClickListener{

    public ImageView foodImage;
    public TextView foodName;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ItemClickListener itemClickListener;

    public ViewHolder_Item(@NonNull View itemView) {
        super(itemView);

        foodImage= (ImageView) itemView.findViewById(R.id.foodImage);
        foodName=(TextView) itemView.findViewById(R.id.foodName);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);

    }
}
*/
