-- Resource Manager Database Setup
-- Run this script if you need to manually set up the database

-- Create database (run this as superuser)
-- CREATE DATABASE resource_manager;

-- Connect to the resource_manager database and create the resources table
-- The application will automatically create this table on startup via migration

CREATE TABLE IF NOT EXISTS resources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_resources_name ON resources (name);
CREATE INDEX IF NOT EXISTS idx_resources_created_at ON resources (created_at);

-- Sample data (optional)
INSERT INTO resources (name, description, created_at, updated_at) VALUES
    ('Sample Resource 1', 'This is a sample resource for testing', NOW(), NOW()),
    ('Sample Resource 2', 'Another sample resource', NOW(), NOW())
ON CONFLICT DO NOTHING;