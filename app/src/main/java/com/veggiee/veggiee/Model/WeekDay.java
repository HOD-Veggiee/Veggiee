package com.veggiee.veggiee.Model;

public class WeekDay {
    private String Name;
    private String Quantity;
    private String PerDayBill;

    public WeekDay() {
    }

    public WeekDay(String name, String quantity, String perDayBill) {
        Name = name;
        Quantity = quantity;
        PerDayBill = perDayBill;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getPerDayBill() {
        return PerDayBill;
    }

    public void setPerDayBill(String perDayBill) {
        PerDayBill = perDayBill;
    }
}
