package com.utegear.service;

import com.utegear.dto.store.StoreActiveRequest;
import com.utegear.dto.store.StoreDetail;
import com.utegear.dto.store.StorePage;
import com.utegear.dto.store.StoreUpsertRequest;
import com.utegear.dto.store.StoreSummary;
import com.utegear.entity.Store;
import com.utegear.exception.ApiException;
import com.utegear.repository.StoreRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class StoreService {

    private final StoreRepository repository;

    public StoreService(StoreRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<StoreSummary> findPublic(boolean active, String province) {
        String normalizedProvince = normalize(province);
        List<Store> stores = normalizedProvince == null
                ? repository.findByActiveOrderByNameAsc(active)
                : repository.findByActiveAndProvinceIgnoreCaseOrderByNameAsc(active, normalizedProvince);
        return stores.stream().map(StoreSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public StoreDetail findPublicDetail(Long id) {
        return repository.findByIdAndActiveTrue(id)
                .map(StoreDetail::from)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "STORE_NOT_FOUND", "Không tìm thấy cửa hàng"));
    }

    @Transactional(readOnly = true)
    public StorePage findAll(Boolean active, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Phân trang không hợp lệ");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Store> stores = active == null
                ? repository.findAll(pageable)
                : repository.findByActive(active, pageable);
        return StorePage.from(stores.map(StoreDetail::from));
    }

    @Transactional
    public StoreDetail create(StoreUpsertRequest request) {
        validateRequest(request);
        String phone = normalize(request.phone());
        if (phone != null && repository.existsByPhone(phone)) {
            throw duplicatePhone();
        }
        return StoreDetail.from(save(toStore(request, phone)));
    }

    @Transactional
    public StoreDetail update(Long id, StoreUpsertRequest request) {
        validateRequest(request);
        Store store = requireStore(id);
        String phone = normalize(request.phone());
        if (phone != null && repository.existsByPhoneAndIdNot(phone, id)) {
            throw duplicatePhone();
        }
        store.update(
                request.name().trim(), request.address().trim(), request.province().trim(),
                normalize(request.district()), phone, normalize(request.email()),
                normalize(request.imageUrl()), request.openingTime(), request.closingTime(),
                normalize(request.description()), request.latitude(), request.longitude(), request.active());
        return StoreDetail.from(save(store));
    }

    @Transactional
    public StoreDetail changeActive(Long id, StoreActiveRequest request) {
        Store store = requireStore(id);
        store.setActive(request.active());
        return StoreDetail.from(repository.save(store));
    }

    private Store toStore(StoreUpsertRequest request, String phone) {
        return new Store(
                request.name().trim(), request.address().trim(), request.province().trim(),
                normalize(request.district()), phone, normalize(request.email()),
                normalize(request.imageUrl()), request.openingTime(), request.closingTime(),
                normalize(request.description()), request.latitude(), request.longitude(), request.active());
    }

    private void validateRequest(StoreUpsertRequest request) {
        if (request.openingTime().equals(request.closingTime())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Giờ mở cửa và đóng cửa phải khác nhau");
        }
    }

    private Store requireStore(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "STORE_NOT_FOUND", "Không tìm thấy cửa hàng"));
    }

    private Store save(Store store) {
        try {
            return repository.save(store);
        } catch (DataIntegrityViolationException exception) {
            throw duplicatePhone();
        }
    }

    private ApiException duplicatePhone() {
        return new ApiException(
                HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Số điện thoại cửa hàng đã tồn tại");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
