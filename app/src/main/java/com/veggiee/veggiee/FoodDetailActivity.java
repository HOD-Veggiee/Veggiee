package com.veggiee.veggiee;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.veggiee.veggiee.Model.Food;

public class FoodDetailActivity extends AppCompatActivity {

    TextView foodIItemName,foodItemPrice,foodItemDescription;
    ImageView foodItemImage;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton addToCartButton;
    ElegantNumberButton quantityButton;

    String foodItemId=null;


    //Firebase
    FirebaseDatabase mDatabase;
    DatabaseReference food;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);


        //init views
        foodIItemName=(TextView) findViewById(R.id.foodItemNameText);
        foodItemPrice=(TextView) findViewById(R.id.foodItempPriceText);
        foodItemDescription=(TextView) findViewById(R.id.foodItemDescription);
        quantityButton=(ElegantNumberButton) findViewById(R.id.quantityButton);
        foodItemImage=(ImageView) findViewById(R.id.foodItemImage);
        collapsingToolbarLayout=(CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        food=mDatabase.getReference("Food");


        if(getIntent()!=null)
            foodItemId=getIntent().getStringExtra("FoodItemId");

        if(!foodItemId.isEmpty())
            getFoodItemDetails(foodItemId);
    }

    private void getFoodItemDetails(final String foodItemId) {

        food.child(foodItemId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Food food=dataSnapshot.getValue(Food.class);

                Picasso.get().load(food.getImage()).into(foodItemImage);

                collapsingToolbarLayout.setTitle(food.getName());
                foodItemPrice.setText("RS "+food.getPrice()+"-/");
                foodIItemName.setText(food.getName());
                foodItemDescription.setText(food.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
