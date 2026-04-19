package com.example.petstore.controller.base;

import com.example.petstore.delegate.VaccinationsApiDelegate;
import com.example.petstore.dto.CreateVaccinationRequest;
import com.example.petstore.dto.VaccinationDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @generated
 */
public abstract class VaccinationsControllerBase {
    private final VaccinationsApiDelegate delegate;

    protected VaccinationsControllerBase(VaccinationsApiDelegate delegate) {
        this.delegate = delegate;
    }

    protected VaccinationsApiDelegate getDelegate() {
        return delegate;
    }

    /**
     * <pre>curl -X GET http://localhost:8080/vaccinations</pre>
     */
    @GetMapping("/vaccinations")
    public ResponseEntity<List<VaccinationDTO>> listVaccinations(
            @RequestParam(value = "petId", required = false) UUID petId) {
        return delegate.listVaccinations(petId);
    }

    /**
     * <pre>curl -X POST http://localhost:8080/vaccinations \
     *   -H 'Content-Type: application/json' \
     *   -d '{}'</pre>
     */
    @PostMapping("/vaccinations")
    public ResponseEntity<VaccinationDTO> createVaccination(
            @Valid @RequestBody(required = true) CreateVaccinationRequest createVaccinationRequest) {
        return delegate.createVaccination(createVaccinationRequest);
    }

    /**
     * <pre>curl -X GET http://localhost:8080/vaccinations/{id}</pre>
     */
    @GetMapping("/vaccinations/{id}")
    public ResponseEntity<VaccinationDTO> getVaccinationById(@PathVariable("id") UUID id) {
        return delegate.getVaccinationById(id);
    }

    /**
     * <pre>curl -X DELETE http://localhost:8080/vaccinations/{id}</pre>
     */
    @DeleteMapping("/vaccinations/{id}")
    public ResponseEntity<Void> deleteVaccination(@PathVariable("id") UUID id) {
        return delegate.deleteVaccination(id);
    }
}
