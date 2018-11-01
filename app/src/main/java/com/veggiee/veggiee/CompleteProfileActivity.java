package com.veggiee.veggiee;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CompleteProfileActivity extends AppCompatActivity {

    AppCompatEditText emailEditText;
    AppCompatEditText phoneEditText;
    AppCompatEditText nameEditText;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        emailEditText=findViewById(R.id.emailEditText);
        phoneEditText=findViewById(R.id.phoneEditText);
        nameEditText=findViewById(R.id.nameEditText);


        //Firebase Auth
        mAuth=FirebaseAuth.getInstance();

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            }
        });


        fillPresentInfo();



    }

    private void fillPresentInfo()
    {
        FirebaseUser mUser=mAuth.getCurrentUser();

        if(mUser!=null)
        {
            if(mUser.getEmail()!=null)
            {
                emailEditText.setText(mUser.getEmail());
                emailEditText.setClickable(false);
            }


            if(mUser.getPhoneNumber()!=null)
            {
                phoneEditText.setText(mUser.getPhoneNumber());
                phoneEditText.setClickable(false);
            }

            if(mUser.getDisplayName()!=null)
            {
                nameEditText.setText(mUser.getDisplayName());
                nameEditText.setClickable(false);
            }

        }
    }




}
