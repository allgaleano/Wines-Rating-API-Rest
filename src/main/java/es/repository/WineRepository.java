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

import sos.model.Wine;
import sos.utils.DatabaseUtil;

/**
 * The WineRepository class is responsible for interacting with the database to perform CRUD operations on Wine objects.
 * It provides methods for adding, retrieving, updating, and deleting wines from the database.
 */
public class WineRepository {

    /**
     * Adds a new wine to the database.
     *
     * @param wine the Wine object representing the wine to be added
     * @throws SQLException if there is an error executing the SQL statement
     */
	public void addWine(Wine wine) throws SQLException {
	    String sql = "INSERT INTO Wines (Name, Winery, Origin, Type, Grapes, Vintage, Incorporation) VALUES (?, ?, ?, ?, ?, ?, ?)";

	    try (Connection conn = DatabaseUtil.getConnection();
	         PreparedStatement statement = conn.prepareStatement(sql);
	    ) {
            
	        statement.setString(1, wine.getName());
	        statement.setString(2, wine.getWinery());
	        statement.setString(3, wine.getOrigin());
	        statement.setString(4, wine.getType());
	        statement.setString(5, String.join(",", wine.getGrapes()));
	        statement.setShort(6, wine.getVintage());

            Timestamp incorporation = Timestamp.valueOf(LocalDateTime.now());
	        statement.setTimestamp(7, incorporation);

	        statement.executeUpdate();

	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw e;
	    }
	}

    /**
     * Retrieves a list of wines based on the provided search filters.
     *
     * @param namePattern    The pattern to match against the wine name. Can be null or empty to ignore this filter.
     * @param winery         The winery to match against. Can be null or empty to ignore this filter.
     * @param vintage        The vintage year to match against. Can be null or less than or equal to 0 to ignore this filter.
     * @param origin         The origin to match against. Can be null or empty to ignore this filter.
     * @param type           The wine type to match against. Can be null or empty to ignore this filter.
     * @param grape          The grape to match against. Can be null or empty to ignore this filter.
     * @param incorporation The date of incorporation to match against. Can be null to ignore this filter.
     * @param page           The page number for pagination. Must be a non-negative integer.
     * @param size           The number of wines to retrieve per page. Must be a positive integer.
     * @return A list of Wine objects that match the provided search filters.
     * @throws SQLException If there is an error executing the SQL query.
     */
	public List<Wine> getWines(
        String namePattern, 
        String winery, 
        String vintage, 
        String origin, 
        String type, 
        String grape, 
        String incorporation,
        int page,
        int size
    ) throws SQLException {

        List<Wine> wines = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT WineID ,Name, Winery, Vintage, Origin, Type, Grapes, Incorporation FROM Wines WHERE 1=1");

        Short vintageShort = null;
        if (vintage != null && !vintage.isBlank()) {
            try {
                vintageShort = Short.parseShort(vintage);
            } catch (NumberFormatException e) {
                throw new SQLException("El año de la cosecha debe ser un número entero.");
            }
        }

        // Verificar si se ha proporcionado algún filtro de búsqueda
        if (namePattern != null && !namePattern.isBlank()) {
            sql.append(" AND Name LIKE ?");
        }

        if (winery != null && !winery.isBlank()) {
            sql.append(" AND Winery LIKE ?");
        }

        if (vintageShort != null && vintageShort >= 0) {
            sql.append(" AND Vintage = ?");
        }

        if (origin != null && !origin.isBlank()) {
            sql.append(" AND Origin LIKE ?");
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND Type LIKE ?");
        }

        if (grape != null && !grape.isBlank()) {
            sql.append(" AND Grapes REGEXP CONCAT('(^|,)', '.*', ?, '.*', '($|,)')");
        }
        
        if (incorporation != null && !incorporation.isBlank()) {
            if (!isDateFormat(incorporation)) {
                throw new SQLException("La fecha de incorporación no tiene el formato correcto. Debe ser yyyy-MM-dd.");
            }
            sql.append(" AND DATE(Incorporation) = DATE(?)");
        }
            // Ordenar por fecha de adición de forma descendente
        sql.append(" ORDER BY Incorporation DESC");
        

        // Agregar paginación
        sql.append(" LIMIT ? OFFSET ?");


        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql.toString());
        ) {
            int parameterIndex = 1;

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
            if (incorporation != null && !incorporation.isBlank()) {
                statement.setDate(parameterIndex++, stringToDate(incorporation));
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
                    wine.setIncorporation(resultSet.getTimestamp("Incorporation"));
                    wines.add(wine);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return wines;
    }

    /**
     * Converts a string representation of a date to a Date object.
     *
     * @param dateStr the string representation of the date in the format "yyyy-MM-dd"
     * @return the Date object representing the given date string
     */
    private Date stringToDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateStr, formatter);
        return Date.valueOf(date);
    }

    /**
     * Checks if the given string represents a valid date format.
     *
     * @param dateStr the string to be checked
     * @return true if the string represents a valid date format, false otherwise
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
     * Retrieves the total number of wines in the database that match the given name pattern.
     *
     * @param namePattern the pattern to match against wine names (can be null or empty)
     * @return the total number of wines that match the name pattern
     * @throws SQLException if a database access error occurs
     */
    public int getTotalWines(
        String namePattern,
        String winery,
        String vintage,
        String origin,
        String type,
        String grape,
        String incorporation
    ) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM Wines WHERE 1=1");
        
        Short vintageShort = null;
        if (vintage != null && !vintage.isBlank()) {
            vintageShort = Short.parseShort(vintage);
        }

        if (namePattern != null && !namePattern.isBlank()) {
            sql.append(" AND Name LIKE ?");
        }

        if (winery != null && !winery.isBlank()) {
            sql.append(" AND Winery LIKE ?");
        }

        if (vintageShort != null && vintageShort >= 0) {
            sql.append(" AND Vintage = ?");
        }

        if (origin != null && !origin.isBlank()) {
            sql.append(" AND Origin LIKE ?");
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND Type LIKE ?");
        }

        if (grape != null && !grape.isBlank()) {
            sql.append(" AND Grapes REGEXP CONCAT('(^|,)', '.*', ?, '.*', '($|,)')");
        }

        if (incorporation != null && !incorporation.isBlank()) {
            if (!isDateFormat(incorporation)) {
                throw new SQLException("La fecha de incorporación no tiene el formato correcto. Debe ser yyyy-MM-dd.");
            }
            sql.append(" AND DATE(Incorporation) = DATE(?)");
        }

        try (
            Connection conn = DatabaseUtil.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql.toString());
        ) {
            int parameterIndex = 1;
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

            if (incorporation != null && !incorporation.isBlank()) {
                statement.setDate(parameterIndex++, stringToDate(incorporation));
            }

            try (
                ResultSet resultSet = statement.executeQuery();
            ){
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return 0;
    }

    /**
    * Retrieves a Wine object by its ID from the database.
    *
    * @param id the ID of the wine to retrieve
    * @return the Wine object with the specified ID, or null if not found
    * @throws SQLException if there is an error executing the SQL query
    */
    public Wine getWineById(int id) throws SQLException {
        Wine wine = null;
        String sql = "SELECT * FROM Wines WHERE WineID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                wine = new Wine();
                wine.setId(resultSet.getInt("WineID"));
                wine.setName(resultSet.getString("Name"));
                wine.setWinery(resultSet.getString("Winery"));
                wine.setVintage(resultSet.getShort("Vintage"));
                wine.setOrigin(resultSet.getString("Origin"));
                wine.setType(resultSet.getString("Type"));

                String grapesStr = resultSet.getString("Grapes");
                wine.setGrapes(grapesStr != null && !grapesStr.isBlank() ? Arrays.asList(grapesStr.split(",")) : new ArrayList<>());
                wine.setIncorporation(resultSet.getTimestamp("Incorporation"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return wine;
    }

    /**
    * Retrieves a Wine object from the database based on the given name.
    *
    * @param name the name of the wine to retrieve
    * @return the Wine object with the matching name, or null if not found
    * @throws SQLException if there is an error executing the SQL query
    */
    public Wine getWineByName(String name) throws SQLException {
        Wine wine = null;
        String sql = "SELECT * FROM Wines WHERE Name = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                wine = new Wine();
                wine.setId(resultSet.getInt("WineID"));
                wine.setName(resultSet.getString("Name"));
                wine.setWinery(resultSet.getString("Winery"));
                wine.setVintage(resultSet.getShort("Vintage"));
                wine.setOrigin(resultSet.getString("Origin"));
                wine.setType(resultSet.getString("Type"));

                String grapesStr = resultSet.getString("Grapes");
                wine.setGrapes(grapesStr != null && !grapesStr.isBlank() ? Arrays.asList(grapesStr.split(",")) : new ArrayList<>());
                wine.setIncorporation(resultSet.getTimestamp("Incorporation"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return wine;
    }

    /**
     * Updates a wine in the database based on the provided Wine object.
     * Only the non-null and non-blank fields of the Wine object will be updated.
     *
     * @param wine The Wine object containing the updated information.
     * @return The number of rows affected in the database.
     * @throws SQLException If an error occurs while updating the wine in the database.
     */
    public int updateWineById(Wine wine) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE Wines SET ");
       
        int fieldsToUpdate = 0;

        if (wine.getName() != null && !wine.getName().isBlank()) {

            sql.append("Name = ?");
            fieldsToUpdate++;
        }

        if (wine.getWinery() != null && !wine.getWinery().isBlank()) {

            if (fieldsToUpdate > 0) sql.append(", "); 

            sql.append("Winery = ?");
            fieldsToUpdate++;
        }

        if (wine.getVintage() != null && wine.getVintage() > 0) {

            if (fieldsToUpdate > 0) sql.append(", ");
            
            sql.append("Vintage = ?");
            fieldsToUpdate++;
        }

        if (wine.getOrigin() != null && !wine.getOrigin().isBlank()) {
            
            if (fieldsToUpdate > 0) sql.append(", ");
            
            sql.append("Origin = ?");
            fieldsToUpdate++;
        }

        if (wine.getType() != null && !wine.getType().isBlank()) {
            
            if (fieldsToUpdate > 0) sql.append(", ");
            
            sql.append("Type = ?");
            fieldsToUpdate++;
        }

        if (wine.getGrapes() != null && !wine.getGrapes().isEmpty()) {

            if (fieldsToUpdate > 0) sql.append(", ");

            sql.append("Grapes = ?");
            fieldsToUpdate++;
        }

        sql.append(" WHERE WineID = ?");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql.toString());
        ) {
            int parameterIndex = 1;

            if (wine.getName() != null && !wine.getName().isBlank()) {
                statement.setString(parameterIndex++, wine.getName());
            }

            if (wine.getWinery() != null && !wine.getWinery().isBlank()) {
                statement.setString(parameterIndex++, wine.getWinery());
            }

            if (wine.getVintage() != null && wine.getVintage() > 0) {
                statement.setShort(parameterIndex++, wine.getVintage());
            }

            if (wine.getOrigin() != null && !wine.getOrigin().isBlank()) {
                statement.setString(parameterIndex++, wine.getOrigin());
            }

            if (wine.getType() != null && !wine.getType().isBlank()) {
                statement.setString(parameterIndex++, wine.getType());
            }

            if (wine.getGrapes() != null && !wine.getGrapes().isEmpty()) {
                statement.setString(parameterIndex++, String.join(",", wine.getGrapes()));
            }
            
            statement.setInt(parameterIndex, wine.getId());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Deletes a wine from the database based on the specified ID.
     *
     * @param id the ID of the wine to be deleted
     * @return the number of rows deleted from the database
     * @throws SQLException if an error occurs while deleting the wine
     */
    public int deleteWine(int id) throws SQLException {
        String sql = "DELETE FROM Wines WHERE WineID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();
            
            return rowsDeleted;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
}

