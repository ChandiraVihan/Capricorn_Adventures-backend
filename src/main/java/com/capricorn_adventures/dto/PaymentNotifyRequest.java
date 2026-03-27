package com.capricorn_adventures.dto;

public class PaymentNotifyRequest {

    private String merchant_id;
    private String order_id;
    private String payment_id;
    private String payhere_amount;
    private String payhere_currency;
    private String status_code;
    private String md5sig;
    private String custom_1;
    private String custom_2;
    private String method;
    private String status_message;
    private String card_holder_name;
    private String card_expiry;
    private String card_no;

    // No-args constructor
    public PaymentNotifyRequest() {
    }

    // All-args constructor
    public PaymentNotifyRequest(String merchant_id, String order_id, String payment_id,
                                String payhere_amount, String payhere_currency, String status_code,
                                String md5sig, String custom_1, String custom_2, String method,
                                String status_message, String card_holder_name,
                                String card_expiry, String card_no) {
        this.merchant_id = merchant_id;
        this.order_id = order_id;
        this.payment_id = payment_id;
        this.payhere_amount = payhere_amount;
        this.payhere_currency = payhere_currency;
        this.status_code = status_code;
        this.md5sig = md5sig;
        this.custom_1 = custom_1;
        this.custom_2 = custom_2;
        this.method = method;
        this.status_message = status_message;
        this.card_holder_name = card_holder_name;
        this.card_expiry = card_expiry;
        this.card_no = card_no;
    }

    // Getters and Setters
    public String getMerchant_id() { return merchant_id; }
    public void setMerchant_id(String merchant_id) { this.merchant_id = merchant_id; }

    public String getOrder_id() { return order_id; }
    public void setOrder_id(String order_id) { this.order_id = order_id; }

    public String getPayment_id() { return payment_id; }
    public void setPayment_id(String payment_id) { this.payment_id = payment_id; }

    public String getPayhere_amount() { return payhere_amount; }
    public void setPayhere_amount(String payhere_amount) { this.payhere_amount = payhere_amount; }

    public String getPayhere_currency() { return payhere_currency; }
    public void setPayhere_currency(String payhere_currency) { this.payhere_currency = payhere_currency; }

    public String getStatus_code() { return status_code; }
    public void setStatus_code(String status_code) { this.status_code = status_code; }

    public String getMd5sig() { return md5sig; }
    public void setMd5sig(String md5sig) { this.md5sig = md5sig; }

    public String getCustom_1() { return custom_1; }
    public void setCustom_1(String custom_1) { this.custom_1 = custom_1; }

    public String getCustom_2() { return custom_2; }
    public void setCustom_2(String custom_2) { this.custom_2 = custom_2; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus_message() { return status_message; }
    public void setStatus_message(String status_message) { this.status_message = status_message; }

    public String getCard_holder_name() { return card_holder_name; }
    public void setCard_holder_name(String card_holder_name) { this.card_holder_name = card_holder_name; }

    public String getCard_expiry() { return card_expiry; }
    public void setCard_expiry(String card_expiry) { this.card_expiry = card_expiry; }

    public String getCard_no() { return card_no; }
    public void setCard_no(String card_no) { this.card_no = card_no; }


}