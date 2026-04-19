package com.easybase.forge.rest.model;

import java.util.List;

public record UnionDiscriminator(String propertyName, List<SubtypeMapping> subtypes) {

	public record SubtypeMapping(String discriminatorValue, String className) {}
}
