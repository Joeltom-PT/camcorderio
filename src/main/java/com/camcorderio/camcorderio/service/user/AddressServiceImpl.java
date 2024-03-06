package com.camcorderio.camcorderio.service.user;

import com.camcorderio.camcorderio.entity.user.Address;
import com.camcorderio.camcorderio.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class AddressServiceImpl implements AddressService{
    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public Address findById(Long selectedAddressId) {
        return addressRepository.findById(selectedAddressId).orElse(null);
    }

}
