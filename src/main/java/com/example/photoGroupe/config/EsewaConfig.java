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
    private String workshopSuccessUrl;
    private String workshopFailureUrl;
    private String boostSuccessUrl;
    private String boostFailureUrl;
    private String bannerSuccessUrl;
    private String bannerFailureUrl;

    public String getMerchantCode()        { return merchantCode; }
    public String getSecretKey()           { return secretKey; }
    public String getPaymentUrl()          { return paymentUrl; }
    public String getVerifyUrl()           { return verifyUrl; }
    public String getSuccessUrl()          { return successUrl; }
    public String getFailureUrl()          { return failureUrl; }
    public String getWorkshopSuccessUrl()  { return workshopSuccessUrl; }   // ← ADD
    public String getWorkshopFailureUrl()  { return workshopFailureUrl; }   // ← ADD
    public String getBoostSuccessUrl()     { return boostSuccessUrl; }
    public String getBoostFailureUrl()     { return boostFailureUrl; }
    public String getBannerSuccessUrl()    { return bannerSuccessUrl; }
    public String getBannerFailureUrl()    { return bannerFailureUrl; }


    public void setMerchantCode(String merchantCode)               { this.merchantCode = merchantCode; }
    public void setSecretKey(String secretKey)                     { this.secretKey = secretKey; }
    public void setPaymentUrl(String paymentUrl)                   { this.paymentUrl = paymentUrl; }
    public void setVerifyUrl(String verifyUrl)                     { this.verifyUrl = verifyUrl; }
    public void setSuccessUrl(String successUrl)                   { this.successUrl = successUrl; }
    public void setFailureUrl(String failureUrl)                   { this.failureUrl = failureUrl; }
    public void setWorkshopSuccessUrl(String workshopSuccessUrl)   { this.workshopSuccessUrl = workshopSuccessUrl; }   // ← ADD
    public void setWorkshopFailureUrl(String workshopFailureUrl)   { this.workshopFailureUrl = workshopFailureUrl; }   // ← ADD
    public void setBoostSuccessUrl(String boostSuccessUrl)         { this.boostSuccessUrl = boostSuccessUrl; }
    public void setBoostFailureUrl(String boostFailureUrl)         { this.boostFailureUrl = boostFailureUrl; }
    public void setBannerSuccessUrl(String bannerSuccessUrl)       { this.bannerSuccessUrl = bannerSuccessUrl; }
    public void setBannerFailureUrl(String bannerFailureUrl)       { this.bannerFailureUrl = bannerFailureUrl; }
}