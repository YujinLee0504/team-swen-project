package com.ufund.api.ufundapi.model;

import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

    public enum AccountType {
        User,
        Manager
    }

    @JsonProperty("id") private int id;
    @JsonProperty("username") private String username;
    @JsonProperty("password") private String password;
    @JsonProperty("accountType") private AccountType accountType;
    @JsonProperty("latitude") private double latitude;
    @JsonProperty("longitude") private double longitude;
    // @JsonProperty("pledgeBasket") private PledgeBasket pledgeBasket;
    @JsonCreator
    public User (@JsonProperty("id") int id, @JsonProperty("username") String username, @JsonProperty("password") String password, @JsonProperty("accountType")AccountType accountType, @JsonProperty("latitude") double latitude, @JsonProperty("longitude") double longitude, @JsonProperty("pledgeBasket") PledgeBasket pledgeBasket) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.accountType = accountType;
        this.latitude = latitude;
        this.longitude = longitude;
        // this.pledgeBasket = pledgeBasket;
    }

    public User (@JsonProperty("id") int id, @JsonProperty("username") String username, @JsonProperty("password") String password, @JsonProperty("accountType")AccountType accountType, @JsonProperty("latitude") double latitude, @JsonProperty("longitude") double longitude) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.accountType = accountType;
        this.latitude = latitude;
        this.longitude = longitude;
        // this.pledgeBasket = new PledgeBasket(id, Collections.emptyList());
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public AccountType getAccountType() { return accountType; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    // public PledgeBasket getPledgeBasket() { return pledgeBasket; }

    public void setUsername( String username ) { this.username = username; }
    public void setPassword( String password ) { this.password = password; }
    public void setAccountType( AccountType accountType ) { this.accountType = accountType; }
    public void setLatitude( double latitude ) { this.latitude = latitude; }
    public void setLongitude( double longitude ) { this.longitude = longitude; }
    // public void setPledgeBasket( PledgeBasket pledgeBasket ) { this.pledgeBasket = pledgeBasket; }

    // 7 characters alphanumeric special symbol

}
