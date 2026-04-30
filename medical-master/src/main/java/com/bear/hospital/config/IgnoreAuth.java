package com.bear.hospital.config;

import java.lang.annotation.*;

/**
 * 忽略Token验证
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreAuth {

}
