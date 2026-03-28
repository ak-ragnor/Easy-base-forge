# REST Builder

**OpenAPI-first code generation for Spring Boot REST layers.**

Give REST Builder an OpenAPI spec and a config file — it generates controllers, delegates, and DTOs. Your business logic lives in hand-written classes that the generator never touches, so you can regenerate freely as your API evolves.

---

## How It Works

```
OpenAPI spec (api.yaml)
        │
        ▼
  easybase-config.yaml
        │
        ▼
┌───────────────────────────────────────┐
│         GENERATED (always safe to     │
│         regenerate — never loses your │
│         changes)                      │
│                                       │
│  {Resource}ControllerBase.java        │  ← abstract Spring controller
│  {Resource}ApiDelegate.java           │  ← endpoint interface
│  {Resource}ApiDelegateImplBase.java   │  ← stub base (if delegateImpl: true)
│  DTOs / Request objects               │  ← Lombok + validation
└───────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────┐
│         CUSTOM (written once,         │
│         never overwritten)            │
│                                       │
│  {Resource}Controller.java            │  ← extends ControllerBase
│  {Resource}ApiDelegateImpl.java       │  ← your business logic
└───────────────────────────────────────┘
```

The regeneration contract: files marked **always overwritten** update when you regenerate; files marked **never overwritten** are created once and then left alone forever.

---

## What Gets Generated

For each resource (OpenAPI **tag**) in your spec:

| Artifact | Overwritten | Description |
|----------|:-----------:|-------------|
| `{Resource}ControllerBase.java` | ✅ Always | Abstract `@RestController` wired to the delegate |
| `{Resource}Controller.java` | ❌ Never | Your thin extension — add `@PreAuthorize`, custom logic here |
| `{Resource}ApiDelegate.java` | ✅ Always | Interface with one method per endpoint |
| `{Resource}ApiDelegateImplBase.java` | ✅ Always | Abstract stub — all methods throw `UnsupportedOperationException` |
| `{Resource}ApiDelegateImpl.java` | ❌ Never | Your implementation — override only what you need |
| DTOs and request/response classes | ✅ Always | Lombok `@Data` classes with Jakarta validation annotations |

`DelegateImplBase` + `DelegateImpl` are only generated when `generate.delegateImpl: true`.

---

## Features

### Layout Strategies

| Mode | Package structure | Best for |
|------|-------------------|----------|
| `FLAT` | `com.example.api.controller`, `com.example.api.dto` | APIs where multiple resources share DTOs |
| `MULTI_MODULE` | `com.example.api.pets.controller`, `com.example.api.pets.dto` | Per-resource isolation |

FLAT layout automatically detects DTO name collisions across resources before writing any files.

### Response Wrapping

| Mode | Return type | Use when |
|------|-------------|----------|
| `ALWAYS` | `ResponseEntity<T>` for every method | Full HTTP control (status codes, headers) |
| `NEVER` | Raw type `T` | Simpler signatures; status handled by exception mappers |
| `VOID_ONLY` | `ResponseEntity<Void>` for void; raw `T` otherwise | Mixed APIs with custom wrapper classes |

### Custom Response Wrapper

Wrap return types in your own generic class instead of `ResponseEntity`:

```yaml
generate:
  responseWrapper:
    enabled: true
    singleClass: "com.example.api.ApiResponse"
    pagedClass: "com.example.api.ApiPageResponse"
```

### Pagination

```yaml
generate:
  pagination: SPRING_DATA
```

Mark an endpoint as paginated in your spec:

```yaml
x-easybase-paginated: true
```

REST Builder injects `Pageable` as a parameter and returns `Page<T>`.

### Delegate Implementation Stubs

```yaml
generate:
  delegateImpl: true
```

Generates `{Resource}ApiDelegateImplBase` (always regenerated) and `{Resource}ApiDelegateImpl` (written once). Override only the methods you implement — unimplemented ones throw `UnsupportedOperationException` at runtime.

### Controller Annotations

```yaml
generate:
  crossOrigin: "*"          # adds @CrossOrigin to all base controllers
  slf4j: true               # adds @Slf4j (Lombok) to all base controllers
```

### Authorship

```yaml
generate:
  authors:
    - "Jane Doe"
    - "John Smith"
```

All generated files carry `@author` Javadoc entries for every name in the list. Adding a new author on regeneration appends — it does not replace.

### Bean Validation

```yaml
generate:
  beanValidation: true   # default
```

OpenAPI constraints map to Jakarta validation annotations:

| OpenAPI constraint | Generated annotation |
|--------------------|---------------------|
| `required` (string) | `@NotBlank` |
| `required` (non-string) | `@NotNull` |
| `minLength` / `maxLength` | `@Size(min=, max=)` |
| `minimum` / `maximum` | `@Min` / `@Max` |
| `pattern` | `@Pattern(regexp=)` |
| `format: email` | `@Email` |

### @Generated Annotation

```yaml
generate:
  addGeneratedAnnotation: true   # default
```

Stamps `@Generated("EasyBase Forge")` on every generated file, making it easy to exclude them from coverage reports and static analysis.

### Post-Generate Command

```yaml
generate:
  postGenerateCommand: "google-java-format --replace src/main/java/**/*.java"
```

Shell command executed after generation completes. Useful for auto-formatting. Times out after 10 minutes.

---

## Installation

### Maven Plugin

```xml
<plugin>
    <groupId>com.easybase</groupId>
    <artifactId>easybase-maven-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <configuration>
        <specFile>${project.basedir}/src/main/resources/api.yaml</specFile>
        <!-- configFile defaults to ${project.basedir}/easybase-config.yaml -->
    </configuration>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
        </execution>
    </executions>
</plugin>
```

Run manually: `mvn generate-sources`

Skip: `mvn ... -Deasybase.skip=true`

### Gradle Plugin

```groovy
plugins {
    id 'com.easybase.forge' version '0.1.0-SNAPSHOT'
}

repositories {
    mavenLocal() // plugin is currently local-only
}

easybase {
    specFile = file('src/main/resources/api.yaml')
    // configFile defaults to easybase-config.yaml in project root
}
```

Run: `./gradlew easybaseGenerate`

The task is automatically wired before `compileJava`.

### CLI

```bash
java -jar easybase-cli.jar generate api.yaml

# Options
-c, --config   Config file path   (default: ./easybase-config.yaml)
-o, --output   Output directory   (overrides config)
    --dry-run  Preview without writing any files
-h             Help
-V             Version
```

**Native binary (GraalVM — no JVM required):**

```bash
mvn package -pl easybase-cli -am -DskipTests -Pnative
./easybase-cli/target/easybase generate api.yaml
```

---

## Configuration Reference

### Minimal

```yaml
basePackage: com.example.api
```

### Complete Example

```yaml
basePackage: com.example.api

output:
  directory: src/main/java          # where to write generated sources
  layout: FLAT                      # FLAT | MULTI_MODULE

structure:
  controller:
    package:     "{basePackage}.controller"
    basePackage: "{basePackage}.controller.base"
  delegate:
    package: "{basePackage}.delegate"
  dto:
    package: "{basePackage}.dto"

generate:
  delegateImpl:           true
  responseEntityWrapping: ALWAYS    # ALWAYS | NEVER | VOID_ONLY
  beanValidation:         true
  pagination:             SPRING_DATA  # NONE | SPRING_DATA
  addGeneratedAnnotation: true
  authors:
    - "Your Name"
  crossOrigin:            "*"
  slf4j:                  true
  postGenerateCommand:    "echo done"
  responseWrapper:
    enabled:     false
    singleClass: "com.example.ApiResponse"
    pagedClass:  "com.example.ApiPageResponse"
```

### Package Pattern Placeholders

Placeholders in `structure.*` patterns are substituted per resource:

| Placeholder | Expands to |
|-------------|------------|
| `{basePackage}` | Value of `basePackage` root field |
| `{resource}` | Resource name, lowercase (e.g. `pets`) |
| `{Resource}` | Resource name, capitalized (e.g. `Pets`) |

### `generate` Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `delegateImpl` | boolean | `false` | Generate `DelegateImplBase` + `DelegateImpl` stubs |
| `responseEntityWrapping` | enum | `ALWAYS` | `ALWAYS` / `NEVER` / `VOID_ONLY` |
| `beanValidation` | boolean | `true` | Jakarta validation annotations on DTOs |
| `pagination` | enum | `NONE` | `NONE` / `SPRING_DATA` |
| `addGeneratedAnnotation` | boolean | `true` | Stamp `@Generated` on all generated files |
| `author` | string | `""` | Single author name |
| `authors` | list | `[]` | Multiple author names (merged with `author`) |
| `crossOrigin` | string | `null` | Value for `@CrossOrigin` on base controllers |
| `slf4j` | boolean | `false` | Add Lombok `@Slf4j` to base controllers |
| `postGenerateCommand` | string | `null` | Shell command to run after generation |
| `responseWrapper.enabled` | boolean | `false` | Use a custom wrapper class instead of `ResponseEntity` |
| `responseWrapper.singleClass` | string | — | Fully-qualified class for single-item responses |
| `responseWrapper.pagedClass` | string | — | Fully-qualified class for paginated responses |

---

## Generated Structure

### FLAT Layout

All resources share top-level packages. Best when DTOs are shared across resources.

```
src/main/java/com/example/api/
├── controller/
│   ├── base/
│   │   ├── PetsControllerBase.java        ← generated
│   │   └── OwnersControllerBase.java      ← generated
│   ├── PetsController.java                ← yours
│   └── OwnersController.java              ← yours
├── delegate/
│   ├── impl/
│   │   ├── base/
│   │   │   ├── PetsApiDelegateImplBase.java    ← generated
│   │   │   └── OwnersApiDelegateImplBase.java  ← generated
│   │   ├── PetsApiDelegateImpl.java        ← yours
│   │   └── OwnersApiDelegateImpl.java      ← yours
│   ├── PetsApiDelegate.java               ← generated
│   └── OwnersApiDelegate.java             ← generated
└── dto/
    ├── PetDTO.java                        ← generated
    ├── CreatePetRequest.java              ← generated
    └── OwnerDTO.java                      ← generated (shared)
```

### MULTI_MODULE Layout

Each resource gets its own package tree. Best for large APIs with strict per-resource boundaries.

```
src/main/java/com/example/api/
├── pets/
│   ├── controller/
│   │   ├── base/PetsControllerBase.java   ← generated
│   │   └── PetsController.java            ← yours
│   ├── delegate/
│   │   ├── impl/base/PetsApiDelegateImplBase.java  ← generated
│   │   ├── impl/PetsApiDelegateImpl.java   ← yours
│   │   └── PetsApiDelegate.java           ← generated
│   └── dto/
│       ├── PetDTO.java                    ← generated
│       └── CreatePetRequest.java          ← generated
└── owners/
    ├── controller/ ...
    ├── delegate/ ...
    └── dto/ ...
```

---

## How the Generated Code Works

### Delegate Interface

One method per endpoint. Always regenerated — adding an endpoint to your spec means a new method appears here automatically.

```java
public interface PetsApiDelegate {
    ResponseEntity<Page<PetDTO>> listPets(String status, String species, Pageable pageable);
    ResponseEntity<PetDTO>       createPet(CreatePetRequest request);
    ResponseEntity<PetDTO>       getPet(UUID id);
    ResponseEntity<PetDTO>       updatePet(UUID id, UpdatePetRequest request);
    ResponseEntity<Void>         deletePet(UUID id);
}
```

### Abstract Base Controller

Handles routing, Spring MVC annotations, validation, and delegation. You never touch this class.

```java
@Slf4j
@CrossOrigin("*")
@RestController
public abstract class PetsControllerBase {

    private final PetsApiDelegate delegate;

    protected PetsControllerBase(PetsApiDelegate delegate) {
        this.delegate = delegate;
    }

    @GetMapping("/pets")
    public ResponseEntity<Page<PetDTO>> listPets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String species,
            Pageable pageable) {
        return delegate.listPets(status, species, pageable);
    }

    @PostMapping("/pets")
    public ResponseEntity<PetDTO> createPet(
            @Valid @RequestBody CreatePetRequest request) {
        return delegate.createPet(request);
    }
}
```

### Custom Controller — Your Safe Zone

Extend the base. Add security, custom annotations, or override individual endpoints. Never regenerated.

```java
@RestController
public class PetsController extends PetsControllerBase {

    public PetsController(PetsApiDelegate delegate) {
        super(delegate);
    }

    // override endpoints here if needed
}
```

### Delegate Implementation

Extend the base stub. Override only the methods you implement. Unimplemented methods throw `UnsupportedOperationException` — easy to spot in tests.

```java
@Component
public class PetsApiDelegateImpl extends PetsApiDelegateImplBase {

    private final PetRepository repository;

    public PetsApiDelegateImpl(PetRepository repository) {
        this.repository = repository;
    }

    @Override
    public ResponseEntity<PetDTO> getPet(UUID id) {
        return repository.findById(id)
                .map(PetMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

---

## OpenAPI Feature Support

| OpenAPI feature | What REST Builder produces |
|-----------------|---------------------------|
| Tags | One resource (controller + delegate + DTOs) per tag |
| `$ref` schemas | DTO class |
| `allOf` | Merged field set into one DTO |
| `oneOf` + `discriminator` | Abstract base class with `@JsonTypeInfo` / `@JsonSubTypes`; concrete subclasses |
| `anyOf` (no discriminator) | `Object` return type |
| `nullable: true` | `@Nullable` annotation on field |
| `readOnly: true` | `@JsonProperty(access = READ_ONLY)` |
| `format: uuid` | `UUID` |
| `format: date` | `LocalDate` |
| `format: date-time` | `OffsetDateTime` |
| `format: email` | `String` + `@Email` |
| `format: int64` | `Long` |
| `format: float` | `Float` |
| `type: boolean` | `Boolean` |
| `minimum` / `maximum` | `@Min` / `@Max` |
| `minLength` / `maxLength` | `@Size` |
| `pattern` | `@Pattern` |
| Path parameters | `@PathVariable` |
| Query parameters | `@RequestParam(required = false)` |
| Request body | `@Valid @RequestBody` |
| `x-easybase-paginated: true` | Adds `Pageable` parameter; wraps return in `Page<T>` |

---

## Advanced Usage

### Custom Response Wrapper

Define your own wrapper classes for non-`ResponseEntity` responses:

```java
// Write these by hand — REST Builder uses them as return types
public class ApiResponse<T> { ... }
public class ApiPageResponse<T> { ... }
```

```yaml
generate:
  responseEntityWrapping: VOID_ONLY
  responseWrapper:
    enabled: true
    singleClass: "com.example.api.ApiResponse"
    pagedClass:  "com.example.api.ApiPageResponse"
```

Generated delegate methods then return `ApiResponse<PetDTO>` and `ApiPageResponse<PetDTO>` instead of `ResponseEntity`.

### Polymorphic Schemas (oneOf with discriminator)

```yaml
MedicalRecord:
  oneOf:
    - $ref: '#/components/schemas/VaccinationRecord'
    - $ref: '#/components/schemas/SurgeryRecord'
  discriminator:
    propertyName: recordType
    mapping:
      vaccination: '#/components/schemas/VaccinationRecord'
      surgery:     '#/components/schemas/SurgeryRecord'
```

Generates:
- `MedicalRecord.java` — interface
- `MedicalRecordBase.java` — abstract class with `@JsonTypeInfo` / `@JsonSubTypes`
- `VaccinationRecord.java` — concrete subclass
- `SurgeryRecord.java` — concrete subclass

### allOf Composition

```yaml
PatchPetRequest:
  allOf:
    - type: object
      properties:
        name:
          type: string
          nullable: true
        status:
          type: string
          nullable: true
```

Fields from all `allOf` schemas are merged into a single flat DTO.

### readOnly Fields

```yaml
PetDTO:
  properties:
    id:
      type: string
      format: uuid
      readOnly: true
```

Generates `@JsonProperty(access = JsonProperty.Access.READ_ONLY)` — the field is included in responses but ignored in deserialization.

### Dry Run (CLI)

Preview what would be generated without writing any files:

```bash
java -jar easybase-cli.jar generate api.yaml --dry-run
```

Output lists each file with its action: `CREATE` (new), `UPDATE` (regenerated), or `SKIP` (custom file, untouched).

---

## Demo Projects

The **[easy-base-forge-demo](https://github.com/ak-ragnor/Easy-base-forge-demo)** repository contains two fully-working Spring Boot projects:

| Project | Build tool | Layout | Notable config |
|---------|-----------|--------|----------------|
| `petstore-maven` | Maven | FLAT | `ALWAYS` wrapping, `SPRING_DATA` pagination, `delegateImpl`, `@Slf4j`, `@CrossOrigin`, multi-author, `postGenerateCommand` |
| `bookstore-gradle` | Gradle | MULTI_MODULE | `VOID_ONLY` wrapping, custom `ApiResponse` wrapper, `addGeneratedAnnotation: false` |

Each project has a commit-by-commit history showing the real workflow: project setup → add config → write OpenAPI spec → run generation.
