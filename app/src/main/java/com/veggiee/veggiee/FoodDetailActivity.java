package com.veggiee.veggiee;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;
import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Database.Database;
import com.veggiee.veggiee.Model.Food;
import com.veggiee.veggiee.Model.MyResponse;
import com.veggiee.veggiee.Model.Notification;
import com.veggiee.veggiee.Model.Order;
import com.veggiee.veggiee.Model.Planner;
import com.veggiee.veggiee.Model.Rating;
import com.veggiee.veggiee.Model.Sender;
import com.veggiee.veggiee.Model.Token;
import com.veggiee.veggiee.Model.WeekDay;
import com.veggiee.veggiee.Remote.APIService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodDetailActivity extends AppCompatActivity implements RatingDialogListener {

    TextView foodIItemName,foodItemPrice,foodItemDescription,foodItemDiscount,itemPriceTxt,weeklyBillTxt, foodNameTxtView;
    ImageView foodItemImage;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton addToCartButton, btnRating, addToPlannerButton;
    ElegantNumberButton quantityButton, mondayQuantityButton, tuesdayQuantityButton,wednesdayQuantityButton, thursdayQuantityButton, fridayQuantityButton, saturdayQuantityButton, sundayQuantityButton;
    RatingBar ratingBar;

    String foodItemId=null;

    //Firebase
    FirebaseDatabase mDatabase;
    DatabaseReference food, ratingTbl, plannerTbl;

    Food currentFood;

    MaterialSpinner deliveryTimeSpinner;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        //init Service
        mService = Common.getFCMService();

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
        addToPlannerButton = (FloatingActionButton) findViewById(R.id.addToPlannerButton);
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
        plannerTbl = mDatabase.getReference("Planner");

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

        addToPlannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSchedulerDialog();
            }
        });
    }

    private void showSchedulerDialog() {

        final int[] weeklyBill = new int[8];
        final int itemPrice = Integer.parseInt(currentFood.getPrice());
        final List<WeekDay> days=new ArrayList<>();

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodDetailActivity.this);

        LayoutInflater layoutInflater = this.getLayoutInflater();
        final View add_planner_layout = layoutInflater.inflate(R.layout.add_new_planner_layout, null);

        final AppCompatButton startDatePickerBtn;

        // Init View
        itemPriceTxt = (TextView) add_planner_layout.findViewById(R.id.itemPriceTxt);
        itemPriceTxt.setText("Price: " + itemPrice + " Rs.");
        weeklyBillTxt = (TextView) add_planner_layout.findViewById(R.id.weeklyBillTxt);
        foodNameTxtView = add_planner_layout.findViewById(R.id.foodNameTxtView);
        foodNameTxtView.setText("(" + currentFood.getName().toUpperCase() + ")");
        startDatePickerBtn = (AppCompatButton) add_planner_layout.findViewById(R.id.startDatePicker);
        deliveryTimeSpinner = add_planner_layout.findViewById(R.id.deliveryTimeSpinner);
        deliveryTimeSpinner.setItems("Select Delivery Time Slot", "09AM - 01PM ", "01PM - 05PM", "05PM - 09PM");
        mondayQuantityButton = add_planner_layout.findViewById(R.id.mondayQuantityButton);
        tuesdayQuantityButton = add_planner_layout.findViewById(R.id.tuesdayQuantityButton);
        wednesdayQuantityButton = add_planner_layout.findViewById(R.id.wednesdayQuantityButton);
        thursdayQuantityButton = add_planner_layout.findViewById(R.id.thursdayQuantityButton);
        fridayQuantityButton = add_planner_layout.findViewById(R.id.fridayQuantityButton);
        saturdayQuantityButton = add_planner_layout.findViewById(R.id.saturdayQuantityButton);
        sundayQuantityButton = add_planner_layout.findViewById(R.id.sundayQuantityButton);

//        CompoundButton.OnCheckedChangeListener multiSwitchListener = new CompoundButton.OnCheckedChangeListener() {
//
//            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
//                switch (v.getId()){
//                    case R.id.mondaySwitch:
//                        if (isChecked)
//                            mondayQuantityButton.setVisibility(View.VISIBLE);
//                        else
//                        {
//                            mondayQuantityButton.setNumber("0");
//                            mondayQuantityButton.setVisibility(View.GONE);
//                        }
//                        break;
//                    case R.id.tuesdaySwitch:
//                        if (isChecked)
//                            tuesdayQuantityButton.setVisibility(View.VISIBLE);
//                        else
//                        {
//                            tuesdayQuantityButton.setNumber("0");
//                            tuesdayQuantityButton.setVisibility(View.GONE);
//                        }
//                        break;
//                    case R.id.wednesdaySwitch:
//                        if (isChecked)
//                            wednesdayQuantityButton.setVisibility(View.VISIBLE);
//                        else
//                        {
//                            wednesdayQuantityButton.setNumber("0");
//                            wednesdayQuantityButton.setVisibility(View.GONE);
//                        }
//                        break;
//                    case R.id.thursdaySwitch:
//                        if (isChecked)
//                            thursdayQuantityButton.setVisibility(View.VISIBLE);
//                        else
//                        {
//                            thursdayQuantityButton.setNumber("0");
//                            thursdayQuantityButton.setVisibility(View.GONE);
//                        }
//                        break;
//                    case R.id.fridaySwitch:
//                        if (isChecked)
//                            fridayQuantityButton.setVisibility(View.VISIBLE);
//                        else
//                        {
//                            fridayQuantityButton.setNumber("0");
//                            fridayQuantityButton.setVisibility(View.GONE);
//                        }
//                        break;
//                    case R.id.saturdaySwitch:
//                        if (isChecked)
//                            saturdayQuantityButton.setVisibility(View.VISIBLE);
//                        else
//                        {
//                            saturdayQuantityButton.setNumber("0");
//                            saturdayQuantityButton.setVisibility(View.GONE);
//                        }
//                        break;
//                    case R.id.sundaySwitch:
//                        if (isChecked)
//                            sundayQuantityButton.setVisibility(View.VISIBLE);
//                        else
//                        {
//                            sundayQuantityButton.setNumber("0");
//                            sundayQuantityButton.setVisibility(View.GONE);
//                        }
//                        break;
//                }
//            }
//        };
//
//        //on each switch
//        ((SwitchCompat) add_planner_layout.findViewById(R.id.mondaySwitch)).setOnCheckedChangeListener(multiSwitchListener);
//        ((SwitchCompat) add_planner_layout.findViewById(R.id.tuesdaySwitch)).setOnCheckedChangeListener(multiSwitchListener);
//        ((SwitchCompat) add_planner_layout.findViewById(R.id.wednesdaySwitch)).setOnCheckedChangeListener(multiSwitchListener);
//        ((SwitchCompat) add_planner_layout.findViewById(R.id.thursdaySwitch)).setOnCheckedChangeListener(multiSwitchListener);
//        ((SwitchCompat) add_planner_layout.findViewById(R.id.fridaySwitch)).setOnCheckedChangeListener(multiSwitchListener);
//        ((SwitchCompat) add_planner_layout.findViewById(R.id.saturdaySwitch)).setOnCheckedChangeListener(multiSwitchListener);
//        ((SwitchCompat) add_planner_layout.findViewById(R.id.sundaySwitch)).setOnCheckedChangeListener(multiSwitchListener);

        ElegantNumberButton.OnValueChangeListener multiNumberListener = new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                switch (view.getId()){
                    case R.id.mondayQuantityButton:
                        weeklyBill[0] = itemPrice * newValue;
                        break;
                    case R.id.tuesdayQuantityButton:
                        weeklyBill[1] = itemPrice * newValue;
                        break;
                    case R.id.wednesdayQuantityButton:
                        weeklyBill[2] = itemPrice * newValue;
                        break;
                    case R.id.thursdayQuantityButton:
                        weeklyBill[3] = itemPrice * newValue;
                        break;
                    case R.id.fridayQuantityButton:
                        weeklyBill[4] = itemPrice * newValue;
                        break;
                    case R.id.saturdayQuantityButton:
                        weeklyBill[5] = itemPrice * newValue;
                        break;
                    case R.id.sundayQuantityButton:
                        weeklyBill[6] = itemPrice * newValue;
                        break;
                }

                weeklyBill[7] = weeklyBill[0] + weeklyBill[1] + weeklyBill[2] + weeklyBill[3] + weeklyBill[4] + weeklyBill[5] + weeklyBill[6];
                weeklyBillTxt.setText("Bill/Week: " + weeklyBill[7] + " Rs.");
            }
        };

        //on each ElegantNumberButton
        mondayQuantityButton.setOnValueChangeListener(multiNumberListener);
        tuesdayQuantityButton.setOnValueChangeListener(multiNumberListener);
        wednesdayQuantityButton.setOnValueChangeListener(multiNumberListener);
        thursdayQuantityButton.setOnValueChangeListener(multiNumberListener);
        fridayQuantityButton.setOnValueChangeListener(multiNumberListener);
        saturdayQuantityButton.setOnValueChangeListener(multiNumberListener);
        sundayQuantityButton.setOnValueChangeListener(multiNumberListener);

        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        final Date currentTime = Calendar.getInstance().getTime();
        final int yy = calendar.get(Calendar.YEAR);
        final int mm = calendar.get(Calendar.MONTH);
        final int dd = calendar.get(Calendar.DAY_OF_MONTH);

        final String startDate = String.valueOf(dd) + "/" + String.valueOf(mm+1) + "/" + String.valueOf(yy);
        final String date = "Start Date: " + startDate;
        startDatePickerBtn.setText(date);

        startDatePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                DatePickerDialog datePicker = new DatePickerDialog(FoodDetailActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String date = "Start Date: " + String.valueOf(dayOfMonth) + "/" + String.valueOf(monthOfYear+1) + "/" + String.valueOf(year);
                        startDatePickerBtn.setText(date);
                    }
                }, yy, mm, dd);
                datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                datePicker.show();
            }
        });

        alertDialog.setPositiveButton("Subscribe", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                setWeekDays(mondayQuantityButton, 0);
                setWeekDays(tuesdayQuantityButton, 1);
                setWeekDays(wednesdayQuantityButton, 2);
                setWeekDays(thursdayQuantityButton, 3);
                setWeekDays(fridayQuantityButton, 4);
                setWeekDays(saturdayQuantityButton, 5);
                setWeekDays(sundayQuantityButton, 6);

                if (days.isEmpty())
                    Toast.makeText(FoodDetailActivity.this, "Please select at least 1 day to subscribe!", Toast.LENGTH_SHORT).show();
                else
                {
                    Planner planner=new Planner(
                            Common.currentUser.getDeliveryAddress(),
                            foodItemId,
                            currentFood.getName(),
                            currentFood.getPrice(),
                            Common.currentUser.getPhoneNumber(),
                            String.valueOf(weeklyBill[7]),
                            startDate,
                            currentTime.toString(),
                            Common.convertCodeToDeliveryTime(deliveryTimeSpinner.getSelectedIndex()),
                            days,
                            "subscribed",
                            Common.currentUser.getPhoneNumber() + "_subscribed"
                    );

                    //pushing Planner to Firebase. Current time in miliseconds will be used as order key to keep it unique
                    String order_number = String.valueOf(System.currentTimeMillis());
                    plannerTbl.child(order_number).setValue(planner);

                    dialogInterface.dismiss();
                    sendNotificationOrder(order_number);
                }
            }

            private void setWeekDays(ElegantNumberButton quantityButton, int index) {
                if (Integer.parseInt(quantityButton.getNumber()) > 0)
                    days.add(new WeekDay(quantityButton.getContentDescription().toString(), quantityButton.getNumber(), String.valueOf(weeklyBill[index])));
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setView(add_planner_layout);
        alertDialog.show();
    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true); // Get all node with isServerToken is true
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Token serverToken = postSnapshot.getValue(Token.class);

                    //Create raw payload to send
                    Notification notification = new Notification("Veggie", "You Have new Planner " + order_number);
                    Sender content = new Sender(serverToken.getToken(), notification);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    // Only run when get result
                                    if (response.code() == 200) {
                                        if (response.body().success == 1)
                                            Toast.makeText(getApplicationContext(), "Planner Subscribed Successfully!", Toast.LENGTH_LONG).show();
                                        else
                                            Toast.makeText(getApplicationContext(), "Planner Failed !!!", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR_Notification", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

                //Picasso.get().load(currentFood.getImage()).into(foodItemImage); // For latest version of Picasso (implementation 'com.squareup.picasso:picasso:2.71828')
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(foodItemImage); // Bcz slider support lower version of Picasso, so downgraded to (implementation 'com.squareup.picasso:picasso:2.5.2')

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
