-- ============================================================
-- V1__create_report_reserva_snapshots_table.sql
-- Tabla principal del microservicio ms-campuslab-report
-- Motor: Oracle Database
-- ============================================================

CREATE TABLE REPORT_RESERVA_SNAPSHOTS (
    RESERVA_ID          NUMBER(19)      NOT NULL,
    RECURSO_ID          NUMBER(19)      NOT NULL,
    USUARIO_ID          NUMBER(19),
    ESTADO_ACTUAL       VARCHAR2(20)    NOT NULL,
    ULTIMO_EVENT_TYPE   VARCHAR2(40)    NOT NULL,
    FECHA_CREACION      TIMESTAMP       NOT NULL,
    ULTIMO_EVENTO_EN    TIMESTAMP       NOT NULL,
    CONSTRAINT PK_REPORT_RESERVA_SNAPSHOTS PRIMARY KEY (RESERVA_ID)
);

CREATE INDEX IX_REPORT_SNAPSHOTS_ESTADO   ON REPORT_RESERVA_SNAPSHOTS (ESTADO_ACTUAL);
CREATE INDEX IX_REPORT_SNAPSHOTS_RECURSO  ON REPORT_RESERVA_SNAPSHOTS (RECURSO_ID);

COMMENT ON TABLE REPORT_RESERVA_SNAPSHOTS IS 'Ultimo estado conocido de cada reserva, mantenido al dia desde Kafka (bookings.events), usado para calcular KPIs';
