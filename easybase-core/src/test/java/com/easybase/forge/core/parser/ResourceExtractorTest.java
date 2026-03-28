package com.easybase.forge.core.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.easybase.forge.core.model.*;

import io.swagger.v3.oas.models.OpenAPI;

class ResourceExtractorTest {

	private List<ApiResource> resources;

	@BeforeEach
	void parseSpec() {
		URL specUrl = getClass().getResource("/specs/petstore.yaml");
		assertThat(specUrl).as("petstore.yaml not found on classpath").isNotNull();

		Path specPath = Paths.get(specUrl.getPath());
		OpenAPI openApi = new OpenApiLoader().load(specPath);

		ValidationMapper validationMapper = new ValidationMapper();
		SchemaResolver schemaResolver = new SchemaResolver(openApi, validationMapper);
		PaginationDetector paginationDetector = new PaginationDetector();

		resources = new ResourceExtractor(schemaResolver, validationMapper, paginationDetector).extract(openApi);
	}

	@Test
	void twoResourcesAreExtracted() {
		assertThat(resources).hasSize(2);
		assertThat(resources).extracting(ApiResource::name).containsExactlyInAnyOrder("Pets", "Orders");
	}

	@Test
	void petsResourceHasFiveEndpoints() {
		ApiResource pets = findResource("Pets");
		assertThat(pets.endpoints()).hasSize(5);
	}

	@Test
	void ordersResourceHasTwoEndpoints() {
		ApiResource orders = findResource("Orders");
		assertThat(orders.endpoints()).hasSize(2);
	}

	@Test
	void listPetsIsPaginated() {
		ApiResource pets = findResource("Pets");
		ApiEndpoint listPets = findEndpoint(pets, "listPets");
		assertThat(listPets.paginated()).isTrue();
	}

	@Test
	void getPetByIdHasPathParameter() {
		ApiResource pets = findResource("Pets");
		ApiEndpoint get = findEndpoint(pets, "getPetById");

		assertThat(get.parameters()).hasSize(1);

		ApiParameter idParam = get.parameters().get(0);

		assertThat(idParam.name()).isEqualTo("id");
		assertThat(idParam.in()).isEqualTo(ParameterLocation.PATH);
		assertThat(idParam.required()).isTrue();
		assertThat(idParam.schema().javaType()).isEqualTo("UUID");
	}

	@Test
	void createPetHasRequestBody() {
		ApiResource pets = findResource("Pets");
		ApiEndpoint create = findEndpoint(pets, "createPet");

		assertThat(create.requestBody()).isNotNull();
		assertThat(create.requestBody().schema().javaType()).isEqualTo("CreatePetRequest");
		assertThat(create.requestBody().required()).isTrue();
	}

	@Test
	void deletePetReturns204WithNoBody() {
		ApiResource pets = findResource("Pets");
		ApiEndpoint delete = findEndpoint(pets, "deletePet");

		assertThat(delete.responses()).containsKey(204);
		assertThat(delete.responses().get(204).schema()).isNull();
	}

	@Test
	void createPetRequestHasValidationOnNameField() {
		ApiResource pets = findResource("Pets");
		List<DtoSchema> dtos = pets.dtoSchemas();
		DtoSchema createRequest = findDto(dtos, "CreatePetRequest");

		DtoField nameField = createRequest.fields().stream()
				.filter(f -> f.name().equals("name"))
				.findFirst()
				.orElseThrow(() -> new AssertionError("name field not found"));

		assertThat(nameField.required()).isTrue();
		assertThat(nameField.jsonName()).isEqualTo("name");
		assertThat(nameField.validations()).anyMatch(v -> v instanceof ValidationConstraint.NotBlank);
		assertThat(nameField.validations())
				.anyMatch(v -> v instanceof ValidationConstraint.Size s && s.min() == 1 && s.max() == 100);
	}

	@Test
	void createOrderRequestQuantityHasMinMaxConstraints() {
		ApiResource orders = findResource("Orders");
		DtoSchema createOrder = findDto(orders.dtoSchemas(), "CreateOrderRequest");

		DtoField quantity = createOrder.fields().stream()
				.filter(f -> f.name().equals("quantity"))
				.findFirst()
				.orElseThrow();

		assertThat(quantity.validations()).anyMatch(v -> v instanceof ValidationConstraint.Min m && m.value() == 1);
		assertThat(quantity.validations()).anyMatch(v -> v instanceof ValidationConstraint.Max m && m.value() == 100);
	}

	@Test
	void petDtoHasOffsetDateTimeForCreatedAt() {
		ApiResource pets = findResource("Pets");
		DtoSchema petDto = findDto(pets.dtoSchemas(), "PetDTO");
		Optional<DtoField> createdAt = petDto.fields().stream()
				.filter(f -> f.name().equals("createdAt"))
				.findFirst();

		assertThat(createdAt).isPresent();
		assertThat(createdAt.get().javaType()).isEqualTo("OffsetDateTime");
	}

	@Test
	void orderDtoHasLocalDateForOrderDate() {
		ApiResource orders = findResource("Orders");
		DtoSchema orderDto = findDto(orders.dtoSchemas(), "OrderDTO");
		Optional<DtoField> orderDate = orderDto.fields().stream()
				.filter(f -> f.name().equals("orderDate"))
				.findFirst();

		assertThat(orderDate).isPresent();
		assertThat(orderDate.get().javaType()).isEqualTo("LocalDate");
	}

	private ApiResource findResource(String name) {
		return resources.stream()
				.filter(r -> r.name().equals(name))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Resource not found: " + name));
	}

	private ApiEndpoint findEndpoint(ApiResource resource, String operationId) {
		return resource.endpoints().stream()
				.filter(e -> e.operationId().equals(operationId))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Endpoint not found: " + operationId));
	}

	private DtoSchema findDto(List<DtoSchema> dtos, String className) {
		return dtos.stream()
				.filter(d -> d.className().equals(className))
				.findFirst()
				.orElseThrow(() -> new AssertionError("DTO not found: " + className));
	}
}
