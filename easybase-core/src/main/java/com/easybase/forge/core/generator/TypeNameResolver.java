package com.easybase.forge.core.generator;

import com.squareup.javapoet.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Converts a Java type string (as produced by {@link com.easybase.forge.core.parser.SchemaResolver})
 * into a JavaPoet {@link TypeName}.
 *
 * <p>Unknown types (i.e. DTO class names) are resolved against {@code dtoPkg}.
 */
public class TypeNameResolver {

    private final String dtoPkg;

    public TypeNameResolver(String dtoPkg) {
        this.dtoPkg = dtoPkg;
    }

    public TypeName resolve(String javaType) {
        if (javaType == null || javaType.isBlank()) {
            return TypeName.VOID;
        }

        // List<X>
        if (javaType.startsWith("List<") && javaType.endsWith(">")) {
            String inner = javaType.substring(5, javaType.length() - 1);
            return ParameterizedTypeName.get(
                    ClassName.get("java.util", "List"),
                    resolve(inner));
        }

        return switch (javaType) {
            case "void", "Void"      -> TypeName.VOID.box();
            case "String"            -> ClassName.get(String.class);
            case "Integer"           -> ClassName.get(Integer.class);
            case "Long"              -> ClassName.get(Long.class);
            case "Boolean"           -> ClassName.get(Boolean.class);
            case "Float"             -> ClassName.get(Float.class);
            case "BigDecimal"        -> ClassName.get(BigDecimal.class);
            case "LocalDate"         -> ClassName.get(LocalDate.class);
            case "OffsetDateTime"    -> ClassName.get(OffsetDateTime.class);
            case "UUID"              -> ClassName.get(UUID.class);
            case "Object"            -> ClassName.get(Object.class);
            case "byte[]"            -> ArrayTypeName.of(TypeName.BYTE);
            default                  -> ClassName.get(dtoPkg, javaType);
        };
    }

    /** Wraps a type in {@code ResponseEntity<T>}. Void becomes {@code ResponseEntity<Void>}. */
    public TypeName responseEntity(String javaType) {
        TypeName body = (javaType == null || javaType.equals("Void") || javaType.isBlank())
                ? ClassName.get(Void.class)
                : resolve(javaType);
        return ParameterizedTypeName.get(
                ClassName.get("org.springframework.http", "ResponseEntity"),
                body);
    }
}
