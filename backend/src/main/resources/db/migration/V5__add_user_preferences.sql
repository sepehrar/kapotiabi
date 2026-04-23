ALTER TABLE users
  ADD COLUMN preferred_city VARCHAR(100),
  ADD COLUMN search_radius_km INT DEFAULT 10,
  ADD COLUMN parts_quality_preference VARCHAR(20) CHECK (parts_quality_preference IN ('oem','oem_equivalent','budget','not_sure'));
