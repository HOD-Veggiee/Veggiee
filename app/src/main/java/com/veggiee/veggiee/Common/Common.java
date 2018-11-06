package com.veggiee.veggiee.Common;

import com.veggiee.veggiee.Model.User;

public class Common {

    public static User currentUser;

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
}
