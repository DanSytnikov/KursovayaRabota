
package com.example.user.kursach;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Buy implements Comparable<Buy>{

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


    @Override
    /*Issue with Bar-chart rendering on zooming https://github.com/PhilJay/MPAndroidChart/issues/718#event-550002344*/
    public int compareTo(@NonNull Buy o) {
        return (int) (this.rate - o.rate);
    }
}
