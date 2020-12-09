package com.example.ocrtest.Rest;

import com.example.ocrtest.Response.IdiomResponse;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;

public interface RestApi {

    @GET("idiom/idiom")
    Call<IdiomResponse> getIdiom();
}
