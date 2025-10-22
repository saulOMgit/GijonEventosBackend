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

/* Events */
INSERT INTO events (id_event, title, description, date, location, organizer_id, max_attendees) 
VALUES (default, 'Concierto de Jazz en el Puerto', 'Disfruta de una velada de jazz con vistas al mar.', '2025-11-15 21:00:00', 'Puerto Deportivo de Gijón', (SELECT id_user FROM users WHERE username = 'daisy'), 150);
INSERT INTO events (id_event, title, description, date, location, organizer_id, max_attendees) 
VALUES (default, 'Maratón de Gijón 2025', 'Carrera anual por las calles de Gijón.', '2025-11-20 09:00:00', 'Plaza Mayor - Salida', (SELECT id_user FROM users WHERE username = 'donald'), 500);
INSERT INTO events (id_event, title, description, date, location, organizer_id, max_attendees) 
VALUES (default, 'Festival de Gastronomía Asturiana', 'Degustación de productos locales.', '2025-10-25 12:00:00', 'Parque de Begoña', (SELECT id_user FROM users WHERE username = 'daisy'), 300);

/* Attendees (event_attendees) */
INSERT INTO event_attendees (event_id, user_id) 
VALUES ((SELECT id_event FROM events WHERE title = 'Concierto de Jazz en el Puerto'), (SELECT id_user FROM users WHERE username = 'donald'));
INSERT INTO event_attendees (event_id, user_id) 
VALUES ((SELECT id_event FROM events WHERE title = 'Maratón de Gijón 2025'), (SELECT id_user FROM users WHERE username = 'daisy'));
INSERT INTO event_attendees (event_id, user_id) 
VALUES ((SELECT id_event FROM events WHERE title = 'Maratón de Gijón 2025'), (SELECT id_user FROM users WHERE username = 'donald'));