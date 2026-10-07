-- Migration V11: Convert NUMERIC columns to DOUBLE PRECISION to match Java Double entity fields
ALTER TABLE quotes ALTER COLUMN discount_percentage TYPE DOUBLE PRECISION;
ALTER TABLE subscription_plans ALTER COLUMN monthly_price TYPE DOUBLE PRECISION;
