package com.veggiee.veggiee.Model;

public class User {

    private String name;
    private String password;
    private String deliveryAddress;

    public User()
    {}

    public User(String name, String password,String deliveryAddress) {
        this.name = name;
        this.password = password;
        this.deliveryAddress=deliveryAddress;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



}
