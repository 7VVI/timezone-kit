package com.tzkit.annotation;

import java.lang.annotation.*;

/**
 * Annotation for controller parameters to indicate timezone-aware parsing.
 * Use with @RequestParam for date query parameters.
 *
 * Example:
 * @RequestParam @UserTZ LocalDate date
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserTZ {
}
