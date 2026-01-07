package mingovvv.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private ValidEnum annotation;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return true;
        }

        // 검증 대상 Enum 상수들 가져오기
        Object[] enumValues = this.annotation.enumClass().getEnumConstants();

        if (enumValues == null) {
            return false;
        }

        // 순회하며 매칭 확인
        return Arrays.stream(enumValues)
            .anyMatch(enumValue -> {
                if (this.annotation.ignoreCase()) {
                    return value.equalsIgnoreCase(enumValue.toString());
                } else {
                    return value.equals(enumValue.toString());
                }
            });
    }

}
