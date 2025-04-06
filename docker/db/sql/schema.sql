-- DROP SCHEMA public;

CREATE SCHEMA public AUTHORIZATION pg_database_owner;

COMMENT ON SCHEMA public IS 'standard public schema';

-- DROP TYPE public."auth_status";

CREATE TYPE public."auth_status" AS ENUM (
	'ACTIVE',
	'EXPIRED');

CREATE CAST (varchar as auth_status) WITH INOUT AS IMPLICIT;

-- DROP TYPE public.banking_account_currency;

CREATE TYPE public.banking_account_currency AS ENUM (
	'EUR',
	'USD');

-- DROP TYPE public.banking_account_status;

CREATE TYPE public.banking_account_status AS ENUM (
	'OPEN',
	'CLOSED',
	'LOCKED');

-- DROP TYPE public.banking_account_transaction_status;

CREATE TYPE public.banking_account_transaction_status AS ENUM (
	'PENDING',
	'FAILED',
	'COMPLETED');

-- DROP TYPE public.banking_account_transaction_type;

CREATE TYPE public.banking_account_transaction_type AS ENUM (
	'DEPOSIT',
	'TRANSFER',
	'WITHDRAWAL');

-- DROP TYPE public.banking_account_type;

CREATE TYPE public.banking_account_type AS ENUM (
	'SAVINGS',
	'CHECK');

-- DROP TYPE public."customer_gender";

CREATE TYPE public."customer_gender" AS ENUM (
	'MALE',
	'FEMALE');

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
-- DROP SEQUENCE public.banking_account_transactions_id_seq;

CREATE SEQUENCE public.banking_account_transactions_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE public.banking_accounts_id_seq;

CREATE SEQUENCE public.banking_accounts_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE public.customer_banking_account_id_seq;

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
-- DROP SEQUENCE public.profiles_id_seq;

CREATE SEQUENCE public.profiles_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 2147483647
	START 1
	CACHE 1
	NO CYCLE;-- public.banking_accounts definition

-- Drop table

-- DROP TABLE public.banking_accounts;

CREATE TABLE public.banking_accounts (
	id serial4 NOT NULL,
	account_number varchar(20) NOT NULL,
	balance numeric(15, 2) DEFAULT 0.00 NOT NULL,
	account_type public.banking_account_type DEFAULT 'SAVINGS'::banking_account_type NOT NULL,
	currency public.banking_account_currency DEFAULT 'EUR'::banking_account_currency NOT NULL,
	status public.banking_account_status DEFAULT 'CLOSED'::banking_account_status NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT banking_accounts_account_number_key UNIQUE (account_number),
	CONSTRAINT banking_accounts_pkey PRIMARY KEY (id)
);


-- public.customers definition

-- Drop table

-- DROP TABLE public.customers;

CREATE TABLE public.customers (
	id serial4 NOT NULL,
	email varchar(255) NOT NULL,
	role public.customer_role DEFAULT 'CUSTOMER'::customer_role NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT customers_email_key UNIQUE (email),
	CONSTRAINT customers_pkey PRIMARY KEY (id)
);


-- public.auth definition

-- Drop table

-- DROP TABLE public.auth;

CREATE TABLE public.auth (
	id serial4 NOT NULL,
	customer_id int4 NOT NULL,
	password_hash varchar(255) NOT NULL,
	verified bool DEFAULT false NULL,
	status public."auth_status" DEFAULT 'ACTIVE'::auth_status NULL,
	CONSTRAINT auth_customer_id_key UNIQUE (customer_id),
	CONSTRAINT auth_pkey PRIMARY KEY (id),
	CONSTRAINT auth_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);


-- public.banking_account_transactions definition

-- Drop table

-- DROP TABLE public.banking_account_transactions;

CREATE TABLE public.banking_account_transactions (
	id serial4 NOT NULL,
	banking_account_id int4 NOT NULL,
	transaction_type public.banking_account_transaction_type NOT NULL,
	amount numeric(15, 2) NOT NULL,
	transaction_date timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	description text NULL,
	status public.banking_account_transaction_status DEFAULT 'PENDING'::banking_account_transaction_status NOT NULL,
	CONSTRAINT banking_account_transactions_pkey PRIMARY KEY (id),
	CONSTRAINT banking_account_transactions_banking_account_id_fkey FOREIGN KEY (banking_account_id) REFERENCES public.banking_accounts(id) ON DELETE CASCADE
);


-- public.customer_banking_account definition

-- Drop table

-- DROP TABLE public.customer_banking_account;

CREATE TABLE public.customer_banking_account (
	id serial4 NOT NULL,
	customer_id int4 NOT NULL,
	banking_account_id int4 NOT NULL,
	CONSTRAINT customer_banking_account_pkey PRIMARY KEY (id),
	CONSTRAINT customer_banking_account_banking_account_id_fkey FOREIGN KEY (banking_account_id) REFERENCES public.banking_accounts(id) ON DELETE CASCADE,
	CONSTRAINT customer_banking_account_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);


-- public.profiles definition

-- Drop table

-- DROP TABLE public.profiles;

CREATE TABLE public.profiles (
	id serial4 NOT NULL,
	customer_id int4 NOT NULL,
	"name" varchar(100) NOT NULL,
	surname varchar(100) NOT NULL,
	phone varchar(20) NULL,
	birthdate date NULL,
	gender public."customer_gender" NOT NULL,
	photo text NULL,
	address varchar(255) NULL,
	postal_code varchar(20) NULL,
	country varchar(100) NULL,
	national_id varchar(50) NOT NULL,
	CONSTRAINT profiles_customer_id_key UNIQUE (customer_id),
	CONSTRAINT profiles_national_id_key UNIQUE (national_id),
	CONSTRAINT profiles_pkey PRIMARY KEY (id),
	CONSTRAINT profiles_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);