package com.veggiee.veggiee.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.veggiee.veggiee.Model.User;
import com.veggiee.veggiee.Remote.APIService;
import com.veggiee.veggiee.Remote.RetrofitClient;

public class Common {

    public static final String UPDATE = "Update";
    public static final String SUBSCRIBE_UNSUBSCRIBE = "Subscribe/Unsubscribe";
    public static final String DELETE = "Delete";
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
                return "Pending";

            case "1":
                return "Preparing";

            case "2":
                return "On it's way";

            case "3":
                return "Completed";

            default:
                return "Pending";
        }
    }

    public static String convertCodeToDeliveryTime(int code)
    {
        switch (code)
        {
            case 1:
                return "09AM - 01PM";

            case 2:
                return "01PM - 05PM";

            case 3:
                return "05PM - 09PM";

            default:
                return "09AM - 01PM";
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

