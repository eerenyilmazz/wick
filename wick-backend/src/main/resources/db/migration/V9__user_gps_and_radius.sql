ALTER TABLE users ADD COLUMN latitude DOUBLE PRECISION;
ALTER TABLE users ADD COLUMN longitude DOUBLE PRECISION;

ALTER TABLE discovery_preferences DROP COLUMN province;
ALTER TABLE discovery_preferences ADD COLUMN radius_km INTEGER;
