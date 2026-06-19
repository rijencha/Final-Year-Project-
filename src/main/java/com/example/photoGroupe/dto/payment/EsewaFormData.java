package com.example.photoGroupe.dto.payment;

public record EsewaFormData(
        String amount,
        String taxAmount,
        String totalAmount,
        String transactionUuid,
        String productCode,
        String productServiceCharge,
        String productDeliveryCharge,
        String successUrl,
        String failureUrl,
        String signedFieldNames,
        String signature,
        String paymentUrl
) {}