package com.stockflow.shared.domain.exception;

/**
 * Exception thrown when a stock operation cannot be completed due to insufficient stock.
 *
 * <p>This exception indicates that an attempt to withdraw or transfer stock
 * would result in a negative stock balance, which is not allowed by the system.</p>
 *
 * <p>Common scenarios:</p>
 * <ul>
 *   <li>Attempting to sell more items than available in stock</li>
 *   <li>Attempting to transfer more items than available in source branch</li>
 *   <li>Attempting to process a loss greater than current stock</li>
 * </ul>
 *
 * <p>Maps to HTTP 409 Conflict.</p>
 */
public class InsufficientStockException extends BaseDomainException {

    /**
     * Constructs a new insufficient stock exception with the specified error code and message.
     *
     * @param errorCode the error code (e.g., "STOCK_INSUFFICIENT")
     * @param message   the detail message explaining the stock shortage
     */
    public InsufficientStockException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructs a new insufficient stock exception with the specified error code, message, and cause.
     *
     * @param errorCode the error code
     * @param message   the detail message
     * @param cause     the cause of the exception
     */
    public InsufficientStockException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * Creates an insufficient stock exception for a specific product and branch.
     *
     * @param productId    the product ID that has insufficient stock
     * @param branchId     the branch ID where stock is insufficient
     * @param requestedQty the requested quantity
     * @param availableQty the available quantity
     * @return a new InsufficientStockException instance
     */
    public static InsufficientStockException of(Long productId, Long branchId, int requestedQty, int availableQty) {
        return new InsufficientStockException(
            "STOCK_INSUFFICIENT",
            String.format("Insufficient stock for product %d at branch %d. Requested: %d, Available: %d",
                productId, branchId, requestedQty, availableQty)
        );
    }
}
