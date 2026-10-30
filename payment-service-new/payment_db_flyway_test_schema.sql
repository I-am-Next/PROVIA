--
-- PostgreSQL database dump
--

\restrict KkUq6OS51h8V9Nd7IVZEoyNTICfuULaIzrOd6m8OitbUegmuKYGycZvBJyk3E5D

-- Dumped from database version 16.11 (Debian 16.11-1.pgdg13+1)
-- Dumped by pg_dump version 16.11 (Debian 16.11-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: escrow_releases; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.escrow_releases (
    id uuid NOT NULL,
    escrow_id uuid NOT NULL,
    booking_id uuid NOT NULL,
    provider_id uuid NOT NULL,
    release_type character varying(255) NOT NULL,
    amount numeric(19,2) NOT NULL,
    description character varying(500),
    released_at timestamp with time zone NOT NULL
);


--
-- Name: escrows; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.escrows (
    id uuid NOT NULL,
    payment_id uuid NOT NULL,
    booking_id uuid NOT NULL,
    customer_id uuid NOT NULL,
    provider_id uuid NOT NULL,
    total_amount numeric(19,2) NOT NULL,
    released_amount numeric(19,2) NOT NULL,
    status character varying(255) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    fully_released_at timestamp with time zone,
    refunded_at timestamp with time zone
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: outbox_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.outbox_events (
    id uuid NOT NULL,
    aggregate_id uuid NOT NULL,
    aggregate_type character varying(255) NOT NULL,
    event_type character varying(255) NOT NULL,
    payload text NOT NULL,
    occurred_at timestamp with time zone NOT NULL,
    status character varying(255) NOT NULL,
    published_at timestamp with time zone,
    retry_count integer NOT NULL,
    last_error character varying(2000)
);


--
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    id uuid NOT NULL,
    booking_id uuid NOT NULL,
    customer_id uuid NOT NULL,
    provider_id uuid NOT NULL,
    amount numeric(19,2) NOT NULL,
    status character varying(255) NOT NULL,
    currency character varying(3) NOT NULL,
    gateway_reference character varying(255),
    gateway_paid_at timestamp with time zone,
    created_at timestamp with time zone NOT NULL,
    completed_at timestamp with time zone,
    failed_at timestamp with time zone
);


--
-- Name: processed_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.processed_events (
    id uuid NOT NULL,
    provider character varying(255) NOT NULL,
    idempotency_key character varying(255) NOT NULL,
    event_type character varying(255) NOT NULL,
    processed_at timestamp with time zone NOT NULL
);


--
-- Name: wallet_transactions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.wallet_transactions (
    id uuid NOT NULL,
    wallet_id uuid NOT NULL,
    provider_id uuid NOT NULL,
    type character varying(255) NOT NULL,
    amount numeric(19,2) NOT NULL,
    reference_id uuid NOT NULL,
    description character varying(255) NOT NULL,
    occurred_at timestamp with time zone NOT NULL
);


--
-- Name: wallets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.wallets (
    id uuid NOT NULL,
    version bigint,
    provider_id uuid NOT NULL,
    available_balance numeric(19,2) NOT NULL,
    pending_balance numeric(19,2) NOT NULL,
    reserved_balance numeric(19,2) NOT NULL,
    created_at timestamp with time zone NOT NULL
);


--
-- Name: withdrawals; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.withdrawals (
    id uuid NOT NULL,
    version bigint,
    wallet_id uuid NOT NULL,
    provider_id uuid NOT NULL,
    amount numeric(19,2) NOT NULL,
    status character varying(255) NOT NULL,
    failure_reason character varying(500),
    requested_at timestamp with time zone NOT NULL,
    processed_at timestamp with time zone,
    completed_at timestamp with time zone,
    failed_at timestamp with time zone,
    cancelled_at timestamp with time zone
);


--
-- Name: escrow_releases escrow_releases_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escrow_releases
    ADD CONSTRAINT escrow_releases_pkey PRIMARY KEY (id);


--
-- Name: escrows escrows_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escrows
    ADD CONSTRAINT escrows_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: outbox_events outbox_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.outbox_events
    ADD CONSTRAINT outbox_events_pkey PRIMARY KEY (id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: processed_events processed_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.processed_events
    ADD CONSTRAINT processed_events_pkey PRIMARY KEY (id);


--
-- Name: escrows uk_escrows_booking_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escrows
    ADD CONSTRAINT uk_escrows_booking_id UNIQUE (booking_id);


--
-- Name: escrows uk_escrows_payment_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escrows
    ADD CONSTRAINT uk_escrows_payment_id UNIQUE (payment_id);


--
-- Name: payments uk_payments_booking_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT uk_payments_booking_id UNIQUE (booking_id);


--
-- Name: payments uk_payments_gateway_reference; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT uk_payments_gateway_reference UNIQUE (gateway_reference);


--
-- Name: processed_events uk_processed_event; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.processed_events
    ADD CONSTRAINT uk_processed_event UNIQUE (provider, idempotency_key, event_type);


--
-- Name: wallets uk_wallets_provider_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT uk_wallets_provider_id UNIQUE (provider_id);


--
-- Name: wallet_transactions wallet_transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallet_transactions
    ADD CONSTRAINT wallet_transactions_pkey PRIMARY KEY (id);


--
-- Name: wallets wallets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT wallets_pkey PRIMARY KEY (id);


--
-- Name: withdrawals withdrawals_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.withdrawals
    ADD CONSTRAINT withdrawals_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_escrow_booking; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escrow_booking ON public.escrows USING btree (booking_id);


--
-- Name: idx_escrow_customer; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escrow_customer ON public.escrows USING btree (customer_id);


--
-- Name: idx_escrow_provider; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escrow_provider ON public.escrows USING btree (provider_id);


--
-- Name: idx_escrow_release_booking; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escrow_release_booking ON public.escrow_releases USING btree (booking_id);


--
-- Name: idx_escrow_release_escrow; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escrow_release_escrow ON public.escrow_releases USING btree (escrow_id);


--
-- Name: idx_escrow_release_occurred_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escrow_release_occurred_at ON public.escrow_releases USING btree (released_at);


--
-- Name: idx_escrow_release_provider; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escrow_release_provider ON public.escrow_releases USING btree (provider_id);


--
-- Name: idx_escrow_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escrow_status ON public.escrows USING btree (status);


--
-- Name: idx_outbox_occurred_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_outbox_occurred_at ON public.outbox_events USING btree (occurred_at);


--
-- Name: idx_outbox_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_outbox_status ON public.outbox_events USING btree (status);


--
-- Name: idx_payment_customer; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_customer ON public.payments USING btree (customer_id);


--
-- Name: idx_payment_provider; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_provider ON public.payments USING btree (provider_id);


--
-- Name: idx_payment_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_status ON public.payments USING btree (status);


--
-- Name: idx_processed_key; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_processed_key ON public.processed_events USING btree (idempotency_key);


--
-- Name: idx_processed_provider; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_processed_provider ON public.processed_events USING btree (provider);


--
-- Name: idx_withdrawal_provider; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_withdrawal_provider ON public.withdrawals USING btree (provider_id);


--
-- Name: idx_withdrawal_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_withdrawal_status ON public.withdrawals USING btree (status);


--
-- Name: idx_withdrawal_wallet; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_withdrawal_wallet ON public.withdrawals USING btree (wallet_id);


--
-- PostgreSQL database dump complete
--

\unrestrict KkUq6OS51h8V9Nd7IVZEoyNTICfuULaIzrOd6m8OitbUegmuKYGycZvBJyk3E5D
