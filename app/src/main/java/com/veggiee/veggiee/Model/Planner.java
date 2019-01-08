package com.veggiee.veggiee.Model;

import java.util.List;

public class Planner {
    private String Address;
    private String FoodId;
    private String FoodName;
    private String FoodPrice;
    private String Phone;
    private String TotalWeeklyBill;
    private String StartDate;
    private String CreatedDate;
    private String DeliveryTimeSlot;
    private List<WeekDay> Days;
    private String status;
    private String phone_status;

    public Planner() {
    }

    public Planner(String address, String foodId, String foodName, String foodPrice, String phone, String totalWeeklyBill, String startDate, String createdDate, String deliveryTimeSlot, List<WeekDay> days, String status, String phone_status) {
        Address = address;
        FoodId = foodId;
        FoodName = foodName;
        FoodPrice = foodPrice;
        Phone = phone;
        TotalWeeklyBill = totalWeeklyBill;
        StartDate = startDate;
        CreatedDate = createdDate;
        DeliveryTimeSlot = deliveryTimeSlot;
        Days = days;
        this.status = status;
        this.phone_status = phone_status;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getFoodId() {
        return FoodId;
    }

    public void setFoodId(String foodId) {
        FoodId = foodId;
    }

    public String getFoodName() {
        return FoodName;
    }

    public void setFoodName(String foodName) {
        FoodName = foodName;
    }

    public String getFoodPrice() {
        return FoodPrice;
    }

    public void setFoodPrice(String foodPrice) {
        FoodPrice = foodPrice;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getTotalWeeklyBill() {
        return TotalWeeklyBill;
    }

    public void setTotalWeeklyBill(String totalWeeklyBill) {
        TotalWeeklyBill = totalWeeklyBill;
    }

    public String getStartDate() {
        return StartDate;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }

    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public String getDeliveryTimeSlot() {
        return DeliveryTimeSlot;
    }

    public void setDeliveryTimeSlot(String deliveryTimeSlot) {
        DeliveryTimeSlot = deliveryTimeSlot;
    }

    public List<WeekDay> getDays() {
        return Days;
    }

    public void setDays(List<WeekDay> days) {
        Days = days;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone_status() {
        return phone_status;
    }

    public void setPhone_status(String phone_status) {
        this.phone_status = phone_status;
    }
}
