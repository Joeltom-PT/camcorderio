package com.camcorderio.camcorderio.entity.user;

import com.camcorderio.camcorderio.model.user.AddressDto;
import com.camcorderio.camcorderio.service.verification.OtpService;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String mobile;
    private String address;
    private String city;
    private String pin;
    private String state;
    private boolean isDeleted = false;

    @Transient
    public String getFullAddress() {
        return address + ", " + state+ ", " + city + " - " + pin;
    }

    public AddressDto toDto() {
        return new AddressDto(
                this.id,
                this.name,
                this.mobile,
                this.address,
                this.city,
                this.pin,
                this.state
        );
    }

    public void updateFromDto(AddressDto addressDto) {
        this.name = addressDto.getName();
        this.mobile = addressDto.getMobile();
        this.address = addressDto.getAddress();
        this.city = addressDto.getCity();
        this.pin = addressDto.getPin();
        this.state = addressDto.getState();
    }

}
