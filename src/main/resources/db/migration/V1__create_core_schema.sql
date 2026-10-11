CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_roles_name UNIQUE (name),
    CONSTRAINT ck_roles_name_not_blank CHECK (btrim(name) <> '')
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone UNIQUE (phone),
    CONSTRAINT ck_users_full_name_not_blank CHECK (btrim(full_name) <> ''),
    CONSTRAINT ck_users_email_not_blank CHECK (btrim(email) <> ''),
    CONSTRAINT ck_users_password_hash_not_blank CHECK (btrim(password_hash) <> '')
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    street VARCHAR(255) NOT NULL,
    ward VARCHAR(100),
    district VARCHAR(100),
    province VARCHAR(100) NOT NULL,
    label VARCHAR(100),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_addresses_full_name_not_blank CHECK (btrim(full_name) <> ''),
    CONSTRAINT ck_addresses_phone_not_blank CHECK (btrim(phone) <> ''),
    CONSTRAINT ck_addresses_street_not_blank CHECK (btrim(street) <> ''),
    CONSTRAINT ck_addresses_province_not_blank CHECK (btrim(province) <> '')
);

CREATE UNIQUE INDEX uk_addresses_one_default_per_user
    ON addresses (user_id)
    WHERE is_default;

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_categories_name UNIQUE (name),
    CONSTRAINT uk_categories_slug UNIQUE (slug),
    CONSTRAINT ck_categories_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT ck_categories_slug_not_blank CHECK (btrim(slug) <> ''),
    CONSTRAINT ck_categories_sort_order CHECK (sort_order >= 0)
);

CREATE TABLE brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    logo_url VARCHAR(500),
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_brands_name UNIQUE (name),
    CONSTRAINT uk_brands_slug UNIQUE (slug),
    CONSTRAINT ck_brands_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT ck_brands_slug_not_blank CHECK (btrim(slug) <> '')
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    brand_id BIGINT,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(220) NOT NULL,
    description TEXT,
    specifications JSONB NOT NULL DEFAULT '{}'::jsonb,
    base_price NUMERIC(12,2) NOT NULL,
    sale_price NUMERIC(12,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    rating_average NUMERIC(3,2) NOT NULL DEFAULT 0,
    review_count INTEGER NOT NULL DEFAULT 0,
    sold_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands (id),
    CONSTRAINT uk_products_slug UNIQUE (slug),
    CONSTRAINT ck_products_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT ck_products_slug_not_blank CHECK (btrim(slug) <> ''),
    CONSTRAINT ck_products_specifications_object CHECK (jsonb_typeof(specifications) = 'object'),
    CONSTRAINT ck_products_base_price CHECK (base_price >= 0),
    CONSTRAINT ck_products_sale_price CHECK (sale_price IS NULL OR (sale_price >= 0 AND sale_price <= base_price)),
    CONSTRAINT ck_products_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    CONSTRAINT ck_products_rating CHECK (rating_average >= 0 AND rating_average <= 5),
    CONSTRAINT ck_products_review_count CHECK (review_count >= 0),
    CONSTRAINT ck_products_sold_count CHECK (sold_count >= 0)
);

CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sku VARCHAR(50) NOT NULL,
    option_values JSONB NOT NULL DEFAULT '{}'::jsonb,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    price_adjustment NUMERIC(12,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_variants_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uk_product_variants_sku UNIQUE (sku),
    CONSTRAINT uk_product_variants_options UNIQUE (product_id, option_values),
    CONSTRAINT ck_product_variants_sku_not_blank CHECK (btrim(sku) <> ''),
    CONSTRAINT ck_product_variants_options_object CHECK (jsonb_typeof(option_values) = 'object')
);

CREATE UNIQUE INDEX uk_product_variants_one_default
    ON product_variants (product_id)
    WHERE is_default;

CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    secure_url VARCHAR(500) NOT NULL,
    public_id VARCHAR(255) NOT NULL,
    alt_text VARCHAR(255),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT uk_product_images_public_id UNIQUE (public_id),
    CONSTRAINT uk_product_images_sort_order UNIQUE (product_id, sort_order),
    CONSTRAINT ck_product_images_secure_url_not_blank CHECK (btrim(secure_url) <> ''),
    CONSTRAINT ck_product_images_public_id_not_blank CHECK (btrim(public_id) <> ''),
    CONSTRAINT ck_product_images_sort_order CHECK (sort_order >= 0)
);

CREATE UNIQUE INDEX uk_product_images_one_primary
    ON product_images (product_id)
    WHERE is_primary;

CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(255) NOT NULL,
    province VARCHAR(100) NOT NULL,
    district VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    image_url VARCHAR(500),
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    description TEXT,
    latitude NUMERIC(9,6),
    longitude NUMERIC(9,6),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_stores_phone UNIQUE (phone),
    CONSTRAINT ck_stores_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT ck_stores_address_not_blank CHECK (btrim(address) <> ''),
    CONSTRAINT ck_stores_province_not_blank CHECK (btrim(province) <> ''),
    CONSTRAINT ck_stores_hours CHECK (opening_time <> closing_time),
    CONSTRAINT ck_stores_latitude CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_stores_longitude CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
);

CREATE TABLE store_inventory (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_store_inventory_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT fk_store_inventory_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id),
    CONSTRAINT uk_store_inventory_store_variant UNIQUE (store_id, variant_id),
    CONSTRAINT ck_store_inventory_quantity CHECK (quantity >= 0)
);

CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_carts_user UNIQUE (user_id)
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id),
    CONSTRAINT uk_cart_items_cart_variant UNIQUE (cart_id, variant_id),
    CONSTRAINT ck_cart_items_quantity CHECK (quantity > 0)
);

CREATE TABLE wishlist_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wishlist_items_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uk_wishlist_items_user_product UNIQUE (user_id, product_id)
);

CREATE TABLE coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value NUMERIC(12,2) NOT NULL,
    minimum_order_value NUMERIC(12,2) NOT NULL DEFAULT 0,
    maximum_discount NUMERIC(12,2),
    valid_from TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    max_uses INTEGER NOT NULL DEFAULT 0,
    used_count INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_coupons_code UNIQUE (code),
    CONSTRAINT ck_coupons_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT ck_coupons_discount_type CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    CONSTRAINT ck_coupons_discount_value CHECK (discount_value >= 0),
    CONSTRAINT ck_coupons_percentage CHECK (discount_type <> 'PERCENTAGE' OR discount_value <= 100),
    CONSTRAINT ck_coupons_minimum_order CHECK (minimum_order_value >= 0),
    CONSTRAINT ck_coupons_maximum_discount CHECK (maximum_discount IS NULL OR maximum_discount >= 0),
    CONSTRAINT ck_coupons_valid_period CHECK (valid_until > valid_from),
    CONSTRAINT ck_coupons_max_uses CHECK (max_uses >= 0),
    CONSTRAINT ck_coupons_used_count CHECK (used_count >= 0 AND (max_uses = 0 OR used_count <= max_uses))
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    fulfillment_store_id BIGINT,
    coupon_id BIGINT,
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_email VARCHAR(100) NOT NULL,
    shipping_street VARCHAR(255) NOT NULL,
    shipping_ward VARCHAR(100),
    shipping_district VARCHAR(100),
    shipping_province VARCHAR(100) NOT NULL,
    shipping_label VARCHAR(100),
    shipping_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    subtotal NUMERIC(12,2) NOT NULL,
    discount NUMERIC(12,2) NOT NULL DEFAULT 0,
    shipping_fee NUMERIC(12,2) NOT NULL DEFAULT 0,
    total NUMERIC(12,2) NOT NULL,
    coupon_code VARCHAR(50),
    note VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMPTZ,
    processing_at TIMESTAMPTZ,
    shipped_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_orders_store FOREIGN KEY (fulfillment_store_id) REFERENCES stores (id),
    CONSTRAINT fk_orders_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE SET NULL,
    CONSTRAINT uk_orders_order_code UNIQUE (order_code),
    CONSTRAINT ck_orders_shipping_method CHECK (shipping_method IN ('HOME_DELIVERY', 'STORE_PICKUP')),
    CONSTRAINT ck_orders_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_orders_subtotal CHECK (subtotal >= 0),
    CONSTRAINT ck_orders_discount CHECK (discount >= 0),
    CONSTRAINT ck_orders_shipping_fee CHECK (shipping_fee >= 0),
    CONSTRAINT ck_orders_total CHECK (total >= 0)
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    option_values JSONB NOT NULL DEFAULT '{}'::jsonb,
    image_url VARCHAR(500),
    unit_price NUMERIC(12,2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id),
    CONSTRAINT uk_order_items_order_variant UNIQUE (order_id, variant_id),
    CONSTRAINT ck_order_items_product_name_not_blank CHECK (btrim(product_name) <> ''),
    CONSTRAINT ck_order_items_sku_not_blank CHECK (btrim(sku) <> ''),
    CONSTRAINT ck_order_items_options_object CHECK (jsonb_typeof(option_values) = 'object'),
    CONSTRAINT ck_order_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_items_subtotal CHECK (subtotal >= 0)
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    amount NUMERIC(12,2) NOT NULL,
    transaction_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMPTZ,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT uk_payments_order UNIQUE (order_id),
    CONSTRAINT uk_payments_transaction_id UNIQUE (transaction_id),
    CONSTRAINT ck_payments_method CHECK (method IN ('COD', 'BANK_TRANSFER')),
    CONSTRAINT ck_payments_status CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED')),
    CONSTRAINT ck_payments_amount CHECK (amount >= 0)
);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    comment TEXT NOT NULL,
    verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    helpful_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_reviews_user_product UNIQUE (user_id, product_id),
    CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT ck_reviews_comment_not_blank CHECK (btrim(comment) <> ''),
    CONSTRAINT ck_reviews_helpful_count CHECK (helpful_count >= 0)
);

CREATE TABLE review_images (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL,
    secure_url VARCHAR(500) NOT NULL,
    public_id VARCHAR(255) NOT NULL,
    alt_text VARCHAR(255),
    sort_order INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_review_images_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT uk_review_images_public_id UNIQUE (public_id),
    CONSTRAINT uk_review_images_sort_order UNIQUE (review_id, sort_order),
    CONSTRAINT ck_review_images_secure_url_not_blank CHECK (btrim(secure_url) <> ''),
    CONSTRAINT ck_review_images_public_id_not_blank CHECK (btrim(public_id) <> ''),
    CONSTRAINT ck_review_images_sort_order CHECK (sort_order >= 0)
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    reference_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_notifications_type_not_blank CHECK (btrim(type) <> ''),
    CONSTRAINT ck_notifications_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT ck_notifications_message_not_blank CHECK (btrim(message) <> '')
);

CREATE INDEX ix_products_category_status ON products (category_id, status);
CREATE INDEX ix_products_brand_status ON products (brand_id, status);
CREATE INDEX ix_product_variants_product ON product_variants (product_id);
CREATE INDEX ix_product_variants_options ON product_variants USING GIN (option_values);
CREATE INDEX ix_store_inventory_variant ON store_inventory (variant_id);
CREATE INDEX ix_store_inventory_store ON store_inventory (store_id);
CREATE INDEX ix_orders_user_created ON orders (user_id, created_at DESC);
CREATE INDEX ix_orders_status_created ON orders (status, created_at DESC);
CREATE INDEX ix_orders_store_status ON orders (fulfillment_store_id, status);
CREATE INDEX ix_reviews_product_created ON reviews (product_id, created_at DESC);
CREATE INDEX ix_notifications_user_read_created ON notifications (user_id, is_read, created_at DESC);
