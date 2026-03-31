// Verify the EasyBase Maven plugin generated the expected Service layer files

def base = new File(basedir, "target/generated-sources/easybase")

def expectedFiles = [
    // Domain model
    "com/example/userservice/model/User.java",

    // Repository
    "com/example/userservice/repository/UserRepository.java",

    // Persistence layer
    "com/example/userservice/persistence/BaseEntity.java",
    "com/example/userservice/persistence/UserEntity.java",
    "com/example/userservice/persistence/UserJpaRepository.java",
    "com/example/userservice/persistence/UserPersistenceAdapter.java",

    // Service layer
    "com/example/userservice/service/base/UserBaseService.java",
    "com/example/userservice/service/base/UserBaseServiceImpl.java",
    "com/example/userservice/service/UserService.java",
    "com/example/userservice/service/UserServiceImpl.java",

    // Hook infrastructure
    "com/example/userservice/hook/UserHook.java",
    "com/example/userservice/hook/UserHookImpl.java",
]

expectedFiles.each { path ->
    def file = new File(base, path)
    assert file.exists() : "Expected generated file not found: ${file.absolutePath}"
}

// Spot-check: domain model is an immutable record
def model = new File(base, "com/example/userservice/model/User.java")
def modelText = model.text
assert modelText.contains("public record User(")       : "Model must be a record"
assert modelText.contains("UUID id")                   : "Model must have UUID id field"
assert modelText.contains("String email")              : "Model must have email field"
assert modelText.contains("UUID tenantId")             : "Model must have tenantId FK field"
assert modelText.contains("Instant createdAt")         : "Model must have audit createdAt field"
assert modelText.contains("Boolean deleted")           : "Model must have soft-delete field"

// Spot-check: JPA entity extends BaseEntity and has correct annotations
def entity = new File(base, "com/example/userservice/persistence/UserEntity.java")
def entityText = entity.text
assert entityText.contains("@Entity")                  : "Entity must have @Entity"
assert entityText.contains("eb_users")                 : "Entity table name must include prefix"
assert entityText.contains("extends BaseEntity")       : "Entity must extend BaseEntity (audit enabled)"
assert entityText.contains("@ManyToOne")               : "Entity must declare MANY_TO_ONE relationship"
assert entityText.contains("tenant_id")                : "Entity must have tenant FK column"
assert entityText.contains("fk_eb_users_tenant_id")    : "Entity must have FK constraint name"

// Spot-check: BaseEntity has audit fields and soft delete
def baseEntity = new File(base, "com/example/userservice/persistence/BaseEntity.java")
def baseEntityText = baseEntity.text
assert baseEntityText.contains("@MappedSuperclass")    : "BaseEntity must be a @MappedSuperclass"
assert baseEntityText.contains("abstract class BaseEntity") : "BaseEntity must be abstract"
assert baseEntityText.contains("@CreationTimestamp")   : "BaseEntity must have @CreationTimestamp"
assert baseEntityText.contains("Boolean deleted")      : "BaseEntity must have soft-delete field"

// Spot-check: JPA repository has soft-delete query methods
def jpaRepo = new File(base, "com/example/userservice/persistence/UserJpaRepository.java")
def jpaRepoText = jpaRepo.text
assert jpaRepoText.contains("@NoRepositoryBean")       : "JpaRepository must have @NoRepositoryBean"
assert jpaRepoText.contains("findActiveById(")         : "JpaRepository must have findActiveById"
assert jpaRepoText.contains("findAllActive(")          : "JpaRepository must have findAllActive"
assert jpaRepoText.contains("deleted = false")         : "Soft-delete query must filter on deleted = false"

// Spot-check: persistence adapter bridges domain and JPA
def adapter = new File(base, "com/example/userservice/persistence/UserPersistenceAdapter.java")
def adapterText = adapter.text
assert adapterText.contains("implements UserRepository") : "Adapter must implement UserRepository"
assert adapterText.contains("toDomain(")               : "Adapter must have toDomain method"
assert adapterText.contains("toEntity(")               : "Adapter must have toEntity method"

// Spot-check: abstract base service impl has transactional hooks
def baseServiceImpl = new File(base, "com/example/userservice/service/base/UserBaseServiceImpl.java")
def baseImplText = baseServiceImpl.text
assert baseImplText.contains("abstract class UserBaseServiceImpl") : "Must be abstract"
assert baseImplText.contains("@Transactional")         : "Write methods must be @Transactional"
assert baseImplText.contains("readOnly = true")        : "Read methods must use readOnly transaction"
assert baseImplText.contains("for (UserHook h : hooks)") : "Must iterate hooks (multiple=true)"
assert baseImplText.contains("beforeCreate")           : "Must call beforeCreate hook"
assert baseImplText.contains("afterCreate")            : "Must call afterCreate hook"

// Spot-check: developer-owned files are generated
def userService = new File(base, "com/example/userservice/service/UserService.java")
assert userService.text.contains("interface UserService") : "UserService must be an interface"

def userHook = new File(base, "com/example/userservice/hook/UserHook.java")
assert userHook.text.contains("interface UserHook")    : "UserHook must be an interface"

println "✓ All EasyBase build-service assertions passed."
