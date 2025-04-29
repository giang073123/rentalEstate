package com.giang.rentalEstate.enums;

public enum PropertyStatus {
//    PENDING_PAYMENT, //chờ thanh toán để xét duyệt
    PENDING_REVIEW, //chờ admin duyệt
    APPROVED, //đã được duyệt, đang hiển thị
    REJECTED, //bị từ chối
    EXPIRED, //hết thời gian hiển thị
    RENTED //đã được thuê

}
