package ua.kpi.comsys.dop;

/** Закрита ієрархія можливих станів замовлення. */
public sealed interface OrderStatus permits Pending, Paid, Shipped, Cancelled {
}
