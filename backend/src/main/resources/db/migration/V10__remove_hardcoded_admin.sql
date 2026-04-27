-- Remove the admin user seeded with a hardcoded password in V3.
-- The admin is now created at startup by AdminSeeder using ADMIN_DEFAULT_PASSWORD env var.
DELETE FROM users WHERE email = 'admin@kapotiabi.ee' AND role = 'admin';
