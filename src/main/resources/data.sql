/* Roles */
INSERT INTO roles (id_role, name) VALUES (default, 'ROLE_ADMIN');
INSERT INTO roles (id_role, name) VALUES (default, 'ROLE_USER');

/* Users */
INSERT INTO users (id_user, full_name, username, email, phone, password) 
VALUES (default, 'Daisy User', 'daisy', 'daisy@example.com', '123456789', '$2a$12$8LegtLQWe717tIPvZeivjuqKnaAs5.bm0Q05.5GrAmcKzXw2NjoUO');
INSERT INTO users (id_user, full_name, username, email, phone, password) 
VALUES (default, 'Donald User', 'donald', 'donald@example.com', '987654321', '$2a$12$8LegtLQWe717tIPvZeivjuqKnaAs5.bm0Q05.5GrAmcKzXw2NjoUO');

/* Roles_users */
INSERT INTO roles_users (user_id, role_id) 
VALUES ((SELECT id_user FROM users WHERE username = 'daisy'), (SELECT id_role FROM roles WHERE name = 'ROLE_ADMIN'));
INSERT INTO roles_users (user_id, role_id) 
VALUES ((SELECT id_user FROM users WHERE username = 'donald'), (SELECT id_role FROM roles WHERE name = 'ROLE_USER'));