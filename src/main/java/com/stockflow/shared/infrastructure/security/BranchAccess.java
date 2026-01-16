package com.stockflow.shared.infrastructure.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BranchAccess {
    /**
     * Parameter name that contains the branch ID
     * Default is "branchId"
     */
    String paramName() default "branchId";

    /**
     * Whether to throw exception or return empty result
     * Default is true (throw exception)
     */
    boolean throwIfDenied() default true;
}
