
package com.example.user.kursach;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Buy {

    @SerializedName("Quantity")
    @Expose
    private Float quantity;
    @SerializedName("Rate")
    @Expose
    private Float rate;

    public Float getQuantity() {
        return quantity;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

}
