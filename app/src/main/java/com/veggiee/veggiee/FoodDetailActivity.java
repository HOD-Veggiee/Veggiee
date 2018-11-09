package com.veggiee.veggiee;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.veggiee.veggiee.Database.Database;
import com.veggiee.veggiee.Model.Food;
import com.veggiee.veggiee.Model.Order;

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


    Food currentFood;

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
        addToCartButton=(FloatingActionButton) findViewById(R.id.addToCartButton);

        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        food=mDatabase.getReference("Food");


        if(getIntent()!=null)
            foodItemId=getIntent().getStringExtra("FoodItemId");

        if(!foodItemId.isEmpty())
            getFoodItemDetails(foodItemId);

        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Database(getBaseContext()).addToCart(
                        new Order(
                                foodItemId,
                                currentFood.getName(),
                                quantityButton.getNumber(),
                                String.valueOf(Integer.parseInt(currentFood.getPrice())*Integer.parseInt(quantityButton.getNumber())),
                                currentFood.getDiscount()
                        )
                );


                Toast.makeText(getApplicationContext(),"Added to cart.",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getFoodItemDetails(final String foodItemId) {

        food.child(foodItemId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood=dataSnapshot.getValue(Food.class);

                Picasso.get().load(currentFood.getImage()).into(foodItemImage);

                collapsingToolbarLayout.setTitle(currentFood.getName());
                foodItemPrice.setText("RS "+currentFood.getPrice()+" -/");
                foodIItemName.setText(currentFood.getName());
                foodItemDescription.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
