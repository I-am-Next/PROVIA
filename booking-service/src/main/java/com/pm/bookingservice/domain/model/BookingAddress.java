package com.pm.bookingservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BookingAddress {

    @Column(nullable = false)
    private String addressLine1;

    private String addressLine2;

    @Column(nullable = false)
    private String city;

    private String state;

    private String postalCode;

    @Column(nullable = false)
    private String country;



    protected BookingAddress() {
    }


    public BookingAddress(
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode,
            String country

    ) {

        validate(addressLine1, city, country);

        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;

        this.city = city;
        this.state = state;
        this.postalCode = postalCode;

        this.country = country;

    }


    private void validate(
            String addressLine1,
            String city,
            String country
    ) {

        if (addressLine1 == null || addressLine1.isBlank()) {
            throw new IllegalArgumentException("Address line 1 is required");
        }

        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }

        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country is required");
        }
    }


    public String getAddressLine1() {
        return addressLine1;
    }


    public String getAddressLine2() {
        return addressLine2;
    }


    public String getCity() {
        return city;
    }


    public String getState() {
        return state;
    }


    public String getPostalCode() {
        return postalCode;
    }


    public String getCountry() {
        return country;
    }


}