package com.utegear.dto.order;

import com.utegear.entity.Address;

public record AddressResponse(
        Long id, String fullName, String phone, String street, String ward,
        String district, String province, String label, boolean isDefault) {

    public static AddressResponse from(Address address) {
        return new AddressResponse(address.getId(), address.getFullName(), address.getPhone(),
                address.getStreet(), address.getWard(), address.getDistrict(), address.getProvince(),
                address.getLabel(), address.isDefault());
    }
}
