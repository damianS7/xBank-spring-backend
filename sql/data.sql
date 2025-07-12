INSERT INTO public.customers (email,"role",created_at,updated_at) VALUES
	 ('admin@demo.com','ADMIN'::public."customer_role_type",'2025-04-17 02:07:41.382291','2025-04-17 02:07:41.382291'),
	 ('alice@demo.com','CUSTOMER'::public."customer_role_type",'2025-04-06 02:42:27.378616','2025-04-06 02:42:27.378616');
INSERT INTO public.customer_profiles (customer_id,first_name,last_name,phone,birthdate,gender,photo_path,address,postal_code,country,national_id,updated_at) VALUES
	 (1,'David','White','701 444 113','1987-06-02','MALE'::public."customer_gender_type",NULL,'Apple AV.','50240','USA','723173823Z','2025-04-28 01:47:10.771533'),
	 (2,'Alice','Brown','901 322 223','1993-07-04','FEMALE'::public."customer_gender_type",NULL,'Lemon AV','30140','USA','01116613Z','2025-04-28 01:48:28.903419');
INSERT INTO public.customer_auth (customer_id,password_hash,auth_account_status,"email_verification_status",updated_at) VALUES
	 (1,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','ENABLED'::public."auth_status_type",'NOT_VERIFIED'::public."email_verification_status_type",'2025-04-28 13:11:10.477439'),
	 (2,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','ENABLED'::public."auth_status_type",'NOT_VERIFIED'::public."email_verification_status_type",'2025-04-28 13:11:10.477439');
INSERT INTO public.banking_accounts (customer_id,account_number,balance,account_type,account_currency,account_status,notes,created_at,updated_at) VALUES
	 (1,'US0100239312949422823929',0.00,'SAVINGS'::public."banking_account_type",'USD'::public."banking_account_currency_type",'OPEN'::public."banking_account_status_type",NULL,'2025-04-14 00:24:39.778237','2025-04-14 00:24:39.778237'),
	 (1,'ES8800229311949422123226',0.00,'SAVINGS'::public."banking_account_type",'EUR'::public."banking_account_currency_type",'OPEN'::public."banking_account_status_type",NULL,'2025-04-14 00:24:39.778237','2025-04-14 00:24:39.778237'),
	 (2,'US8824259955211847104942',0.00,'SAVINGS'::public."banking_account_type",'USD'::public."banking_account_currency_type",'OPEN'::public."banking_account_status_type",NULL,'2025-04-14 00:22:57.944342','2025-04-14 00:22:57.944342'),
	 (2,'IT0504210065223147526742',0.00,'SAVINGS'::public."banking_account_type",'EUR'::public."banking_account_currency_type",'OPEN'::public."banking_account_status_type",NULL,'2025-04-14 00:22:57.944342','2025-04-14 00:22:57.944342');
INSERT INTO public.banking_cards (banking_account_id,card_type,card_status,card_number,notes, card_pin, card_cvv,created_at,updated_at) VALUES
	 (1,'CREDIT'::public."banking_card_type",'ENABLED'::public."banking_card_status_type",'4031514725218343',NULL, '8182', '874','2025-04-26 01:24:09.431798','2025-04-28 01:52:53.251241'),
	 (2,'CREDIT'::public."banking_card_type",'ENABLED'::public."banking_card_status_type",'2061141832719453',NULL, '4734', '512','2025-04-26 01:24:09.431798','2025-04-28 01:52:53.251241');
INSERT INTO public.banking_transactions (banking_account_id, banking_card_id, transaction_type, amount, description, transaction_status, created_at, updated_at) VALUES
(1, NULL, 'DEPOSIT'::public."banking_transaction_type", 250.00, 'Initial deposit', 'COMPLETED'::public."banking_transaction_status_type", '2025-04-10 09:30:00', '2025-04-10 09:30:00'),
(1, 1, 'CARD_CHARGE'::public."banking_transaction_type", 19.99, 'Netflix Subscription', 'COMPLETED'::public."banking_transaction_status_type", '2025-04-12 12:00:00', '2025-04-12 12:00:00'),
(1, NULL, 'WITHDRAWAL'::public."banking_transaction_type", 50.00, 'ATM Withdrawal', 'COMPLETED'::public."banking_transaction_status_type", '2025-04-15 18:20:00', '2025-04-15 18:20:00'),
(1, NULL, 'DEPOSIT'::public."banking_transaction_type", 500.00, 'Payroll', 'COMPLETED'::public."banking_transaction_status_type", '2025-04-30 09:00:00', '2025-04-30 09:00:00'),
(1, 1, 'CARD_CHARGE'::public."banking_transaction_type", 75.00, 'Apple Store', 'PENDING'::public."banking_transaction_status_type", '2025-05-01 11:00:00', '2025-05-01 11:00:00'),
(2, 2, 'CARD_CHARGE'::public."banking_transaction_type", 15.50, 'Spotify', 'COMPLETED'::public."banking_transaction_status_type", '2025-04-22 07:30:00', '2025-04-22 07:30:00'),
(2, NULL, 'WITHDRAWAL'::public."banking_transaction_type", 80.00, 'ATM Withdrawal', 'PENDING'::public."banking_transaction_status_type", '2025-05-05 16:45:00', '2025-05-05 16:45:00'),
(2, NULL, 'DEPOSIT'::public."banking_transaction_type", 1200.00, 'Freelance Project Payment wall', 'COMPLETED'::public."banking_transaction_status_type", '2025-05-07 20:10:00', '2025-05-07 20:10:00'),
(2, 2, 'CARD_CHARGE'::public."banking_transaction_type", 8.99, 'YouTube Premium', 'REJECTED'::public."banking_transaction_status_type", '2025-05-10 13:00:00', '2025-05-10 13:00:00');