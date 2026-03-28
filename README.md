# EasyBase REST Builder

**OpenAPI-first code generation for Spring Boot REST layers.**

Give EasyBase an OpenAPI spec and a minimal config — it generates controllers, delegates, and DTOs while keeping your business logic completely untouched.

---

## ✨ Why EasyBase?

- **Spec-driven** — OpenAPI is the single source of truth
- **Safe regeneration** — your custom code is never overwritten
- **Clean architecture** — strict separation between generated and custom layers
- **Zero boilerplate** — focus only on business logic

---

## 📦 What Gets Generated

For each resource (**tag**) in your OpenAPI spec:

| File | Overwritten | Purpose |
|------|------------|--------|
| `{Resource}ControllerBase.java` | ✅ Always | Abstract controller wired to delegate |
| `{Resource}ApiDelegate.java` | ✅ Always | Interface defining all endpoints |
| `DTOs & Requests` | ✅ Always | Lombok DTOs with validation |
| `{Resource}Controller.java` | ❌ Never | Your extension layer (safe zone) |

> Regeneration updates only generated files — your custom code remains untouched.

---

## 🚀 Quick Start

### 1. Add Maven Plugin

```xml
<plugin>
    <groupId>com.easybase</groupId>
    <artifactId>easybase-maven-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <configuration>
        <specFile>${project.basedir}/src/main/resources/api.yaml</specFile>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

### 2. Create Config

```yaml
basePackage: com.example.api
```

---

### 3. Generate Code

```bash
mvn generate-sources
```

Generated sources appear in:

```
target/generated-sources/easybase/
```

---

## ⚙️ Configuration

### Minimal

```yaml
basePackage: com.example.api
```

---

### Full Example

```yaml
basePackage: com.example.api

output:
  directory: target/generated-sources/easybase
  layout: MULTI_MODULE

structure:
  controller:
    package:     "{basePackage}.{resource}.controller"
    basePackage: "{basePackage}.{resource}.controller.base"
  delegate:
    package: "{basePackage}.{resource}.delegate"
  dto:
    package: "{basePackage}.{resource}.dto"

generate:
  delegateImpl: false
  responseEntityWrapping: ALWAYS
  beanValidation: true
  pagination: NONE
```

---

## 🧩 Configuration Breakdown

### Output Layout

| Option | Description |
|------|-------------|
| `MULTI_MODULE` | Each resource gets its own package |
| `FLAT` | All resources share common packages |

> FLAT mode detects DTO name collisions before generation.

---

### Response Wrapping

| Mode | Behavior |
|------|--------|
| `ALWAYS` | `ResponseEntity<T>` |
| `NEVER` | Plain return types |
| `VOID_ONLY` | Wrap only void responses |

---

### Pagination

```yaml
generate:
  pagination: SPRING_DATA
```

Enables:

- `Pageable` injection
- `Page<T>` return types

Trigger via:

```yaml
x-easybase-paginated: true
```

---

## 🖥 CLI Usage

```bash
java -jar easybase-cli.jar generate api.yaml
```

### Options

```
-c, --config     Config file (default: ./easybase-config.yaml)
-o, --output     Output directory
--dry-run        Preview without writing files
-h               Help
-V               Version
```

---

### Examples

```bash
# Default usage
java -jar easybase-cli.jar generate api.yaml

# Custom config
java -jar easybase-cli.jar generate api.yaml \
  --config my-config.yaml \
  --output src/main/java

# Preview only
java -jar easybase-cli.jar generate api.yaml --dry-run
```

---

## ⚡ Native Binary (GraalVM)

```bash
mvn package -pl easybase-cli -am -Pnative -DskipTests

./easybase-cli/target/easybase generate api.yaml
```

---

## 🏗 Generated Structure

```
com/example/api/
└── pets/
    ├── controller/
    │   ├── base/
    │   │   └── PetsControllerBase.java
    │   └── PetsController.java
    ├── delegate/
    │   └── PetsApiDelegate.java
    └── dto/
        ├── PetDTO.java
        ├── CreatePetRequest.java
        └── UpdatePetRequest.java
```

---

## 🧠 Generated Code Overview

### Delegate Interface

```java
public interface PetsApiDelegate {
    ResponseEntity<List<PetDTO>> listPets(String status, Integer page, Integer size);
    ResponseEntity<PetDTO> createPet(CreatePetRequest request);
}
```

---

### Base Controller

```java
@RestController
public abstract class PetsControllerBase {

    private final PetsApiDelegate delegate;

    protected PetsControllerBase(PetsApiDelegate delegate) {
        this.delegate = delegate;
    }

    @GetMapping("/pets")
    public ResponseEntity<List<PetDTO>> listPets(...) {
        return ResponseEntity.ok(delegate.listPets(...));
    }
}
```

---

### Custom Controller (Safe Zone)

```java
@RestController
public class PetsController extends PetsControllerBase {

    public PetsController(PetsApiDelegate delegate) {
        super(delegate);
    }
}
```

---

### Business Logic

```java
@Service
@RequiredArgsConstructor
public class PetsService implements PetsApiDelegate {

    private final PetRepository repository;

    @Override
    public ResponseEntity<List<PetDTO>> listPets(...) {
        // your logic
    }
}
```

---

## 🔍 OpenAPI Support

| Feature | Behavior |
|--------|--------|
| `$ref` | Generates DTO classes |
| `allOf` | Merges schemas |
| `oneOf` + discriminator | Polymorphic models |
| `nullable` | `@Nullable` |
| Validation | `@NotNull`, `@Size`, etc. |
| Formats | `UUID`, `LocalDate`, etc. |
| Pagination | Spring Data support |

---

## 🛠 Build from Source

```bash
git clone https://github.com/easybase/easy-base-forge.git
cd easy-base-forge

mvn verify

mvn package -pl easybase-cli -am -DskipTests

java -jar easybase-cli/target/easybase-cli-*.jar generate api.yaml
```

---

## 💡 Design Philosophy

- **Generated code is disposable**
- **Custom code is permanent**
- **OpenAPI is the contract**
- **Separation of concerns is enforced by design**