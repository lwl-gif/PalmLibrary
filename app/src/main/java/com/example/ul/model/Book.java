package com.example.ul.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author luoweili
 */
public class Book implements Serializable, Parcelable {

    private static final long serialVersionUID = 1L;

    public Book(){

    }

    public Book(Integer id,String name,String isbn,String author,String house,Date date,String library,
                String location, String callNumber,String typeId,String theme,String description,
                String state,String images,String belong,int hot,BigDecimal price){
        this.id = id;
        this.name = name;
        this.isbn = isbn;
        this.author = author;
        this.house = house;
        this.date = date;
        this.library = library;
        this.location = location;
        this.callNumber = callNumber;
        this.typeId = typeId;
        this.theme = theme;
        this.description = description;
        this.state = state;
        this.images = images;
        this.belong = belong;
        this.hot = hot;
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn == null ? null : isbn.trim();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author == null ? null : author.trim();
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house == null ? null : house.trim();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library == null ? null : library.trim();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location == null ? null : location.trim();
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber == null ? null : callNumber.trim();
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId == null ? null : typeId.trim();
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme == null ? null : theme.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images == null ? null : images.trim();
    }

    public ArrayList<String> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }

    public String getBelong() {
        return belong;
    }

    public void setBelong(String belong) {
        this.belong = belong == null ? null : belong.trim();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getHot() {
        return hot;
    }

    public void setHot(Integer hot) {
        this.hot = hot;
    }

    public Classification getClassification(){
        return this.classification;
    }

    public void  setClassification(Classification classification){
        this.classification = classification;
    }

    private Integer id;

    private String name;

    private String isbn;

    private String author;

    private String house;

    private Date date;

    private String library;

    private String location;

    private String callNumber;

    private String typeId;

    private String typeName;

    private String theme;

    private String description;

    private String state;
    /**存放图片的文件夹的名称*/
    private String images;

    /**文件夹下所有的图片的名称*/
    private ArrayList<String> pictures;

    private String belong;

    private Classification classification;

    private BigDecimal price;

    private Integer hot;

    @Override
    public int describeContents() {
        return 0;
    }

    protected Book(Parcel in) {
        id = in.readInt();
        name = in.readString();
        isbn = in.readString();
        author = in.readString();
        house = in.readString();
        date = (Date) in.readSerializable();
        library = in.readString();
        location = in.readString();
        callNumber = in.readString();
        typeId = in.readString();
        typeName = in.readString();
        theme = in.readString();
        description = in.readString();
        state = in.readString();
        images = in.readString();
        pictures = (ArrayList<String>) in.readSerializable();
        belong = in.readString();
        classification = in.readParcelable(Classification.class.getClassLoader());
        price = (BigDecimal) in.readSerializable();
        hot = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(isbn);
        dest.writeString(author);
        dest.writeString(house);
        dest.writeSerializable(date);
        dest.writeString(library);
        dest.writeString(location);
        dest.writeString(callNumber);
        dest.writeString(typeId);
        dest.writeString(typeName);
        dest.writeString(theme);
        dest.writeString(description);
        dest.writeString(state);
        dest.writeString(images);
        dest.writeSerializable(pictures);
        dest.writeString(belong);
        dest.writeParcelable(classification,0);
        dest.writeSerializable(price);
        dest.writeInt(hot);
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
