package com.pack.material.common;

public class Constants {

    public static final String REDIS_STOCK_PREFIX = "pack:stock:";
    public static final String REDIS_SAFETY_STOCK_PREFIX = "pack:safety:stock:";
    public static final String REDIS_STOCK_LOCK_PREFIX = "pack:stock:lock:";

    public static final Integer RECORD_TYPE_PURCHASE_IN = 1;
    public static final Integer RECORD_TYPE_RECEIVE_OUT = 2;
    public static final Integer RECORD_TYPE_REPORT_DEDUCT = 3;
    public static final Integer RECORD_TYPE_INVENTORY_PROFIT = 4;
    public static final Integer RECORD_TYPE_INVENTORY_LOSS = 5;

    public static final Integer REPORT_STATUS_PENDING = 1;
    public static final Integer REPORT_STATUS_DEDUCTED = 2;
    public static final Integer REPORT_STATUS_CANCELLED = 3;

    public static final Integer PURCHASE_STATUS_PENDING = 1;
    public static final Integer PURCHASE_STATUS_PURCHASED = 2;
    public static final Integer PURCHASE_STATUS_CLOSED = 3;

    public static final Integer STOCK_LOCK_EXPIRE_SECONDS = 300;
}
