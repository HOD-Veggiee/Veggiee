package com.veggiee.veggiee.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.veggiee.veggiee.Model.User;
import com.veggiee.veggiee.Remote.APIService;
import com.veggiee.veggiee.Remote.RetrofitClient;

public class Common {

    public static User currentUser;
    public static String PHONE_TEXT = "userPhone";

    public static final String BASE_URL = "https://fcm.googleapis.com/";

    public static APIService getFCMService()
    {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static String convertCodeToStatus(String code)
    {
        switch (code)
        {
            case "0":
                return "Order placed.";

            case "1":
                return "In Process.";

            case "2":
                return "On way.";

            default:
                return "In Process";
        }
    }

    public static boolean isConnectedToInternet(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();

            if (info != null)
            {
                for (int i = 0; i < info.length; i++)
                {
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
}

