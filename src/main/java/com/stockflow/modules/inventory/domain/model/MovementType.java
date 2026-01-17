package com.stockflow.modules.inventory.domain.model;

/**
 * Enumeration representing the type of stock movement.
 *
 * <p>Movement types categorize stock operations for tracking and reporting purposes:</p>
 * <ul>
 *   <li><b>IN:</b> Stock entering the branch (purchases, returns, transfers in)</li>
 *   <li><b>OUT:</b> Stock leaving the branch (sales, losses, transfers out)</li>
 *   <li><b>ADJUSTMENT:</b> Manual stock corrections (positive or negative)</li>
 *   <li><b>TRANSFER:</b> Stock movements between branches (logged as both OUT and IN)</li>
 * </ul>
 */
public enum MovementType {

    /**
     * Stock entering the branch.
     * Increases the quantity in branch_product_stock.
     */
    IN,

    /**
     * Stock leaving the branch.
     * Decreases the quantity in branch_product_stock.
     */
    OUT,

    /**
     * Manual stock adjustment.
     * Can increase or decrease quantity depending on the specific reason.
     */
    ADJUSTMENT,

    /**
     * Transfer between branches.
     * Creates two movements: TRANSFER_OUT (source) and TRANSFER_IN (destination).
     */
    TRANSFER
}
