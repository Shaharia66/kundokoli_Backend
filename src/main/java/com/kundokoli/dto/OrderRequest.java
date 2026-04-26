package com.kundokoli.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String deliveryAddress;
    private String city;
    private String postalCode;
    private String paymentMethod;
    private String transactionId;
    private List<CartItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        private Long productId;
        private Integer quantity;
    }
}
