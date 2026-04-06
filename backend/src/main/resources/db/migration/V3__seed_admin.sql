-- Initial admin user. Password: Admin1234  (bcrypt cost 12 — change after first login)
INSERT INTO users (email, password_hash, role, first_name, last_name)
VALUES (
    'admin@kapotiabi.ee',
    '$2a$10$ETnDB1UA3HMz4A.gzb4YpeVvzm.lNb650w0gik8dJr4M7EZy7K0GG',
    'admin',
    'Admin',
    'Kapotiabi'
);
