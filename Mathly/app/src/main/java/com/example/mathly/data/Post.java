package com.example.mathly.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Post {


    @SerializedName("src")
    @Expose
    private String src;
    @SerializedName("formats")
    @Expose
    private List<String> formats = null;
    @SerializedName("data_options")
    @Expose
    private DataOptions dataOptions;

    public Post(String src, List<String> formats, DataOptions dataOptions) {
        this.src = src;
        this.formats = formats;
        this.dataOptions = dataOptions;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

    public DataOptions getDataOptions() {
        return dataOptions;
    }

    public void setDataOptions(DataOptions dataOptions) {
        this.dataOptions = dataOptions;
    }
}
