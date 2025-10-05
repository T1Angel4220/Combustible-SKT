-- Script de inicialización de base de datos para Docker
-- Este script se ejecuta después del schema principal

-- Crear base de datos para drivers service
CREATE DATABASE drivers_db;

-- Crear usuario para drivers service
CREATE USER drivers_user WITH PASSWORD 'drivers_pass';

-- Otorgar permisos al usuario
GRANT ALL PRIVILEGES ON DATABASE drivers_db TO drivers_user;

-- Conectar a la base de datos drivers_db
\c drivers_db;

-- Crear esquemas necesarios
CREATE SCHEMA IF NOT EXISTS common_schema;
CREATE SCHEMA IF NOT EXISTS liviana_schema;
CREATE SCHEMA IF NOT EXISTS pesada_schema;

-- Otorgar permisos en los esquemas
GRANT ALL ON SCHEMA common_schema TO drivers_user;
GRANT ALL ON SCHEMA liviana_schema TO drivers_user;
GRANT ALL ON SCHEMA pesada_schema TO drivers_user;

-- Otorgar permisos de uso en los esquemas
GRANT USAGE ON SCHEMA common_schema TO drivers_user;
GRANT USAGE ON SCHEMA liviana_schema TO drivers_user;
GRANT USAGE ON SCHEMA pesada_schema TO drivers_user;

-- Otorgar permisos por defecto para objetos futuros
ALTER DEFAULT PRIVILEGES IN SCHEMA common_schema GRANT ALL ON TABLES TO drivers_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA common_schema GRANT ALL ON SEQUENCES TO drivers_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA liviana_schema GRANT ALL ON TABLES TO drivers_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA liviana_schema GRANT ALL ON SEQUENCES TO drivers_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA pesada_schema GRANT ALL ON TABLES TO drivers_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA pesada_schema GRANT ALL ON SEQUENCES TO drivers_user;
