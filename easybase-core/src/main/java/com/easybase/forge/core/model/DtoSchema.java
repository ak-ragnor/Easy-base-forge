package com.easybase.forge.core.model;

import java.util.List;

public record DtoSchema(
        /** Simple class name, e.g. {@code PetDTO}, {@code CreatePetRequest}. */
        String className,
        /** Fully-qualified package name for this DTO. */
        String packageName,
        List<DtoField> fields
) {
    public String fullyQualifiedName() {
        return packageName + "." + className;
    }
}
