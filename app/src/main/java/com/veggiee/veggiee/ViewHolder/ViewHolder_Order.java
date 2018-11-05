package com.veggiee.veggiee.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.veggiee.veggiee.Interface.ItemClickListener;
import com.veggiee.veggiee.R;

public class ViewHolder_Order extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView orderId,orderStatus,orderPhoneNumber,orderAddress;


    private ItemClickListener itemClickListener;


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ViewHolder_Order(@NonNull View itemView) {
        super(itemView);

        orderId=(TextView) itemView.findViewById(R.id.orderId);
        orderStatus=(TextView) itemView.findViewById(R.id.orderStatus);
        orderPhoneNumber=(TextView) itemView.findViewById(R.id.orderPhoneNumber);
        orderAddress=(TextView) itemView.findViewById(R.id.orderAddress);

        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);

    }
}
