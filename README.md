# Easy Base Forge

**Code generation toolkit for Spring Boot applications.**

Easy Base Forge generates production-ready Spring Boot boilerplate from your API specifications — keeping your business logic untouched while the framework evolves around it.

---

## Modules

| Module | Status | Description |
|--------|--------|-------------|
| [REST Builder](RESTBUILDER.md) | ✅ Available | Controllers, delegates, and DTOs generated from OpenAPI specs |
| Service Builder | 🔜 Planned | Service layer scaffolding |

---

## REST Builder — Quick Start

Generate a complete Spring Boot REST layer in three steps.

**1. Add the Maven plugin**

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
            <goals><goal>generate</goal></goals>
        </execution>
    </executions>
</plugin>
```

**2. Create `easybase-config.yaml`**

```yaml
basePackage: com.example.api
```

**3. Generate**

```bash
mvn generate-sources
```

→ [Full REST Builder documentation](RESTBUILDER.md)

---

## Build from Source

```bash
git clone https://github.com/ak-ragnor/Easy-base-forge.git
cd Easy-base-forge

# Build and test all modules
mvn verify

# Build fat JAR for CLI use
mvn package -pl easybase-cli -am -DskipTests

# Run the CLI
java -jar easybase-cli/target/easybase-cli-*.jar generate api.yaml
```

---

## Project Structure

```
easy-base-forge/
├── easybase-core/            Core engine, generators, parser, config
├── easybase-maven-plugin/    Maven plugin (generate-sources phase)
├── easybase-gradle-plugin/   Gradle plugin (easybaseGenerate task)
├── easybase-cli/             Standalone CLI with native-image support
└── easybase-test-fixtures/   Shared test utilities and sample specs
```

---

## License

MIT — see [LICENSE](LICENSE)
