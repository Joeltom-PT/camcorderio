package com.camcorderio.camcorderio.service.user;

import com.camcorderio.camcorderio.entity.user.ReturnRequest;

import java.util.List;

public interface ReturnRequestService {
    void saveReturnRequest(ReturnRequest returnRequest);

    List<ReturnRequest> getAllReturnRequests();

    ReturnRequest getReturnRequestById(Long id);

    ReturnRequest getReturnRequestByOrderId(Long orderId);

    boolean returnReturnDecision(long id, String returnDecision);
}
