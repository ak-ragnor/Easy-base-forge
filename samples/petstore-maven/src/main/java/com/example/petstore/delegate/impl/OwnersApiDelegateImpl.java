package com.example.petstore.delegate.impl;

import com.example.petstore.delegate.impl.base.OwnersApiDelegateImplBase;
import com.example.petstore.domain.model.Owner;
import com.example.petstore.dto.CreateOwnerRequest;
import com.example.petstore.dto.OwnerDTO;
import com.example.petstore.dto.UpdateOwnerRequest;
import com.example.petstore.service.OwnerService;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class OwnersApiDelegateImpl extends OwnersApiDelegateImplBase {

    private final OwnerService ownerService;

    public OwnersApiDelegateImpl(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @Override
    public ResponseEntity<List<OwnerDTO>> listOwners(String email) {
        List<OwnerDTO> owners = ownerService.findAll().stream()
                .filter(o -> email == null || email.equalsIgnoreCase(o.getEmail()))
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(owners);
    }

    @Override
    public ResponseEntity<OwnerDTO> createOwner(CreateOwnerRequest request) {
        Owner owner = new Owner();
        owner.setName(request.getName());
        owner.setEmail(request.getEmail());
        owner.setPhone(request.getPhone());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(ownerService.create(owner)));
    }

    @Override
    public ResponseEntity<OwnerDTO> getOwnerById(UUID id) {
        return ownerService.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<OwnerDTO> updateOwner(UUID id, UpdateOwnerRequest request) {
        return ownerService.findById(id).map(existing -> {
            if (request.getName() != null) existing.setName(request.getName());
            if (request.getPhone() != null) existing.setPhone(request.getPhone());
            return ResponseEntity.ok(toDto(ownerService.update(id, existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> deleteOwner(UUID id) {
        ownerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private OwnerDTO toDto(Owner owner) {
        OwnerDTO dto = new OwnerDTO();
        dto.setId(owner.getId());
        dto.setName(owner.getName());
        dto.setEmail(owner.getEmail());
        dto.setPhone(owner.getPhone());
        if (owner.getCreatedAt() != null) {
            dto.setCreatedAt(owner.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return dto;
    }
}
