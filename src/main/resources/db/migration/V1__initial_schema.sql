-- Schema inicial (banco vazio). Flyway roda antes do JPA; não use só ALTER sem CREATE.
CREATE TABLE companies (
                           id UUID PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           auto_approval_limit NUMERIC(19, 2) NOT NULL DEFAULT 0
);

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       company_id UUID NOT NULL REFERENCES companies (id),
                       password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE financial_orders (
                                  id UUID PRIMARY KEY,
                                  company_id UUID NOT NULL REFERENCES companies (id),
                                  amount NUMERIC(19, 2) NOT NULL,
                                  order_type VARCHAR(50) NOT NULL,
                                  order_status VARCHAR(50) NOT NULL,
                                  description VARCHAR(500),
                                  created_at TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_financial_orders_company_status
    ON financial_orders (company_id, order_status);

CREATE INDEX idx_financial_orders_company_created
    ON financial_orders (company_id, created_at);