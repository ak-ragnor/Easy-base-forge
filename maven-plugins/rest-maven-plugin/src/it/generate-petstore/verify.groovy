// Verify the EasyBase Maven plugin generated the expected REST layer files

def base = new File(basedir, "target/generated-sources/easybase")

def expectedFiles = [
    // Pets resource
    "com/example/petstore/pets/dto/PetDTO.java",
    "com/example/petstore/pets/dto/CreatePetRequest.java",
    "com/example/petstore/pets/dto/UpdatePetRequest.java",
    "com/example/petstore/pets/delegate/PetsApiDelegate.java",
    "com/example/petstore/pets/controller/base/PetsControllerBase.java",
    "com/example/petstore/pets/controller/PetsController.java",

    // Orders resource
    "com/example/petstore/orders/dto/OrderDTO.java",
    "com/example/petstore/orders/dto/CreateOrderRequest.java",
    "com/example/petstore/orders/delegate/OrdersApiDelegate.java",
    "com/example/petstore/orders/controller/base/OrdersControllerBase.java",
    "com/example/petstore/orders/controller/OrdersController.java",
]

expectedFiles.each { path ->
    def file = new File(base, path)
    assert file.exists() : "Expected generated file not found: ${file.absolutePath}"
}

// Spot-check content
def delegate = new File(base, "com/example/petstore/pets/delegate/PetsApiDelegate.java")
def content = delegate.text
assert content.contains("interface PetsApiDelegate")     : "Missing interface declaration"
assert content.contains("ResponseEntity")                : "Missing ResponseEntity import/usage"
assert content.contains("listPets(")                     : "Missing listPets method"
assert content.contains("createPet(")                    : "Missing createPet method"
assert content.contains("deletePet(")                    : "Missing deletePet method"

def baseCtrl = new File(base, "com/example/petstore/pets/controller/base/PetsControllerBase.java")
assert baseCtrl.text.contains("abstract class PetsControllerBase") : "Missing abstract class"
assert baseCtrl.text.contains("@GetMapping")                       : "Missing @GetMapping"
assert baseCtrl.text.contains("@PostMapping")                      : "Missing @PostMapping"

def dto = new File(base, "com/example/petstore/pets/dto/CreatePetRequest.java")
assert dto.text.contains("@Data")          : "Missing @Data"
assert dto.text.contains("@NotBlank")      : "Missing @NotBlank"
assert dto.text.contains("@JsonProperty")  : "Missing @JsonProperty"

println "✓ All EasyBase generation assertions passed."
