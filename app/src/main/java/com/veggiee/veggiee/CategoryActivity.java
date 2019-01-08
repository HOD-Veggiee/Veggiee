package com.veggiee.veggiee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.veggiee.veggiee.Model.Banner;
import com.veggiee.veggiee.Model.Token;
import com.veggiee.veggiee.Utility.SquareImage;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Interface.ItemClickListener;
import com.veggiee.veggiee.Model.Category;
import com.veggiee.veggiee.Model.User;

import java.util.HashMap;
import java.util.Objects;


public class CategoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    FirebaseDatabase mDatabase;
    DatabaseReference category,user, Banners;

    TextView username, emptyCategoryText;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    String phoneNumber=null,name=null;

    FirebaseRecyclerAdapter<Category,ViewHolder_CategoryItem> adapter;

    ProgressBar mProgressBar;

    NavigationView navigationView;

    HashMap<String, String> image_list;
    SliderLayout mSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Common.isConnectedToInternet(this))
            Toast.makeText(this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();


        //if user is not signed in, sent back to login screen
        if(FirebaseAuth.getInstance().getCurrentUser()==null)
        {
            Intent loginIntent=new Intent(CategoryActivity.this,AuthenticationActivity.class);
            startActivity(loginIntent);
            finish();
        }

        setContentView(R.layout.activity_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Categories");
        setSupportActionBar(toolbar);


        //init views
        emptyCategoryText = (TextView) findViewById(R.id.emptyCategoryText);
        username=findViewById(R.id.userName);
        mRecyclerView=(RecyclerView) findViewById(R.id.categoriesRecyclerView);
        //mLayoutManager=new LinearLayoutManager(this);
        //mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,3));
        mProgressBar=(ProgressBar) findViewById(R.id.progress_bar);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView=navigationView.getHeaderView(0);
        username=(TextView) headerView.findViewById(R.id.userName);


        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        category=mDatabase.getReference("Category");
        user=mDatabase.getReference("User");
        Banners = mDatabase.getReference("Banner");

        //getting phone number from shared preference to load user information from firebase
        phoneNumber=getPhoneNumberFromSharedPref();
        name=getNameFromSharedPref();
        username.setText(name);



        //=======================================
        //IMPORTANT; If phoneNumber is null it means Shared prefs are removed or modified, hence send back user to login
        //=======================================

        if(phoneNumber==null || phoneNumber.isEmpty())
        {
            Intent loginIntent=new Intent(CategoryActivity.this,AuthenticationActivity.class);
            startActivity(loginIntent);
            finish();
        }

/*        user.orderByChild("phoneNumber").equalTo(phoneNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(Common.currentUser==null)
                {
                    for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                        if(Objects.requireNonNull(userSnapshot.getValue(User.class)).getPhoneNumber().equals(phoneNumber))
                        {
                            Common.currentUser= new User(
                                    Objects.requireNonNull(userSnapshot.getValue(User.class)).getName(),
                                    Objects.requireNonNull(userSnapshot.getValue(User.class)).getEmail(),
                                    Objects.requireNonNull(userSnapshot.getValue(User.class)).getDeliveryAddress(),
                                    Objects.requireNonNull(userSnapshot.getValue(User.class)).getPhoneNumber()
                            );
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        BackgroundTasks backgroundTasks=new BackgroundTasks();
        backgroundTasks.execute();

        loadCategoriesData();

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(CategoryActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                updateToken(token);
                Log.i("ttokennn", token);
            }
        });

        /*adapter.notifyDataSetChanged();*/


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.viewCartFAB);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent=new Intent(CategoryActivity.this,CartActivity.class);
                startActivity(cartIntent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Setup Slider
        setupSlider();
    }

    private void setupSlider() {
        mSlider =findViewById(R.id.slider);

        image_list = new HashMap<>();

        Banners.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot .getChildren())
                {
                    Banner banner = postSnapshot.getValue(Banner.class);
                    image_list.put(banner.getName(), banner.getImage());
                }

                for (String key:image_list.keySet())
                {
                    // Create Slider
                    DefaultSliderView defaultSliderView = new DefaultSliderView(getBaseContext());
                    defaultSliderView
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit);

                    mSlider.addSlider(defaultSliderView);
                    Banners.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.Stack);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setDuration(4000);
    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token, false); // False bcz sending from client app
        tokens.child(getPhoneNumberFromSharedPref()).setValue(data);
    }

    public class BackgroundTasks extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            user.orderByChild("phoneNumber").equalTo(phoneNumber).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(Common.currentUser==null)
                    {
                        for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                            if(Objects.requireNonNull(userSnapshot.getValue(User.class)).getPhoneNumber().equals(phoneNumber))
                            {
                                Common.currentUser= new User(
                                        Objects.requireNonNull(userSnapshot.getValue(User.class)).getName(),
                                        Objects.requireNonNull(userSnapshot.getValue(User.class)).getEmail(),
                                        Objects.requireNonNull(userSnapshot.getValue(User.class)).getDeliveryAddress(),
                                        Objects.requireNonNull(userSnapshot.getValue(User.class)).getPhoneNumber()
                                );
                                break;
                            }
                        }

                        //Common.currentUser.printUser();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
            /*username.setText(Common.currentUser.getName());*/
        }
    }


    private String getPhoneNumberFromSharedPref() {

        SharedPreferences pref=getSharedPreferences("NUM_Info",Context.MODE_PRIVATE);
        String number=pref.getString("phoneNumber","");
        //Toast.makeText(getApplicationContext(),number,Toast.LENGTH_SHORT).show();

        return number;
    }

    private String getNameFromSharedPref()
    {
        SharedPreferences pref=getSharedPreferences("NUM_Info",Context.MODE_PRIVATE);
        String name=pref.getString("name","");
        //Toast.makeText(getApplicationContext(),number,Toast.LENGTH_SHORT).show();

        return name;
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
                //Picasso.get().load(model.getImage()).into(holder.categoryImage); // For latest version of Picasso (implementation 'com.squareup.picasso:picasso:2.71828')
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.categoryImage); // Bcz slider support lower version of Picasso, so downgraded to (implementation 'com.squareup.picasso:picasso:2.5.2')

                Log.i("obj","\nimg: "+model.getImage()+"\nname: "+model.getName());

                final Category clickItem=model;

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //get category id and send it to foodlist activity to get foodlist of specific category
                        Intent subCategoryIntent=new Intent(CategoryActivity.this,SubCategoryActivity.class);
                        subCategoryIntent.putExtra("CategoryId",adapter.getRef(position).getKey());
                        subCategoryIntent.putExtra("categoryName",adapter.getItem(position).getName());
                        startActivity(subCategoryIntent);
                    }
                });
            }
        };

        mRecyclerView.setAdapter(adapter);

        category.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    emptyCategoryText.setVisibility(View.GONE);
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
            mSlider.startAutoCycle();
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

        Intent orderStatusIntent=new Intent(CategoryActivity.this,OrderStatusActivity.class);
        Intent plannerIntent=new Intent(CategoryActivity.this,PlannerActivity.class);

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
                Intent loginIntent=new Intent(CategoryActivity.this,AuthenticationActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static class ViewHolder_CategoryItem extends RecyclerView.ViewHolder implements View.OnClickListener {

        public SquareImage categoryImage;
        public TextView categoryName;

        public ItemClickListener itemClickListener;


        public ViewHolder_CategoryItem(@NonNull View itemView) {
            super(itemView);

            categoryImage= (SquareImage) itemView.findViewById(R.id.categoryImage);
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