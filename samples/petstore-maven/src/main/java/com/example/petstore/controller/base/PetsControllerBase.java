package com.example.petstore.controller.base;

import com.example.petstore.delegate.PetsApiDelegate;
import com.example.petstore.dto.CreatePetRequest;
import com.example.petstore.dto.PetDTO;
import com.example.petstore.dto.UpdatePetRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @generated
 */
public abstract class PetsControllerBase {
    private final PetsApiDelegate delegate;

    protected PetsControllerBase(PetsApiDelegate delegate) {
        this.delegate = delegate;
    }

    protected PetsApiDelegate getDelegate() {
        return delegate;
    }

    /**
     * <pre>curl -X GET http://localhost:8080/pets</pre>
     */
    @GetMapping("/pets")
    public ResponseEntity<List<PetDTO>> listPets(
            @RequestParam(value = "ownerId", required = false) UUID ownerId,
            @RequestParam(value = "status", required = false) String status) {
        return delegate.listPets(ownerId, status);
    }

    /**
     * <pre>curl -X POST http://localhost:8080/pets \
     *   -H 'Content-Type: application/json' \
     *   -d '{}'</pre>
     */
    @PostMapping("/pets")
    public ResponseEntity<PetDTO> createPet(
            @Valid @RequestBody(required = true) CreatePetRequest createPetRequest) {
        return delegate.createPet(createPetRequest);
    }

    /**
     * <pre>curl -X GET http://localhost:8080/pets/{id}</pre>
     */
    @GetMapping("/pets/{id}")
    public ResponseEntity<PetDTO> getPetById(@PathVariable("id") UUID id) {
        return delegate.getPetById(id);
    }

    /**
     * <pre>curl -X PUT http://localhost:8080/pets/{id} \
     *   -H 'Content-Type: application/json' \
     *   -d '{}'</pre>
     */
    @PutMapping("/pets/{id}")
    public ResponseEntity<PetDTO> updatePet(@PathVariable("id") UUID id,
            @Valid @RequestBody(required = true) UpdatePetRequest updatePetRequest) {
        return delegate.updatePet(id, updatePetRequest);
    }

    /**
     * <pre>curl -X DELETE http://localhost:8080/pets/{id}</pre>
     */
    @DeleteMapping("/pets/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable("id") UUID id) {
        return delegate.deletePet(id);
    }
}
