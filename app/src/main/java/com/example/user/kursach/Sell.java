
package com.example.user.kursach;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sell {

    @SerializedName("Quantity")
    @Expose
    private Double quantity;
    @SerializedName("Rate")
    @Expose
    private Double rate;

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

}
