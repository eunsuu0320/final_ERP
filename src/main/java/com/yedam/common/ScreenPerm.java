package com.yedam.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ScreenPerm {
	String screen();
	
	Action action() default Action.READ;
	
	Action[] anyOf() default {};

	enum Action {
		READ, CREATE, UPDATE, DELETE
	}
}