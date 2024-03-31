package sos.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sos.model.FriendWines;
import sos.model.User;
import sos.model.UserWine;
import sos.model.Wine;
import sos.utils.DatabaseUtil;

public class UserWineRepository {

    /**
     * Adds a new user wine to the database.
     * 
     * @param userWine the UserWine object to be added
     * @return the number of rows affected (1 if successful, 0 otherwise)
     * @throws SQLException if a database access error occurs
     */
    public int addUserWine(UserWine userWine) throws SQLException {
        String sql = "INSERT INTO UserWines (UserID, WineID, Rating, DateAdded) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, userWine.getUserId());
            statement.setInt(2, userWine.getWineId());
            statement.setShort(3, userWine.getRating());

            Timestamp dateAdded = Timestamp.valueOf(LocalDateTime.now());
            statement.setTimestamp(4, dateAdded);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Checks if a wine is already added by a user.
     * 
     * @param userId the ID of the user
     * @param wineId the ID of the wine
     * @return true if the wine is already added by the user, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isWineAlreadyAdded(int userId, int wineId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM UserWines WHERE UserID = ? AND WineID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, userId);
            statement.setInt(2, wineId);
            
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
     * Gets the wines added by a user.
     * 
     * @param userId the ID of the user
     * @param namePattern the pattern to match the name of the wine
     * @param winery the winery of the wine
     * @param vintage the vintage of the wine
     * @param origin the origin of the wine
     * @param type the type of the wine
     * @param grape the grape of the wine
     * @param rating the rating of the wine
     * @param dateAdded the date the wine was added
     * @param page the page number
     * @param size the number of wines per page
     * @return a list of wines added by the user
     * @throws SQLException if a database access error occurs
     */
    public List<Wine> getWines(
        int userId,
        String namePattern, 
        String winery, 
        String vintage, 
        String origin, 
        String type, 
        String grape, 
        String rating,
        String dateAdded,
        int page,
        int size
    ) throws SQLException { 

        List<Wine> wines = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT W.WineID, W.Name, W.Winery, W.Vintage, W.Origin, W.Type, W.Grapes, U.Rating, U.DateAdded\n" + //
                        "FROM Wines W\n" + //
                        "JOIN UserWines U ON W.WineID = U.WineID\n" + //
                        "WHERE U.UserID = ?");

        Short vintageShort = null;
        if (vintage != null && !vintage.isBlank()) {
            try {
                vintageShort = Short.parseShort(vintage);
            } catch (NumberFormatException e) {
                throw new SQLException("El año de la cosecha debe ser un número entero.");
            }
        }
        Short ratingShort = null;
        if (rating != null && !rating.isBlank()) {
            try {
                ratingShort = Short.parseShort(rating);
            } catch (NumberFormatException e) {
                throw new SQLException("La puntuación debe ser un número entero.");
            }
        }

        // Verificar si se ha proporcionado algún filtro de búsqueda
        if (namePattern != null && !namePattern.isBlank()) {
            sql.append(" AND W.Name LIKE ?");
        }

        if (winery != null && !winery.isBlank()) {
            sql.append(" AND W.Winery LIKE ?");
        }

        if (vintageShort != null && vintageShort > 0) {
            sql.append(" AND W.Vintage = ?");
        }

        if (origin != null && !origin.isBlank()) {
            sql.append(" AND W.Origin LIKE ?");
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND W.Type LIKE ?");
        }

        if (grape != null && !grape.isBlank()) {
            sql.append(" AND W.Grapes REGEXP CONCAT('(^|,)', '.*', ?, '.*', '($|,)')");
        }
        
        
        if (ratingShort != null) {
            if (ratingShort < 0 || ratingShort > 10) {
                throw new SQLException("La puntuación debe estar entre 0 y 5.");
            }
            sql.append(" AND U.Rating = ?");
        }
        
        if (dateAdded != null) {
            
            if (!isDateFormat(dateAdded)) {
                throw new SQLException("La fecha en la que se añadió el vino no tiene el formato correcto. Debe ser yyyy-MM-dd.");
            }
            
            sql.append(" AND U.DateAdded = ?");
        } else {
            // Ordenar por fecha de adición de forma descendente
            sql.append(" ORDER BY U.DateAdded DESC");
        }

        // Agregar paginación
        sql.append(" LIMIT ? OFFSET ?");


        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql.toString());
        ) {
            int parameterIndex = 1;
            statement.setInt(parameterIndex++, userId);

            if (namePattern != null && !namePattern.isBlank()) {
                statement.setString(parameterIndex++, "%" + namePattern.trim() + "%");
            }
            if (winery != null && !winery.isBlank()) {
                statement.setString(parameterIndex++, "%" + winery.trim() + "%");
            }
            if (vintageShort != null && vintageShort >= 0) {
                statement.setShort(parameterIndex++, vintageShort);
            }
            if (origin != null && !origin.isBlank()) {
                statement.setString(parameterIndex++, "%" + origin.trim() + "%");
            }
            if (type != null && !type.isBlank()) {
                statement.setString(parameterIndex++, "%" + type.trim() + "%");
            }
            if (grape != null && !grape.isBlank()) {
                statement.setString(parameterIndex++, grape.trim());
            }
            if (ratingShort != null) {
                statement.setShort(parameterIndex++, ratingShort);
            }
            if (dateAdded != null) {
                statement.setDate(parameterIndex++, stringToDate(dateAdded));
            }

            statement.setInt(parameterIndex++, size);

            int offset = page * size;
            statement.setInt(parameterIndex, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Wine wine = new Wine();
                    wine.setId(resultSet.getInt("WineID"));
                    wine.setName(resultSet.getString("Name"));
                    wine.setWinery(resultSet.getString("Winery"));
                    wine.setVintage(resultSet.getShort("Vintage"));
                    wine.setOrigin(resultSet.getString("Origin"));
                    wine.setType(resultSet.getString("Type"));

                    String grapesStr = resultSet.getString("Grapes");
                    wine.setGrapes(grapesStr != null && !grapesStr.isBlank() ? Arrays.asList(grapesStr.split(",")) : new ArrayList<>());
                    wine.setRating(resultSet.getShort("Rating"));
                    wine.setDateAdded(resultSet.getTimestamp("DateAdded"));
                    wines.add(wine);
                }
            }
        } catch (SQLException e) {
            throw e;
        }
        return wines;
    }

    /**
     * Converts a string to a Date object.
     * @param dateStr
     * @return
     */
    private Date stringToDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateStr, formatter);
        return Date.valueOf(date);
    }

    /**
     * Checks if a string has a valid date format.
     * @param dateStr
     * @return
     */
    private boolean isDateFormat(String dateStr) {
        try {
            stringToDate(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retrieves the total number of wines for a given user ID and name pattern.
     *
     * @param userId       the ID of the user
     * @param namePattern  the pattern to match against wine names (can be null or empty)
     * @return the total number of wines matching the criteria
     * @throws SQLException if a database access error occurs
     */
    public int getTotalWines(int userId, String namePattern) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM UserWines UW JOIN Wines W ON UW.WineID = W.WineID WHERE UW.UserID = ?";

        if (namePattern != null && !namePattern.isBlank()) {
            sql += " AND W.Name LIKE ?";
        }

        try (
            Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, userId);

            if (namePattern != null && !namePattern.isBlank()) {
                statement.setString(2, "%" + namePattern.trim() + "%");
            }

            try (
                ResultSet resultSet = statement.executeQuery();
            ){
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
     * Gets a wine added by a user by its ID.
     * @param userId
     * @param wineId
     * @return
     * @throws SQLException
     */
    public Wine getUserWineById(int userId, int wineId) throws SQLException{
        Wine wine = null;
        String sql = "SELECT W.Name, W.Winery, W.Vintage, W.Origin, W.Type, W.Grapes, U.Rating, U.DateAdded\n" + 
                        "FROM Wines W\n" + 
                        "JOIN UserWines U ON W.WineID = U.WineID\n" + 
                        "WHERE U.UserID = ? AND U.WineID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, userId);
            statement.setInt(2, wineId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    wine = new Wine();
                    wine.setName(resultSet.getString("Name"));
                    wine.setWinery(resultSet.getString("Winery"));
                    wine.setVintage(resultSet.getShort("Vintage"));
                    wine.setOrigin(resultSet.getString("Origin"));
                    wine.setType(resultSet.getString("Type"));

                    String grapesStr = resultSet.getString("Grapes");

                    wine.setGrapes(
                        grapesStr != null && !grapesStr.isBlank() 
                        ? Arrays.asList(grapesStr.split(",")) 
                        : new ArrayList<>()
                    );
                    
                    wine.setRating(resultSet.getShort("Rating"));
                    wine.setDateAdded(resultSet.getTimestamp("DateAdded"));
                }
            }
        } catch (SQLException e) {
            throw e;
        }
        return wine;
    
    }


    /**
     * Updates a wine added by a user by its ID.
     * @param userId
     * @param wineId
     * @param rating
     * @return the number of rows affected (1 if successful, 0 otherwise)
     * @throws SQLException
     */
    public int updateUserWineById (int userId, int wineId, Short rating) throws SQLException {
        String sql = "UPDATE UserWines SET Rating = ? WHERE UserID = ? AND WineID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setShort(1, rating);
            statement.setInt(2, userId);
            statement.setInt(3, wineId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            throw e;
        }
    }


    /**
     * Deletes a wine added by a user by its ID.
     * 
     * @param userId
     * @param wineId
     * @return the number of rows affected (1 if successful, 0 otherwise)
     * @throws SQLException
     */
    public int deleteUserWineById (int userId, int wineId) throws SQLException {
        String sql = "DELETE FROM UserWines WHERE UserID = ? AND WineID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, userId);
            statement.setInt(2, wineId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Retrieves the last added wines for a specific user.
     *
     * @param userId         the ID of the user
     * @param numberOfWines  the number of wines to retrieve
     * @return an array of Wine objects representing the last added wines
     * @throws SQLException if a database access error occurs
     */
    public Wine[] getLastAddedWines(int userId, int numberOfWines) throws SQLException {
        String sql = "SELECT W.WineID, W.Name, W.Winery, W.Vintage, W.Origin, W.Type, W.Grapes, U.Rating, U.DateAdded\n" + //
                        "FROM Wines W\n" + //
                        "JOIN UserWines U ON W.WineID = U.WineID\n" + //
                        "WHERE U.UserID = ?\n" + //
                        "ORDER BY U.DateAdded DESC\n" + //
                        "LIMIT ?";
        
        Wine[] wines = new Wine[numberOfWines];

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, userId);
            statement.setInt(2, numberOfWines);

            ResultSet resultSet = statement.executeQuery();

            int i = 0;
            while (resultSet.next()) {
                Wine wine = new Wine();
                wine.setId(resultSet.getInt("WineID"));
                wine.setName(resultSet.getString("Name"));
                wine.setWinery(resultSet.getString("Winery"));
                wine.setVintage(resultSet.getShort("Vintage"));
                wine.setOrigin(resultSet.getString("Origin"));
                wine.setType(resultSet.getString("Type"));

                String grapesStr = resultSet.getString("Grapes");
                wine.setGrapes(grapesStr != null && !grapesStr.isBlank() ? Arrays.asList(grapesStr.split(",")) : new ArrayList<>());
                wine.setRating(resultSet.getShort("Rating"));
                wine.setDateAdded(resultSet.getTimestamp("DateAdded"));
                wines[i++] = wine;
            }
            
        } catch (SQLException e) {
            throw e;
        }
        return wines;
    }

    /**
     * Retrieves the top rated wines for a given user.
     *
     * @param userId The ID of the user.
     * @param numberOfWines The number of top rated wines to retrieve.
     * @return An array of Wine objects representing the top rated wines.
     * @throws SQLException If an error occurs while accessing the database.
     */
    public Wine[] getTopRatedWines(
        int userId, 
        int numberOfWines
    ) throws SQLException {
        String sql = "SELECT W.WineID, W.Name, W.Winery, W.Vintage, W.Origin, W.Type, W.Grapes, U.Rating, U.DateAdded\n" + //
                        "FROM Wines W\n" + //
                        "JOIN UserWines U ON W.WineID = U.WineID\n" + //
                        "WHERE U.UserID = ?\n" + //
                        "ORDER BY U.Rating DESC\n" + //
                        "LIMIT ?";

        Wine[] wines = new Wine[numberOfWines];

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, userId);
            statement.setInt(2, numberOfWines);

            ResultSet resultSet = statement.executeQuery();

            int i = 0;
            while (resultSet.next() && i < numberOfWines) {
                Wine wine = new Wine();
                wine.setId(resultSet.getInt("WineID"));
                wine.setName(resultSet.getString("Name"));
                wine.setWinery(resultSet.getString("Winery"));
                wine.setVintage(resultSet.getShort("Vintage"));
                wine.setOrigin(resultSet.getString("Origin"));
                wine.setType(resultSet.getString("Type"));

                String grapesStr = resultSet.getString("Grapes");
                wine.setGrapes(grapesStr != null && !grapesStr.isBlank() ? Arrays.asList(grapesStr.split(",")) : new ArrayList<>());
                wine.setRating(resultSet.getShort("Rating"));
                wine.setDateAdded(resultSet.getTimestamp("DateAdded"));
                wines[i++] = wine;
            }
            
        } catch (SQLException e) {
            throw e;
        }
        return wines;
    }

    /**
     * Retrieves the top rated wines of a user's friends.
     *
     * @param userId        the ID of the user
     * @param numberOfWines the number of wines to retrieve for each friend
     * @param page          the page number of the results
     * @param size          the number of results per page
     * @return a list of FriendWines objects containing the friend's information and their top rated wines
     * @throws SQLException if a database access error occurs
     */
    public List<FriendWines> getFriendsTopRatedWines (
        int userId, 
        int numberOfWines,
        int page, 
        int size
    ) throws SQLException {
        String sql = "SELECT U.UserID, U.Username, U.DateOfBirth, U.Email\n" + 
                    "FROM Users U\n" +
                    "JOIN Followers F ON U.UserID = F.FollowedUserID\n" + 
                    "WHERE F.FollowingUserID = ?\n"
                    + "LIMIT ? OFFSET ?";

        List<FriendWines> friendsWines = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, userId);
            statement.setInt(2, size);
            statement.setInt(3, page * size);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    FriendWines friendWines = new FriendWines();
                    User user = new User();

                    user.setUserId(resultSet.getInt("UserID"));
                    user.setUsername(resultSet.getString("Username"));
                    user.setDateOfBirth(resultSet.getString("DateOfBirth"));
                    user.setEmail(resultSet.getString("Email"));
                    
                    friendWines.setFriend(user);

                    Wine[] wines = getTopRatedWines(user.getUserId(), numberOfWines);
                    friendWines.setWines(wines);
                    friendsWines.add(friendWines);
                }
            }
        } catch (SQLException e) {
            throw e;
        }
        return friendsWines;
    }

    
}
