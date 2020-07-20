package com.example.mathly.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class Post {


    @SerializedName("base64")
    @Expose
    private String base64;

    @SerializedName("userId")
    @Expose
    private Integer userId;

    public Post(String base64, Integer userId) {
        this.base64 = base64;
        this.userId = userId;
    }

    public String getBase64() {
        return base64;
    }
    public void setBase64(String base64) {
        this.base64 = base64;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Post{" +
                "base64='" + base64 + '\'' +
                ", userId=" + userId +
                '}';
    }
}
