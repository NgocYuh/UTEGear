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

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(400,100,'QA Page 00','qa-page-400','QA paging','{}' FORMAT JSON,10000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(400,400,'QA-PAGE-400','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(400,100,400,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(401,100,'QA Page 01','qa-page-401','QA paging','{}' FORMAT JSON,11000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(401,401,'QA-PAGE-401','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(401,100,401,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(402,100,'QA Page 02','qa-page-402','QA paging','{}' FORMAT JSON,12000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(402,402,'QA-PAGE-402','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(402,100,402,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(403,100,'QA Page 03','qa-page-403','QA paging','{}' FORMAT JSON,13000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(403,403,'QA-PAGE-403','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(403,100,403,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(404,100,'QA Page 04','qa-page-404','QA paging','{}' FORMAT JSON,14000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(404,404,'QA-PAGE-404','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(404,100,404,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(405,100,'QA Page 05','qa-page-405','QA paging','{}' FORMAT JSON,15000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(405,405,'QA-PAGE-405','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(405,100,405,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(406,100,'QA Page 06','qa-page-406','QA paging','{}' FORMAT JSON,16000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(406,406,'QA-PAGE-406','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(406,100,406,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(407,100,'QA Page 07','qa-page-407','QA paging','{}' FORMAT JSON,17000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(407,407,'QA-PAGE-407','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(407,100,407,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(408,100,'QA Page 08','qa-page-408','QA paging','{}' FORMAT JSON,18000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(408,408,'QA-PAGE-408','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(408,100,408,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(409,100,'QA Page 09','qa-page-409','QA paging','{}' FORMAT JSON,19000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(409,409,'QA-PAGE-409','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(409,100,409,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(410,100,'QA Page 10','qa-page-410','QA paging','{}' FORMAT JSON,20000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(410,410,'QA-PAGE-410','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(410,100,410,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(411,100,'QA Page 11','qa-page-411','QA paging','{}' FORMAT JSON,21000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(411,411,'QA-PAGE-411','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(411,100,411,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(412,100,'QA Page 12','qa-page-412','QA paging','{}' FORMAT JSON,22000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(412,412,'QA-PAGE-412','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(412,100,412,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(413,100,'QA Page 13','qa-page-413','QA paging','{}' FORMAT JSON,23000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(413,413,'QA-PAGE-413','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(413,100,413,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(414,100,'QA Page 14','qa-page-414','QA paging','{}' FORMAT JSON,24000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(414,414,'QA-PAGE-414','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(414,100,414,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(415,100,'QA Page 15','qa-page-415','QA paging','{}' FORMAT JSON,25000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(415,415,'QA-PAGE-415','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(415,100,415,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(416,100,'QA Page 16','qa-page-416','QA paging','{}' FORMAT JSON,26000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(416,416,'QA-PAGE-416','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(416,100,416,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(417,100,'QA Page 17','qa-page-417','QA paging','{}' FORMAT JSON,27000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(417,417,'QA-PAGE-417','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(417,100,417,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(418,100,'QA Page 18','qa-page-418','QA paging','{}' FORMAT JSON,28000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(418,418,'QA-PAGE-418','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(418,100,418,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(419,100,'QA Page 19','qa-page-419','QA paging','{}' FORMAT JSON,29000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(419,419,'QA-PAGE-419','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(419,100,419,1,CURRENT_TIMESTAMP);

INSERT INTO products(id,category_id,name,slug,description,specifications,base_price,status,rating_average,review_count,sold_count,created_at,updated_at) VALUES(420,100,'QA Page 20','qa-page-420','QA paging','{}' FORMAT JSON,30000,'ACTIVE',0,0,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO product_variants(id,product_id,sku,option_values,is_default,price_adjustment,active,created_at,updated_at) VALUES(420,420,'QA-PAGE-420','{}' FORMAT JSON,true,0,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO store_inventory(id,store_id,variant_id,quantity,updated_at) VALUES(420,100,420,1,CURRENT_TIMESTAMP);
