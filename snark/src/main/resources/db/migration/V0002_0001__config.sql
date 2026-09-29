CREATE TABLE config (
                        id BIGSERIAL PRIMARY KEY,
                        module VARCHAR(255) NOT NULL,
                        thread VARCHAR(255) NOT NULL,
                        config_key VARCHAR(255) NOT NULL,
                        value TEXT,

                        CONSTRAINT uk_config UNIQUE (module, thread, config_key)
);

CREATE INDEX idx_config_module ON config(module);
CREATE INDEX idx_config_thread ON config(thread);