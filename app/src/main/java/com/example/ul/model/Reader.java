package com.example.ul.model;

import java.io.Serializable;

/**
 * @author luoweili
 */

public class Reader implements Serializable {

    private String id;

    private String name;

    private String sex;

    private Integer age;

    private String department;

    private String classroom;

    private String phone;

    private String email;

    private String username;

    private String password;

    private ReaderPermission readerPermission;

    public Reader(){

    }

    public Reader(String id, String name, String sex, Integer age, String department, String classroom,
                  String phone, String email, String username, String password, ReaderPermission readerPermission){
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.department = department;
        this.classroom = classroom;
        this.phone = phone;
        this.email = email;
        this.username = username;
        this.password = password;
        this.readerPermission = readerPermission;
    }
    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex == null ? null : sex.trim();
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department == null ? null : department.trim();
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom == null ? null : classroom.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public ReaderPermission getReaderPermission(){
        return readerPermission;
    }

    public void setReaderPermission(ReaderPermission readerPermission){
        this.readerPermission = readerPermission;
    }
}