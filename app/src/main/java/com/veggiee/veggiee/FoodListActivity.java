package com.veggiee.veggiee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.veggiee.veggiee.Common.Common;
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


    TextView emptyFoodText, username;
    ProgressBar mProgressBar;

    NavigationView navigationView;
    String name=null;

    FirebaseRecyclerAdapter<Food,ViewHolder_Item> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("subCategoryName").toUpperCase());
        setSupportActionBar(toolbar);




        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        food=mDatabase.getReference("Food");


        //init views
        emptyFoodText = (TextView) findViewById(R.id.emptyFoodText);
        username=findViewById(R.id.userName);
        mRecyclerView=(RecyclerView) findViewById(R.id.FoodListRecyclerView);
        mLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
      /* mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));*/

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView=navigationView.getHeaderView(0);
        username=(TextView) headerView.findViewById(R.id.userName);

        //getting category id from category screen
        if(getIntent()!=null)
        {
            categoryId=getIntent().getStringExtra("subCategoryId");

        }

        if(!categoryId.isEmpty())
        {
            if(Common.isConnectedToInternet(getBaseContext()))
                loadFoodList(categoryId);
            else
                Toast.makeText(FoodListActivity.this, "Please Check your Internet Connection", Toast.LENGTH_LONG).show();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.viewCartFAB);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent=new Intent(FoodListActivity.this,CartActivity.class);
                startActivity(cartIntent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        name=getNameFromSharedPref();
        username.setText(name);
    }

    private String getNameFromSharedPref()
    {
        SharedPreferences pref=getSharedPreferences("NUM_Info",Context.MODE_PRIVATE);
        String name=pref.getString("name","");
        //Toast.makeText(getApplicationContext(),number,Toast.LENGTH_SHORT).show();

        return name;
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
                holder.foodPrice.setText(model.getPrice());
 //               holder.ratingBar.setRating(Float.parseFloat(model.getRating)); Needs to change Food model to get Average rating from Rating model
                //Picasso.get().load(model.getImage()).into(holder.foodImage); // For latest version of Picasso (implementation 'com.squareup.picasso:picasso:2.71828')
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.foodImage); // Bcz slider support lower version of Picasso, so downgraded to (implementation 'com.squareup.picasso:picasso:2.5.2')

                Log.i("obj","\nimg: "+model.getImage()+"\nname: "+model.getName());

                final Food clickItem=model;

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //get food item id and send it to food detail activity to get food detail of specific food
                        Intent foodListIntent=new Intent(FoodListActivity.this,FoodDetailActivity.class);
                        foodListIntent.putExtra("FoodItemId",adapter.getRef(position).getKey());
                        startActivity(foodListIntent);
                    }
                });
            }
        };

        mRecyclerView.setAdapter(adapter);

        food.orderByChild("menuId").equalTo(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    emptyFoodText.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

        Intent orderStatusIntent=new Intent(FoodListActivity.this,OrderStatusActivity.class);
        Intent plannerIntent=new Intent(FoodListActivity.this,PlannerActivity.class);

        switch (id)
        {
            case R.id.orderStatus:
                startActivity(orderStatusIntent);
                break;

            case R.id.orderHistory:
                orderStatusIntent.putExtra("order_status", "completed");
                startActivity(orderStatusIntent);
                break;

            case R.id.subscribedPlanners:
                startActivity(plannerIntent);
                break;

            case R.id.unSubscribedPlanners:
                plannerIntent.putExtra("planner_status", "unsubscribed");
                startActivity(plannerIntent);
                break;

            case R.id.logout:
                Intent loginIntent=new Intent(FoodListActivity.this,AuthenticationActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class ViewHolder_Item extends RecyclerView.ViewHolder implements View.OnClickListener{

        public SquareImage foodImage;
        public TextView foodName;
        public TextView foodPrice;
        public RatingBar ratingBar;

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public ItemClickListener itemClickListener;

        public ViewHolder_Item(@NonNull View itemView) {
            super(itemView);

            foodImage= (SquareImage) itemView.findViewById(R.id.foodImage);
            foodName=(TextView) itemView.findViewById(R.id.foodName);
            foodPrice=(TextView) itemView.findViewById(R.id.foodPrice);
            ratingBar=(RatingBar) itemView.findViewById(R.id.ratingBar);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            itemClickListener.onClick(view,getAdapterPosition(),false);

        }
    }

}
