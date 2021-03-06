package com.veggiee.veggiee;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.veggiee.veggiee.Common.Common;

import java.util.Arrays;
import java.util.List;

public class AuthenticationActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthUiSetup();

        if (!Common.isConnectedToInternet(getBaseContext()))
            Toast.makeText(AuthenticationActivity.this, "Please Check your Internet Connection", Toast.LENGTH_LONG).show();

    }

    public void AuthUiSetup()
    {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());


        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.logo)
                        .build(),
                RC_SIGN_IN);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN)
        {
            IdpResponse response=IdpResponse.fromResultIntent(data);

            if(resultCode==RESULT_OK)
            {
                Intent completeProfileIntent=new Intent(AuthenticationActivity.this,CompleteProfileActivity.class);
                startActivity(completeProfileIntent);
                finish();
            }
        }
    }
}
