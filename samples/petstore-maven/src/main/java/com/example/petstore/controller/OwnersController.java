package com.example.petstore.controller;

import com.example.petstore.controller.base.OwnersControllerBase;
import com.example.petstore.delegate.OwnersApiDelegate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OwnersController extends OwnersControllerBase {
    public OwnersController(OwnersApiDelegate delegate) {
        super(delegate);
    }
}
