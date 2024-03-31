package es.resources;

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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import es.model.RatingUpdate;
import es.model.User;
import es.model.UserWine;
import es.model.Wine;
import es.repository.UserRepository;
import es.repository.UserWineRepository;
import es.repository.WineRepository;

@Path("/users")
public class UserWineResource {

    private UserRepository userRepository = new UserRepository();
    private WineRepository wineRepository = new WineRepository();
    private UserWineRepository userWineRepository = new UserWineRepository();

    
    @POST
    @Path("/{userId}/wines")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUserWine(
        @PathParam("userId") int userId, 
        UserWine userWine
    ) {
        try {
            User user = userRepository.getUserById(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
            }

            Wine wine = wineRepository.getWineById(userWine.getWineId());
            if (wine == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Vino no encontrado").build();
            }
            if (userWineRepository.isWineAlreadyAdded(userId, userWine.getWineId())) {
                return Response.status(Response.Status.BAD_REQUEST).entity(String.format("El vino con id: %d ya se encuentra en la lista de vinos puntuados del usuario", userWine.getWineId())).build();
            }
            userWine.setUserId(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        if (userWine.getWineId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El ID del vino es requerido").build();
        }

        if (userWine.getWineId() < 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El ID del vino debe ser un número positivo").build();
        }

        if (userWine.getRating() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("La valoración del vino es requerida").build();
        }

        if (userWine.getRating() < 0 || userWine.getRating() > 10) { 
            return Response.status(Response.Status.BAD_REQUEST).entity("La valoración del vino debe ser un número entre 0 y 10").build();
        }
        try {
            int rowsAffected = userWineRepository.addUserWine(userWine);
            if (rowsAffected < 1) {
                return Response.status(Response.Status.NOT_FOUND).entity("Error al añadir el vino").build();
            }
            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
            
    }

    @GET
    @Path("/{userId}/wines")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserWines(
        @PathParam("userId") int userId,
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
            // Verifica si el usuario existe antes de intentar obtener sus vinos
            if (userRepository.getUserById(userId) == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
            }
            // Calcular el número de vinos para saber si se trata de la última página
            int totalWines = userWineRepository.getTotalWines(userId, namePattern);
            int totalPages = (int) Math.ceil((double) totalWines / size);

            List<Wine> wines = userWineRepository.getWines(
                userId, 
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

            StringBuilder baseUri = new StringBuilder(String.format("http://localhost:8080/vinoteca/api/users/%d/wines?", userId));

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

            if (page > 0 && page < totalPages) {
                int prevPage = page - 1;
                response.link(String.format(baseUri.toString(), prevPage, size), "prev");
            }

            return response.build();
      } catch (SQLException | UnsupportedEncodingException e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
    }
    
    @GET
    @Path("/{userId}/wines/{wineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserWineById(
        @PathParam("userId") int userId, 
        @PathParam("wineId") int wineId
    ) {
        try {
            // Verifica si el usuario y el vino existen antes de intentar obtener la relación
            if (userRepository.getUserById(userId) == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
            }
            // Verifica si el vino existe antes de intentar obtener la relación
            if (wineRepository.getWineById(wineId) == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Vino no encontrado").build();
            }
            // Obtiene la relación entre el usuario y el vino
            Wine userWine = userWineRepository.getUserWineById(userId, wineId);
            if (userWine == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("El vino no se encuentra en la lista de favoritos del usuario").build();
            }
            return Response.ok(userWine).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{userId}/wines/{wineId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserWine(
        @PathParam("userId") int userId, 
        @PathParam("wineId") int wineId, 
        RatingUpdate ratingUpdateRequest
    ) {
        
        if (ratingUpdateRequest == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("La valoración del vino es requerida").build();
        }

        if (ratingUpdateRequest.getRating() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("La valoración del vino es requerida").build();
        }

        if (ratingUpdateRequest.getRating() < 0 || ratingUpdateRequest.getRating() > 10) {
            return Response.status(Response.Status.BAD_REQUEST).entity("La valoración del vino debe ser un número entre 0 y 10").build();
        }
      
        try {
            // Verifica si el usuario y el vino existen antes de intentar obtener la relación
            if (userRepository.getUserById(userId) == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
            }
            // Verifica si el vino existe antes de intentar obtener la relación
            if (wineRepository.getWineById(wineId) == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Vino no encontrado").build();
            }

            // Verifica si la relación entre el usuario y el vino existe antes de intentar actualizarla
            Wine existingWine = userWineRepository.getUserWineById(userId, wineId);
            if (existingWine == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Vino no encontrado").build();
            }
            
            existingWine.setRating(ratingUpdateRequest.getRating());

            int rowsAffected = userWineRepository.updateUserWineById(userId, wineId, ratingUpdateRequest.getRating());
            
            if (rowsAffected < 1) {
                return Response.status(Response.Status.NOT_FOUND).entity("Error al actualizar el vino").build();
            }

            return Response.ok(existingWine).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{userId}/wines/{wineId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUserWine(
        @PathParam("userId") int userId, 
        @PathParam("wineId") int wineId
    ) {
        try {
            if (userRepository.getUserById(userId) == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
            }
            Wine userWine = userWineRepository.getUserWineById(userId, wineId);
            if (userWine == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Vino no encontrado").build();
            }
            int rowsAffected = userWineRepository.deleteUserWineById(userId, wineId);

            if (rowsAffected < 1) {
                return Response.status(Response.Status.NOT_FOUND).entity("Error al eliminar el vino").build();
            }
            return Response.ok(userWine).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
} 
