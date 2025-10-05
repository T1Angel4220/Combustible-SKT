-- Tabla de Roles de Usuario
CREATE TABLE IF NOT EXISTS common_schema.roles (
    id SERIAL PRIMARY KEY,
    nombre common_schema.rol_usuario_enum UNIQUE NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS common_schema.usuarios (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    rol_id INTEGER NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rol_id) REFERENCES common_schema.roles(id)
);

-- Tabla de Choferes
CREATE TABLE IF NOT EXISTS common_schema.choferes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) UNIQUE NOT NULL,
    licencia VARCHAR(50) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    fecha_contratacion DATE,
    estado common_schema.estado_operativo_enum NOT NULL DEFAULT 'ACTIVO',
    tipo_maquinaria_asignada common_schema.tipo_maquinaria_enum,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- 4. Tablas en liviana_schema (Maquinaria Liviana)
-- -----------------------------------------------------------------------------

-- Tabla de Vehículos Livianos
CREATE TABLE IF NOT EXISTS liviana_schema.vehiculos (
    id SERIAL PRIMARY KEY,
    placa VARCHAR(20) UNIQUE NOT NULL,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    anio INTEGER,
    tipo_maquinaria common_schema.tipo_maquinaria_enum NOT NULL,
    capacidad_combustible_litros NUMERIC(10, 2),
    estado_operativo common_schema.estado_operativo_enum NOT NULL DEFAULT 'DISPONIBLE',
    kilometraje_horas NUMERIC(15, 2) DEFAULT 0.00,
    chofer_asignado_id INTEGER,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chofer_asignado_id) REFERENCES common_schema.choferes(id)
);

-- Tabla de Rutas para Maquinaria Liviana
CREATE TABLE IF NOT EXISTS liviana_schema.rutas (
    id SERIAL PRIMARY KEY,
    nombre_ruta VARCHAR(255) NOT NULL,
    origen VARCHAR(255) NOT NULL,
    destino VARCHAR(255) NOT NULL,
    distancia_km NUMERIC(10, 2) NOT NULL,
    duracion_estimada_horas NUMERIC(5, 2),
    consumo_estimado_litros NUMERIC(10, 2),
    vehiculo_asignado_id INTEGER,
    chofer_asignado_id INTEGER,
    activa BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehiculo_asignado_id) REFERENCES liviana_schema.vehiculos(id),
    FOREIGN KEY (chofer_asignado_id) REFERENCES common_schema.choferes(id)
);

-- Tabla de Consumo de Combustible para Maquinaria Liviana
CREATE TABLE IF NOT EXISTS liviana_schema.registros_combustible (
    id SERIAL PRIMARY KEY,
    fecha_hora TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    cantidad_litros NUMERIC(10, 2) NOT NULL,
    tipo_combustible VARCHAR(50) NOT NULL DEFAULT 'DIESEL',
    precio_por_litro NUMERIC(10, 2),
    costo_total NUMERIC(10, 2),
    vehiculo_id INTEGER NOT NULL,
    chofer_id INTEGER NOT NULL,
    ruta_id INTEGER,
    lectura_odometro_horas NUMERIC(15, 2),
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehiculo_id) REFERENCES liviana_schema.vehiculos(id),
    FOREIGN KEY (chofer_id) REFERENCES common_schema.choferes(id),
    FOREIGN KEY (ruta_id) REFERENCES liviana_schema.rutas(id)
);

-- -----------------------------------------------------------------------------
-- 5. Tablas en pesada_schema (Maquinaria Pesada)
-- -----------------------------------------------------------------------------

-- Tabla de Vehículos Pesados
CREATE TABLE IF NOT EXISTS pesada_schema.vehiculos (
    id SERIAL PRIMARY KEY,
    placa VARCHAR(20) UNIQUE NOT NULL,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    anio INTEGER,
    tipo_maquinaria common_schema.tipo_maquinaria_enum NOT NULL,
    capacidad_combustible_litros NUMERIC(10, 2),
    estado_operativo common_schema.estado_operativo_enum NOT NULL DEFAULT 'DISPONIBLE',
    kilometraje_horas NUMERIC(15, 2) DEFAULT 0.00,
    chofer_asignado_id INTEGER,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chofer_asignado_id) REFERENCES common_schema.choferes(id)
);

-- Tabla de Rutas para Maquinaria Pesada
CREATE TABLE IF NOT EXISTS pesada_schema.rutas (
    id SERIAL PRIMARY KEY,
    nombre_ruta VARCHAR(255) NOT NULL,
    origen VARCHAR(255) NOT NULL,
    destino VARCHAR(255) NOT NULL,
    distancia_km NUMERIC(10, 2) NOT NULL,
    duracion_estimada_horas NUMERIC(5, 2),
    consumo_estimado_litros NUMERIC(10, 2),
    vehiculo_asignado_id INTEGER,
    chofer_asignado_id INTEGER,
    activa BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehiculo_asignado_id) REFERENCES pesada_schema.vehiculos(id),
    FOREIGN KEY (chofer_asignado_id) REFERENCES common_schema.choferes(id)
);

-- Tabla de Consumo de Combustible para Maquinaria Pesada
CREATE TABLE IF NOT EXISTS pesada_schema.registros_combustible (
    id SERIAL PRIMARY KEY,
    fecha_hora TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    cantidad_litros NUMERIC(10, 2) NOT NULL,
    tipo_combustible VARCHAR(50) NOT NULL DEFAULT 'DIESEL',
    precio_por_litro NUMERIC(10, 2),
    costo_total NUMERIC(10, 2),
    vehiculo_id INTEGER NOT NULL,
    chofer_id INTEGER NOT NULL,
    ruta_id INTEGER,
    lectura_odometro_horas NUMERIC(15, 2),
    observaciones TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehiculo_id) REFERENCES pesada_schema.vehiculos(id),
    FOREIGN KEY (chofer_id) REFERENCES common_schema.choferes(id),
    FOREIGN KEY (ruta_id) REFERENCES pesada_schema.rutas(id)
);