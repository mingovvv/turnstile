package mingovvv.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {

    String message() default "Invalid enum value.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // 검증할 Enum 클래스 지정
    Class<? extends Enum<?>> enumClass();

    // 대소문자 무시 여부 (기본값 false)
    boolean ignoreCase() default false;

}
