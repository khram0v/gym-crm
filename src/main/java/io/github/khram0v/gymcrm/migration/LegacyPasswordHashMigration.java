package io.github.khram0v.gymcrm.migration;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LegacyPasswordHashMigration implements CustomTaskChange {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[ayb]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void execute(Database database) throws CustomChangeException {
        Connection connection = database.getConnection().getUnderlyingConnection();
        Map<Long, String> legacyPasswords = readLegacyPasswords(connection);

        if (legacyPasswords.isEmpty()) {
            return;
        }

        rehashAndUpdate(connection, legacyPasswords);
    }

    private Map<Long, String> readLegacyPasswords(Connection connection) throws CustomChangeException {
        Map<Long, String> legacyPasswords = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, password FROM users")) {
            while (resultSet.next()) {
                String password = resultSet.getString("password");
                if (password != null && !BCRYPT_PATTERN.matcher(password).matches()) {
                    legacyPasswords.put(resultSet.getLong("id"), password);
                }
            }
        } catch (SQLException e) {
            throw new CustomChangeException("Failed to read legacy passwords", e);
        }
        return legacyPasswords;
    }

    private void rehashAndUpdate(Connection connection, Map<Long, String> legacyPasswords)
            throws CustomChangeException {
        try (PreparedStatement statement =
                     connection.prepareStatement("UPDATE users SET password = ? WHERE id = ?")) {
            for (Map.Entry<Long, String> entry : legacyPasswords.entrySet()) {
                statement.setString(1, passwordEncoder.encode(entry.getValue()));
                statement.setLong(2, entry.getKey());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new CustomChangeException("Failed to hash legacy passwords", e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Legacy plaintext passwords hashed with BCrypt";
    }

    @Override
    public void setUp() throws SetupException {
        // no setup required
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // resource access not required
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}
