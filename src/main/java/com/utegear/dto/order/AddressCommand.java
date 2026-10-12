package com.utegear.dto.order;

public record AddressCommand(
        String fullName,
        String phone,
        String street,
        String ward,
        String district,
        String province,
        String label) {
}
