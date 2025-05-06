package com.gentlecorp.person.models.filter;

import java.lang.annotation.*;

/**
 * Annotation zur Markierung von Filterfeldern in einem Filter-DTO.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FilterField {
    String path() default ""; // MongoDB-Feldname (optional überschreibbar)
    FilterOperator operator() default FilterOperator.IS;
}
