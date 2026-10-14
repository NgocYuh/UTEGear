-- Disposable H2 in-memory browser fixture; not a Flyway migration or production seed.
INSERT INTO roles (id,name) VALUES (100,'CUSTOMER');
INSERT INTO categories(id,name,slug,active,sort_order,created_at,updated_at) VALUES(100,'QA Keyboard','qa-keyboard',true,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(100,100,'QA Keyboard <safe>','qa-keyboard-100','QA description <script>literal</script>','{"connection":"USB"}' FORMAT JSON,500000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(100,100,'QA-DEFAULT','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO stores(id,name,address,province,opening_time,closing_time,active,created_at,updated_at) VALUES(100,'QA Store','QA address','QA province','08:00:00','22:00:00',true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(100,100,100,10,CURRENT_TIMESTAMP);
