package com.example.mathly.data.remote;
import com.example.mathly.data.Post;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {


    /** indicates we want to execute a POST request
     *  the full URL will be
     *  http://jsonplaceholder.typicode.com/posts
     */

//    @POST("/posts")
//    @FormUrlEncoded
//    Call<Post> savePost(@Field("base64") String base64,
//                        @Field("userId") long userId);

    @POST("/posts")
    Call<Post> createPost(@Body Post post);
}
