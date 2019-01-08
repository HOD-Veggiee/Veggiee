package com.veggiee.veggiee.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.veggiee.veggiee.Interface.ItemClickListener;
import com.veggiee.veggiee.R;

public class ViewHolder_Planner extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

    public TextView plannerId,plannerStatus,plannerPhoneNumber,plannerAddress, plannerFoodName;


    private ItemClickListener itemClickListener;


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ViewHolder_Planner(@NonNull View itemView) {
        super(itemView);

        plannerId=(TextView) itemView.findViewById(R.id.plannerId);
        plannerFoodName= itemView.findViewById(R.id.plannerFoodName);
        plannerStatus=(TextView) itemView.findViewById(R.id.plannerStatus);
        plannerPhoneNumber=(TextView) itemView.findViewById(R.id.plannerPhoneNumber);
        plannerAddress=(TextView) itemView.findViewById(R.id.plannerAddress);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select Action");

        contextMenu.add(0, 0, getAdapterPosition(), "Update");
        contextMenu.add(0, 1, getAdapterPosition(), "Subscribe/Unsubscribe");
        contextMenu.add(0, 2, getAdapterPosition(), "Delete");
    }
}
