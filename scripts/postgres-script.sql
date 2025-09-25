INSERT INTO countries (name) VALUES ('Serbia');

INSERT INTO cities (name, postal_code, country_id) VALUES ('Belgrade', '11000', 1);
INSERT INTO cities (name, postal_code, country_id) VALUES ('Novi Sad', '21000', 1);

INSERT INTO addresses (street, street_number, apartment_number, city_id) VALUES ('Cvetna', '123', '4', 1);
INSERT INTO addresses (street, street_number, apartment_number, city_id) VALUES ('Prvomajska', '5', null, 1);
INSERT INTO addresses (street, street_number, apartment_number, city_id) VALUES ('Cvetna', '123', '4', 2);

INSERT INTO users (first_name, last_name, email, phone_number, date_of_birth, gender, password, address_id)
VALUES ('Ana', 'Ivanovic', 'ana.ivanovic@example.com', '+987654321', '1985-02-15T00:00:00', 0, 'pass123', 1);
INSERT INTO users (first_name, last_name, email, phone_number, date_of_birth, gender, password, address_id)
VALUES ('Marko', 'Jovanovic', 'marko.jovanovic@example.com', '+1122334455', '1992-05-20T00:00:00', 1, 'pass123', 2);
INSERT INTO users (first_name, last_name, email, phone_number, date_of_birth, gender, password, address_id)
VALUES ('Jovana', 'Petrovic', 'jovana.petrovic@example.com', '+3344556677', '1988-08-10T00:00:00', 0, 'pass123', 3);