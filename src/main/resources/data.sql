-- Datos iniciales de ejemplo
-- Prima = canon_mensual * meses_vigencia

-- Poliza 1: INDIVIDUAL, VIGENTE, 12 meses, canon 1.500.000 -> prima 18.000.000
INSERT INTO polizas (tipo, estado, tomador, vigencia_inicio, vigencia_fin, meses_vigencia, canon_mensual, prima)
VALUES ('INDIVIDUAL', 'VIGENTE', 'Juan Perez', '2025-01-01', '2025-12-31', 12, 1500000.00, 18000000.00);

-- Poliza 2: COLECTIVA, VIGENTE, 12 meses, canon 2.000.000 -> prima 24.000.000
INSERT INTO polizas (tipo, estado, tomador, vigencia_inicio, vigencia_fin, meses_vigencia, canon_mensual, prima)
VALUES ('COLECTIVA', 'VIGENTE', 'Inmobiliaria Las Palmas', '2025-02-01', '2026-01-31', 12, 2000000.00, 24000000.00);

-- Poliza 3: COLECTIVA, CANCELADA, 6 meses, canon 1.000.000 -> prima 6.000.000
INSERT INTO polizas (tipo, estado, tomador, vigencia_inicio, vigencia_fin, meses_vigencia, canon_mensual, prima)
VALUES ('COLECTIVA', 'CANCELADA', 'Administracion Torres del Parque', '2024-06-01', '2024-11-30', 6, 1000000.00, 6000000.00);

-- Riesgos
-- La poliza individual (1) tiene exactamente 1 riesgo
INSERT INTO riesgos (direccion_inmueble, arrendatario, estado, poliza_id)
VALUES ('Calle 100 # 15-20 Apto 301, Bogota', 'Juan Perez', 'ACTIVO', 1);

-- La poliza colectiva (2) tiene varios riesgos activos
INSERT INTO riesgos (direccion_inmueble, arrendatario, estado, poliza_id)
VALUES ('Carrera 7 # 45-10 Local 2, Bogota', 'Maria Gomez', 'ACTIVO', 2);

INSERT INTO riesgos (direccion_inmueble, arrendatario, estado, poliza_id)
VALUES ('Avenida 68 # 22-15 Apto 802, Bogota', 'Carlos Ramirez', 'ACTIVO', 2);

-- La poliza colectiva cancelada (3) con su riesgo ya cancelado
INSERT INTO riesgos (direccion_inmueble, arrendatario, estado, poliza_id)
VALUES ('Calle 80 # 30-25 Casa 5, Bogota', 'Ana Torres', 'CANCELADO', 3);
