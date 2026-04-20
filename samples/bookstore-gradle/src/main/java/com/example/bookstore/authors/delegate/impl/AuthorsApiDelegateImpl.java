package com.example.bookstore.authors.delegate.impl;

import com.example.bookstore.author.domain.model.Author;
import com.example.bookstore.author.service.AuthorService;
import com.example.bookstore.authors.delegate.impl.base.AuthorsApiDelegateImplBase;
import com.example.bookstore.authors.dto.AuthorDTO;
import com.example.bookstore.authors.dto.CreateAuthorRequest;
import com.example.bookstore.authors.dto.UpdateAuthorRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
public class AuthorsApiDelegateImpl extends AuthorsApiDelegateImplBase {

    private final AuthorService authorService;

    public AuthorsApiDelegateImpl(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Override
    public Page<AuthorDTO> listAuthors(String name, Pageable pageable) {
        List<AuthorDTO> all = authorService.findAll().stream()
                .filter(a -> name == null || a.getName().toLowerCase().contains(name.toLowerCase()))
                .map(this::toDTO)
                .collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<AuthorDTO> page = start > all.size() ? List.of() : all.subList(start, end);
        return new PageImpl<>(page, pageable, all.size());
    }

    @Override
    public AuthorDTO createAuthor(CreateAuthorRequest req) {
        Author author = new Author();
        author.setName(req.getName());
        author.setBio(req.getBio());
        author.setEmail(req.getEmail());
        return toDTO(authorService.create(author));
    }

    @Override
    public AuthorDTO getAuthorById(UUID id) {
        return authorService.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Override
    public AuthorDTO updateAuthor(UUID id, UpdateAuthorRequest req) {
        Author existing = authorService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (req.getName() != null) existing.setName(req.getName());
        if (req.getBio() != null) existing.setBio(req.getBio());
        return toDTO(authorService.update(id, existing));
    }

    @Override
    public ResponseEntity<Void> deleteAuthor(UUID id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private AuthorDTO toDTO(Author author) {
        AuthorDTO dto = new AuthorDTO();
        dto.setId(author.getId());
        dto.setName(author.getName());
        dto.setBio(author.getBio());
        dto.setEmail(author.getEmail());
        if (author.getCreatedAt() != null) {
            dto.setCreatedAt(author.getCreatedAt().atOffset(ZoneOffset.UTC));
        }
        return dto;
    }
}
