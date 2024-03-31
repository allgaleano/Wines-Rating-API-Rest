package sos.resources;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.sql.SQLException;
import java.time.Period;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sos.model.FriendWines;
import sos.model.User;
import sos.model.UserRecommendations;
import sos.model.UserUpdate;
import sos.model.Wine;
import sos.repository.FollowersRepository;
import sos.repository.UserRepository;
import sos.repository.UserWineRepository;

@Path("/users")
public class UserResource {

    private UserRepository userRepository = new UserRepository();
    private UserWineRepository userWineRepository = new UserWineRepository();
    private FollowersRepository followersRepository = new FollowersRepository();

    // Crea un nuevo usuario mayor de edad
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(User user) {
        
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El nombre de usuario es requerido").build();
        }
        
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El correo electrónico es requerido").build();
        }
        
        if (user.getDateOfBirth() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("La fecha de nacimiento es requerida").build();
        }
        
        if (!userRepository.isDateFormat(user.getDateOfBirth())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("La fecha de nacimiento debe tener el formato yyyy-MM-dd").build();
        }
        
        LocalDate dateOfBirth = LocalDate.parse(user.getDateOfBirth());
        
        // Calcular la edad
        LocalDate currentDate = LocalDate.now();
        int age = Period.between(dateOfBirth, currentDate).getYears();
        
        if (age < 18) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El usuario debe de ser mayor de edad para crear una cuenta").build();
        }
        
        try {
            if (userRepository.getUserByUsername(user.getUsername()) != null) {
                return Response.status(Response.Status.CONFLICT).entity("El nombre de usuario ya está en uso").build();
            }
            if (userRepository.getUserByEmail(user.getEmail()) != null) {
                return Response.status(Response.Status.CONFLICT).entity("El correo electrónico ya está en uso").build();
            }

            int rowsAffected = userRepository.addUser(user);

            if (rowsAffected < 1) {
                return Response.status(Response.Status.NOT_FOUND).entity("Error al añadir el usuario").build();
            }

            user.setUserId(userRepository.getUserByEmail(user.getEmail()).getUserId());

            return Response.status(Response.Status.CREATED).entity(user).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    // Devuelve una lista de todos los usuarios
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(
        @QueryParam("usernamePattern") String usernamePattern,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("10") int size
    ) {
      try {
        // Calcula el numero total de usuarios para saber si se trata de la ultima pagina
        int totalUsers = userRepository.getTotalUsers(usernamePattern);
        int totalPages = (int) Math.ceil((double) totalUsers / size);

        List<User> users = userRepository.getUsers(usernamePattern, page, size);
        Response.ResponseBuilder responseBuilder = Response.ok(users);

        StringBuilder baseUri = new StringBuilder("http://localhost:8080/vinoteca/api/users?");

        if (usernamePattern != null) baseUri.append("usernamePattern=").append(URLEncoder.encode(usernamePattern, StandardCharsets.UTF_8.toString())).append("&");

        baseUri.append("page=%d&size=%d");
        // Agregar enlace a la siguiente página si no es la última
        if (page < totalPages - 1) {
            int nextPage = page + 1;
            responseBuilder.link(String.format(baseUri.toString(), nextPage, size), "next");
        }

        // Agregar enlace a la página anterior si no es la primera
        if (page > 0 && page < totalPages) {
            int prevPage = page - 1;
            responseBuilder.link(String.format(baseUri.toString(), prevPage, size), "prev");
        }

        return responseBuilder.build();
      } catch (SQLException | UnsupportedEncodingException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
    }

    // Devuelve un usuario por ID
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("id") int id) {
        try {
            // Verificar si el usuario existe antes de intentar obtenerlo
            User user = userRepository.getUserById(id); 

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
            }

            return Response.ok(user).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener usuario por ID").build();
        }
    }
    
    // Actualiza valores de un usuario por ID
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserById(
        @PathParam("id") int id, 
        UserUpdate userUpdate
    ) {
        try {
            // Verificar si el usuario existe antes de intentar actualizarlo
            User existingUser = userRepository.getUserById(id);
            if (existingUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
            }
            // Actualizar los campos del usuario con los datos del usuario actualizado
            if (userUpdate.getUsername() != null && !userUpdate.getUsername().isBlank()) {
                existingUser.setUsername(userUpdate.getUsername());
            }

            if (userUpdate.getDateOfBirth() != null) {
                if (!userRepository.isDateFormat(userUpdate.getDateOfBirth())) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("La fecha de nacimiento debe tener el formato yyyy-MM-dd").build();
                }

                LocalDate dateOfBirth = LocalDate.parse(userUpdate.getDateOfBirth());
                LocalDate currentDate = LocalDate.now();
                int age = Period.between(dateOfBirth, currentDate).getYears();
                if (age < 18) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("El usuario debe de ser mayor de edad").build();
                }

                existingUser.setDateOfBirth(userUpdate.getDateOfBirth());
            }

            if (userUpdate.getEmail() != null && !userUpdate.getEmail().isBlank()) {
                existingUser.setEmail(userUpdate.getEmail());
            }

            // Llamar al método para actualizar el usuario en UserRepository
            
            int rowsAffected = userRepository.updateUserById(id, userUpdate);
            if (rowsAffected < 1) {
                return Response.status(Response.Status.NOT_FOUND).entity("Error al actualizar el usuario").build();
            }
            
            // Retornar una respuesta con el usuario actualizado
            return Response.ok(existingUser).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    // Borra a un usuario por ID
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUsuario(@PathParam("id") int id) {
        try {
            
            User user = userRepository.getUserById(id);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
            }
            
            int rowsAffected = userRepository.deleteUser(id);

            if (rowsAffected < 1) {
                return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
            }
            return Response.ok(user).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    /**
     * sistema de recomendaciones
        personalizado para un usuario. Esta operación debe devolver toda la información
        del usuario (datos personales), un listado con sus 5 últimos vinos añadidos, un
        listado con sus 5 vinos con mayor puntuación y otro listado con los 5 mejores
        vinos de todos sus amigos
     */
    @GET
    @Path("/{userId}/recommendations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecommendations(
        @PathParam("userId") int userId,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("5") int size
    ) {
        try {
            User user = userRepository.getUserById(userId);

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
            }

            UserRecommendations userRecommendations = new UserRecommendations();
            userRecommendations.setUserInfo(user);

            // Obtener los 5 últimos vinos añadidos por el usuario
            Wine[] lastAddedWines = userWineRepository.getLastAddedWines(userId, 5);
            userRecommendations.setLastAddedWines(lastAddedWines);

            // Obtener los 5 vinos con mayor puntuación del usuario
            Wine[] topRatedWines = userWineRepository.getTopRatedWines(userId, 5);
            userRecommendations.setTopRatedWines(topRatedWines);

            
            // Obtener los 5 mejores vinos de todos los amigos del usuario
            List<FriendWines> friendsTopRatedWines = userWineRepository.getFriendsTopRatedWines(userId, 5, page, size);
            userRecommendations.setFriendsWines(friendsTopRatedWines);
            
            String baseUri = String.format("http://localhost:8080/vinoteca/api/users/%d/recommendations", userId);
            
            int totalFriends = followersRepository.getTotalFriends(userId);
            int totalPages = (int) Math.ceil((double) totalFriends / size);

            Response.ResponseBuilder response = Response.ok(userRecommendations);

            if (page < totalPages - 1) {
                int nextPage = page + 1;
                String nextUri = String.format("%s?page=%d&size=%d", baseUri, nextPage, size);
                response.link(nextUri, "next").build();
            }

            if (page > 0) {
                int prevPage = page - 1;
                String prevUri = String.format("%s?page=%d&size=%d", baseUri, prevPage, size);
                response.link(prevUri, "prev").build();
            }

            return response.build();

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
} 
