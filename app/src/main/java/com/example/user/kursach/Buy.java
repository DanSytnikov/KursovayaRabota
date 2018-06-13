
package com.example.user.kursach;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Buy implements Comparable<Buy>{

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


    @Override
    /*Issue with Bar-chart rendering on zooming https://github.com/PhilJay/MPAndroidChart/issues/718#event-550002344*/
    public int compareTo(@NonNull Buy o) {
        return (int) (this.rate - o.rate);
    }
}
