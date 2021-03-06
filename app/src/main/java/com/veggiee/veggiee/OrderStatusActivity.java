package com.veggiee.veggiee;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Interface.ItemClickListener;
import com.veggiee.veggiee.Model.Order;
import com.veggiee.veggiee.Model.Request;
import com.veggiee.veggiee.ViewHolder.ViewHolder_Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrderStatusActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    LinearLayoutManager mLinearLayout;

    //Firebase
    FirebaseDatabase mDatabase;
    DatabaseReference requests;

    TextView emptyOrderText;


    FirebaseRecyclerAdapter<Request,ViewHolder_Order> adapter;

    //this list will be shown to user for food list when clicked on a specific order
    Map<String,Request> previousOrdersDetailList=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //init views
        emptyOrderText = (TextView)findViewById(R.id.emptyOrderText);
        mRecyclerView=(RecyclerView) findViewById(R.id.ordersListRecyclerView);
        mLinearLayout=new LinearLayoutManager (this);
        mRecyclerView.setLayoutManager(mLinearLayout);

        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        requests=mDatabase.getReference("Request");


        if (Common.isConnectedToInternet(getBaseContext()))
            loadOrders(Common.currentUser.getPhoneNumber());
        else
            Toast.makeText(this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();

        /*
        if(getIntent() == null){
            Toast.makeText(OrderStatusActivity.this, "= Common > " + Common.currentUser.getPhoneNumber() + " <=", Toast.LENGTH_LONG).show();
            loadOrders(Common.currentUser.getPhoneNumber());
        }
        else
        {
            Toast.makeText(OrderStatusActivity.this, "= userPhone > " + getIntent().getStringExtra("userPhone") + " <=", Toast.LENGTH_LONG).show();
            loadOrders(getIntent().getStringExtra("userPhone"));
        }/**/
    }

    private void loadOrders(String phoneNumber) {

        Query query = requests.orderByChild("phone_status").equalTo(phoneNumber + "_incomplete");

        if(getIntent().getStringExtra("order_status") != null)
            query = requests.orderByChild("phone_status").equalTo(phoneNumber + "_" + getIntent().getStringExtra("order_status"));

        FirebaseRecyclerOptions<Request> options=new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(query,Request.class).build();

        adapter=new FirebaseRecyclerAdapter<Request, ViewHolder_Order>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder_Order holder, int position, @NonNull final Request model) {

                holder.orderId.setText(adapter.getRef(position).getKey());
                holder.orderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                holder.orderPhoneNumber.setText(model.getPhone());
                holder.orderAddress.setText(model.getAddress());

                previousOrdersDetailList.put(
                        holder.orderId.getText().toString(),
                        new Request(
                                model.getPhone(),
                                model.getAddress(),
                                model.getTotal(),
                                model.getFoods()
                        )
                );

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Request currentOrder=new Request();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            currentOrder.setFoods(Objects.requireNonNull(previousOrdersDetailList.getOrDefault(holder.orderId.getText().toString(), null)).getFoods());
                        }
                        else
                        {
                            currentOrder.setFoods(previousOrdersDetailList.get(holder.orderId.getText().toString()).getFoods());
                        }

                        currentOrder.setTotal(model.getTotal());

                        AlertDialog.Builder foodListDialog=new AlertDialog.Builder(OrderStatusActivity.this);
                        foodListDialog.setTitle("Order Items List");
                        foodListDialog.setIcon(R.drawable.ic_shopping_cart);

                        StringBuilder foods= new StringBuilder();

                        for(int i=0; i<currentOrder.getFoods().size();i++)
                        {
                            foods.append(
                                    currentOrder.getFoods().get(i).getProductName()
                                            +" x "
                                            +currentOrder.getFoods().get(i).getQuantity()
                                            +"\t( Rs "+currentOrder.getFoods().get(i).getPrice()+" )").append("\n");
                        }



                        foods.append("\nTotal Amount: Rs ").append(currentOrder.getTotal()).append("-/");
                        foodListDialog.setMessage(foods.toString());


                        foodListDialog.setNeutralButton("Okay",null);


                        foodListDialog.show();
                    }
                });
            }

            @NonNull
            @Override
            public ViewHolder_Order onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_item_view,viewGroup,false);
                return new ViewHolder_Order(view);
            }
        };

        mRecyclerView.setAdapter(adapter);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    emptyOrderText.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (Common.isConnectedToInternet(getBaseContext()))
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Common.isConnectedToInternet(getBaseContext()))
            adapter.stopListening();
    }
}
