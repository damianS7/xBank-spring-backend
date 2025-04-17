-- DROP SCHEMA public;

CREATE SCHEMA public AUTHORIZATION pg_database_owner;

COMMENT ON SCHEMA public IS 'standard public schema';

-- DROP TYPE public."auth_status";

CREATE TYPE public."auth_status" AS ENUM (
	'RESET_PASSWORD',
	'ENABLED_ACCOUNT',
	'DISABLED_ACCOUNT'
);

CREATE CAST (varchar as auth_status) WITH INOUT AS IMPLICIT;
-- DROP TYPE public."banking_account_currency";

CREATE TYPE public.banking_account_currency AS ENUM (
	'EUR',
	'USD',
	'JPY'
);

CREATE CAST (varchar as banking_account_currency) WITH INOUT AS IMPLICIT;

-- DROP TYPE public."banking_account_status";

CREATE TYPE public."banking_account_status" AS ENUM (
	'OPEN',
	'CLOSED',
	'LOCKED'
);

CREATE CAST (varchar as banking_account_status) WITH INOUT AS IMPLICIT;

-- DROP TYPE public."banking_account_transaction_status";

CREATE TYPE public.banking_account_transaction_status AS ENUM (
	'PENDING',
	'FAILED',
	'REJECTED',
	'COMPLETED'
);

CREATE CAST (varchar as banking_account_transaction_status) WITH INOUT AS IMPLICIT;

-- DROP TYPE public."banking_account_transaction_type";

CREATE TYPE public.banking_account_transaction_type AS ENUM (
	'DEPOSIT',
	'TRANSFER',
	'WITHDRAWAL',
	'CARD_CHARGE'
);

CREATE CAST (varchar as banking_account_transaction_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE public."banking_account_type";
CREATE TYPE public.banking_account_type AS ENUM (
	'SAVINGS',
	'CHECK
);

CREATE CAST (varchar as banking_account_type) WITH INOUT AS IMPLICIT;

-- DROP TYPE public."customer_gender";

CREATE TYPE public."customer_gender" AS ENUM (
	'MALE',
	'FEMALE');

CREATE CAST (varchar as customer_gender) WITH INOUT AS IMPLICIT;

-- DROP TYPE public."customer_role";

CREATE TYPE public."customer_role" AS ENUM (
	'CUSTOMER',
	'ADMIN');

CREATE CAST (varchar as customer_role) WITH INOUT AS IMPLICIT;

-- DROP SEQUENCE public.auth_id_seq;

CREATE SEQUENCE public.auth_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;

CREATE SEQUENCE public.banking_account_transactions_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;

CREATE SEQUENCE public.banking_accounts_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;

CREATE SEQUENCE public.customer_banking_account_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE public.customers_id_seq;

CREATE SEQUENCE public.customers_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;

CREATE SEQUENCE public.profiles_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;

-- DROP TABLE public.customers;
CREATE TABLE public.customers (
	id int8 GENERATED ALWAYS AS IDENTITY NOT NULL,
	email varchar(80) NOT NULL,
	"role" public."customer_role" DEFAULT 'CUSTOMER'::customer_role NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT customers_email_key UNIQUE (email),
	CONSTRAINT customers_pkey PRIMARY KEY (id)
);


-- public.banking_accounts definition

-- Drop table

-- DROP TABLE public.banking_accounts;

CREATE TABLE public.banking_accounts (
	id int8 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int8 NOT NULL,
	account_number varchar(29) NOT NULL,
	balance numeric(15, 2) DEFAULT 0.00 NOT NULL,
	account_type public."banking_account_type" DEFAULT 'SAVINGS'::banking_account_type NOT NULL,
	account_currency public."banking_account_currency" DEFAULT 'EUR'::banking_account_currency NOT NULL,
	account_status public."banking_account_status" DEFAULT 'CLOSED'::banking_account_status NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT banking_accounts_account_number_key UNIQUE (account_number),
	CONSTRAINT banking_accounts_pkey PRIMARY KEY (id),
	CONSTRAINT banking_accounts_customers_fk FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- public.customer_auth definition

-- Drop table

-- DROP TABLE public.customer_auth;

CREATE TABLE public.customer_auth (
	id int8 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int8 NOT NULL,
	password_hash varchar(60) NOT NULL,
	"auth_status" public."auth_status" DEFAULT 'ACTIVE'::auth_status NULL,
	email_verified bool DEFAULT false NOT NULL,
	CONSTRAINT auth_customer_id_key UNIQUE (customer_id),
	CONSTRAINT auth_pkey PRIMARY KEY (id),
	CONSTRAINT auth_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);


-- public.customer_profiles definition

-- Drop table

-- DROP TABLE public.customer_profiles;

CREATE TABLE public.customer_profiles (
    id int8 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	"name" varchar(20) NOT NULL,
	surname varchar(40) NOT NULL,
	phone varchar(14) NOT NULL,
	birthdate date NOT NULL,
	gender public."customer_gender" NOT NULL,
	photo_path varchar(100) NULL, -- path to image
	address varchar(50) NOT NULL,
	postal_code varchar(8) NOT NULL,
	country varchar(12) NOT NULL,
	national_id varchar(12) NOT NULL,
	CONSTRAINT profiles_customer_id_key UNIQUE (customer_id),
	CONSTRAINT profiles_national_id_key UNIQUE (national_id),
	CONSTRAINT profiles_pkey PRIMARY KEY (id),
	CONSTRAINT profiles_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

-- Column comments

COMMENT ON COLUMN public.customer_profiles.photo_path IS 'path to image';


-- public.banking_account_transactions definition

-- Drop table

-- DROP TABLE public.banking_account_transactions;

CREATE TABLE public.banking_account_transactions (
	id int8 GENERATED ALWAYS AS IDENTITY NOT NULL,
	banking_account_id int4 NOT NULL,
	transaction_type public."banking_account_transaction_type" NOT NULL,
	amount numeric(15, 2) NOT NULL,
	description text NULL,
	transaction_status public."banking_account_transaction_status" DEFAULT 'PENDING'::banking_account_transaction_status NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT banking_account_transactions_pkey PRIMARY KEY (id),
	CONSTRAINT banking_account_transactions_banking_account_id_fkey FOREIGN KEY (banking_account_id) REFERENCES public.banking_accounts(id) ON DELETE CASCADE
);