package com.veggiee.veggiee.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Interface.ItemClickListener;
import com.veggiee.veggiee.R;
import com.veggiee.veggiee.Utility.SquareImage;

public class SubCategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{

    public SquareImage sub_category_image;
    public TextView sub_category_name;

    public ItemClickListener itemClickListener;


    public SubCategoryViewHolder(@NonNull View itemView) {
        super(itemView);

        sub_category_image = (SquareImage) itemView.findViewById(R.id.sub_category_Image);
        sub_category_name = (TextView) itemView.findViewById(R.id.sub_category_name);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);

    }
}