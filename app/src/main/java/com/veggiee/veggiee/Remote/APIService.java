package com.veggiee.veggiee.Remote;

import com.veggiee.veggiee.Model.MyResponse;
import com.veggiee.veggiee.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAMgCQ0r0:APA91bG69bNTn7ru2Wnk7r6pcuH6sba_P1Uz1MMkWgTuYN8cjk5DqSSXan3kig1RbdtnCL0bRQS0RuuFYs3ghWFysTaA09OcpImkraZsWER7pLIkVvKNjtH8Rr0E83w1vFpJ_Al0TZIW"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
