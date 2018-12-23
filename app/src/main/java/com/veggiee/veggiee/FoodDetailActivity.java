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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;
import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Database.Database;
import com.veggiee.veggiee.Model.Food;
import com.veggiee.veggiee.Model.Order;
import com.veggiee.veggiee.Model.Rating;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FoodDetailActivity extends AppCompatActivity implements RatingDialogListener {

    TextView foodIItemName,foodItemPrice,foodItemDescription,foodItemDiscount;
    ImageView foodItemImage;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton addToCartButton, btnRating;
    ElegantNumberButton quantityButton;
    RatingBar ratingBar;

    String foodItemId=null;


    //Firebase
    FirebaseDatabase mDatabase;
    DatabaseReference food;
    DatabaseReference ratingTbl;


    Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);


        //init views
        foodIItemName=(TextView) findViewById(R.id.foodItemNameText);
        foodItemPrice=(TextView) findViewById(R.id.foodItempPriceText);
        foodItemDescription=(TextView) findViewById(R.id.foodItemDescription);
        foodItemDiscount=(TextView) findViewById(R.id.foodItempDiscountText);
        quantityButton=(ElegantNumberButton) findViewById(R.id.quantityButton);
        foodItemImage=(ImageView) findViewById(R.id.foodItemImage);
        collapsingToolbarLayout=(CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);
        addToCartButton=(FloatingActionButton) findViewById(R.id.addToCartButton);
        btnRating = (FloatingActionButton) findViewById(R.id.btn_rating);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();
            }
        });

        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        food=mDatabase.getReference("Food");
        ratingTbl = mDatabase.getReference("Rating");


        if(getIntent()!=null)
            foodItemId=getIntent().getStringExtra("FoodItemId");

        if(!foodItemId.isEmpty())
            if(Common.isConnectedToInternet(getBaseContext()))
            {
                getFoodItemDetails(foodItemId);
                getFoodItemRating(foodItemId);
            }
            else
                Toast.makeText(FoodDetailActivity.this, "Please Check your Internet Connection", Toast.LENGTH_LONG).show();


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

    public void getFoodItemRating(String foodItemId) {
        Query foodrating = ratingTbl.child(Common.currentUser.getPhoneNumber()).orderByChild("foodId").equalTo(foodItemId);

        foodrating.addValueEventListener(new ValueEventListener() {

            int count = 0, sum = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum += Integer.parseInt(item.getRateValue());
                    count++;
                }

                if (count != 0)
                {
                    float average = sum/count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not Good", "Quite Ok", "Very Good", "Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetailActivity.this)
                .show();
    }

    private void getFoodItemDetails(final String foodItemId) {

        food.child(foodItemId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood=dataSnapshot.getValue(Food.class);

                Picasso.get().load(currentFood.getImage()).into(foodItemImage);

//                collapsingToolbarLayout.setTitle(currentFood.getName());
                foodItemPrice.setText(getString(R.string.label_price)+currentFood.getPrice()+getString(R.string.label_price_sign));
                foodIItemName.setText(currentFood.getName());
                foodItemDescription.setText(currentFood.getDescription());
                foodItemDiscount.setText(getString(R.string.label_discount)+currentFood.getDiscount()+getString(R.string.label_discount_sign));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comments) {
        // Get Rating and upload to Firebase
        final Rating rating = new Rating(Common.currentUser.getPhoneNumber(),
                foodItemId,
                String.valueOf(value),
                comments);

        ratingTbl.child(Common.currentUser.getPhoneNumber()).child(foodItemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Common.currentUser.getPhoneNumber()).child(foodItemId).exists())
                {
                    // Remove old value
                    ratingTbl.child(Common.currentUser.getPhoneNumber()).child(foodItemId).removeValue();

                    //Update new value
                    ratingTbl.child(Common.currentUser.getPhoneNumber()).child(foodItemId).setValue(rating);
                }
                else
                {
                    //Add new value
                    ratingTbl.child(Common.currentUser.getPhoneNumber()).child(foodItemId).setValue(rating);
                }

                getFoodItemRating(foodItemId);
                Toast.makeText(FoodDetailActivity.this, "Thank you for your feedback !!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
