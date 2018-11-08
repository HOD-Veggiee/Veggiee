package com.veggiee.veggiee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Interface.ItemClickListener;
import com.veggiee.veggiee.Model.Category;


public class CategoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    FirebaseDatabase mDatabase;
    DatabaseReference category;

    TextView username;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;

    FirebaseRecyclerAdapter<Category,ViewHolder_CategoryItem> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if user is not signed in, sent back to login screen
        if(FirebaseAuth.getInstance().getCurrentUser()==null)
        {
            Intent loginIntent=new Intent(CategoryActivity.this,AuthenticationActivity.class);
            startActivity(loginIntent);
            finish();
        }/**/

        setContentView(R.layout.activity_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Categories");
        setSupportActionBar(toolbar);


        //Log.i("user info","Name: "+Common.currentUser.getName()+"\nemail: "+Common.currentUser.getEmail()+"\nphone: "+Common.currentUser.getPhoneNumber()+"\n");


        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        category=mDatabase.getReference("Category");

        //init views
        username=findViewById(R.id.userName);

        //load recycler view
        mRecyclerView=(RecyclerView) findViewById(R.id.categoriesRecyclerView);
        //mRecyclerView.setHasFixedSize(true);
        mLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        loadCategoriesData();

        adapter.notifyDataSetChanged();



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.viewCartFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Cart will be shown on press", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //setting name of user in app drawer
        View headerView=navigationView.getHeaderView(0);
        username=(TextView) headerView.findViewById(R.id.userName);
        //username.setText(Common.currentUser.getName());
        username.setText("Guest");


    }

    private void loadCategoriesData() {

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class).build();

        //getting data from Firebase
        adapter=new FirebaseRecyclerAdapter<Category, ViewHolder_CategoryItem>(options) {

            @NonNull
            @Override
            public ViewHolder_CategoryItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.category_item_view,viewGroup,false);
                return new ViewHolder_CategoryItem(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder_CategoryItem holder, int position, @NonNull Category model) {
                holder.categoryName.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.categoryImage);

                Log.i("obj","\nimg: "+model.getImage()+"\nname: "+model.getName());

                final Category clickItem=model;

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //get category id and send it to foodlist activity to get foodlist of specific category
                        Intent foodListIntent=new Intent(CategoryActivity.this,FoodListActivity.class);
                        foodListIntent.putExtra("CategoryId",adapter.getRef(position).getKey());
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

        if (id == R.id.logout) {
            // Handle the camera action
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class ViewHolder_CategoryItem extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView categoryImage;
        public TextView categoryName;

        public ItemClickListener itemClickListener;


        public ViewHolder_CategoryItem(@NonNull View itemView) {
            super(itemView);

            categoryImage= (ImageView) itemView.findViewById(R.id.categoryImage);
            categoryName=(TextView) itemView.findViewById(R.id.categoryName);
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
}
