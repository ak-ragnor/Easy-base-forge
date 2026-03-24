# EasyBase REST Builder

OpenAPI-first code generator for Spring Boot REST layers. Give it an OpenAPI spec and a one-line config; it generates the controller skeleton, delegate interface, and DTOs — without ever touching your business logic.

## What it generates

For each resource (tag) in your spec, EasyBase produces four files:

| File | Overwrite on regen | Description |
|---|---|---|
| `{Resource}ControllerBase.java` | Always | Abstract Spring MVC controller wired to the delegate |
| `{Resource}ApiDelegate.java` | Always | Interface with one method per endpoint |
| `{Resource}DTO.java`, `Create{Resource}Request.java`, … | Always | Lombok `@Data` DTOs with validation |
| `{Resource}Controller.java` | **Never** (create once) | Your empty shell — extend and inject your delegate |

Regenerating after a spec change updates the base/delegate/DTOs but leaves your `Controller.java` untouched.

---

## Quick Start

### 1 — Add the Maven plugin

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

### 2 — Create `easybase-config.yaml`

```yaml
basePackage: com.example.api
```

That's the minimum. Run `mvn generate-sources` and your REST layer appears under `target/generated-sources/easybase/`.

---

## Configuration Reference

```yaml
# Required
basePackage: com.example.api

# Optional — output location
output:
  directory: target/generated-sources/easybase   # default
  layout: MULTI_MODULE                             # MULTI_MODULE (default) | FLAT

# Optional — package patterns
# {basePackage}, {resource}, {Resource} are substituted at runtime
structure:
  controller:
    package:     "{basePackage}.{resource}.controller"
    basePackage: "{basePackage}.{resource}.controller.base"
  delegate:
    package: "{basePackage}.{resource}.delegate"
  dto:
    package: "{basePackage}.{resource}.dto"

# Optional — generation options
generate:
  delegateImpl:          false    # generate a default delegate no-op implementation
  responseEntityWrapping: ALWAYS  # ALWAYS | NEVER | VOID_ONLY
  beanValidation:         true    # emit @NotNull / @Size / @Email / @NotBlank / etc.
  pagination:             NONE    # NONE | SPRING_DATA
```

### `output.layout`

| Value | Effect |
|---|---|
| `MULTI_MODULE` | Each resource in its own sub-package: `com.example.api.pets.controller` |
| `FLAT` | All resources share a single package set: `com.example.api.controller` |

When using `FLAT`, EasyBase detects and reports DTO class name collisions across resources before writing any files.

### `generate.responseEntityWrapping`

| Value | Delegate / controller return type |
|---|---|
| `ALWAYS` | `ResponseEntity<PetDTO>`, `ResponseEntity<Void>` |
| `NEVER` | `PetDTO`, `void` |
| `VOID_ONLY` | `PetDTO` for body responses; `ResponseEntity<Void>` for void |

### `generate.pagination: SPRING_DATA`

Endpoints tagged with `x-easybase-paginated: true` (or detected by `page`/`size` query params) receive:
- `org.springframework.data.domain.Pageable pageable` as the final parameter
- `Page<T>` return type (or `ResponseEntity<Page<T>>` with `ALWAYS` wrapping)

Requires `spring-data-commons` on the consumer's classpath.

---

## Maven Plugin

```xml
<plugin>
    <groupId>com.easybase</groupId>
    <artifactId>easybase-maven-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <configuration>
        <!-- Required: path to your OpenAPI YAML or JSON spec -->
        <specFile>${project.basedir}/src/main/resources/api.yaml</specFile>

        <!-- Optional: path to easybase-config.yaml
             Default: ${project.basedir}/easybase-config.yaml -->
        <configFile>${project.basedir}/easybase-config.yaml</configFile>

        <!-- Optional: override the output directory
             Default: ${project.build.directory}/generated-sources/easybase -->
        <outputDirectory>${project.build.directory}/generated-sources/easybase</outputDirectory>

        <!-- Optional: skip generation entirely -->
        <skip>false</skip>
    </configuration>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
            <!-- Default phase is generate-sources -->
        </execution>
    </executions>
</plugin>
```

The generated sources directory is automatically added to the compile source roots — no extra configuration needed.

---

## CLI

Download `easybase-cli-<version>.jar` from the releases page, or build it with `mvn package -pl easybase-cli -am`.

```
Usage: easybase generate [-hV] [--dry-run] [-c=<configFile>] [-o=<outputDirectory>]
                         <specFile>

      <specFile>        Path to the OpenAPI YAML/JSON spec file.
  -c, --config=FILE     Path to easybase-config.yaml.
                          Default: ./easybase-config.yaml
  -o, --output=DIR      Output directory. Overrides output.directory in config.
      --dry-run         Print what would be generated without writing any files.
  -h, --help            Show this help message and exit.
  -V, --version         Print version information and exit.
```

### Examples

```bash
# Generate with defaults (reads easybase-config.yaml in current dir)
java -jar easybase-cli.jar generate api.yaml

# Custom config + output directory
java -jar easybase-cli.jar generate api.yaml \
  --config my-config.yaml \
  --output src/main/java

# Preview what would be written
java -jar easybase-cli.jar generate api.yaml --dry-run
```

### Native binary (GraalVM)

Build a standalone binary (no JVM required):

```bash
# Requires GraalVM JDK 21+ with native-image installed
mvn package -pl easybase-cli -am -Pnative -DskipTests
./easybase-cli/target/easybase generate api.yaml
```

---

## Generated Code Structure

Given `basePackage: com.example.api` and a `pets` resource:

```
com/example/api/
└── pets/
    ├── controller/
    │   ├── base/
    │   │   └── PetsControllerBase.java    ← abstract, always regenerated
    │   └── PetsController.java            ← yours, never overwritten
    ├── delegate/
    │   └── PetsApiDelegate.java           ← interface, always regenerated
    └── dto/
        ├── PetDTO.java
        ├── CreatePetRequest.java
        └── UpdatePetRequest.java
```

### `PetsApiDelegate.java`
```java
public interface PetsApiDelegate {
    ResponseEntity<List<PetDTO>> listPets(String status, Integer page, Integer size);
    ResponseEntity<PetDTO> createPet(CreatePetRequest createPetRequest);
    ResponseEntity<PetDTO> getPetById(UUID id);
    ResponseEntity<PetDTO> updatePet(UUID id, UpdatePetRequest updatePetRequest);
    ResponseEntity<Void> deletePet(UUID id);
}
```

### `PetsControllerBase.java`
```java
@RestController
@RequestMapping
public abstract class PetsControllerBase {
    private final PetsApiDelegate delegate;

    protected PetsControllerBase(PetsApiDelegate delegate) {
        this.delegate = delegate;
    }

    @GetMapping("/pets")
    public ResponseEntity<List<PetDTO>> listPets(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(delegate.listPets(status, page, size));
    }
    // ...
}
```

### `PetsController.java` (created once, never overwritten)
```java
@RestController
public class PetsController extends PetsControllerBase {
    public PetsController(PetsApiDelegate delegate) {
        super(delegate);
    }
}
```

### Typical service wiring

```java
@Service
@RequiredArgsConstructor
public class PetsService implements PetsApiDelegate {

    private final PetRepository petRepository;

    @Override
    public ResponseEntity<List<PetDTO>> listPets(String status, Integer page, Integer size) {
        // your business logic here
    }
}
```

---

## OpenAPI Feature Support

| Feature | Behaviour |
|---|---|
| `$ref` schemas | Resolved to named DTO class |
| `allOf` | Fields merged from all referenced schemas |
| `oneOf` + `discriminator` | Abstract base class with `@JsonTypeInfo`/`@JsonSubTypes`; variants extend it |
| `oneOf` / `anyOf` (no discriminator) | All variant schemas generated as DTOs; return type is `Object` |
| `nullable: true` | `@org.springframework.lang.Nullable` on the generated field |
| String formats | `date` → `LocalDate`, `date-time` → `OffsetDateTime`, `uuid` → `UUID`, `binary` → `byte[]` |
| `required` fields | `@NotNull` / `@NotBlank` |
| `minLength` / `maxLength` | `@Size(min=, max=)` |
| `minimum` / `maximum` | `@Min` / `@Max` |
| `format: email` | `@Email` |
| `pattern` | `@Pattern(regexp=)` |
| `x-easybase-paginated: true` | Injects `Pageable`, returns `Page<T>` (requires `generate.pagination: SPRING_DATA`) |

---

## Building from Source

```bash
git clone https://github.com/easybase/easy-base-forge.git
cd easy-base-forge

# Build and run all tests (including Maven plugin invoker test)
mvn verify

# Build the CLI fat JAR
mvn package -pl easybase-cli -am -DskipTests

# Run against your own spec
java -jar easybase-cli/target/easybase-cli-*.jar generate path/to/api.yaml
```

