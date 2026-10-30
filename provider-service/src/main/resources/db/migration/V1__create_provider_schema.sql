-- ============================================================
-- Provider Service - Initial Database Schema
-- Version: V1
--
-- Creates the Provider Service-owned tables and indexes.
-- This migration intentionally contains no application seed data.
--
-- Media Storage is a separate service. Therefore,
-- profile_photo_media_id is a service-level UUID reference and
-- does not have a foreign key to a Media Storage database.
-- ============================================================

-- ============================================================
-- 1. PROVIDER PROFILES
-- ============================================================

CREATE TABLE provider_profiles (
                                   id UUID NOT NULL,
                                   auth_user_id VARCHAR(255) NOT NULL,
                                   business_name VARCHAR(255) NOT NULL,
                                   email VARCHAR(255) NOT NULL,
                                   category VARCHAR(255) NOT NULL,
                                   description VARCHAR(255),
                                   phone VARCHAR(255),
                                   verified BOOLEAN NOT NULL DEFAULT FALSE,
                                   average_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                                   rating_count INTEGER NOT NULL DEFAULT 0,
                                   profile_photo_media_id UUID,
                                   created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                   CONSTRAINT pk_provider_profiles
                                       PRIMARY KEY (id),

                                   CONSTRAINT uk_provider_profiles_auth_user_id
                                       UNIQUE (auth_user_id),

                                   CONSTRAINT uk_provider_profiles_email
                                       UNIQUE (email),

                                   CONSTRAINT chk_provider_profiles_average_rating
                                       CHECK (average_rating >= 0.0 AND average_rating <= 5.0),

                                   CONSTRAINT chk_provider_profiles_rating_count
                                       CHECK (rating_count >= 0)
);

-- Supports provider discovery/filtering by category.
CREATE INDEX idx_provider_profiles_category
    ON provider_profiles (category);


-- ============================================================
-- 2. PROVIDER SERVICES
-- ============================================================

CREATE TABLE provider_services (
                                   id UUID NOT NULL,
                                   provider_profile_id UUID NOT NULL,
                                   service_item_id UUID NOT NULL,
                                   price NUMERIC(19, 2) NOT NULL,
                                   active BOOLEAN NOT NULL,
                                   created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                   CONSTRAINT pk_provider_services
                                       PRIMARY KEY (id),

                                   CONSTRAINT uk_provider_service
                                       UNIQUE (provider_profile_id, service_item_id),

                                   CONSTRAINT fk_provider_services_provider_profile
                                       FOREIGN KEY (provider_profile_id)
                                           REFERENCES provider_profiles (id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT chk_provider_services_price
                                       CHECK (price >= 0)
);

-- Supports queries for services belonging to a provider.
CREATE INDEX idx_provider_services_provider_profile_id
    ON provider_services (provider_profile_id);

-- service_item_id belongs to Service Catalog, a separate service.
-- No cross-service database foreign key is created.


-- ============================================================
-- 3. OUTBOX EVENTS
-- ============================================================

CREATE TABLE outbox_events (
                               id UUID NOT NULL,
                               aggregate_type VARCHAR(255) NOT NULL,
                               aggregate_id UUID NOT NULL,
                               event_type VARCHAR(255) NOT NULL,
                               payload TEXT NOT NULL,
                               status VARCHAR(255) NOT NULL,
                               retry_count INTEGER NOT NULL DEFAULT 0,
                               last_error TEXT,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               processed_at TIMESTAMP WITH TIME ZONE,

                               CONSTRAINT pk_outbox_events
                                   PRIMARY KEY (id),

                               CONSTRAINT chk_outbox_events_retry_count
                                   CHECK (retry_count >= 0)
);

-- Supports polling pending/failed events in status, retry and time order.
CREATE INDEX idx_outbox_events_status_retry_created
    ON outbox_events (status, retry_count, created_at);
