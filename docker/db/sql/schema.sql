DROP SCHEMA IF EXISTS testing CASCADE;

CREATE SCHEMA testing AUTHORIZATION pg_database_owner;

COMMENT ON SCHEMA testing IS 'standard testing schema';

-- DROP TYPE testing."auth_status_type";

CREATE TYPE testing."auth_status_type" AS ENUM (
	'DISABLED',
	'ENABLED'
);

CREATE CAST (varchar as auth_status_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE testing."banking_account_currency_type";

CREATE TYPE testing."banking_account_currency_type" AS ENUM (
	'EUR',
	'USD'
);

CREATE CAST (varchar as banking_account_currency_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE testing."banking_account_status_type";

CREATE TYPE testing."banking_account_status_type" AS ENUM (
	'OPEN',
	'CLOSED',
	'SUSPENDED'
);

CREATE CAST (varchar as banking_account_status_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE testing."banking_account_transaction_status";

CREATE TYPE testing."banking_transaction_status_type" AS ENUM (
	'PENDING',
	'FAILED',
	'COMPLETED',
	'REJECTED'
);

CREATE CAST (varchar as banking_transaction_status_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE testing."banking_account_transaction_type";

CREATE TYPE testing."banking_transaction_type" AS ENUM (
	'DEPOSIT',
	'WITHDRAWAL',
	'CARD_CHARGE',
	'TRANSFER_TO',
	'TRANSFER_FROM'
);

CREATE CAST (varchar as banking_transaction_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE testing."banking_account_type";

CREATE TYPE testing."banking_account_type" AS ENUM (
	'SAVINGS',
	'CHECK'
);

CREATE CAST (varchar as banking_account_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE testing."banking_card_status_type";

CREATE TYPE testing."banking_card_status_type" AS ENUM (
	'ENABLED',
	'DISABLED',
	'SUSPENDED'
);

CREATE CAST (varchar as banking_card_status_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE testing."banking_card_type";

CREATE TYPE testing."banking_card_type" AS ENUM (
	'CREDIT',
	'DEBIT'
);

CREATE CAST (varchar as banking_card_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE testing."customer_gender_type";

CREATE TYPE testing."customer_gender_type" AS ENUM (
	'MALE',
	'FEMALE'
);

CREATE CAST (varchar as customer_gender_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE testing."customer_role";

CREATE TYPE testing."customer_role_type" AS ENUM (
	'CUSTOMER',
	'ADMIN'
);

CREATE CAST (varchar as customer_role_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE testing."email_verification_status_type";

CREATE TYPE testing."email_verification_status_type" AS ENUM (
	'NOT_VERIFIED',
	'VERIFIED'
);

CREATE CAST (varchar as email_verification_status_type) WITH INOUT AS IMPLICIT;

-- Drop table

-- DROP TABLE testing.customers;

CREATE TABLE testing.customers (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	email varchar(80) NOT NULL,
	"role" testing."customer_role_type" DEFAULT 'CUSTOMER'::customer_role_type NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT customers_email_key UNIQUE (email),
	CONSTRAINT customers_pkey PRIMARY KEY (id)
);


-- testing.banking_accounts definition

-- Drop table

-- DROP TABLE testing.banking_accounts;

CREATE TABLE testing.banking_accounts (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	account_number varchar(32) NOT NULL,
	balance numeric(15, 2) DEFAULT 0.00 NOT NULL,
	account_type testing."banking_account_type" DEFAULT 'SAVINGS'::banking_account_type NOT NULL,
	account_currency testing."banking_account_currency_type" DEFAULT 'EUR'::banking_account_currency_type NOT NULL,
	account_status testing."banking_account_status_type" DEFAULT 'CLOSED'::banking_account_status_type NOT NULL,
	notes text NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT banking_accounts_account_number_key UNIQUE (account_number),
	CONSTRAINT banking_accounts_pkey PRIMARY KEY (id),
	CONSTRAINT banking_accounts_customers_fk FOREIGN KEY (customer_id) REFERENCES testing.customers(id) ON DELETE CASCADE ON UPDATE CASCADE
);


-- testing.customer_auth definition

-- Drop table

-- DROP TABLE testing.customer_auth;

CREATE TABLE testing.customer_auth (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	password_hash varchar(60) NOT NULL,
	auth_account_status testing."auth_status_type" DEFAULT 'ENABLED'::auth_status_type NOT NULL,
	"email_verification_status" testing."email_verification_status_type" DEFAULT 'NOT_VERIFIED'::email_verification_status_type NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT auth_customer_id_key UNIQUE (customer_id),
	CONSTRAINT auth_pkey PRIMARY KEY (id),
	CONSTRAINT auth_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES testing.customers(id) ON DELETE CASCADE
);


-- testing.customer_profiles definition

-- Drop table

-- DROP TABLE testing.customer_profiles;

CREATE TABLE testing.customer_profiles (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	"name" varchar(20) NOT NULL,
	surname varchar(40) NOT NULL,
	phone varchar(14) NOT NULL,
	birthdate date NOT NULL,
	gender testing."customer_gender_type" NOT NULL,
	photo_path varchar(100) NULL, -- path to image
	address varchar(50) NOT NULL,
	postal_code varchar(8) NOT NULL,
	country varchar(12) NOT NULL,
	national_id varchar(12) NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT profiles_customer_id_key UNIQUE (customer_id),
	CONSTRAINT profiles_national_id_key UNIQUE (national_id),
	CONSTRAINT profiles_pkey PRIMARY KEY (id),
	CONSTRAINT profiles_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES testing.customers(id) ON DELETE CASCADE
);

-- Column comments

COMMENT ON COLUMN testing.customer_profiles.photo_path IS 'path to image';


-- testing.banking_cards definition

-- Drop table

-- DROP TABLE testing.banking_cards;

CREATE TABLE testing.banking_cards (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	banking_account_id int4 NOT NULL,
	card_type testing."banking_card_type" NOT NULL,
	card_status testing."banking_card_status_type" DEFAULT 'DISABLED'::banking_card_status_type NOT NULL,
	card_number varchar(32) NOT NULL,
	notes text NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT banking_card_pkey PRIMARY KEY (id),
	CONSTRAINT banking_card_banking_account_id_fkey FOREIGN KEY (banking_account_id) REFERENCES testing.banking_accounts(id) ON DELETE CASCADE
);


-- testing.banking_transactions definition

-- Drop table

-- DROP TABLE testing.banking_transactions;

CREATE TABLE testing.banking_transactions (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	banking_account_id int4 NOT NULL,
	transaction_type testing."banking_transaction_type" NOT NULL,
	amount numeric(15, 2) NOT NULL,
	description text NULL,
	transaction_status testing."banking_transaction_status_type" DEFAULT 'PENDING'::banking_transaction_status_type NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT banking_transactions_pkey PRIMARY KEY (id),
	CONSTRAINT banking_transactions_banking_account_id_fkey FOREIGN KEY (banking_account_id) REFERENCES testing.banking_accounts(id) ON DELETE CASCADE
);