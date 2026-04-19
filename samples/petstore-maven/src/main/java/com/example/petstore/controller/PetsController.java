package com.example.petstore.controller;

import com.example.petstore.controller.base.PetsControllerBase;
import com.example.petstore.delegate.PetsApiDelegate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PetsController extends PetsControllerBase {
    public PetsController(PetsApiDelegate delegate) {
        super(delegate);
    }
}
