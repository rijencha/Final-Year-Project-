package com.example.photoGroupe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "esewa")
public class EsewaConfig {

    private String merchantCode;
    private String secretKey;
    private String paymentUrl;
    private String verifyUrl;
    private String successUrl;
    private String failureUrl;

    public String getMerchantCode()  { return merchantCode; }
    public String getSecretKey()     { return secretKey; }
    public String getPaymentUrl()    { return paymentUrl; }
    public String getVerifyUrl()     { return verifyUrl; }
    public String getSuccessUrl()    { return successUrl; }
    public String getFailureUrl()    { return failureUrl; }

    public void setMerchantCode(String merchantCode)   { this.merchantCode = merchantCode; }
    public void setSecretKey(String secretKey)         { this.secretKey = secretKey; }
    public void setPaymentUrl(String paymentUrl)       { this.paymentUrl = paymentUrl; }
    public void setVerifyUrl(String verifyUrl)         { this.verifyUrl = verifyUrl; }
    public void setSuccessUrl(String successUrl)       { this.successUrl = successUrl; }
    public void setFailureUrl(String failureUrl)       { this.failureUrl = failureUrl; }
}