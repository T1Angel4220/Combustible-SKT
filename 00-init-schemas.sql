-- Script de inicialización de esquemas y usuarios
-- Este script se ejecuta ANTES del schema principal
-- Crear esquemas en la base de datos principal
CREATE SCHEMA IF NOT EXISTS common_schema;

CREATE SCHEMA IF NOT EXISTS liviana_schema;

CREATE SCHEMA IF NOT EXISTS pesada_schema;

-- Crear tipos personalizados en common_schema
DO $ $ BEGIN -- Crear tipos de enumeración si no existen
IF NOT EXISTS (
    SELECT
        1
    FROM
        pg_type
    WHERE
        typname = 'rol_usuario_enum'
) THEN CREATE TYPE common_schema.rol_usuario_enum AS1

END IF;

IF NOT EXISTS (
    SELECT
        1
    FROM
        pg_type
    WHERE
        typname = 'estado_operativo_enum'
) THEN CREATE TYPE common_schema.estado_operativo_enum AS ENUM (
    -- Estados para choferes (principales)
    'DISPONIBLE',
    'ASIGNADO',
    'EN_RUTA',
    'DESCANSANDO',
    'VACACIONES',
    'ENFERMO',
    'LICENCIA',
    -- Estados para vehículos (compatibilidad)
    'EN_USO',
    'MANTENIMIENTO',
    'FUERA_SERVICIO',
    'RESERVADO'
);

END IF;

IF NOT EXISTS (
    SELECT
        1
    FROM
        pg_type
    WHERE
        typname = 'tipo_maquinaria_enum'
) THEN CREATE TYPE common_schema.tipo_maquinaria_enum AS ENUM (
    'CAMION',
    'VOLQUETE',
    'EXCAVADORA',
    'CARGADOR',
    'GRUA',
    'MOTONIVELADORA'
);

END IF;

END $ $;

-- Crear base de datos para drivers service
CREATE DATABASE drivers_db;

-- Crear usuario para drivers service
CREATE USER drivers_user WITH PASSWORD 'drivers_pass';

-- Otorgar permisos al usuario
GRANT ALL PRIVILEGES ON DATABASE drivers_db TO drivers_user;

-- Conectar a la base de datos drivers_db
\ c drivers_db;

-- Crear esquemas en drivers_db
CREATE SCHEMA IF NOT EXISTS common_schema;

CREATE SCHEMA IF NOT EXISTS liviana_schema;

CREATE SCHEMA IF NOT EXISTS pesada_schema;

-- Crear tipos personalizados en drivers_db
DO $ $ BEGIN -- Crear tipos de enumeración si no existen
IF NOT EXISTS (
    SELECT
        1
    FROM
        pg_type
    WHERE
        typname = 'rol_usuario_enum'
) THEN CREATE TYPE common_schema.rol_usuario_enum AS ENUM ('ADMIN', 'OPERADOR', 'SUPERVISOR', 'CONDUCTOR');

END IF;

IF NOT EXISTS (
    SELECT
        1
    FROM
        pg_type
    WHERE
        typname = 'estado_operativo_enum'
) THEN CREATE TYPE common_schema.estado_operativo_enum AS ENUM (
    -- Estados para choferes (principales)
    'DISPONIBLE',
    'ASIGNADO',
    'EN_RUTA',
    'DESCANSANDO',
    'VACACIONES',
    'ENFERMO',
    'LICENCIA',
    -- Estados para vehículos (compatibilidad)
    'EN_USO',
    'MANTENIMIENTO',
    'FUERA_SERVICIO',
    'RESERVADO'
);

END IF;

IF NOT EXISTS (
    SELECT
        1
    FROM
        pg_type
    WHERE
        typname = 'tipo_maquinaria_enum'
) THEN CREATE TYPE common_schema.tipo_maquinaria_enum AS ENUM (
    'CAMION',
    'VOLQUETE',
    'EXCAVADORA',
    'CARGADOR',
    'GRUA',
    'MOTONIVELADORA'
);

END IF;

END $ $;

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