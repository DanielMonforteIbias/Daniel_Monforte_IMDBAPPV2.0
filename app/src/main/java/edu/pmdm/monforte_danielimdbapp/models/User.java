package edu.pmdm.monforte_danielimdbapp.models;

public class User {
    String userId;
    String name;
    String email;
    String loginTime;
    String logoutTime;

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
}
