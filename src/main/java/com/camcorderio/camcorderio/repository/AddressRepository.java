package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address,Long> {



}
