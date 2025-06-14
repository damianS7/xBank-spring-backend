DROP SCHEMA IF EXISTS public CASCADE;

CREATE SCHEMA public AUTHORIZATION pg_database_owner;

COMMENT ON SCHEMA public IS 'standard public schema';


CREATE TYPE public."customer_role_type" AS ENUM (
	'CUSTOMER',
	'ADMIN'
);

CREATE CAST (varchar as customer_role_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.customers (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	email varchar(80) NOT NULL,
	"role" public."customer_role_type" DEFAULT 'CUSTOMER'::customer_role_type NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT customers_email_key UNIQUE (email),
	CONSTRAINT customers_pkey PRIMARY KEY (id)
);

CREATE TYPE public."customer_gender_type" AS ENUM (
	'MALE',
	'FEMALE'
);

CREATE CAST (varchar as customer_gender_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.customer_profiles (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	first_name varchar(20) NOT NULL,
	last_name varchar(40) NOT NULL,
	phone varchar(14) NOT NULL,
	birthdate date NOT NULL,
	gender public."customer_gender_type" NOT NULL,
	photo_path varchar(100) NULL, -- path to image
	address varchar(50) NOT NULL,
	postal_code varchar(8) NOT NULL,
	country varchar(12) NOT NULL,
	national_id varchar(12) NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT profiles_customer_id_key UNIQUE (customer_id),
	CONSTRAINT profiles_national_id_key UNIQUE (national_id),
	CONSTRAINT profiles_pkey PRIMARY KEY (id),
	CONSTRAINT profiles_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TYPE public."auth_status_type" AS ENUM (
	'DISABLED',
	'ENABLED'
);

CREATE CAST (varchar as auth_status_type) WITH INOUT AS IMPLICIT;

CREATE TYPE public."email_verification_status_type" AS ENUM (
	'NOT_VERIFIED',
	'VERIFIED'
);

CREATE CAST (varchar as email_verification_status_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.customer_auth (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	password_hash varchar(60) NOT NULL,
	auth_account_status public."auth_status_type" DEFAULT 'ENABLED'::auth_status_type NOT NULL,
	"email_verification_status" public."email_verification_status_type" DEFAULT 'NOT_VERIFIED'::email_verification_status_type NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT auth_customer_id_key UNIQUE (customer_id),
	CONSTRAINT auth_pkey PRIMARY KEY (id),
	CONSTRAINT auth_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TYPE public."banking_account_currency_type" AS ENUM (
	'EUR',
	'USD'
);

CREATE CAST (varchar as banking_account_currency_type) WITH INOUT AS IMPLICIT;

CREATE TYPE public."banking_account_status_type" AS ENUM (
	'OPEN',
	'CLOSED',
	'SUSPENDED'
);

CREATE CAST (varchar as banking_account_status_type) WITH INOUT AS IMPLICIT;

CREATE TYPE public."banking_account_type" AS ENUM (
	'SAVINGS',
	'CHECKING'
);

CREATE CAST (varchar as banking_account_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.banking_accounts (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	alias varchar(64) NULL,
	account_number varchar(32) NOT NULL,
	balance numeric(15, 2) DEFAULT 0.00 NOT NULL,
	account_type public."banking_account_type" DEFAULT 'SAVINGS'::banking_account_type NOT NULL,
	account_currency public."banking_account_currency_type" DEFAULT 'EUR'::banking_account_currency_type NOT NULL,
	account_status public."banking_account_status_type" DEFAULT 'CLOSED'::banking_account_status_type NOT NULL,
	notes text NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT banking_accounts_account_number_key UNIQUE (account_number),
	CONSTRAINT banking_accounts_pkey PRIMARY KEY (id),
	CONSTRAINT banking_accounts_customers_fk FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TYPE public."banking_card_status_type" AS ENUM (
	'ENABLED',
	'DISABLED'
);

CREATE CAST (varchar as banking_card_status_type) WITH INOUT AS IMPLICIT;

CREATE TYPE public."banking_card_lock_status_type" AS ENUM (
	'LOCKED',
	'UNLOCKED'
);

CREATE CAST (varchar as banking_card_lock_status_type) WITH INOUT AS IMPLICIT;

CREATE TYPE public."banking_card_type" AS ENUM (
	'CREDIT',
	'DEBIT'
);

CREATE CAST (varchar as banking_card_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.banking_cards (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	banking_account_id int4 NOT NULL,
	card_type public."banking_card_type" NOT NULL,
	card_status public."banking_card_status_type" DEFAULT 'DISABLED'::banking_card_status_type NOT NULL,
	lock_status public."banking_card_lock_status_type" DEFAULT 'UNLOCKED'::banking_card_lock_status_type NOT NULL,
	card_number varchar(32) NOT NULL,
	card_pin varchar(4) NOT NULL,
	card_cvv varchar(3) NOT NULL,
	daily_limit numeric(15, 2) DEFAULT 0.00 NOT NULL,
	expired_date timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	notes text NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT banking_card_pkey PRIMARY KEY (id),
	CONSTRAINT banking_card_banking_account_id_fkey FOREIGN KEY (banking_account_id) REFERENCES public.banking_accounts(id) ON DELETE CASCADE
);

CREATE TYPE public."banking_transaction_status_type" AS ENUM (
	'PENDING',
	'FAILED',
	'COMPLETED',
	'REJECTED'
);

CREATE CAST (varchar as banking_transaction_status_type) WITH INOUT AS IMPLICIT;

CREATE TYPE public."banking_transaction_type" AS ENUM (
	'DEPOSIT',
	'WITHDRAWAL',
	'CARD_CHARGE',
	'TRANSFER_TO',
	'TRANSFER_FROM'
);

CREATE CAST (varchar as banking_transaction_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.banking_transactions (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	banking_account_id int4 NOT NULL,
	banking_card_id int4 NULL,
	account_balance numeric(15, 2) NOT NULL,
	transaction_type public."banking_transaction_type" NOT NULL,
	amount numeric(15, 2) NOT NULL,
	description text NULL,
	transaction_status public."banking_transaction_status_type" DEFAULT 'PENDING'::banking_transaction_status_type NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT banking_transactions_pkey PRIMARY KEY (id),
	CONSTRAINT banking_transactions_banking_card_id_fkey FOREIGN KEY (banking_card_id) REFERENCES public.banking_cards(id) ON DELETE SET NULL,
	CONSTRAINT banking_transactions_banking_account_id_fkey FOREIGN KEY (banking_account_id) REFERENCES public.banking_accounts(id) ON DELETE CASCADE
);
