package com.veggiee.veggiee;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Interface.ItemClickListener;
import com.veggiee.veggiee.Model.SubCategory;
import com.veggiee.veggiee.Utility.SquareImage;
import com.veggiee.veggiee.ViewHolder.SubCategoryViewHolder;

import java.util.UUID;

public class SubCategoryActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    RecyclerView recycler_sub_category;
    TextView emptySubCategoryText, username;
    LinearLayoutManager mLayoutManager;
    FloatingActionButton fab;

    // Firebase

    FirebaseDatabase db;
    DatabaseReference subCategoryList;

    String categoryId = "";

    FirebaseRecyclerAdapter<SubCategory, SubCategoryViewHolder> adapter;

    ProgressBar mProgressBar;

    NavigationView navigationView;
    String name=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("categoryName").toUpperCase());
        setSupportActionBar(toolbar);

        // Firebase

        db = FirebaseDatabase.getInstance();
        subCategoryList = db.getReference("SubCategory");

        // Init

        emptySubCategoryText = (TextView) findViewById(R.id.emptySubCategoryText);
        username=findViewById(R.id.userName);
        recycler_sub_category=(RecyclerView) findViewById(R.id.SubCategoryListRecyclerView);
        mLayoutManager=new LinearLayoutManager(this);
        recycler_sub_category.setLayoutManager(mLayoutManager);
        //recycler_sub_category.setLayoutManager(new GridLayoutManager(this,2));

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView=navigationView.getHeaderView(0);
        username=(TextView) headerView.findViewById(R.id.userName);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        name=getNameFromSharedPref();
        username.setText(name);

        fab = (FloatingActionButton)findViewById(R.id.viewCartFAB);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent=new Intent(SubCategoryActivity.this,CartActivity.class);
                startActivity(cartIntent);
            }
        });

        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");

        if(!categoryId.isEmpty())
            loadSubCategoryList(categoryId);
    }

    private void loadSubCategoryList(String categoryId) {


        FirebaseRecyclerOptions<SubCategory> options = new FirebaseRecyclerOptions.Builder<SubCategory>()
                .setQuery(subCategoryList.orderByChild("categoryId").equalTo(categoryId), SubCategory.class).build();

        //getting data from Firebase
        adapter=new FirebaseRecyclerAdapter<SubCategory, SubCategoryViewHolder>(options) {
            @NonNull
            @Override
            public SubCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sub_category_item,viewGroup,false);
                return new SubCategoryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull SubCategoryViewHolder holder, int position, @NonNull final SubCategory model) {
                holder.sub_category_name.setText(model.getName());
                //Picasso.get().load(model.getImage()).into(holder.sub_category_image); // For latest version of Picasso (implementation 'com.squareup.picasso:picasso:2.71828')
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.sub_category_image); // Bcz slider support lower version of Picasso, so downgraded to (implementation 'com.squareup.picasso:picasso:2.5.2')

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //get sub_category id and send it to foodlist activity to get foodlist of specific sub_category
                        Intent foodListIntent=new Intent(SubCategoryActivity.this, FoodListActivity.class);
                        foodListIntent.putExtra("subCategoryId",adapter.getRef(position).getKey());
                        foodListIntent.putExtra("subCategoryName",adapter.getItem(position).getName());
                        startActivity(foodListIntent);
                    }
                });
            }
        };

        recycler_sub_category.setAdapter(adapter);

        subCategoryList.orderByChild("categoryId").equalTo(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    emptySubCategoryText.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getNameFromSharedPref()
    {
        SharedPreferences pref=getSharedPreferences("NUM_Info",Context.MODE_PRIVATE);
        String name=pref.getString("name","");
        //Toast.makeText(getApplicationContext(),number,Toast.LENGTH_SHORT).show();

        return name;
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

        Intent orderStatusIntent=new Intent(SubCategoryActivity.this,OrderStatusActivity.class);
        Intent plannerIntent=new Intent(SubCategoryActivity.this,PlannerActivity.class);

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
                Intent loginIntent=new Intent(SubCategoryActivity.this,AuthenticationActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}