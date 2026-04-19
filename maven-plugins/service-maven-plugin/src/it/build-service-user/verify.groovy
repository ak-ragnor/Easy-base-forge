def base = new File(basedir, "target/generated-sources/easybase")

def expectedFiles = [
    "com/example/userservice/user/domain/model/UserBase.java",
    "com/example/userservice/user/domain/model/User.java",

    "com/example/userservice/user/domain/entity/UserEntity.java",

    "com/example/userservice/user/infrastructure/repository/base/UserRepositoryBase.java",
    "com/example/userservice/user/infrastructure/repository/UserRepository.java",

    "com/example/userservice/user/infrastructure/persistence/base/UserJpaRepositoryBase.java",
    "com/example/userservice/user/infrastructure/persistence/base/UserPersistenceAdapterBase.java",
    "com/example/userservice/user/infrastructure/persistence/UserJpaRepository.java",
    "com/example/userservice/user/infrastructure/persistence/UserPersistenceAdapter.java",

    "com/example/userservice/user/infrastructure/hook/base/UserHookBase.java",
    "com/example/userservice/user/infrastructure/hook/UserHook.java",

    "com/example/userservice/user/service/base/UserServiceBase.java",
    "com/example/userservice/user/service/base/UserServiceBaseImpl.java",
    "com/example/userservice/user/service/base/UserLocalServiceBase.java",
    "com/example/userservice/user/service/base/UserLocalServiceBaseImpl.java",
    "com/example/userservice/user/service/UserLocalService.java",
    "com/example/userservice/user/service/UserService.java",
]

expectedFiles.each { path ->
    def file = new File(base, path)
    assert file.exists() : "Expected generated file not found: ${file.absolutePath}"
}

def commonEntity = new File(base, "com/example/userservice/common/entity")
assert !commonEntity.exists() : "common/entity should NOT be generated"

def userBase = new File(base, "com/example/userservice/user/domain/model/UserBase.java")
def userBaseText = userBase.text
assert userBaseText.contains("public class UserBase")           : "UserBase must be a class"
assert userBaseText.contains("@AllArgsConstructor")             : "UserBase must have @AllArgsConstructor"
assert userBaseText.contains("UUID id")                         : "UserBase must have UUID id field"
assert userBaseText.contains("String email")                    : "UserBase must have email field"
assert userBaseText.contains("UUID tenantId")                   : "UserBase must have tenantId FK field (from ONE_TO_ONE relationship)"
assert userBaseText.contains("Instant createdAt")               : "UserBase must have audit createdAt field"
assert userBaseText.contains("Boolean deleted")                 : "UserBase must have soft-delete field"

def user = new File(base, "com/example/userservice/user/domain/model/User.java")
def userText = user.text
assert userText.contains("public class User extends UserBase")  : "User must extend UserBase"
assert !userText.contains("UUID id")                            : "User shell must NOT redeclare id"
assert !userText.contains("String email")                       : "User shell must NOT redeclare business fields"

def entity = new File(base, "com/example/userservice/user/domain/entity/UserEntity.java")
def entityText = entity.text
assert entityText.contains("@Entity")                              : "Entity must have @Entity"
assert entityText.contains("eb_users")                             : "Entity table name must include prefix"
assert !entityText.contains("extends ")                            : "Entity must NOT extend any base class (traits are inline)"
assert entityText.contains("@Id")                                  : "Entity must have @Id field directly"
assert entityText.contains("private Instant createdAt")            : "Entity must declare createdAt inline"
assert entityText.contains("private Instant updatedAt")            : "Entity must declare updatedAt inline"
assert entityText.contains("private Boolean deleted")              : "Entity must declare deleted inline"
assert entityText.contains("@Getter")                              : "Entity must have @Getter"
assert entityText.contains("@Setter")                              : "Entity must have @Setter"
assert !entityText.contains("callSuper")                           : "Entity must NOT have callSuper (no inheritance)"
assert entityText.contains("tenant_id")                            : "Entity must have tenant FK column"
assert !entityText.contains("@ManyToOne")                          : "Entity must NOT have @ManyToOne"
assert !entityText.contains("@JoinColumn")                         : "Entity must NOT have @JoinColumn"

def jpaRepoBase = new File(base, "com/example/userservice/user/infrastructure/persistence/base/UserJpaRepositoryBase.java")
def jpaRepoBaseText = jpaRepoBase.text
assert jpaRepoBaseText.contains("@NoRepositoryBean")       : "JpaRepositoryBase must have @NoRepositoryBean"
assert jpaRepoBaseText.contains("findActiveById(")         : "JpaRepositoryBase must have findActiveById"
assert jpaRepoBaseText.contains("findAllActive(")          : "JpaRepositoryBase must have findAllActive"
assert jpaRepoBaseText.contains("deleted = false")         : "Soft-delete query must filter on deleted = false"

def jpaRepo = new File(base, "com/example/userservice/user/infrastructure/persistence/UserJpaRepository.java")
def jpaRepoText = jpaRepo.text
assert jpaRepoText.contains("interface UserJpaRepository")   : "JpaRepository must be an interface"
assert jpaRepoText.contains("extends UserJpaRepositoryBase") : "JpaRepository must extend the base"
assert !jpaRepoText.contains("@NoRepositoryBean")            : "JpaRepository stub must NOT have @NoRepositoryBean"

def adapterBase = new File(base, "com/example/userservice/user/infrastructure/persistence/base/UserPersistenceAdapterBase.java")
def adapterBaseText = adapterBase.text
assert adapterBaseText.contains("abstract class UserPersistenceAdapterBase") : "Must be abstract"
assert adapterBaseText.contains("implements UserRepository")               : "AdapterBase must implement UserRepository"
assert adapterBaseText.contains("toDomain(")                               : "AdapterBase must have toDomain"
assert adapterBaseText.contains("toEntity(")                               : "AdapterBase must have toEntity"
assert adapterBaseText.contains("for (UserHookBase h : userHooks)")        : "AdapterBase must iterate userHooks"
assert adapterBaseText.contains("beforeSave")                              : "AdapterBase must call beforeSave"
assert adapterBaseText.contains("afterSave")                               : "AdapterBase must call afterSave"
assert adapterBaseText.contains("beforeUpdate")                            : "AdapterBase must call beforeUpdate"
assert adapterBaseText.contains("afterUpdate")                             : "AdapterBase must call afterUpdate"
assert !adapterBaseText.contains("@Autowired")                             : "AdapterBase must NOT use @Autowired"
assert adapterBaseText.contains("protected final UserJpaRepository userJpaRepository") : "Field must be entity-qualified"
assert !adapterBaseText.contains("hooks != null ?")                        : "AdapterBase constructor must NOT use ternary"
assert adapterBaseText.contains("Collections.emptyList()")                 : "AdapterBase constructor must initialize to emptyList()"

def adapter = new File(base, "com/example/userservice/user/infrastructure/persistence/UserPersistenceAdapter.java")
def adapterText = adapter.text
assert adapterText.contains("class UserPersistenceAdapter")      : "Adapter must be a class"
assert adapterText.contains("extends UserPersistenceAdapterBase") : "Adapter must extend base"
assert adapterText.contains("toDomain(")                         : "Adapter must implement toDomain"
assert adapterText.contains("toEntity(")                         : "Adapter must implement toEntity"
assert !adapterText.contains("@Autowired")                       : "Adapter must NOT use @Autowired"
assert adapterText.contains("domain.setId(")                     : "toDomain must use setter for id"
assert adapterText.contains("domain.setEmail(")                  : "toDomain must use setter for email"
assert !adapterText.contains("return new User(")                 : "toDomain must NOT use constructor args"
assert adapterText.contains("super(userJpaRepository")           : "Adapter constructor must call super with entity-qualified param"

def serviceBaseImpl = new File(base, "com/example/userservice/user/service/base/UserServiceBaseImpl.java")
def baseImplText = serviceBaseImpl.text
assert baseImplText.contains("abstract class UserServiceBaseImpl") : "Must be abstract"
assert baseImplText.contains("implements UserServiceBase")         : "Must implement UserServiceBase"
assert baseImplText.contains("@Transactional")                    : "Write methods must be @Transactional"
assert baseImplText.contains("readOnly = true")                   : "Read methods must use readOnly transaction"
assert !baseImplText.contains("UserHook")                         : "ServiceBaseImpl must NOT reference hooks"
assert baseImplText.contains("UserLocalService userLocalService")       : "Must inject concrete UserLocalService"
assert !baseImplText.contains("UserRepository")                   : "ServiceBaseImpl must NOT reference repository"
assert baseImplText.contains("userLocalService.create(")               : "Must delegate create to userLocalService"
assert baseImplText.contains("userLocalService.update(")               : "Must delegate update to userLocalService"

def localServiceBase = new File(base, "com/example/userservice/user/service/base/UserLocalServiceBase.java")
def localServiceBaseText = localServiceBase.text
assert localServiceBaseText.contains("interface UserLocalServiceBase")  : "UserLocalServiceBase must be an interface"
assert localServiceBaseText.contains("BaseService")                     : "UserLocalServiceBase must extend BaseService"
assert !localServiceBaseText.contains("extends UserServiceBase")        : "UserLocalServiceBase must NOT extend UserServiceBase"

def localServiceBaseImpl = new File(base, "com/example/userservice/user/service/base/UserLocalServiceBaseImpl.java")
def localBaseImplText = localServiceBaseImpl.text
assert localBaseImplText.contains("abstract class UserLocalServiceBaseImpl") : "Must be abstract"
assert localBaseImplText.contains("implements UserLocalServiceBase")          : "Must implement UserLocalServiceBase"
assert localBaseImplText.contains("UserRepository userRepository")             : "Must own entity-qualified repository field"
assert localBaseImplText.contains("userRepository.create(")                    : "Must call userRepository.create()"
assert localBaseImplText.contains("userRepository.update(")                    : "Must call userRepository.update()"
assert !localBaseImplText.contains("extends UserServiceBaseImpl")             : "Must NOT extend UserServiceBaseImpl"

def localService = new File(base, "com/example/userservice/user/service/UserLocalService.java")
assert localService.text.contains("@Service")                    : "LocalService must be annotated @Service"
assert localService.text.contains("extends UserLocalServiceBaseImpl") : "LocalService must extend base impl"

def userService = new File(base, "com/example/userservice/user/service/UserService.java")
def userServiceText = userService.text
assert userServiceText.contains("@Component")                        : "UserService must be annotated @Component"
assert userServiceText.contains("extends UserServiceBaseImpl")       : "UserService must extend UserServiceBaseImpl"
assert userServiceText.contains("UserLocalService userLocalService") : "UserService constructor must accept concrete UserLocalService"
assert userServiceText.contains("super(userLocalService)")           : "UserService constructor must call super(userLocalService)"

def hookBase = new File(base, "com/example/userservice/user/infrastructure/hook/base/UserHookBase.java")
def hookBaseText = hookBase.text
assert hookBaseText.contains("interface UserHookBase")     : "HookBase must be an interface"
assert hookBaseText.contains("beforeSave")                 : "HookBase must have beforeSave"
assert hookBaseText.contains("afterSave")                  : "HookBase must have afterSave"
assert hookBaseText.contains("beforeUpdate")               : "HookBase must have beforeUpdate"
assert hookBaseText.contains("afterUpdate")                : "HookBase must have afterUpdate"

def userHook = new File(base, "com/example/userservice/user/infrastructure/hook/UserHook.java")
assert userHook.text.contains("class UserHook")            : "UserHook must be a class"
assert userHook.text.contains("implements UserHookBase")   : "UserHook must implement UserHookBase"

println "All EasyBase build-service assertions passed."
