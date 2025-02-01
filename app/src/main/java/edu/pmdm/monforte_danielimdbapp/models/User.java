package edu.pmdm.monforte_danielimdbapp.models;

public class User {
    private String userId;
    private String name;
    private String email;
    private String loginTime;
    private String logoutTime;
    private String address;
    private String phone;
    private String image;

    public User() {
        this.userId = "";
        this.name ="";
        this.email = "";
        this.loginTime = "";
        this.logoutTime = "";
    }
    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.loginTime = "";
        this.logoutTime = "";
        this.address="";
        this.phone="";
        this.image="";
    }

    public User(String userId, String name, String email, String image) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.loginTime = "";
        this.logoutTime = "";
        this.address="";
        this.phone="";
        this.image=image;
    }

    public User(String userId, String name, String email, String address, String phone, String image) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.loginTime = "";
        this.logoutTime = "";
        this.address=address;
        this.phone=phone;
        this.image=image;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(String logoutTime) {
        this.logoutTime = logoutTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
