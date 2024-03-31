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

import es.model.Wine;
import es.model.WineUpdate;
import es.repository.WineRepository;

@Path("/wines")
public class WineResource {

    private WineRepository wineRepository = new WineRepository();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addVino(Wine wine) { 

        if (wine.getName() == null || wine.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El nombre del vino es requerido").build();
        }

        try {
            Wine existingWine = wineRepository.getWineByName(wine.getName());

            if (existingWine != null) {
                return Response.status(Response.Status.CONFLICT).entity(String.format("El vino %s ya ha sido añadido previamente", existingWine.getName())).build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        if (wine.getWinery() == null || wine.getWinery().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("La bodega del vino es requerida").build();
        }

        if (wine.getOrigin() == null || wine.getOrigin().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El origen del vino es requerido").build();
        }

        if (wine.getType() == null || wine.getType().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El tipo de vino es requerido").build();
        }

        if (wine.getGrapes() == null || wine.getGrapes().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Las uvas del vino son requeridas").build();
        }

        if (wine.getVintage() == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("El año de cosecha del vino es requerido, si no tiene añada asigna un 0").build();
        }

        if (wine.getVintage() < 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El año de cosecha del vino no puede ser negativo, si no tiene añada asigna un 0").build();
        }

        try {
            wineRepository.addWine(wine);

            Wine newWine = wineRepository.getWineByName(wine.getName());
            wine.setId(newWine.getId());
            wine.setIncorporation(newWine.getIncorporation());

            return Response.status(Response.Status.CREATED).entity(wine).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWines(
        @QueryParam("namePattern") String namePattern,
        @QueryParam("winery") String winery,
        @QueryParam("vintage") String vintage,
        @QueryParam("origin") String origin,
        @QueryParam("type") String type,
        @QueryParam("grape") String grape,
        @QueryParam("incorporation") String incorporation,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("10") int size
    ) {
      try {
        // Calcular el número de vinos para saber si se trata de la última página
        int totalWines = wineRepository.getTotalWines(namePattern, winery, vintage, origin, type, grape, incorporation);
        int totalPages = (int) Math.ceil((double) totalWines / size);

        List<Wine> wines = wineRepository.getWines(namePattern, winery, vintage, origin, type, grape, incorporation, page, size);

        Response.ResponseBuilder responseBuilder = Response.ok(wines);

        // Agregar enlaces a las páginas
        StringBuilder baseUri = new StringBuilder("http://localhost:8080/vinoteca/api/wines?");

        if (namePattern != null) baseUri.append("namePattern=").append(URLEncoder.encode(namePattern, StandardCharsets.UTF_8.toString())).append("&");
        if (winery != null) baseUri.append("winery=").append(winery).append("&");
        if (vintage != null) baseUri.append("vintage=").append(vintage).append("&");
        if (origin != null) baseUri.append("origin=").append(origin).append("&");
        if (type != null) baseUri.append("type=").append(type).append("&");
        if (grape != null) baseUri.append("grape=").append(grape).append("&");
        if (incorporation != null) baseUri.append("incorporation=").append(incorporation).append("&");

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
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWineById(@PathParam("id") int id) {
        try {
            // Obtener el vino por ID
            Wine wine = wineRepository.getWineById(id); 

            // Verificar si el vino existe
            if (wine == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Vino no encontrado").build();
            }

            // Retornar el vino
            return Response.ok(wine).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getStackTrace()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateWineById(@PathParam("id") int id, WineUpdate wineUpdate) {
        try {
            // Verificar si el vino existe
            Wine existingWine = wineRepository.getWineById(id);
            if (existingWine == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Vino no encontrado").build();
            }

            if (wineUpdate == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Petición de actualización vacía").build();
            }
            // Actualizar los campos del vino con los valores del vino actualizado
            if (wineUpdate.getName() != null && !wineUpdate.getName().isBlank()) {
                existingWine.setName(wineUpdate.getName());
            }

            if (wineUpdate.getWinery() != null && !wineUpdate.getWinery().isBlank()) {
                existingWine.setWinery(wineUpdate.getWinery());
            }

            if (wineUpdate.getVintage() != null) {
                existingWine.setVintage(wineUpdate.getVintage());
            }

            if (wineUpdate.getOrigin() != null && !wineUpdate.getOrigin().isBlank()) {
                existingWine.setOrigin(wineUpdate.getOrigin());
            }

            if (wineUpdate.getType() != null && !wineUpdate.getType().isBlank()) {
                existingWine.setType(wineUpdate.getType());
            }

            if (wineUpdate.getGrapes() != null && !wineUpdate.getGrapes().isEmpty()) {
                existingWine.setGrapes(wineUpdate.getGrapes());
            }

            // Llamar al método de actualización
            int rowsAffected = wineRepository.updateWineById(existingWine);

            if (rowsAffected < 1) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo actualizar el vino").build();
            }

            // Retornar el vino actualizado
            return Response.ok(existingWine).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteWine(@PathParam("id") int id) {
        try {
            // Verificar si el vino existe
            Wine wine = wineRepository.getWineById(id);
            if (wine == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Vino no encontrado").build();
            }
            
            // Llamar al método de eliminación
            int rowsDeleted = wineRepository.deleteWine(id);

            // Verificar si se eliminó el vino
            if (rowsDeleted == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Vino no encontrado").build();
            }
            
            return Response.ok(wine).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
}