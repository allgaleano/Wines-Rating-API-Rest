package es.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.model.User;
import es.utils.DatabaseUtil;
/*
 * This class is responsible for interacting with the database to perform operations related to followers.
 */
public class FollowersRepository {

    /**
     * Adds a follower to a user.
     *
     * @param followingUserId The ID of the user who is following.
     * @param followedUserId The ID of the user who is being followed.
     * @return 1 if the user was followed, 0 otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public int followUser(int followingUserId, int followedUserId) throws SQLException {
        String sql = "INSERT INTO Followers (FollowingUserId, FollowedUserId) VALUES (?, ?)";

        try (Connection connection = DatabaseUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, followingUserId);
            preparedStatement.setInt(2, followedUserId);
            
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Checks if a user is already following another user.
     *
     * @param followingUserId The ID of the user who is following.
     * @param followedUserId The ID of the user who is being followed.
     * @return true if the user is already following the other user, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean isFollowing(int followingUserId, int followedUserId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM Followers WHERE FollowingUserId = ? AND FollowedUserId = ?";

        try (
            Connection connection = DatabaseUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, followingUserId);
            statement.setInt(2, followedUserId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("total") > 0;
            }
            
        } catch (SQLException e) {
            throw e;
        }
        return false;
    }

    
    /**
     * Retrieves a list of followers for a given user.
     *
     * @param userId the ID of the user
     * @param usernamePattern the pattern to match usernames (can be null or empty)
     * @param page the page number for pagination
     * @param size the number of followers to retrieve per page
     * @return a list of User objects representing the followers
     * @throws SQLException if a database access error occurs
     */
    public List<User> getFollowers (
        int userId, 
        String usernamePattern, 
        int page, 
        int size
    ) throws SQLException {
        
        List<User> followers = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT UserID, Username, DateOfBirth, Email FROM Users WHERE UserID IN (SELECT FollowingUserId FROM Followers WHERE FollowedUserId = ?)");

        if (usernamePattern != null && !usernamePattern.isBlank()) {
            sql.append(" AND Username LIKE ?");
        }
        
        // Paginación
        sql.append(" LIMIT ? OFFSET ?");

        try (Connection connection = DatabaseUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql.toString())) 
        
        {
            int parameterIndex = 1;
            statement.setInt(parameterIndex++, userId);

            if (usernamePattern != null && !usernamePattern.isBlank()) {
                statement.setString(parameterIndex++, "%" + usernamePattern.trim() + "%");
            }

            statement.setInt(parameterIndex++, size);

            // Offset
            statement.setInt(parameterIndex, page * size);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("UserID"));
                user.setUsername(resultSet.getString("Username"));
                user.setDateOfBirth(resultSet.getString("DateOfBirth"));
                user.setEmail(resultSet.getString("Email"));
                followers.add(user);
            }
            
        } catch (SQLException e) {
            throw e;
        }
        return followers;
    }

    /**
     * Retrieves the total number of followers for a given user.
     *
     * @param userId The ID of the user.
     * @param usernamePattern The pattern to match usernames against. Can be null or empty.
     * @return The total number of followers for the user.
     * @throws SQLException If an error occurs while accessing the database.
     */
    public int getTotalFollowers (
        int userId, 
        String usernamePattern
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM Followers WHERE FollowedUserId = ?");

        if (usernamePattern != null && !usernamePattern.isEmpty()) {
            sql.append(" AND FollowingUserId IN (SELECT UserID FROM Users WHERE Username LIKE ?)");
        }

        try (
            Connection connection = DatabaseUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql.toString())
        ) {
            statement.setInt(1, userId);

            if (usernamePattern != null && !usernamePattern.isEmpty()) {
                statement.setString(2, "%" + usernamePattern.trim() + "%");
            }

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        } catch (SQLException e) {
            throw e;
        }
        return 0;
    }

    /**
     * Removes the relationship between a following user and a followed user.
     *
     * @param followingUserId The ID of the user who is following.
     * @param followedUserId The ID of the user who is being followed.
     * @return The number of rows affected by the deletion.
     * @throws SQLException If an error occurs while executing the SQL statement.
     */
    public int unFollowUser (
        int followingUserId, 
        int followedUserId
    ) throws SQLException {
        String sql = "DELETE FROM Followers WHERE FollowingUserId = ? AND FollowedUserId = ?";

        try (Connection connection = DatabaseUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, followingUserId);
            statement.setInt(2, followedUserId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Retrieves the total number of friends for a given user.
     *
     * @param userId the ID of the user
     * @return the total number of friends for the user
     * @throws SQLException if a database access error occurs
     */
    public int getTotalFriends (int userId) throws SQLException {
        String sql = "SELECT COUNT(U.UserID) AS TotalFollowedUsers\n" + 
                        "FROM Users U\n" +
                        "JOIN Followers F ON U.UserID = F.FollowedUserID\n" +
                        "WHERE F.FollowingUserID = ?";

        try (Connection connection = DatabaseUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("TotalFollowedUsers");
            }
        } catch (SQLException e) {
            throw e;
        }
        return 0;
    }
}
