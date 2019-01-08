package com.veggiee.veggiee.Model;

import java.util.List;

public class Request {
    private String Phone;
    private String Address;
    private String Total;
    private List<Order> foods;
    private String status;
    private String phone_status;

    public Request() {
    }

    public Request(String phone, String address, String total, List<Order> foods) {
        Phone = phone;
        Address = address;
        Total = total;
        this.foods = foods;
        this.status="0";    //0=Pending, 1=Preparing, 2=On its way, 3=Completed
        this.phone_status = phone + "_incomplete";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getTotal() {
        return Total;
    }

    public void setTotal(String total) {
        Total = total;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }

    public String getPhone_status() { return phone_status; }

    public void setPhone_status(String phone_status) { this.phone_status = phone_status; }
}
