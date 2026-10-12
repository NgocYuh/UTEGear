package com.utegear.service;

import com.utegear.dto.order.AddressCommand;
import com.utegear.entity.Address;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.AddressRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<Address> findAll(User user) {
        requireUser(user);
        return addressRepository.findAllByUserIdOrderByDefaultAddressDescCreatedAtDesc(user.getId());
    }

    @Transactional
    public Address create(User user, AddressCommand command) {
        requireUser(user);
        requireCommand(command);
        Address address = new Address(
                user, command.fullName(), command.phone(), command.street(), command.ward(),
                command.district(), command.province(), command.label());
        if (!addressRepository.existsByUserId(user.getId())) {
            address.markDefault(true);
        }
        return addressRepository.save(address);
    }

    @Transactional
    public Address update(User user, Long addressId, AddressCommand command) {
        requireCommand(command);
        Address address = requireOwned(user, addressId);
        address.update(
                command.fullName(), command.phone(), command.street(), command.ward(),
                command.district(), command.province(), command.label());
        return addressRepository.save(address);
    }

    @Transactional
    public Address setDefault(User user, Long addressId) {
        Address selected = requireOwned(user, addressId);
        addressRepository.findByUserIdAndDefaultAddressTrue(user.getId())
                .filter(current -> !current.getId().equals(selected.getId()))
                .ifPresent(current -> current.markDefault(false));
        selected.markDefault(true);
        return addressRepository.save(selected);
    }

    @Transactional
    public void delete(User user, Long addressId) {
        Address address = requireOwned(user, addressId);
        boolean wasDefault = address.isDefault();
        addressRepository.delete(address);
        if (wasDefault) {
            addressRepository.findAllByUserIdOrderByDefaultAddressDescCreatedAtDesc(user.getId()).stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.markDefault(true);
                        addressRepository.save(next);
                    });
        }
    }

    private Address requireOwned(User user, Long addressId) {
        requireUser(user);
        if (addressId == null || addressId <= 0) {
            throw validation("ID địa chỉ phải là số dương");
        }
        return addressRepository.findByIdAndUserId(addressId, user.getId()).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Không tìm thấy địa chỉ"));
    }

    private void requireUser(User user) {
        if (user == null || user.getId() == null || user.getId() <= 0) {
            throw validation("Người dùng hợp lệ là bắt buộc");
        }
    }

    private void requireCommand(AddressCommand command) {
        if (command == null) {
            throw validation("Thông tin địa chỉ là bắt buộc");
        }
    }

    private ApiException validation(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }
}
