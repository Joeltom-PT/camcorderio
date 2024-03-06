package com.camcorderio.camcorderio.service.others;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Order;
import org.json.JSONObject;

public class RazorpayService {

    private final String apiKey;
    private final String apiSecret;

    public RazorpayService(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public String createOrder(int amount, String currency, String receipt, String notes) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient(apiKey, apiSecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);
        orderRequest.put("notes", notes);

        Order order;
        try {
            order = razorpayClient.orders.create(orderRequest);
        } catch (RazorpayException e) {
            throw new RazorpayException("Failed to create Razorpay order", e);
        }

        return order.get("id");
    }
}
