package com.example.petstore.controller;

import com.example.petstore.controller.base.VaccinationsControllerBase;
import com.example.petstore.delegate.VaccinationsApiDelegate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VaccinationsController extends VaccinationsControllerBase {
    public VaccinationsController(VaccinationsApiDelegate delegate) {
        super(delegate);
    }
}
