package com.veggiee.veggiee;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Model.User;

import java.util.Objects;

public class CompleteProfileActivity extends AppCompatActivity {

    AppCompatEditText emailEditText;
    AppCompatEditText phoneEditText;
    AppCompatEditText nameEditText;
    AppCompatEditText addressEditText;
    AppCompatButton proceedButton;
    TextInputLayout emailTIP,nameTIP,numberTIP,addressTIP;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
    DatabaseReference users=mDatabase.getReference("User");
    String provider=null;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        emailEditText=findViewById(R.id.emailEditText);
        phoneEditText=findViewById(R.id.phoneEditText);
        nameEditText=findViewById(R.id.nameEditText);
        addressEditText=findViewById(R.id.addressEditText);
        proceedButton=findViewById(R.id.updateProfileButton);

        emailTIP=findViewById(R.id.emailTextLayout);
        nameTIP=findViewById(R.id.nameTextLayout);
        numberTIP=findViewById(R.id.phoneTextLayout);
        addressTIP=findViewById(R.id.addressTextLayout);



        //Firebase Auth
        mAuth=FirebaseAuth.getInstance();

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            }
        });


        fillPresentInfo();

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //saves user to database if not already
                saveUserToDB();


                Intent categoryIntent=new Intent(CompleteProfileActivity.this,CategoryActivity.class);
                startActivity(categoryIntent);
                finish();
            }
        });






    }

    private void fillPresentInfo()
    {
        FirebaseUser mUser=mAuth.getCurrentUser();

        if(mUser!=null)
        {
            if(mUser.getEmail()!=null)
            {
                emailEditText.setText(mUser.getEmail());
                nameEditText.setText(mUser.getDisplayName());
                emailTIP.setVisibility(View.GONE);
                nameTIP.setVisibility(View.GONE);
            }


            if(mUser.getPhoneNumber()!=null)
            {
                phoneEditText.setText(mUser.getPhoneNumber());
                numberTIP.setVisibility(View.GONE);
            }

            if(mUser.getDisplayName()!=null)
            {
                nameEditText.setText(mUser.getDisplayName());
                nameEditText.setFocusable(false);
            }

        }
    }


    private void saveUserToDB()
    {

        if(mAuth.getCurrentUser()!=null)
        {
            provider=Objects.requireNonNull(mAuth.getCurrentUser().getProviders()).get(0);
        }

        user=new User(nameEditText.getText().toString(),
                emailEditText.getText().toString(),
                addressEditText.getText().toString(),
                phoneEditText.getText().toString());


        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!(dataSnapshot.child(user.getPhoneNumber()).exists()))
                {
                    users.child(user.getPhoneNumber()).setValue(user);
                }

                Common.currentUser=dataSnapshot.child(user.getPhoneNumber()).getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




}
