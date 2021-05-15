package com.example.ul.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author luoweili
 */
public class ReaderPermission implements Serializable, Parcelable {

    private String id;

    private Integer credit;

    private Integer amount;

    private Integer permission;

    private String permissionName;

    private Integer type;

    private String typeName;

    private String image;

    private String state;

    private ArrayList<String> pictures;

    private Date term;

    private Integer maxAmount;

    private static final long serialVersionUID = 1L;
    
    public ReaderPermission(){
        
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getPermission() {
        return permission;
    }

    public ArrayList<String> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }

    public void setPermission(Integer permission) {
        this.permission = permission;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image == null ? null : image.trim();
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Date getTerm() {
        return term;
    }

    public void setTerm(Date term) {
        this.term = term;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Integer maxAmount) {
        this.maxAmount = maxAmount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected ReaderPermission(Parcel in) {
        id = in.readString();
        credit = in.readInt();
        amount = in.readInt();
        permission = in.readInt();
        permissionName = in.readString();
        type = in.readInt();
        typeName = in.readString();
        image = in.readString();
        state = in.readString();
        pictures = (ArrayList<String>) in.readSerializable();
        term = (Date) in.readSerializable();
        maxAmount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(credit);
        dest.writeInt(amount);
        dest.writeInt(permission);
        dest.writeString(permissionName);
        dest.writeInt(type);
        dest.writeString(typeName);
        dest.writeString(image);
        dest.writeString(state);
        dest.writeSerializable(pictures);
        dest.writeSerializable(term);
        dest.writeInt(maxAmount);
    }

    public static final Creator<ReaderPermission> CREATOR = new Creator<ReaderPermission>() {
        @Override
        public ReaderPermission createFromParcel(Parcel in) {
            return new ReaderPermission(in);
        }

        @Override
        public ReaderPermission[] newArray(int size) {
            return new ReaderPermission[size];
        }
    };
}