-- Replace any legacy 'USER' role with a valid enum
UPDATE users SET role = 'VIEWER' WHERE role = 'USER';