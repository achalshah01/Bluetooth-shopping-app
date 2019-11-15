package com.example.apiauthentication;

public class ItemPojo {
    String Discount;
    String Name;
    String Photo;
    String Price;
    String Region;
    String Count;
    Double TotalAmount;

    public Double getTotalAmount() {
        return TotalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        TotalAmount = totalAmount;
    }

    public String getCount() {
        return Count;
    }

    public void setCount(String count) {
        Count = count;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getRegion() {
        return Region;
    }

    public void setRegion(String region) {
        Region = region;
    }
}
