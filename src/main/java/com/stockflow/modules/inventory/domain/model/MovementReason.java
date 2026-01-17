package com.stockflow.modules.inventory.domain.model;

/**
 * Enumeration representing the reason for a stock movement.
 *
 * <p>Movement reasons provide detailed context about why a stock movement occurred,
 * enabling better inventory tracking, reporting, and audit trails.</p>
 *
 * <p><b>Type Mapping:</b></p>
 * <ul>
 *   <li>IN movements: PURCHASE, RETURN, ADJUSTMENT_IN, TRANSFER_IN</li>
 *   <li>OUT movements: SALE, LOSS, ADJUSTMENT_OUT, TRANSFER_OUT</li>
 * </ul>
 */
public enum MovementReason {

    /**
     * Stock purchased from suppliers.
     * Used with IN movement type.
     */
    PURCHASE,

    /**
     * Stock sold to customers.
     * Used with OUT movement type.
     */
    SALE,

    /**
     * Stock lost due to damage, theft, expiry, or other reasons.
     * Used with OUT movement type.
     */
    LOSS,

    /**
     * Stock returned by customers.
     * Used with IN movement type.
     */
    RETURN,

    /**
     * Manual stock adjustment to increase quantity.
     * Used with ADJUSTMENT movement type when stock needs to be increased.
     */
    ADJUSTMENT_IN,

    /**
     * Manual stock adjustment to decrease quantity.
     * Used with ADJUSTMENT movement type when stock needs to be decreased.
     */
    ADJUSTMENT_OUT,

    /**
     * Stock transferred in from another branch.
     * Used with IN movement type as part of a transfer operation.
     */
    TRANSFER_IN,

    /**
     * Stock transferred out to another branch.
     * Used with OUT movement type as part of a transfer operation.
     */
    TRANSFER_OUT
}
