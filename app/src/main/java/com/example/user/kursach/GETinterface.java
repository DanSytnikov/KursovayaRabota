package com.example.user.kursach;



import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface GETinterface {
    @GET("/api/v1.1/public/getorderbook")
    Call<Bittrex> getData(@Query("market") String market, @Query("type") String type);
}