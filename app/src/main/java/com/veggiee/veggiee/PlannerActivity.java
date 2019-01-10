package com.veggiee.veggiee;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Interface.ItemClickListener;
import com.veggiee.veggiee.Model.MyResponse;
import com.veggiee.veggiee.Model.Notification;
import com.veggiee.veggiee.Model.Planner;
import com.veggiee.veggiee.Model.Sender;
import com.veggiee.veggiee.Model.Token;
import com.veggiee.veggiee.Model.WeekDay;
import com.veggiee.veggiee.Remote.APIService;
import com.veggiee.veggiee.ViewHolder.ViewHolder_Planner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlannerActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    LinearLayoutManager mLinearLayout;

    //Firebase
    FirebaseDatabase mDatabase;
    DatabaseReference planners;

    TextView emptyPlannerText,itemPriceTxt,weeklyBillTxt, headingTxtView, foodNameTxtView;
    ElegantNumberButton quantityButton, mondayQuantityButton, tuesdayQuantityButton,wednesdayQuantityButton, thursdayQuantityButton, fridayQuantityButton, saturdayQuantityButton, sundayQuantityButton;
    MaterialSpinner deliveryTimeSpinner;
    int[] weeklyBill = new int[8];

    APIService mService;

    FirebaseRecyclerAdapter<Planner,ViewHolder_Planner> adapter;

    //this list will be shown to user for Planner list when clicked on a specific planner
    Map<String,Planner> previousPlannersDetailList=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner);

        //init Service
        mService = Common.getFCMService();

        //init views
        emptyPlannerText = (TextView)findViewById(R.id.emptyPlannerText);
        mRecyclerView=(RecyclerView) findViewById(R.id.plannersListRecyclerView);
        mLinearLayout=new LinearLayoutManager (this);
        mRecyclerView.setLayoutManager(mLinearLayout);

        //init firebase
        mDatabase=FirebaseDatabase.getInstance();
        planners=mDatabase.getReference("Planner");


        if (Common.isConnectedToInternet(getBaseContext()))
            loadPlanners(Common.currentUser.getPhoneNumber());
        else
            Toast.makeText(this, "Please check your Internet Connection", Toast.LENGTH_SHORT).show();
    }

    private void loadPlanners(String phoneNumber) {

        Query query = planners.orderByChild("phone_status").equalTo(phoneNumber + "_subscribed");

        if(getIntent().getStringExtra("planner_status") != null)
            query = planners.orderByChild("phone_status").equalTo(phoneNumber + "_" + getIntent().getStringExtra("planner_status"));

        FirebaseRecyclerOptions<Planner> options=new FirebaseRecyclerOptions.Builder<Planner>()
                .setQuery(query,Planner.class).build();

        adapter=new FirebaseRecyclerAdapter<Planner, ViewHolder_Planner>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder_Planner holder, int position, @NonNull final Planner model) {

                holder.plannerId.setText(adapter.getRef(position).getKey());
                holder.plannerFoodName.setText("Name: " + model.getFoodName().toUpperCase());
                holder.plannerStatus.setText("Status: " + model.getStatus().toUpperCase());
                holder.plannerPhoneNumber.setText(model.getPhone());
                holder.plannerAddress.setText(model.getAddress());

                previousPlannersDetailList.put(
                        holder.plannerId.getText().toString(),
                        new Planner(
                                model.getAddress(),
                                model.getFoodId(),
                                model.getFoodName(),
                                model.getFoodPrice(),
                                model.getPhone(),
                                model.getTotalWeeklyBill(),
                                model.getStartDate(),
                                model.getCreatedDate(),
                                model.getDeliveryTimeSlot(),
                                model.getDays(),
                                model.getStatus(),
                                model.getPhone_status()
                        )
                );

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Planner currentPlanner=new Planner();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            currentPlanner.setDays(Objects.requireNonNull(previousPlannersDetailList.getOrDefault(holder.plannerId.getText().toString(), null)).getDays());
                        }
                        else
                        {
                            currentPlanner.setDays(previousPlannersDetailList.get(holder.plannerId.getText().toString()).getDays());
                        }

                        currentPlanner.setTotalWeeklyBill(model.getTotalWeeklyBill());

                        AlertDialog.Builder plannerListDialog=new AlertDialog.Builder(PlannerActivity.this);
                        plannerListDialog.setTitle("Planner Items List");
                        plannerListDialog.setIcon(R.drawable.ic_event_white_24dp);

                        StringBuilder planners= new StringBuilder();

                        if(!currentPlanner.getDays().isEmpty())
                            for(int i=0; i<currentPlanner.getDays().size();i++)
                            {
                                planners.append(
                                        currentPlanner.getDays().get(i).getName()
                                                +" x "
                                                +currentPlanner.getDays().get(i).getQuantity()
                                                +"\t( Rs "+currentPlanner.getDays().get(i).getPerDayBill()+" )").append("\n");
                            }
                        else
                            Toast.makeText(PlannerActivity.this, "This Planner is empty!!!", Toast.LENGTH_SHORT).show();



                        planners.append("\nTotal Weekly/Bill: Rs ").append(currentPlanner.getTotalWeeklyBill()).append("-/");
                        plannerListDialog.setMessage(planners.toString());


                        plannerListDialog.setNeutralButton("Okay",null);


                        plannerListDialog.show();
                    }
                });
            }

            @NonNull
            @Override
            public ViewHolder_Planner onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.planner_item_view,viewGroup,false);
                return new ViewHolder_Planner(view);
            }
        };

        mRecyclerView.setAdapter(adapter);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    emptyPlannerText.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE))
            showUpdateSchedulerDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        else if (item.getTitle().equals(Common.SUBSCRIBE_UNSUBSCRIBE))
            updateStatus(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        else if (item.getTitle().equals(Common.DELETE))
            deletePlanner(adapter.getRef(item.getOrder()).getKey());

        return super.onContextItemSelected(item);
    }

    private void showUpdateSchedulerDialog(final String key, final Planner item) {
        weeklyBill[7] = Integer.parseInt(item.getTotalWeeklyBill());
        final int itemPrice = Integer.parseInt(item.getFoodPrice());
        final List<WeekDay> days=new ArrayList<>();

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlannerActivity.this);

        LayoutInflater layoutInflater = this.getLayoutInflater();
        final View add_planner_layout = layoutInflater.inflate(R.layout.add_new_planner_layout, null);

        final AppCompatButton startDatePickerBtn;

        // Init View
        itemPriceTxt = add_planner_layout.findViewById(R.id.itemPriceTxt);
        itemPriceTxt.setText("Price: " + itemPrice + " Rs.");
        headingTxtView = add_planner_layout.findViewById(R.id.headingTxtView);
        headingTxtView.setText("Update Planner");
        weeklyBillTxt = add_planner_layout.findViewById(R.id.weeklyBillTxt);
        weeklyBillTxt.setText("Bill/Week: " + item.getTotalWeeklyBill() + " Rs.");
        foodNameTxtView = add_planner_layout.findViewById(R.id.foodNameTxtView);
        foodNameTxtView.setText("(" + item.getFoodName().toUpperCase() + ")");
        startDatePickerBtn = add_planner_layout.findViewById(R.id.startDatePicker);
        startDatePickerBtn.setText(item.getStartDate());
        deliveryTimeSpinner = add_planner_layout.findViewById(R.id.deliveryTimeSpinner);
        deliveryTimeSpinner.setItems(item.getDeliveryTimeSlot(), "09AM - 01PM ", "01PM - 05PM", "05PM - 09PM");
        mondayQuantityButton = add_planner_layout.findViewById(R.id.mondayQuantityButton);
        tuesdayQuantityButton = add_planner_layout.findViewById(R.id.tuesdayQuantityButton);
        wednesdayQuantityButton = add_planner_layout.findViewById(R.id.wednesdayQuantityButton);
        thursdayQuantityButton = add_planner_layout.findViewById(R.id.thursdayQuantityButton);
        fridayQuantityButton = add_planner_layout.findViewById(R.id.fridayQuantityButton);
        saturdayQuantityButton = add_planner_layout.findViewById(R.id.saturdayQuantityButton);
        sundayQuantityButton = add_planner_layout.findViewById(R.id.sundayQuantityButton);

        // For setting current planners data
        setUpdateWeekDays(mondayQuantityButton, item);
        setUpdateWeekDays(tuesdayQuantityButton, item);
        setUpdateWeekDays(wednesdayQuantityButton, item);
        setUpdateWeekDays(thursdayQuantityButton, item);
        setUpdateWeekDays(fridayQuantityButton, item);
        setUpdateWeekDays(saturdayQuantityButton, item);
        setUpdateWeekDays(sundayQuantityButton, item);

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
                DatePickerDialog datePicker = new DatePickerDialog(PlannerActivity.this, new DatePickerDialog.OnDateSetListener() {
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

        alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
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
                    Toast.makeText(PlannerActivity.this, "Please select at least 1 day to subscribe!", Toast.LENGTH_SHORT).show();
                else
                {
                    Planner planner=new Planner(
                            Common.currentUser.getDeliveryAddress(),
                            item.getFoodId(),
                            item.getFoodName(),
                            item.getFoodPrice(),
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
                    planners.child(key).setValue(planner);

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

    private void setUpdateWeekDays(ElegantNumberButton quantityButton, Planner item) {
        for(int i=0; i<item.getDays().size();i++)
        {
            if (quantityButton.getContentDescription().equals(item.getDays().get(i).getName()))
            {
                quantityButton.setNumber(item.getDays().get(i).getQuantity());
                switch (quantityButton.getContentDescription().toString())
                {
                    case "Monday":
                        weeklyBill[0] = Integer.parseInt(item.getDays().get(i).getPerDayBill());
                        break;
                    case "Tuesday":
                        weeklyBill[1] = Integer.parseInt(item.getDays().get(i).getPerDayBill());
                        break;
                    case "Wednesday":
                        weeklyBill[2] = Integer.parseInt(item.getDays().get(i).getPerDayBill());
                        break;
                    case "Thursday":
                        weeklyBill[3] = Integer.parseInt(item.getDays().get(i).getPerDayBill());
                        break;
                    case "Friday":
                        weeklyBill[4] = Integer.parseInt(item.getDays().get(i).getPerDayBill());
                        break;
                    case "Saturday":
                        weeklyBill[5] = Integer.parseInt(item.getDays().get(i).getPerDayBill());
                        break;
                    case "Sunday":
                        weeklyBill[6] = Integer.parseInt(item.getDays().get(i).getPerDayBill());
                        break;
                }
            }
        }
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
                    Notification notification = new Notification("Veggie", "Planner #" + order_number + " has been updated!");
                    Sender content = new Sender(serverToken.getToken(), notification);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    // Only run when get result
                                    if (response.code() == 200) {
                                        if (response.body().success == 1)
                                            Toast.makeText(getApplicationContext(), "Planner Updated Successfully!", Toast.LENGTH_LONG).show();
                                        else
                                            Toast.makeText(getApplicationContext(), "Planner Failed to Update!!!", Toast.LENGTH_LONG).show();
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

    private void updateStatus(String key, final Planner item) {

        if(item.getStatus().equals("subscribed"))
            item.setStatus("unsubscribed");
        else if(item.getStatus().equals("unsubscribed"))
            item.setStatus("subscribed");

        item.setPhone_status(item.getPhone() + "_" +  item.getStatus());

        planners.child(key).setValue(item);

        Toast.makeText(PlannerActivity.this, "Planner '" + item.getStatus().toUpperCase() + "' Successfully!", Toast.LENGTH_LONG).show();

        planners.orderByChild("phone_status").equalTo(Common.currentUser.getPhoneNumber() + "_" + getIntent().getStringExtra("planner_status")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren())
                    emptyPlannerText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void deletePlanner(String key) {

        planners.child(key).removeValue();
        Toast.makeText(PlannerActivity.this, "Planner Deleted Successfully!", Toast.LENGTH_LONG).show();

        planners.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren())
                    emptyPlannerText.setVisibility(View.VISIBLE);
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
