CREATE TABLE companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    domain VARCHAR(255)
);

CREATE TABLE job_sources (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL, -- 'LINKEDIN', 'GREENHOUSE', 'RSS'
    base_url VARCHAR(255)
);

CREATE TABLE jobs (
    id UUID PRIMARY KEY,
    external_id VARCHAR(255),
    url TEXT UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    company_id INTEGER REFERENCES companies(id),
    description TEXT,
    
    -- Normalized Fields
    seniority VARCHAR(50), -- JUNIOR, INTERN
    modality VARCHAR(50), -- REMOTE, HYBRID, ONSITE
    tech_stack TEXT[], -- Array of strings for stack
    
    -- Metadata
    posted_at TIMESTAMP,
    collected_at TIMESTAMP DEFAULT NOW(),
    source_id INTEGER REFERENCES job_sources(id),
    
    -- Logic
    raw_text_hash VARCHAR(64),
    score INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'NEW'
);

CREATE INDEX idx_jobs_url ON jobs(url);
CREATE INDEX idx_jobs_hash ON jobs(raw_text_hash);
CREATE INDEX idx_jobs_score ON jobs(score DESC);
