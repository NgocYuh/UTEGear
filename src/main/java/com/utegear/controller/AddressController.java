package com.utegear.controller;

import com.utegear.dto.order.AddressRequest;
import com.utegear.dto.order.AddressResponse;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.AddressService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<AddressResponse> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return addressService.findAll(principal.getUser()).stream().map(AddressResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody AddressRequest request) {
        return AddressResponse.from(addressService.create(principal.getUser(), request.toCommand()));
    }

    @PutMapping("/{addressId}")
    public AddressResponse update(@AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long addressId, @Valid @RequestBody AddressRequest request) {
        return AddressResponse.from(addressService.update(principal.getUser(), addressId, request.toCommand()));
    }

    @PutMapping("/{addressId}/default")
    public AddressResponse setDefault(@AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long addressId) {
        return AddressResponse.from(addressService.setDefault(principal.getUser(), addressId));
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long addressId) {
        addressService.delete(principal.getUser(), addressId);
    }
}
