package es.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.model.User;
import es.model.UserUpdate;
import es.utils.DatabaseUtil;
/*
 * This class is responsible for interacting with the database to perform operations related to users.
 */
public class UserRepository {

    /**
     * Adds a new user to the database.
     *
     * @param user the user object to be added
     * @return the number of rows affected in the database
     * @throws SQLException if an error occurs while executing the SQL statement
     */
    public int addUser(
        User user
    ) throws SQLException {
        
        String sql = "INSERT INTO Users (Username, DateOfBirth, Email) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setString(1, user.getUsername());
            statement.setObject(2, user.getDateOfBirth());
            statement.setString(3, user.getEmail());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            throw e;
        }
    } 
  
    public boolean isDateFormat(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
  
    /**
     * Retrieves a list of users based on the provided username pattern, page number, and page size.
     *
     * @param usernamePattern The pattern to match against the usernames. Can be null or empty to retrieve all users.
     * @param page            The page number of the results to retrieve.
     * @param size            The number of results per page.
     * @return A list of User objects matching the provided criteria.
     * @throws SQLException If an error occurs while executing the SQL query.
     */
    public List<User> getUsers(
        String usernamePattern, 
        int page, 
        int size
    ) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT UserID, Username, DateOfBirth, Email FROM Users";
        
        // Verificar si se proporciona un patrón de nombre de usuario
        if (usernamePattern != null && !usernamePattern.isBlank()) {
            sql += " WHERE Username LIKE ?";
        }
        // Agregar paginación
        sql += " LIMIT ? OFFSET ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            int parameterIndex = 1;
            // Establecer el patrón de nombre de usuario si se proporciona
            if (usernamePattern != null && !usernamePattern.isBlank()) {
                statement.setString(parameterIndex++, "%" + usernamePattern.trim() + "%");
            }
            
            // Calcular el offset basado en la página y el tamaño de la página
            int offset = page * size;
            
            statement.setInt(parameterIndex++, size);
            statement.setInt(parameterIndex, offset);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    User user = new User();
                    user.setUserId(resultSet.getInt("UserID"));
                    user.setUsername(resultSet.getString("Username"));
                    user.setDateOfBirth(resultSet.getString("DateOfBirth"));
                    user.setEmail(resultSet.getString("Email"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            throw e;
        }
        return users;
    }
  
    /**
     * Retrieves the total number of users matching the given username pattern.
     *
     * @param usernamePattern the pattern to match usernames against
     * @return the total number of users matching the pattern
     * @throws SQLException if a database access error occurs
     */
    public int getTotalUsers(String usernamePattern) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM Users";
        
        if (usernamePattern != null && !usernamePattern.isBlank()) {
            sql += " WHERE Username LIKE ?";
        }
        
        try (
            Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
            ) {
                if (usernamePattern != null && !usernamePattern.isBlank()) {
                    statement.setString(1, "%" + usernamePattern.trim() + "%");
            }
            
            try (
                ResultSet resultSet = statement.executeQuery();
                ) {
                    if (resultSet.next()) {
                        return resultSet.getInt("total");
            }
            }

        } catch (SQLException e) {
            throw e;
        }
        return 0;
    }

    /**
     * Retrieves a user in the system.
     * @param id The ID of the user to retrieve.
     * @return The user object if found, null otherwise.
     * @throws SQLException If an error occurs while executing the SQL query.
     */
    public User getUserById(int id) throws SQLException {
        User user = null;
        String sql = "SELECT * FROM Users WHERE UserID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, id);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                user = new User();
                user.setUserId(resultSet.getInt("UserID"));
                user.setUsername(resultSet.getString("Username"));
                user.setDateOfBirth(resultSet.getString("DateOfBirth"));
                user.setEmail(resultSet.getString("Email"));
            }  
            
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

    /**
     * Retrieves the user ID associated with the given email.
     *
     * @param email the email of the user
     * @return the user ID if found, or -1 if not found
     * @throws SQLException if a database access error occurs
     */
    public User getUserByEmail(String email) throws SQLException {
        User user = null;
        String sql = "SELECT * FROM Users WHERE Email = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setString(1, email);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                user = new User();
                user.setUserId(resultSet.getInt("UserID"));
                user.setUsername(resultSet.getString("Username"));
                user.setDateOfBirth(resultSet.getString("DateOfBirth"));
                user.setEmail(resultSet.getString("Email"));
            }  
            
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

    public User getUserByUsername(String username) throws SQLException {
        User user = null;
        String sql = "SELECT * FROM Users WHERE Username = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setString(1, username);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                user = new User();
                user.setUserId(resultSet.getInt("UserID"));
                user.setUsername(resultSet.getString("Username"));
                user.setDateOfBirth(resultSet.getString("DateOfBirth"));
                user.setEmail(resultSet.getString("Email"));
            }  
            
        } catch (SQLException e) {
            throw e;
        }
        return user;
    }

    /**
     * Updates a user in the database by their ID.
     *
     * @param userId The ID of the user to update.
     * @param userUpdate The object containing the updated user information.
     * @return The number of rows affected by the update operation.
     * @throws SQLException If an error occurs while updating the user.
     */
    public int updateUserById(int id, UserUpdate userUpdate) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE Users SET ");
        
        int fieldsToUpdate = 0;
        if (userUpdate.getUsername() != null && !userUpdate.getUsername().isBlank()) {
            
            sql.append("Username = ?");
            fieldsToUpdate++;
        }

        if (userUpdate.getDateOfBirth() != null) {
            if (fieldsToUpdate > 0) {
                sql.append(", ");
            }
            sql.append("DateOfBirth = ?");
            fieldsToUpdate++;
        }

        if (userUpdate.getEmail() != null && !userUpdate.getEmail().isBlank()) {
            if (fieldsToUpdate > 0) {
                sql.append(", ");
            }
            sql.append("Email = ?");
            fieldsToUpdate++;
        }

        if (fieldsToUpdate == 0) {
            throw new SQLException("No se proporcionaron campos para actualizar.");
        }

        sql.append(" WHERE UserID = ?");

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql.toString());
        ) {
            int parameterIndex = 1;
            if (userUpdate.getUsername() != null && !userUpdate.getUsername().isBlank()) {
                statement.setString(parameterIndex++, userUpdate.getUsername());
            }

            if (userUpdate.getDateOfBirth() != null) {
                statement.setString(parameterIndex++, userUpdate.getDateOfBirth());
            }

            if (userUpdate.getEmail() != null && !userUpdate.getEmail().isBlank()) {
                statement.setString(parameterIndex++, userUpdate.getEmail());
            }

            statement.setInt(parameterIndex, id);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            throw e;
        }
    }
  
    /**
     * Deletes a user from the database based on the provided user ID.
     *
     * @param id The ID of the user to be deleted.
     * @return The number of rows affected by the deletion operation.
     * @throws SQLException If an error occurs while executing the SQL statement.
     */
    public int deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM Users WHERE UserID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();

            return rowsAffected;
        } catch (SQLException e) {
            throw e;
        }
    }
}
