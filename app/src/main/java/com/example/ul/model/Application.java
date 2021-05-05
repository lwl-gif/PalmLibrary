package com.example.ul.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: Wallace
 * @Description: 处罚记录实体类
 * @Date: 2021/5/5 19:03
 * @Modified: By yyyy-MM-dd
 */
public class Application implements Serializable, Parcelable {
    /**书本Id*/
    private Integer id;
    /**书本名称*/
    private String name;
    /**读者Id*/
    private String readerId;
    /**读者名称*/
    private String readerName;
    /**申请时间*/
    private Date time;
    /**原还书时间*/
    private Date end;
    /**逾期天数*/
    private Integer days;
    /**待支付的金额*/
    private BigDecimal money;
    /**支付时间*/
    private Date payTime;
    /**详情描述*/
    private String description;
    /**管理员Id*/
    private String librarianId;
    /**支付Id*/
    private String payId;

    private static final long serialVersionUID = 1L;

    public Application(){

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

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId == null ? null : readerId.trim();
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName == null ? null : readerName.trim();
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getLibrarianId() {
        return librarianId;
    }

    public void setLibrarianId(String librarianId) {
        this.librarianId = librarianId == null ? null : librarianId.trim();
    }

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId == null ? null : payId.trim();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected Application(Parcel in) {
        id = in.readInt();
        name = in.readString();
        readerId = in.readString();
        readerName = in.readString();
        time = (Date) in.readSerializable();
        end = (Date) in.readSerializable();
        days = in.readInt();
        money = (BigDecimal) in.readSerializable();
        payTime = (Date) in.readSerializable();
        description = in.readString();
        librarianId = in.readString();
        payId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(readerId);
        dest.writeString(readerName);
        dest.writeSerializable(time);
        dest.writeSerializable(end);
        dest.writeInt(days);
        dest.writeSerializable(money);
        dest.writeSerializable(payTime);
        dest.writeString(description);
        dest.writeString(librarianId);
        dest.writeString(payId);
    }

    public static final Creator<Application> CREATOR = new Creator<Application>() {
        @Override
        public Application createFromParcel(Parcel in) {
            return new Application(in);
        }

        @Override
        public Application[] newArray(int size) {
            return new Application[size];
        }
    };
}
