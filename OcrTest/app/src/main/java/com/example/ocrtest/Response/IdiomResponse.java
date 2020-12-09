package com.example.ocrtest.Response;

import com.example.ocrtest.Model.IdiomModel;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class IdiomResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    List<IdiomModel> idiomModelList;

    public IdiomResponse(boolean status, String message, List<IdiomModel> idiomModelList) {
        this.status = status;
        this.message = message;
        this.idiomModelList = idiomModelList;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<IdiomModel> getIdiomModelList() {
        return idiomModelList;
    }

    public void setIdiomModelList(List<IdiomModel> idiomModelList) {
        this.idiomModelList = idiomModelList;
    }
}
