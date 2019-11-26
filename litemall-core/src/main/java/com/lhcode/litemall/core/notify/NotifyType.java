package com.lhcode.litemall.core.notify;

public enum NotifyType {
    PAY_SUCCEED("paySucceed"),
    SHIP("ship"),
    REFUND("refund"),
    CAPTCHA("captcha"),
    SHUIGUO("水果商城");

    private String type;

    NotifyType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
