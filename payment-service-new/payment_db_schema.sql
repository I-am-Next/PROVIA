--
-- PostgreSQL database dump
--

\restrict elv2bqbJ53aoYN8fb08pF66ei3N5wzxJDwqnf8SxCZ5efwtZAEp1bE8uj7CekNl

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
    amount numeric(19,2) NOT NULL,
    booking_id uuid NOT NULL,
    description character varying(500),
    escrow_id uuid NOT NULL,
    provider_id uuid NOT NULL,
    release_type character varying(255) NOT NULL,
    released_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT escrow_releases_release_type_check CHECK (((release_type)::text = ANY ((ARRAY['MATERIALS'::character varying, 'LABOUR'::character varying, 'REFUND'::character varying])::text[])))
);


--
-- Name: escrows; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.escrows (
    id uuid NOT NULL,
    booking_id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    fully_released_at timestamp(6) with time zone,
    payment_id uuid NOT NULL,
    provider_id uuid NOT NULL,
    refunded_at timestamp(6) with time zone,
    released_amount numeric(19,2) NOT NULL,
    status character varying(255) NOT NULL,
    total_amount numeric(19,2) NOT NULL,
    customer_id uuid,
    CONSTRAINT escrows_status_check CHECK (((status)::text = ANY ((ARRAY['HOLDING'::character varying, 'PARTIALLY_RELEASED'::character varying, 'FULLY_RELEASED'::character varying, 'REFUNDED'::character varying])::text[])))
);


--
-- Name: outbox_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.outbox_events (
    id uuid NOT NULL,
    aggregate_id uuid NOT NULL,
    aggregate_type character varying(255) NOT NULL,
    event_type character varying(255) NOT NULL,
    last_error character varying(2000),
    occurred_at timestamp(6) with time zone NOT NULL,
    payload oid NOT NULL,
    published_at timestamp(6) with time zone,
    retry_count integer NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT outbox_events_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PUBLISHED'::character varying, 'FAILED'::character varying])::text[])))
);


--
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    id uuid NOT NULL,
    amount numeric(19,2) NOT NULL,
    booking_id uuid NOT NULL,
    completed_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone NOT NULL,
    currency character varying(3) NOT NULL,
    customer_id uuid NOT NULL,
    failed_at timestamp(6) with time zone,
    gateway_paid_at timestamp(6) with time zone,
    gateway_reference character varying(255),
    provider_id uuid NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT payments_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying, 'REFUNDED'::character varying])::text[])))
);


--
-- Name: processed_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.processed_events (
    id uuid NOT NULL,
    event_type character varying(255) NOT NULL,
    idempotency_key character varying(255) NOT NULL,
    processed_at timestamp(6) with time zone NOT NULL,
    provider character varying(255) NOT NULL
);


--
-- Name: wallet_transactions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.wallet_transactions (
    id uuid NOT NULL,
    amount numeric(19,2) NOT NULL,
    description character varying(255) NOT NULL,
    occurred_at timestamp(6) with time zone NOT NULL,
    provider_id uuid NOT NULL,
    reference_id uuid NOT NULL,
    type character varying(255) NOT NULL,
    wallet_id uuid NOT NULL,
    CONSTRAINT wallet_transactions_type_check CHECK (((type)::text = ANY ((ARRAY['CREDIT'::character varying, 'RESERVE'::character varying, 'RESERVE_RELEASE'::character varying, 'WITHDRAWAL'::character varying, 'REFUND'::character varying, 'ADJUSTMENT'::character varying, 'MATERIAL_ADVANCE'::character varying])::text[])))
);


--
-- Name: wallets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.wallets (
    id uuid NOT NULL,
    available_balance numeric(19,2) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    pending_balance numeric(19,2) NOT NULL,
    provider_id uuid NOT NULL,
    reserved_balance numeric(19,2) NOT NULL,
    version bigint
);


--
-- Name: withdrawals; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.withdrawals (
    id uuid NOT NULL,
    amount numeric(19,2) NOT NULL,
    cancelled_at timestamp(6) with time zone,
    completed_at timestamp(6) with time zone,
    failed_at timestamp(6) with time zone,
    failure_reason character varying(500),
    processed_at timestamp(6) with time zone,
    provider_id uuid NOT NULL,
    requested_at timestamp(6) with time zone NOT NULL,
    status character varying(255) NOT NULL,
    version bigint,
    wallet_id uuid NOT NULL,
    CONSTRAINT withdrawals_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying])::text[])))
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
-- Name: wallets uk10trf60hg6clh9ynt2i95toaw; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT uk10trf60hg6clh9ynt2i95toaw UNIQUE (provider_id);


--
-- Name: escrows uk1tycabqtpngvbwgsbkbr0uphu; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escrows
    ADD CONSTRAINT uk1tycabqtpngvbwgsbkbr0uphu UNIQUE (booking_id);


--
-- Name: payments uk9v7e32gxe6xobw4amlnq8pr7k; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT uk9v7e32gxe6xobw4amlnq8pr7k UNIQUE (gateway_reference);


--
-- Name: processed_events uk_processed_event; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.processed_events
    ADD CONSTRAINT uk_processed_event UNIQUE (provider, idempotency_key, event_type);


--
-- Name: payments uknuscjm6x127hkb15kcb8n56wo; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT uknuscjm6x127hkb15kcb8n56wo UNIQUE (booking_id);


--
-- Name: escrows ukpuuu1k2ty4n64ql2mwta7yhnn; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escrows
    ADD CONSTRAINT ukpuuu1k2ty4n64ql2mwta7yhnn UNIQUE (payment_id);


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

\unrestrict elv2bqbJ53aoYN8fb08pF66ei3N5wzxJDwqnf8SxCZ5efwtZAEp1bE8uj7CekNl
