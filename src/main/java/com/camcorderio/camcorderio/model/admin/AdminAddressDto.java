package com.camcorderio.camcorderio.model.admin;

import lombok.Data;

@Data
public class AdminAddressDto {

    private Long id;
    private String name;
    private String mobile;
    private String address;
    private String city;
    private String pin;
    private String state;

    public String getFullAddress() {
        return name + ", " + mobile + ", " + address + ", " + city + ", " + pin + ", " + state;
    }

}
