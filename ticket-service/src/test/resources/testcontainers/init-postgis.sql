-- The postgis/postgis image ships the extension's files but doesn't enable it in the
-- default database on its own; Liquibase's 003-add-venue-geography migration has a
-- HALT precondition that requires it already present (see that changeset), so this has
-- to run before Spring Boot's context (and therefore Liquibase) ever connects.
CREATE EXTENSION IF NOT EXISTS postgis;
