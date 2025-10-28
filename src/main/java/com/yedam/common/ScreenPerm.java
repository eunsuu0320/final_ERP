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
	// 화면
	String screen();
	
	// 단일 권한
	Action action() default Action.READ;
	
	// 다중 권한
	Action[] anyOf() default {};
	
	enum Action {
		READ, CREATE, UPDATE, DELETE
	}
}

