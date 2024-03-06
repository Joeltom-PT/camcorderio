package com.camcorderio.camcorderio.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Transient;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {

    private Long id;
    private String name;
    private String mobile;
    private String address;
    private String city;
    private String pin;
    private String state;

    @Transient
    public String getFullAddress() {
        return address + ", " + state+ ", " + city + " - " + pin;
    }
}
