package com.camcorderio.camcorderio.repository;

import com.camcorderio.camcorderio.entity.user.UserInfo;
import com.camcorderio.camcorderio.entity.user.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WishListRepository extends JpaRepository<WishList, UUID> {

    Optional<WishList> findByUser(UserInfo user);
}
