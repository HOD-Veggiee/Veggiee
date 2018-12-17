package com.veggiee.veggiee;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.veggiee.veggiee.Interface.ItemClickListener;
import com.veggiee.veggiee.Model.Food;
import com.veggiee.veggiee.Utility.SquareImage;

public class FoodListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;

    //Firebase
    FirebaseDatabase mDatabase;
    DatabaseReference food;


    //categoryId
    String categoryId=null;

    FirebaseRecyclerAdapter<Food,ViewHolder_Item> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Items");
        setSupportActionBar(toolbar);




        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        food=mDatabase.getReference("Food");


        //init views
        mRecyclerView=(RecyclerView) findViewById(R.id.FoodListRecyclerView);
       mLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
      /* mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));*/


        //getting category id from category screen
        if(getIntent()!=null)
        {
            categoryId=getIntent().getStringExtra("CategoryId");

        }


        if(!categoryId.isEmpty())
        {
            loadFoodList(categoryId);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.viewCartFAB);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent=new Intent(FoodListActivity.this,CartActivity.class);
                startActivity(cartIntent);
            }
        });

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
    }

    private void loadFoodList(String categoryId) {


        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(food.orderByChild("menuId").equalTo(categoryId), Food.class).build();

        //getting data from Firebase
        adapter=new FirebaseRecyclerAdapter<Food, ViewHolder_Item>(options) {
            @NonNull
            @Override
            public ViewHolder_Item onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_list_item_view,viewGroup,false);
                return new ViewHolder_Item(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder_Item holder, int position, @NonNull final Food model) {
                holder.foodName.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.foodImage);

                Log.i("obj","\nimg: "+model.getImage()+"\nname: "+model.getName());

                final Food clickItem=model;

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Toast.makeText(getApplicationContext(),""+clickItem.getDescription(),Toast.LENGTH_SHORT).show();

                        //get food item id and send it to food detail activity to get food detail of specific food
                        Intent foodListIntent=new Intent(FoodListActivity.this,FoodDetailActivity.class);
                        foodListIntent.putExtra("FoodItemId",adapter.getRef(position).getKey());
                        startActivity(foodListIntent);
                    }
                });
            }
        };

        mRecyclerView.setAdapter(adapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.logout:
                Intent loginIntent=new Intent(FoodListActivity.this,AuthenticationActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
                break;

            case R.id.orderStatus:
                Intent orderStatusIntent=new Intent(FoodListActivity.this,OrderStatusActivity.class);
                startActivity(orderStatusIntent);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class ViewHolder_Item extends RecyclerView.ViewHolder implements View.OnClickListener{

        public SquareImage foodImage;
        public TextView foodName;

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public ItemClickListener itemClickListener;

        public ViewHolder_Item(@NonNull View itemView) {
            super(itemView);

            foodImage= (SquareImage) itemView.findViewById(R.id.foodImage);
            foodName=(TextView) itemView.findViewById(R.id.foodName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            itemClickListener.onClick(view,getAdapterPosition(),false);

        }
    }

}
