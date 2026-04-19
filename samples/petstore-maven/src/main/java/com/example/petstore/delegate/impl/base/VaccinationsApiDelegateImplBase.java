package com.example.petstore.delegate.impl.base;

import com.example.petstore.delegate.VaccinationsApiDelegate;
import com.example.petstore.dto.CreateVaccinationRequest;
import com.example.petstore.dto.VaccinationDTO;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public abstract class VaccinationsApiDelegateImplBase implements VaccinationsApiDelegate {
    @Override
    public ResponseEntity<List<VaccinationDTO>> listVaccinations(UUID petId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<VaccinationDTO> createVaccination(
            CreateVaccinationRequest createVaccinationRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<VaccinationDTO> getVaccinationById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> deleteVaccination(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
