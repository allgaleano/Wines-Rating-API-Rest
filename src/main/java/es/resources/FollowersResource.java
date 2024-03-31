package sos.resources;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sos.model.User;
import sos.model.Wine;
import sos.repository.FollowersRepository;
import sos.repository.UserRepository;
import sos.repository.UserWineRepository;

@Path("/users")
public class FollowersResource {
    
    private UserRepository userRepository = new UserRepository();
    private FollowersRepository followersRepository = new FollowersRepository();
    private UserWineRepository userWineRepository = new UserWineRepository();

    // Añadir un seguidor a un usuario
    @POST
    @Path("/{followingUserId}/follows/{followedUserId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response followUser(
        @PathParam("followingUserId") int followingUserId, 
        @PathParam("followedUserId") int followedUserId
    ) {
        if (followingUserId == followedUserId) {
            return Response.status(Response.Status.CONFLICT).entity("Un usuario no puede seguirse a si mismo").build();
        }
        try {
            User followingUser = userRepository.getUserById(followingUserId);

            if (followingUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("El usuario que sigue no existe").build();
            }

            User followedUser = userRepository.getUserById(followedUserId);

            if (followedUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("El usuario seguido no existe").build();
            }

            if (followersRepository.isFollowing(followingUserId, followedUserId)) {
                return Response.status(Response.Status.CONFLICT).entity(String.format("%s ya sigue a %s", followingUser.getUsername(), followedUser.getUsername())).build();
            }

            int rowsAffected = followersRepository.followUser(followingUserId, followedUserId);

            if (rowsAffected < 1) {
                return Response.status(Response.Status.NOT_FOUND).entity("Error al seguir al usuario").build();
            }

            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    // Obtener los seguidores de un usuario
    @GET
    @Path("/{userId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFollowers(
        @PathParam("userId") int userId,
        @QueryParam("usernamePattern") String usernamePattern,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("10") int size
    ) {
        try {
            // Verificar si el usuario existe antes de obtener sus seguidores
            if (userRepository.getUserById(userId) == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("El usuario no existe").build();
            }

            // Calcular el número de seguidores para saber si se trata de la última página
            int totalFollowers = followersRepository.getTotalFollowers(userId, usernamePattern);
            int totalPages = (int) Math.ceil((double) totalFollowers / size);

            // Si es la última página no se establece el Header Link apuntando a la siguiente página
            if (page >= totalPages - 1 || totalPages == 0) {
                // Obtener la lista de seguidores
                List<User> followers = followersRepository.getFollowers(
                    userId, 
                    usernamePattern, 
                    page, 
                    size
                );

                return Response.ok(followers).build();
            } else {
                // Obtener la lista de seguidores
                List<User> followers = followersRepository.getFollowers(
                    userId, 
                    usernamePattern, 
                    page, 
                    size
                );
                String baseUrl = String.format("http://localhost:8080/vinoteca/api/users/%d/followers", userId);

                // Sumar uno para apuntar a la siguiente página
                int nextPage = page + 1;
                // Codificar el patrón de búsqueda para que pueda ser parte de la URI (URL-encoded eliminará caracteres especiales como espacios en blanco)
                String encodedUsernamePattern = usernamePattern == null ? "" : URLEncoder.encode(usernamePattern, StandardCharsets.UTF_8.toString());
                String nextUri = String.format(
                    "%s?usernamePattern=%s&page=%d&size=%d", 
                    baseUrl,
                    encodedUsernamePattern,
                    nextPage,
                    size
                );

                // Establecer el Header Link apuntando a la siguiente página
                return Response.ok(followers) 
                    .link(nextUri, "nextUri")
                    .build();

            }
        } catch (SQLException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    // Eliminar un seguidor de un usuario
    @DELETE
    @Path("/{followingUserId}/follows/{followedUserId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unFollowUser(
        @PathParam("followingUserId") int followingUserId, 
        @PathParam("followedUserId") int followedUserId
    ) {
        try {
            User followingUser = userRepository.getUserById(followingUserId);

            if (followingUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("El usuario que sigue no existe").build();
            }

            User followedUser = userRepository.getUserById(followedUserId);

            if (followedUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("El usuario seguido no existe").build();
            }

            if (!followersRepository.isFollowing(followingUserId, followedUserId)) {
                return Response.status(Response.Status.CONFLICT).entity(String.format("%s no sigue a %s", followingUser.getUsername(), followedUser.getUsername())).build();
            }

            int rowsAffected = followersRepository.unFollowUser(followingUserId, followedUserId);

            if (rowsAffected < 1) {
                return Response.status(Response.Status.NOT_FOUND).entity("Error al dejar de seguir al usuario").build();
            }

            return Response.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    // Obtener la lista de vinos de un seguidor concreto
    @GET
    @Path("/{userId}/followers/{followerId}/wines")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFollowerWines(
        @PathParam("userId") int userId,
        @PathParam("followerId") int followerId,
        @QueryParam("namePattern") String namePattern,
        @QueryParam("winery") String winery,
        @QueryParam("vintage") String vintage,
        @QueryParam("origin") String origin,
        @QueryParam("type") String type,
        @QueryParam("grape") String grape,
        @QueryParam("rating") String rating,
        @QueryParam("dateAdded") String dateAdded,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("10") int size
    ) {
        try {
            User user = userRepository.getUserById(userId);

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("El usuario %d no existe", userId)).build();
            }

            User followerUser = userRepository.getUserById(followerId);

            if (followerUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(String.format("El usuario %d no existe", followerId)).build();
            }

            if (!followersRepository.isFollowing(followerId, userId)) {
                return Response.status(Response.Status.CONFLICT).entity(String.format("%s no sigue a %s", followerUser.getUsername(), user.getUsername())).build();
            }

            // Calcular el número de vinos para saber si se trata de la última página
            int totalWines = userWineRepository.getTotalWines(followerId, namePattern);
            int totalPages = (int) Math.ceil((double) totalWines / size);
            
            List<Wine> wines = userWineRepository.getWines(
                followerId, 
                namePattern, 
                winery, 
                vintage, 
                origin, 
                type, 
                grape, 
                rating, 
                dateAdded, 
                page, 
                size
            );

            Response.ResponseBuilder response = Response.ok(wines);

            StringBuilder baseUri = new StringBuilder(String.format("http://localhost:8080/vinoteca/api/users/%d/followers/%d/wines?", userId, followerId));

            if (namePattern != null) baseUri.append("namePattern=").append(URLEncoder.encode(namePattern, StandardCharsets.UTF_8.toString())).append("&");
            if (winery != null) baseUri.append("winery=").append(winery).append("&");
            if (vintage != null) baseUri.append("vintage=").append(vintage).append("&");
            if (origin != null) baseUri.append("origin=").append(origin).append("&");
            if (type != null) baseUri.append("type=").append(type).append("&");
            if (grape != null) baseUri.append("grape=").append(grape).append("&");
            if (rating != null) baseUri.append("rating=").append(rating).append("&");
            if (dateAdded != null) baseUri.append("dateAdded=").append(dateAdded).append("&");

            baseUri.append("page=%d&size=%d");

            if (page < totalPages - 1) {
                int nextPage = page + 1;
                response.link(String.format(baseUri.toString(), nextPage, size), "next");
            }

            if (page > 0) {
                int prevPage = page - 1;
                response.link(String.format(baseUri.toString(), prevPage, size), "prev");
            }

            return response.build();

        } catch (SQLException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

}
