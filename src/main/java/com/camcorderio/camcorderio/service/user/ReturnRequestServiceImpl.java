package com.camcorderio.camcorderio.service.user;

import com.camcorderio.camcorderio.entity.user.Orders;
import com.camcorderio.camcorderio.entity.user.ReturnRequest;
import com.camcorderio.camcorderio.entity.user.Wallet;
import com.camcorderio.camcorderio.entity.user.WalletHistory;
import com.camcorderio.camcorderio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReturnRequestServiceImpl implements ReturnRequestService{

    private final ReturnRequestRepository returnRequestRepository;

    private final OrderService orderService;

    private final OrderRepository orderRepository;

    private final WalletRepository walletRepository;

    private final WalletHistoryRepository walletHistoryRepository;

    public ReturnRequestServiceImpl(ReturnRequestRepository returnRequestRepository, OrderService orderService, OrderRepository orderRepository, WalletRepository walletRepository, WalletHistoryRepository walletHistoryRepository) {
        this.returnRequestRepository = returnRequestRepository;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.walletRepository = walletRepository;
        this.walletHistoryRepository = walletHistoryRepository;
    }

    @Override
    public void saveReturnRequest(ReturnRequest returnRequest) {
        returnRequestRepository.save(returnRequest);
    }

    @Override
    public List<ReturnRequest> getAllReturnRequests() {
        List<ReturnRequest> allReturnRequests = returnRequestRepository.findAll();
        return allReturnRequests.stream()
                .filter(returnRequest -> !returnRequest.isAccept() && !returnRequest.isReject())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReturnRequest getReturnRequestById(Long id) {
        Optional<ReturnRequest> optionalReturnRequest = returnRequestRepository.findById(id);
        return optionalReturnRequest.orElse(null);
    }

    @Override
    public ReturnRequest getReturnRequestByOrderId(Long orderId) {
        return returnRequestRepository.findByOrderId(orderId);
    }

    @Override
    public boolean returnReturnDecision(long id, String returnDecision) {
        ReturnRequest request = returnRequestRepository.findById(id).orElse(null);
        Orders order = orderService.getOrderById(request.getOrder().getOrderId());
        Wallet wallet = order.getUser().getWallet();
        if (request != null && order != null) {
            if ("accept".equals(returnDecision)) {
                request.setAccept(true);
                returnRequestRepository.save(request);


                double refundAmount = order.getTotalAmount();

                WalletHistory refundTransaction = new WalletHistory();
                refundTransaction.setAddedAmount(refundAmount);
                refundTransaction.setAmountAddedTime(LocalDateTime.now().toString());
                refundTransaction.setDepositOrWithdraw("refund");
                refundTransaction.setWallet(wallet);

                wallet.setTotalAmount(wallet.getTotalAmount() + refundAmount);

                walletHistoryRepository.save(refundTransaction);
                walletRepository.save(wallet);

                order.getPayments().setStatus("Refund");
                order.setRefundStatus(true);
                orderRepository.save(order);

                return true;
            } else if ("reject".equals(returnDecision)) {
                request.setReject(true);
                returnRequestRepository.save(request);
                return true;
            }
        }
        return false;
    }

}
