package com.veggiee.veggiee;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Database.Database;
import com.veggiee.veggiee.Model.Order;
import com.veggiee.veggiee.Model.Request;
import com.veggiee.veggiee.ViewHolder.CartAdapter;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    LinearLayoutManager mLinearLayoutManager;

    //TextView totalPriceText;
    AppCompatButton placeOrderButton;
    AppCompatEditText addressText;
    boolean isNewAddress;
    String totalPrice=null;
    TextView emptyCartText;


    //firebase
    FirebaseDatabase mDatabase;
    DatabaseReference requests;


    //cart
    List<Order> cartItems=new ArrayList<>();
    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //init views
        mRecyclerView=(RecyclerView) findViewById(R.id.cartRecyclerView);
        mLinearLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        emptyCartText=(TextView) findViewById(R.id.emptyCartText);

        //totalPriceText=(TextView) findViewById(R.id.totalAmount);
        placeOrderButton=(AppCompatButton) findViewById(R.id.placeOrderButton);


        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        requests=mDatabase.getReference("Request");

        //onclicks
        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cartItems.size()>0)
                {
                    ShowBill();
                }
            }
        });


        loadCartItems();

        swipeToDelete();
    }

    private void ShowBill() {

        calculateBill();


        final AlertDialog.Builder alertDialog=new AlertDialog.Builder(CartActivity.this);
        alertDialog.setTitle("Bill");
        alertDialog.setMessage("Total bill is Rs "+totalPrice+"-/\nPress Confirm to proceed to checkout");
        alertDialog.setIcon(R.drawable.ic_amount);

        alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //first show user his saved address, if he/she not uses that then ask for new address
                ShowSavedAddressDialog();

            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });


        alertDialog.show();
    }

    private void ShowSavedAddressDialog() {

        final AlertDialog.Builder alertDialog=new AlertDialog.Builder(CartActivity.this);
        alertDialog.setTitle("Delivery Address");
        alertDialog.setMessage("Is this your address?"+"\n"+Common.currentUser.getDeliveryAddress().toUpperCase());
        alertDialog.setIcon(R.drawable.ic_add_address);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //push req to db and clean cart
                placeOrderAndClearCart();

            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                ShowAddressDialog();

            }
        });


        alertDialog.show();
    }

    private void ShowAddressDialog() {

        AlertDialog.Builder alertDialog=new AlertDialog.Builder(CartActivity.this);
        alertDialog.setTitle("Delivery Address");
        alertDialog.setMessage("Enter your address.");

        addressText=new AppCompatEditText(CartActivity.this);
        LinearLayout.LayoutParams parameters=new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        addressText.setLayoutParams(parameters);
        alertDialog.setView(addressText);
        alertDialog.setIcon(R.drawable.ic_add_address);


        alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //push req to db and clean cart
                isNewAddress=true;
                placeOrderAndClearCart();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                isNewAddress=false;

            }
        });


        alertDialog.show();
    }

    private void swipeToDelete()
    {
        ItemTouchHelper.SimpleCallback simpleCallback=new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                adapter.removeItem(viewHolder.getAdapterPosition());

                if(adapter.cartList.size()==0)
                {
                    mRecyclerView.setVisibility(View.GONE);
                    emptyCartText.setVisibility(View.VISIBLE);

                }

            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(mRecyclerView);
    }

    private void calculateBill()
    {
        cartItems=new Database(this).getCartItems();

        //calculating total Price

        int total=0;

        for(Order order:cartItems)
        {
            total+=Integer.parseInt(order.getPrice());
        }


        //totalPriceText.setText(String.valueOf(total));
        totalPrice=String.valueOf(total);
    }

    private void loadCartItems() {

        cartItems=new Database(this).getCartItems();
        adapter=new CartAdapter(cartItems,this);
        mRecyclerView.setAdapter(adapter);

        if(adapter.cartList.size()>0)
        {
            emptyCartText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }

    }

    private void placeOrderAndClearCart()
    {
        String address=null;

        if(isNewAddress)
            address=addressText.getText().toString().toUpperCase();
        else
            address=Common.currentUser.getDeliveryAddress();

        Request request=new Request(
                Common.currentUser.getPhoneNumber(),
                address,
                totalPrice,
                cartItems
        );

        //pushing order to Firebase. Current time in miliseconds will be used as order key to keep it unique
        requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);

        //cleaning cart
        new Database(this).cleanCart();
        Toast.makeText(getApplicationContext(),"Order Placed",Toast.LENGTH_LONG).show();
        finish();
    }
}
