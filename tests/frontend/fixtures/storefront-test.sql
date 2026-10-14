-- Disposable H2 in-memory browser fixture; not a Flyway migration or production seed.
INSERT INTO roles (id,name) VALUES (100,'CUSTOMER');
INSERT INTO categories(id,name,slug,active,sort_order,created_at,updated_at) VALUES(100,'QA Keyboard','qa-keyboard',true,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(100,100,'QA Keyboard <safe>','qa-keyboard-100','QA description <script>literal</script>','{"connection":"USB"}' FORMAT JSON,500000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(100,100,'QA-DEFAULT','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO stores(id,name,address,province,opening_time,closing_time,active,created_at,updated_at) VALUES(100,'QA Store','QA address','QA province','08:00:00','22:00:00',true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(100,100,100,10,CURRENT_TIMESTAMP);
INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(200,100,'QA Multi','qa-multi-200','QA variants','{}' FORMAT JSON,600000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES
(200,200,'QA-RED','{"color":"Red"}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(201,200,'QA-BLUE','{"color":"Blue"}' FORMAT JSON,false,50000,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(202,200,'QA-EMPTY','{"color":"Empty"}' FORMAT JSON,false,100000,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO stores(id,name,address,province,opening_time,closing_time,active,created_at,updated_at) VALUES
(101,'QA Store B','QA address B','QA province','08:00:00','22:00:00',true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(102,'QA Inactive','QA inactive address','QA province','08:00:00','22:00:00',false,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES
(200,100,200,4,CURRENT_TIMESTAMP),(201,100,201,2,CURRENT_TIMESTAMP),
(202,100,202,0,CURRENT_TIMESTAMP),(203,101,201,1,CURRENT_TIMESTAMP),
(204,102,202,5,CURRENT_TIMESTAMP);
INSERT INTO product_images(id,product_id,secure_url,public_id,alt_text,sort_order,is_primary) VALUES
(200,200,'/images/placeholder.svg','qa-local-primary','QA primary image',0,true),
(201,200,'/favicon.svg','qa-local-secondary','QA secondary image',1,false);
INSERT INTO coupons(id,code,discount_type,discount_value,minimum_order_value,maximum_discount,valid_from,valid_until,max_uses,used_count,active) VALUES
(100,'QAVALID','FIXED_AMOUNT',50000,0,null,DATEADD('DAY',-1,CURRENT_TIMESTAMP),DATEADD('DAY',1,CURRENT_TIMESTAMP),100,0,true),
(101,'QAEXPIRED','FIXED_AMOUNT',50000,0,null,DATEADD('DAY',-2,CURRENT_TIMESTAMP),DATEADD('DAY',-1,CURRENT_TIMESTAMP),100,0,true),
(102,'QAMIN','FIXED_AMOUNT',50000,10000000,null,DATEADD('DAY',-1,CURRENT_TIMESTAMP),DATEADD('DAY',1,CURRENT_TIMESTAMP),100,0,true);
