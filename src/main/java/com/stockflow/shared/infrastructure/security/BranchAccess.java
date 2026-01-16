package com.stockflow.shared.infrastructure.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to validate branch access for authenticated users.
 *
 * <p>When applied to a method or parameter, this annotation ensures that
 * the authenticated user has access to the specified branch.</p>
 *
 * <p>Usage examples:</p>
 * <pre>
 * // Method-level validation
 * {@code @BranchAccess}
 * public ResponseEntity<?> getBranchStock(@PathVariable Long branchId) {
 *     // User must have access to branchId
 * }
 *
 * // Parameter-level validation
 * public ResponseEntity<?> updateStock(
 *     {@code @BranchAccess} @PathVariable Long branchId,
 *     @RequestBody StockUpdateRequest request
 * ) {
 *     // User must have access to branchId
 * }
 * </pre>
 *
 * <p>The validation is performed by the {@link BranchAccessAspect} aspect.</p>
 *
 * @see com.stockflow.shared.infrastructure.security.BranchAccessAspect
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface BranchAccess {
}
