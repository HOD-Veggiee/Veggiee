package com.veggiee.veggiee.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.veggiee.veggiee.CartActivity;
import com.veggiee.veggiee.Database.Database;
import com.veggiee.veggiee.Interface.ItemClickListener;
import com.veggiee.veggiee.Model.Order;
import com.veggiee.veggiee.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView cartItemName,cartItemPrice;
    public ElegantNumberButton cartItemCount;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);

        cartItemName=(TextView) itemView.findViewById(R.id.cartItemName);
        cartItemPrice=(TextView) itemView.findViewById(R.id.cartItemPrice);
        cartItemCount=(ElegantNumberButton) itemView.findViewById(R.id.cartItemCount);
    }

    @Override
    public void onClick(View view) {

    }
}
public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    public List<Order> cartList=new ArrayList<>();
    private Context context;

    public CartAdapter(List<Order> cart, Context context) {
        this.cartList=cart;
        this.context=context;

    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(context);

        View view=inflater.inflate(R.layout.cart_item_view,viewGroup,false);

        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CartViewHolder cartViewHolder,final int i) {

        cartViewHolder.cartItemCount.setNumber(cartList.get(i).getQuantity());

        cartViewHolder.cartItemPrice.setText(String.valueOf(Integer.parseInt(cartList.get(i).getQuantity())*Integer.parseInt(cartList.get(i).getPrice())));

        cartViewHolder.cartItemName.setText(cartList.get(i).getProductName());

        final int pricePerItem=Integer.parseInt(cartViewHolder.cartItemPrice.getText().toString())/Integer.parseInt(cartList.get(i).getQuantity());

        Toast.makeText(context,""+pricePerItem,Toast.LENGTH_SHORT).show();

        cartViewHolder.cartItemCount.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {

                cartList.get(i).setPrice(String.valueOf(newValue*pricePerItem));
                cartViewHolder.cartItemPrice.setText(cartList.get(i).getPrice());

                new Database(context).updateCartItem(cartList.get(i).getProductId(),newValue,cartList.get(i).getPrice());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public void removeItem(int position)
    {
        new Database(context).deleteItem(cartList.get(position).getProductId());
        cartList.remove(position);
        notifyItemRemoved(position);
    }

}
