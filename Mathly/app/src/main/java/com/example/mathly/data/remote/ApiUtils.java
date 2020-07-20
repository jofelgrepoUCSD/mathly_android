package com.example.mathly.data.remote;

import retrofit2.Retrofit;

public class ApiUtils {

    private ApiUtils(){}

    public static final String BASE_URL = "https://jsonplaceholder.typicode.com/";

    public static APIService getAPIService(){
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
