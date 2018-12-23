package com.veggiee.veggiee.Model;

public class SubCategory {

    private String Name, Image, CategoryId;

    public SubCategory() {
    }

    public SubCategory(String name, String image, String categoryId) {
        Name = name;
        Image = image;
        CategoryId = categoryId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(String categoryId) {
        CategoryId = categoryId;
    }
}
