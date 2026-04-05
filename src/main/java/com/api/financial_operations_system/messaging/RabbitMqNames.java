package com.api.financial_operations_system.messaging;

public final class RabbitMqNames {

    public static final String EXCHANGE = "finops.events";
    public static final String QUEUE_ORDERS = "finops.financial-order.events";
    public static final String RK_ORDER_CREATED = "financial-order.created";

    private RabbitMqNames() {}
}
