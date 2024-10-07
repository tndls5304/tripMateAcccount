package com.tripmate.account.swagger;

import com.tripmate.account.common.errorcode.CommonErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodeExample {
   Class<? extends Enum<CommonErrorCode>>value();  //
}
