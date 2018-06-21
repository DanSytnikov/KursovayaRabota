package com.example.user.kursach;



import com.example.user.kursach.BittrexPackage.Bittrex;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface GETinterface {
    @GET("/api/v1.1/public/getorderbook")
    Call<Bittrex> getData(@Query("market") String market, @Query("type") String type);

    @GET("/api/order_book/{symbol1}/{symbol2}/?depth=50")
    Call<CEX> getDataCex(@Path("symbol1") String symbol1, @Path("symbol2") String symbol2);

}