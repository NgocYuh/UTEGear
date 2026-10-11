package com.utegear.dto.store;

import jakarta.validation.constraints.NotNull;

public record StoreActiveRequest(@NotNull Boolean active) {
}
