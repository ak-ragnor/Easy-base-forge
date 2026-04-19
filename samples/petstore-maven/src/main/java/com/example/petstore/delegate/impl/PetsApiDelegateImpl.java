package com.example.petstore.delegate.impl;

import com.example.petstore.delegate.impl.base.PetsApiDelegateImplBase;
import com.example.petstore.domain.model.Pet;
import com.example.petstore.dto.CreatePetRequest;
import com.example.petstore.dto.PetDTO;
import com.example.petstore.dto.UpdatePetRequest;
import com.example.petstore.service.PetService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class PetsApiDelegateImpl extends PetsApiDelegateImplBase {

    private final PetService petService;

    public PetsApiDelegateImpl(PetService petService) {
        this.petService = petService;
    }

    @Override
    public ResponseEntity<List<PetDTO>> listPets(UUID ownerId, String status) {
        List<PetDTO> pets = petService.findAll().stream()
                .filter(p -> ownerId == null || ownerId.equals(p.getOwnerId()))
                .filter(p -> status == null || status.equalsIgnoreCase(p.getStatus()))
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(pets);
    }

    @Override
    public ResponseEntity<PetDTO> createPet(CreatePetRequest request) {
        Pet pet = new Pet();
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setStatus(request.getStatus());
        pet.setOwnerId(request.getOwnerId());
        pet.setMedicalRecordId(request.getMedicalRecordId());
        Pet saved = petService.create(pet);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @Override
    public ResponseEntity<PetDTO> getPetById(UUID id) {
        return petService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<PetDTO> updatePet(UUID id, UpdatePetRequest request) {
        return petService.findById(id).map(existing -> {
            if (request.getName() != null) existing.setName(request.getName());
            if (request.getBreed() != null) existing.setBreed(request.getBreed());
            if (request.getStatus() != null) existing.setStatus(request.getStatus());
            if (request.getMedicalRecordId() != null) existing.setMedicalRecordId(request.getMedicalRecordId());
            return ResponseEntity.ok(toDto(petService.update(id, existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> deletePet(UUID id) {
        petService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private PetDTO toDto(Pet pet) {
        PetDTO dto = new PetDTO();
        dto.setId(pet.getId());
        dto.setName(pet.getName());
        dto.setSpecies(pet.getSpecies());
        dto.setBreed(pet.getBreed());
        dto.setStatus(pet.getStatus());
        dto.setOwnerId(pet.getOwnerId());
        dto.setMedicalRecordId(pet.getMedicalRecordId());
        dto.setCreatedAt(pet.getCreatedAt() != null
                ? pet.getCreatedAt().atOffset(java.time.ZoneOffset.UTC)
                : null);
        return dto;
    }
}
