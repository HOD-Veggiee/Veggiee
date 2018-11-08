package com.veggiee.veggiee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import android.widget.RelativeLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.veggiee.veggiee.Common.Common;
import com.veggiee.veggiee.Model.User;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompleteProfileActivity extends AppCompatActivity {

    AppCompatEditText emailEditText;
    AppCompatEditText phoneEditText;
    AppCompatEditText nameEditText;
    AppCompatEditText addressEditText;
    AppCompatButton proceedButton;
    TextInputLayout emailTIP,nameTIP,numberTIP,addressTIP;

    RelativeLayout relativeLayout;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
    DatabaseReference users=mDatabase.getReference("User");
    String provider=null;
    User user;

    boolean isAlreadyReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        //init views
        emailEditText=findViewById(R.id.emailEditText);
        phoneEditText=findViewById(R.id.phoneEditText);
        nameEditText=findViewById(R.id.nameEditText);
        addressEditText=findViewById(R.id.addressEditText);
        proceedButton=findViewById(R.id.updateProfileButton);
        emailTIP=findViewById(R.id.emailTextLayout);
        nameTIP=findViewById(R.id.nameTextLayout);
        numberTIP=findViewById(R.id.phoneTextLayout);
        addressTIP=findViewById(R.id.addressTextLayout);

        relativeLayout=(RelativeLayout) findViewById(R.id.relativeLayout);

        relativeLayout.setOnClickListener(null);

        Common.currentUser=new User();

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

                phoneNumberVerification(phoneEditText.getText().toString());
                emailVerification(emailEditText.getText().toString());
                nameVerification(nameEditText.getText().toString());
                addressVerification(addressEditText.getText().toString());

                if(isFormValidated())
                {
                    user=new User(Objects.requireNonNull(nameEditText.getText()).toString(),
                            Objects.requireNonNull(emailEditText.getText()).toString(),
                            Objects.requireNonNull(addressEditText.getText()).toString(),
                            Objects.requireNonNull(phoneEditText.getText()).toString());

                    savePhoneNumber();

                    users.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(!(dataSnapshot.child(user.getPhoneNumber()).exists()))
                            {
                                users.child(user.getPhoneNumber()).setValue(user);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    Common.currentUser=user;

                    Intent categoryIntent=new Intent(CompleteProfileActivity.this,CategoryActivity.class);
                    startActivity(categoryIntent);
                    finish();
                }

            }
        });


        //Text Listeners
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String email=charSequence.toString();
                emailVerification(email);

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String email=editable.toString();

                emailVerification(email);

            }
        });

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String number=charSequence.toString();
                phoneNumberVerification(number);
            }

            @Override
            public void afterTextChanged(Editable editable) {

                String number=editable.toString();

                phoneNumberVerification(number);

            }
        });

        addressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                nameVerification(charSequence.toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

                nameVerification(editable.toString());

            }
        });

        addressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                addressVerification(charSequence.toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

                addressVerification(editable.toString());

            }
        });

    }

    private void savePhoneNumber() {

        SharedPreferences pref=getSharedPreferences("NUM_Info",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=pref.edit();
        editor.putString("phoneNumber",phoneEditText.getText().toString());
        editor.putString("name",nameEditText.getText().toString());
        editor.apply();
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


    private void phoneNumberVerification(String number)
    {
        if ((number.length()>2)&&(!number.startsWith("+92")))
        {
            numberTIP.setErrorEnabled(true);
            numberTIP.setError("Write in format +923xxxxxxxxxx");
        }
        else if(number.isEmpty())
        {
            numberTIP.setErrorEnabled(true);
            numberTIP.setError("Number is required.");
        }
        else if(number.length()>13)
        {
            numberTIP.setErrorEnabled(true);
            numberTIP.setError("Number must be of 13 digits");
        }
        else
        {
            numberTIP.setErrorEnabled(false);
        }
    }

    private void emailVerification(String email)
    {

        if(email.isEmpty())
        {
            emailTIP.setErrorEnabled(true);
            emailTIP.setError("Email is required.");
            return;
        }

        Pattern pattern = Pattern.compile("^.+@.+\\..+$");
        Matcher matcher = pattern.matcher(email);

        if(matcher.matches())
        {
            emailTIP.setErrorEnabled(false);
        }
        else {
            emailTIP.setErrorEnabled(true);
            emailTIP.setError("Email format not correct");
        }
    }

    private void nameVerification(String name)
    {
        if(name.isEmpty())
        {
            nameTIP.setErrorEnabled(true);
            nameTIP.setError("Name is required.");
        }
        else
        {
            nameTIP.setErrorEnabled(false);
        }
    }

    private void addressVerification(String address)
    {
        if(address.isEmpty())
        {
            addressTIP.setErrorEnabled(true);
            addressTIP.setError("Address is required.");
        }
        else
        {
            addressTIP.setErrorEnabled(false);
        }
    }

    private boolean isFormValidated()
    {
        return !numberTIP.isErrorEnabled()
                &&
                !emailTIP.isErrorEnabled()
                &&
                !addressTIP.isErrorEnabled()
                &&
                !nameTIP.isErrorEnabled();
    }


}
