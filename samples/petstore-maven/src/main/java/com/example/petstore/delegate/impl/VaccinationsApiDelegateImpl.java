package com.example.petstore.delegate.impl;

import com.example.petstore.delegate.impl.base.VaccinationsApiDelegateImplBase;
import com.example.petstore.domain.model.Vaccination;
import com.example.petstore.dto.CreateVaccinationRequest;
import com.example.petstore.dto.VaccinationDTO;
import com.example.petstore.service.VaccinationService;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class VaccinationsApiDelegateImpl extends VaccinationsApiDelegateImplBase {

    private final VaccinationService vaccinationService;

    public VaccinationsApiDelegateImpl(VaccinationService vaccinationService) {
        this.vaccinationService = vaccinationService;
    }

    @Override
    public ResponseEntity<List<VaccinationDTO>> listVaccinations(UUID petId) {
        List<VaccinationDTO> records = vaccinationService.findAll().stream()
                .filter(v -> petId == null || petId.equals(v.getPetId()))
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(records);
    }

    @Override
    public ResponseEntity<VaccinationDTO> createVaccination(CreateVaccinationRequest request) {
        Vaccination vaccination = new Vaccination();
        vaccination.setVaccineName(request.getVaccineName());
        vaccination.setPetId(request.getPetId());
        vaccination.setAdministeredDate(request.getAdministeredDate());
        vaccination.setNextDueDate(request.getNextDueDate());
        vaccination.setBatchNumber(request.getBatchNumber());
        vaccination.setNotes(request.getNotes());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(vaccinationService.create(vaccination)));
    }

    @Override
    public ResponseEntity<VaccinationDTO> getVaccinationById(UUID id) {
        return vaccinationService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> deleteVaccination(UUID id) {
        vaccinationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private VaccinationDTO toDto(Vaccination vaccination) {
        VaccinationDTO dto = new VaccinationDTO();
        dto.setId(vaccination.getId());
        dto.setVaccineName(vaccination.getVaccineName());
        dto.setPetId(vaccination.getPetId());
        dto.setAdministeredDate(vaccination.getAdministeredDate());
        dto.setNextDueDate(vaccination.getNextDueDate());
        dto.setBatchNumber(vaccination.getBatchNumber());
        dto.setNotes(vaccination.getNotes());
        if (vaccination.getCreatedAt() != null) {
            dto.setCreatedAt(vaccination.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return dto;
    }
}
