package com.example.bookstore.authors.delegate.impl.base;

import com.example.bookstore.authors.delegate.AuthorsApiDelegate;
import com.example.bookstore.authors.dto.AuthorDTO;
import com.example.bookstore.authors.dto.CreateAuthorRequest;
import com.example.bookstore.authors.dto.UpdateAuthorRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public abstract class AuthorsApiDelegateImplBase implements AuthorsApiDelegate {
    @Override
    public Page<AuthorDTO> listAuthors(String name, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public AuthorDTO createAuthor(CreateAuthorRequest createAuthorRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public AuthorDTO getAuthorById(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public AuthorDTO updateAuthor(UUID id, UpdateAuthorRequest updateAuthorRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ResponseEntity<Void> deleteAuthor(UUID id) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
