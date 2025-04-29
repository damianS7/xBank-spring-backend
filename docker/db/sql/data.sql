INSERT INTO public.customers (email,"role",created_at,updated_at) VALUES
	 ('admin@test.com','ADMIN'::public."customer_role_type",'2025-04-17 02:07:41.382291','2025-04-17 02:07:41.382291'),
	 ('user@test.com','CUSTOMER'::public."customer_role_type",'2025-04-06 02:42:27.378616','2025-04-06 02:42:27.378616');
INSERT INTO public.customer_profiles (customer_id,"name",surname,phone,birthdate,gender,photo_path,address,postal_code,country,national_id,updated_at) VALUES
	 (1,'alice','white','966 123 123','1980-01-01','FEMALE'::public."customer_gender_type",NULL,'fake ave','50240','USA','123123123Z','2025-04-28 01:47:10.771533'),
	 (2,'alice','white','966 123 123','1980-01-01','FEMALE'::public."customer_gender_type",NULL,'fake ave','50240','USA','12312323Z','2025-04-28 01:48:28.903419');
INSERT INTO public.customer_auth (customer_id,password_hash,auth_account_status,"email_verification_status",updated_at) VALUES
	 (1,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','ENABLED'::public."auth_status_type",'NOT_VERIFIED'::public."email_verification_status_type",'2025-04-28 13:11:10.477439'),
	 (2,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','ENABLED'::public."auth_status_type",'NOT_VERIFIED'::public."email_verification_status_type",'2025-04-28 13:11:10.477439');
INSERT INTO public.banking_accounts (customer_id,account_number,balance,account_type,account_currency,account_status,notes,created_at,updated_at) VALUES
	 (1,'US88 0022 9311 9494 2222 2222',0.00,'SAVINGS'::public."banking_account_type",'EUR'::public."banking_account_currency_type",'OPEN'::public."banking_account_status_type",NULL,'2025-04-14 00:24:39.778237','2025-04-14 00:24:39.778237'),
	 (2,'GB88 0421 0055 2211 4412 4444',0.00,'SAVINGS'::public."banking_account_type",'USD'::public."banking_account_currency_type",'OPEN'::public."banking_account_status_type",NULL,'2025-04-14 00:22:57.944342','2025-04-14 00:22:57.944342');
INSERT INTO public.banking_cards (banking_account_id,card_type,card_status,card_number,notes,created_at,updated_at) VALUES
	 (1,'CREDIT'::public."banking_card_type",'ENABLED'::public."banking_card_status_type",'0000 11111 2222 3333',NULL,'2025-04-26 01:24:09.431798','2025-04-28 01:52:53.251241'),
	 (1,'CREDIT'::public."banking_card_type",'ENABLED'::public."banking_card_status_type",'0000 11111 2222 4443',NULL,'2025-04-26 01:24:09.431798','2025-04-28 01:52:53.251241');
INSERT INTO public.banking_transactions (banking_account_id,transaction_type,amount,description,transaction_status,created_at,updated_at) VALUES
	 (2,'CARD_CHARGE'::public."banking_transaction_type",5.00,'AMAZON','PENDING'::public."banking_transaction_status_type",'2025-04-16 01:04:34.02642','2025-04-28 01:54:05.748913'),
	 (2,'DEPOSIT'::public."banking_transaction_type",100.00,NULL,'COMPLETED'::public."banking_transaction_status_type",'2025-04-16 01:05:12.556914','2025-04-28 13:26:50.572847');
