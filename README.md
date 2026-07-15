# Smart Election Voting Management System

A JavaFX and MySQL-based election management system featuring:

- User registration and login
- Polling-station administration
- NID registration and vote-cast verification
- Live nationwide vote-cast monitoring
- Polling-station-wise monitoring
- Manual final-result submission
- Valid and invalid vote calculation
- Super Admin and Polling Officer messaging

## Technologies

- Java 24
- JavaFX 21
- Maven
- MySQL
- JDBC

## Database Setup

1. Start MySQL or XAMPP.
2. Open phpMyAdmin.
3. Create a database named `voting_system_db1`.
4. Import `database/voting_system_db1.sql`.

Default database configuration:

- Host: 127.0.0.1
- Port: 3306
- Username: root
- Password: empty

## Run the Project

Run these commands from the project directory:

    .\mvnw.cmd clean compile
    .\mvnw.cmd javafx:run

## Important

The included database contains demonstration data only. Do not use real NID, password, email, or voter information.
