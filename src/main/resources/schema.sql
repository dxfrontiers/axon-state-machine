CREATE TABLE IF NOT EXISTS domain_event_entry (
    global_index BIGSERIAL NOT NULL,
    aggregate_identifier VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    type VARCHAR(255),
    event_identifier VARCHAR(255) NOT NULL,
    meta_data bytea,
    payload bytea NOT NULL,
    payload_revision VARCHAR(255),
    payload_type VARCHAR(255) NOT NULL,
    time_stamp VARCHAR(255) NOT NULL,
    PRIMARY KEY (global_index),
    UNIQUE (aggregate_identifier, sequence_number),
    UNIQUE (event_identifier)
);

CREATE TABLE IF NOT EXISTS snapshot_event_entry (
    aggregate_identifier VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    event_identifier VARCHAR(255) NOT NULL,
    meta_data bytea,
    payload bytea NOT NULL,
    payload_revision VARCHAR(255),
    payload_type VARCHAR(255) NOT NULL,
    time_stamp VARCHAR(255) NOT NULL,
    PRIMARY KEY (aggregate_identifier, sequence_number),
    UNIQUE (event_identifier)
);

CREATE TABLE IF NOT EXISTS token_entry (
    processor_name VARCHAR(255) NOT NULL,
    segment INTEGER NOT NULL,
    token bytea NULL,
    token_type VARCHAR(255) NULL,
    timestamp VARCHAR(255) NULL,
    owner VARCHAR(255) NULL,
    PRIMARY KEY (processor_name,segment)
);

CREATE TABLE IF NOT EXISTS saga_entry (
    saga_id VARCHAR(255) NOT NULL,
    revision VARCHAR(255),
    saga_type VARCHAR(255),
    serialized_saga BYTEA,
    PRIMARY KEY (saga_id)
);

CREATE TABLE IF NOT EXISTS association_value_entry (
    id BIGSERIAL NOT NULL,
    association_key VARCHAR(255),
    association_value VARCHAR(255),
    saga_id VARCHAR(255),
    saga_type VARCHAR(255),
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS association_value_entry_idx1 ON association_value_entry (saga_type, association_key, association_value);
CREATE INDEX IF NOT EXISTS association_value_entry_idx2 ON association_value_entry (saga_id, saga_type);
