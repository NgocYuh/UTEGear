-- Migration V6: Allow VNPAY as payment method in payments table
ALTER TABLE payments DROP CONSTRAINT ck_payments_method;
ALTER TABLE payments ADD CONSTRAINT ck_payments_method CHECK (method IN ('COD', 'BANK_TRANSFER', 'VNPAY'));

