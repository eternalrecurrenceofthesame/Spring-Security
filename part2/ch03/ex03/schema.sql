CREATE TABLE IF NOT EXISTS spring_users (
  id INT,
  username VARCHAR(45) NULL,
  password VARCHAR(45) NULL,
  enabled INT NOT NULL,
  PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS spring_authorities (
  id INT,
  username VARCHAR(45) NULL,
  authority VARCHAR(45) NULL,
  PRIMARY KEY (id));