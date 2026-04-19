package com.example.petstore.controller.base;

import com.example.petstore.delegate.OwnersApiDelegate;
import com.example.petstore.dto.CreateOwnerRequest;
import com.example.petstore.dto.OwnerDTO;
import com.example.petstore.dto.UpdateOwnerRequest;
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
public abstract class OwnersControllerBase {
    private final OwnersApiDelegate delegate;

    protected OwnersControllerBase(OwnersApiDelegate delegate) {
        this.delegate = delegate;
    }

    protected OwnersApiDelegate getDelegate() {
        return delegate;
    }

    /**
     * <pre>curl -X GET http://localhost:8080/owners</pre>
     */
    @GetMapping("/owners")
    public ResponseEntity<List<OwnerDTO>> listOwners(
            @RequestParam(value = "email", required = false) String email) {
        return delegate.listOwners(email);
    }

    /**
     * <pre>curl -X POST http://localhost:8080/owners \
     *   -H 'Content-Type: application/json' \
     *   -d '{}'</pre>
     */
    @PostMapping("/owners")
    public ResponseEntity<OwnerDTO> createOwner(
            @Valid @RequestBody(required = true) CreateOwnerRequest createOwnerRequest) {
        return delegate.createOwner(createOwnerRequest);
    }

    /**
     * <pre>curl -X GET http://localhost:8080/owners/{id}</pre>
     */
    @GetMapping("/owners/{id}")
    public ResponseEntity<OwnerDTO> getOwnerById(@PathVariable("id") UUID id) {
        return delegate.getOwnerById(id);
    }

    /**
     * <pre>curl -X PUT http://localhost:8080/owners/{id} \
     *   -H 'Content-Type: application/json' \
     *   -d '{}'</pre>
     */
    @PutMapping("/owners/{id}")
    public ResponseEntity<OwnerDTO> updateOwner(@PathVariable("id") UUID id,
            @Valid @RequestBody(required = true) UpdateOwnerRequest updateOwnerRequest) {
        return delegate.updateOwner(id, updateOwnerRequest);
    }

    /**
     * <pre>curl -X DELETE http://localhost:8080/owners/{id}</pre>
     */
    @DeleteMapping("/owners/{id}")
    public ResponseEntity<Void> deleteOwner(@PathVariable("id") UUID id) {
        return delegate.deleteOwner(id);
    }
}
