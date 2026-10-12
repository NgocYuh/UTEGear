package com.utegear.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPING,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus nextStatus) {
        if (nextStatus == null) {
            return false;
        }
        return switch (this) {
            case PENDING -> nextStatus == CONFIRMED || nextStatus == CANCELLED;
            case CONFIRMED -> nextStatus == PROCESSING || nextStatus == CANCELLED;
            case PROCESSING -> nextStatus == SHIPPING || nextStatus == CANCELLED;
            case SHIPPING -> nextStatus == COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
