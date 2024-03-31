package es.client;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import es.model.FriendWines;
import es.model.User;
import es.model.UserRecommendations;
import es.model.UserWine;
import es.model.Wine;

public class RestClient {

    private static final Scanner scanner = new Scanner(System.in); 
    private static final Client client = ClientBuilder.newClient();
    private static final String BASE_URI = "http://localhost:8080/vinoteca/api";
    public static void main(String[] args) {
        String option;
        System.out.println(CC.WHITE_BOLD_BRIGHT + "\n¡ Bienvenido a la Vinoteca !" + CC.RESET);
        try {
            Response response = client.target(BASE_URI + "/health").request().get();
            if (response.getStatus() == 200) {
                System.out.println(CC.GREEN_BOLD_BRIGHT + "\nEl servidor está activo y respondiendo" + CC.RESET);
                do {
                    showMenu();
                    option = scanner.nextLine();
                    processOption(option);
                } while (!option.isBlank());
            } else {
                System.out.println(CC.RED_BOLD_BRIGHT + "\nEl servidor no está activo. Por favor, levanta un servidor con el archivo vinoteca.war" + CC.RESET);
                return;
            }
        } catch (ProcessingException e) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nNo se pudo conectar al servidor. Por favor, asegúrate de que el servidor esté activo\n" + CC.RESET);
        } finally {
            client.close();
        }
    }

    private static void showMenu() {
        String optionFormat = CC.CYAN_BRIGHT + "%-5d " + CC.RESET + "%s";
        String divider = CC.WHITE + "-".repeat(30) + CC.RESET;
        System.out.println(CC.WHITE_BOLD_BRIGHT + "\nMenú de Administrador" + CC.RESET);
        System.out.println(divider);
        System.out.println(String.format(optionFormat, 1, "Crear Usuario"));
        System.out.println(String.format(optionFormat, 2, "Crear Vino"));
        System.out.println(String.format(optionFormat, 3, "Ver usuarios existentes"));
        System.out.println(String.format(optionFormat, 4, "Ver vinos existentes"));
        System.out.println(String.format(optionFormat, 5, "Entrar como usuario"));
        System.out.println(CC.WHITE_BRIGHT + "\nPresione cualquier otra tecla para salir\n" + CC.RESET);
        System.out.println(divider);
        System.out.print(CC.WHITE_BOLD_BRIGHT + "Selecciona una opción: " + CC.RESET);
    }

    private static void processOption(String option) {
        if (option.equalsIgnoreCase("1")) {
            
            createUser(BASE_URI + "/users");

        } else if (option.equalsIgnoreCase("2")) {

            createWine(BASE_URI + "/wines");

        } else if (option.equalsIgnoreCase("3")) {

            showUsers(BASE_URI + "/users", null, null, false);

        } else if (option.equalsIgnoreCase("4")) {

            showWines(BASE_URI + "/wines", null, false, false);

        } else if (option.equalsIgnoreCase("5")) {

            System.out.print(CC.WHITE_BOLD_BRIGHT + "\nIntroduce el ID del usuario: " + CC.RESET);
            String userIdSearchStr = scanner.nextLine();
            Integer userIdSearch = null;
            try {
                userIdSearch = Integer.parseInt(userIdSearchStr);
            } catch (Exception e) {
                System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de ID inválido" + CC.RESET);
                return;
            }
            showUser(userIdSearch, BASE_URI + "/users/" + userIdSearch + "/recommendations");

        } else if (option.isBlank()) {
            System.out.println(CC.WHITE_BOLD_BRIGHT + "\n¡ Hasta luego !\n" + CC.RESET);
        }
    }

    private static void createUser(String uri) {
        System.out.print(CC.CYAN_BOLD_BRIGHT + "\nNombre de usuario: " + CC.RESET);
        String username = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Correo Electrónico: " + CC.RESET);
        String email = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Fecha de Nacimiento (yyyy-MM-dd): " + CC.RESET);
        String dateOfBirth = scanner.nextLine();
        
        if (username.isBlank() || email.isBlank() || dateOfBirth.isBlank()) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nTodos los campos son obligatorios" + CC.RESET);
            return;
        }

        if (!isDateFormat(dateOfBirth)) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de fecha inválido" + CC.RESET);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setDateOfBirth(dateOfBirth);

        WebTarget target = client.target(uri);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.post(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
        
        if (response.getStatus() == 201) {
            System.out.println(CC.GREEN_BOLD_BRIGHT + "\nUsuario creado con éxito" + CC.RESET);
            int userId = response.readEntity(User.class).getUserId();
            showUser(userId, BASE_URI + "/users/" + userId + "/recommendations");
        } else {
            System.out.println(CC.RED_BOLD_BRIGHT + "\n" + response.readEntity(String.class) + CC.RESET);
        }
        return;         
    }

    private static void createWine(String uri) {
        System.out.print(CC.CYAN_BOLD_BRIGHT + "\nNombre del vino: " + CC.RESET);
        String name = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Bodega: " + CC.RESET);
        String winery = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Origen: " + CC.RESET);
        String origin = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Tipo: " + CC.RESET);
        String type = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Uvas (separadas por comas): " + CC.RESET);
        String grapes = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Añada: " + CC.RESET);
        String vintageStr = scanner.nextLine();

        Short vintage = null;

        if (!vintageStr.isBlank()) {
            try {
                vintage = Short.parseShort(vintageStr);
            } catch (Exception e) {
                System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de añada inválido, si no tiene añada establece 0" + CC.RESET);
                return;
            }
        }

        if (name.isBlank() || winery.isBlank() || origin.isBlank() || type.isBlank() || grapes.isBlank() || vintageStr.isBlank()) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nTodos los campos son obligatorios" + CC.RESET);
            return;
        }

        Wine wine = new Wine();
        wine.setName(name);
        wine.setWinery(winery);
        wine.setOrigin(origin);
        wine.setType(type);
        // Dividir la entrada por comas, aplicar trim a cada elemento, y agrupar en una lista
        List<String> grapesList = Arrays.stream(grapes.split(","))
                                        .map(String::trim)
                                        .collect(Collectors.toList());
        wine.setGrapes(grapesList);
        if (vintage < 0) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de añada inválido, si no tiene añada establece 0" + CC.RESET);
            return;
        }
        wine.setVintage(vintage);

        WebTarget target = client.target(uri);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.post(Entity.entity(wine, MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() == 201) {
            System.out.println(CC.GREEN_BOLD_BRIGHT + "\nVino creado con éxito" + CC.RESET);
            return;
        } else {
            System.out.println(CC.RED_BOLD_BRIGHT + "\n" + response.readEntity(String.class) + CC.RESET);
            return;
        } 
    }

    private static void showUsers(String uri, User followedUser, String searchedUsername, boolean searched) {
        boolean isFollowersList = followedUser != null;
        String nextUri = null;
        String prevUri = null;
        WebTarget target = client.target(uri);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.get();

        if (response.getStatus() != 200) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\n" + response.readEntity(String.class) + CC.RESET);
            return;
        }

        List<User> users = response.readEntity(new GenericType<List<User>>() {});
        
        String divider = CC.WHITE + "-".repeat(10 + 20 + 30 + 15 + 9) + CC.RESET; // Suma de anchos + espacios extra para separadores
        System.out.println(divider);
        if (isFollowersList) {
            if (!searched) {
                System.out.println(CC.WHITE_BOLD_BRIGHT + "\nSeguidores de " + followedUser.getUsername() + ":\n" + CC.RESET);
            } else {
                System.out.println(CC.WHITE_BOLD_BRIGHT + "\nSeguidores de " + followedUser.getUserId() + "con nombre " + searchedUsername + ":\n"  + CC.RESET);
            }
        } else if (searched) {
            System.out.println(CC.WHITE_BOLD_BRIGHT + "\nUsuarios con nombre "+ searchedUsername + ":\n" + CC.RESET);
        } else {
            System.out.println(CC.WHITE_BOLD_BRIGHT + "\nUsuarios existentes:\n" + CC.RESET);
        }
        // Calcular el ancho de la línea basado en la longitud de las columnas
        String headerFormat = CC.CYAN_BOLD_BRIGHT + "%-10s %-20s %-30s %-15s" + CC.RESET;
        String rowFormat = CC.GREEN_BOLD_BRIGHT + "%-10d " + CC.RESET +  "%-20s %-30s %-15s";

        System.out.println(String.format(headerFormat, "ID", "Nombre de Usuario", "Correo Electrónico", "Fecha de Nacimiento"));

        if (users.isEmpty()) {
            System.out.println(CC.WHITE_BRIGHT + "\nNo se encontraron usuarios" + CC.RESET);
        }
        for (User user : users) {
            System.out.println(String.format(rowFormat,
                                user.getUserId(),
                                trimField(user.getUsername(), 20),
                                trimField(user.getEmail(), 30),
                                trimField(user.getDateOfBirth(), 15)));
        }
        String linkHeader = response.getHeaderString("Link");

        if (linkHeader != null) {
            Map<String, String> links = parseLinkHeader(linkHeader);
            nextUri = links.get("next");
            prevUri = links.get("prev");
        }
        System.out.println(divider);
        if (prevUri != null) {
            System.out.print(CC.CYAN_BRIGHT + " P - Página Anterior " + CC.RESET);
        }
        if (nextUri != null) {
            System.out.print(CC.CYAN_BRIGHT + " N - Página Siguiente " + CC.RESET);
        }
        if (!searched) {
            System.out.print(CC.CYAN_BRIGHT + "  B - Busca usuarios por nombre " + CC.RESET);
        }
        if (searched) {
            System.out.print(CC.CYAN_BRIGHT + "  T - Mostrar todos " + CC.RESET);
        }
        
        if (!isFollowersList) {
            System.out.print(CC.CYAN_BRIGHT + " S - Seleccionar usuario " + CC.RESET);
        } else {
            System.out.print(CC.CYAN_BRIGHT + " S - Seguir a otro usuario " + CC.RESET);
            System.out.print(CC.CYAN_BRIGHT + "  D - Dejar de seguir a otro usuario " + CC.RESET);
            System.out.print(CC.CYAN_BRIGHT + " A - Volver atrás " + CC.RESET);
        }
        System.out.println();

        System.out.println("\nPresione cualquier otra tecla para regresar al menú principal");

        System.out.print(CC.WHITE_BOLD_BRIGHT + "\nSelecciona una opción: " + CC.RESET);
        
        String option = scanner.nextLine();
        if (option.equalsIgnoreCase("P") && prevUri != null) {
            showUsers(prevUri, followedUser, searchedUsername, searched);
        
        } else if (option.equalsIgnoreCase("N") && nextUri != null) {
            showUsers(nextUri, followedUser, searchedUsername, searched);

        } else if (option.equalsIgnoreCase("B")) {
            if (!searched) {
                System.out.print(CC.WHITE_BOLD_BRIGHT + "Introduce el nombre del usuario a buscar: " + CC.RESET);
                String usernamePattern = scanner.nextLine();
                
                if (isFollowersList) {
                    showUsers(BASE_URI + "/users/" + followedUser.getUsername() + "/followers?usernamePattern=" + usernamePattern, followedUser, usernamePattern, true);
                } else {
                    showUsers(BASE_URI + "/users?usernamePattern=" + usernamePattern, null, usernamePattern, true);
                }
            }
        } else if (option.equalsIgnoreCase("T")) {
            if (!isFollowersList) {
                showUsers(BASE_URI + "/users", null, null, false);
            } else {
                showUsers(BASE_URI + "/users/" + followedUser.getUserId() + "/followers", followedUser, null, false);
            }
        } else if (option.equalsIgnoreCase("S")) {
            if (!isFollowersList) {
                System.out.print(CC.WHITE_BOLD_BRIGHT + "\nIntroduce el ID del usuario: " + CC.RESET);
                String userIdSearchStr = scanner.nextLine();
                Integer userIdSearch = null;
                try {
                    userIdSearch = Integer.parseInt(userIdSearchStr);
                } catch (Exception e) {
                    System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de ID inválido" + CC.RESET);
                    showUsers(BASE_URI + "/users", null, null, false);
                    return;
                }
                showUser(userIdSearch, BASE_URI + "/users/" + userIdSearch + "/recommendations");
            } else {
                System.out.print(CC.WHITE_BOLD_BRIGHT + "\nIntroduce el ID del usuario a seguir: " + CC.RESET);
                String followingUserIdStr = scanner.nextLine();
                Integer followingUserId = null;
                try {
                    followingUserId = Integer.parseInt(followingUserIdStr);
                } catch (Exception e) {
                    System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de ID inválido" + CC.RESET);
                    if (followedUser != null)
                        showUsers(BASE_URI + "/users/" + followedUser.getUserId() + "/followers", followedUser, null, false);
                    return;
                }

                followUser(followedUser, followingUserId);
            }
        } else if (option.equalsIgnoreCase("A") && isFollowersList) {
            showUser(followedUser.getUserId(), BASE_URI + "/users/" + followedUser.getUserId() + "/recommendations");
        } else if (option.equalsIgnoreCase("D") && isFollowersList) {
            System.out.print(CC.WHITE_BOLD_BRIGHT + "\nIntroduce el ID del usuario a dejar de seguir: " + CC.RESET);
            String followingUserIdStr = scanner.nextLine();
            Integer followingUserId = null;
            try {
                followingUserId = Integer.parseInt(followingUserIdStr);
            } catch (Exception e) {
                System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de ID inválido" + CC.RESET);
                return;
            }
            unFollowUser(followedUser.getUserId(), followingUserId);
        }
    }

    private static void showUser(int userId, String recommendationsUri) {
        String prevUri = null;
        String nextUri = null;
        WebTarget target = client.target(BASE_URI + "/users/" + userId);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.get();
        if (response.getStatus() == 404) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nUsuario no encontrado" + CC.RESET);
            return;
        } else if (response.getStatus() != 200) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nError interno del servidor" + CC.RESET);
            return;
        }
        User user = response.readEntity(User.class);
        
        System.out.println(CC.WHITE + "-".repeat(30) + CC.RESET);
        System.out.println(CC.WHITE_BOLD_BRIGHT + "\nInformación del usuario:\n" + CC.RESET);
        System.out.println(CC.CYAN_BOLD_BRIGHT + "ID: " + CC.RESET + user.getUserId());
        System.out.println(CC.CYAN_BOLD_BRIGHT + "Nombre de Usuario: " + CC.RESET + user.getUsername());
        System.out.println(CC.CYAN_BOLD_BRIGHT + "Correo Electrónico: " + CC.RESET + user.getEmail());
        System.out.println(CC.CYAN_BOLD_BRIGHT + "Fecha de Nacimiento: " + CC.RESET + user.getDateOfBirth());
        
        Response recommendationResponse = showUserRecommendations(recommendationsUri);

        String linkHeader = recommendationResponse.getHeaderString("Link");
        if (linkHeader != null) {
            Map<String, String> links = parseLinkHeader(linkHeader);
            nextUri = links.get("next");
            prevUri = links.get("prev");
        }

        System.out.println(CC.WHITE_BOLD_BRIGHT + "\nSesión Iniciada como " + CC.YELLOW_BOLD_BRIGHT + user.getUsername() + CC.RESET + " id: ["+ CC.YELLOW_BOLD_BRIGHT + user.getUserId() + CC.RESET + "]" +"\n");
        System.out.println(CC.WHITE_BRIGHT + "¿Qué desea hacer?\n" + CC.RESET);
        if (prevUri != null) {
            System.out.print(CC.CYAN_BRIGHT + " P - Página Anterior (seguidores) " + CC.RESET);
        }
        if (nextUri != null) {
            System.out.print(CC.CYAN_BRIGHT + " N - Página Siguiente (seguidores) " + CC.RESET);
        }
        System.out.print(CC.CYAN_BRIGHT + " M - Modificar usuario " + CC.RESET);
        System.out.print(CC.CYAN_BRIGHT + " E - Eliminar usuario " + CC.RESET);
        System.out.print(CC.CYAN_BRIGHT + " S - Mostrar seguidores " + CC.RESET);
        System.out.println(CC.CYAN_BRIGHT + "  V - Ver vinos puntuados" + CC.RESET);
        
        System.out.println();
        System.out.println("\nPresione cualquier otra tecla para regresar al menú principal");
        System.out.print(CC.WHITE_BOLD_BRIGHT + "\nSelecciona una opción: " + CC.RESET);
        
        String option = scanner.nextLine();
        if (option.equalsIgnoreCase("M")) {
            updateUser(userId);
        } else if (option.equalsIgnoreCase("E")) {
            deleteUser(userId);
        } else if (option.equalsIgnoreCase("S")) {
            showFollowers(user);
        } else if (option.equalsIgnoreCase("V")) {
            showRatedWines(user);
        } else if (option.equalsIgnoreCase("P") && prevUri != null) {
            showUser(userId, prevUri);
        } else if (option.equalsIgnoreCase("N") && nextUri != null) {
            showUser(userId, nextUri);
        }
    }

    private static void updateUser(int userId) {
        System.out.println(CC.WHITE_BRIGHT + "\nSi no desea modificar un campo presione la tecla enter\n" + CC.RESET);
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Nombre de usuario: " + CC.RESET);
        String username = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Correo Electrónico: " + CC.RESET);
        String email = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Fecha de Nacimiento (yyyy-MM-dd): " + CC.RESET);
        String dateOfBirth = scanner.nextLine();
        
        if (username.isBlank() && email.isBlank() && dateOfBirth.isBlank()) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nNo se ha modificado ningún campo" + CC.RESET);
            showUser(userId, BASE_URI + "/users/" + userId + "/recommendations");
            return;
        }

        if (!dateOfBirth.isBlank() && !isDateFormat(dateOfBirth)) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de fecha inválido" + CC.RESET);
            showUser(userId, BASE_URI + "/users/" + userId + "/recommendations");
            return;
        }
        
        User user = new User();
        if (!username.isBlank()) {
            user.setUsername(username);
        }
        if (!email.isBlank()) {
            user.setEmail(email);
        }
        if (!dateOfBirth.isBlank()) {
            user.setDateOfBirth(dateOfBirth);
        }

        WebTarget target = client.target(BASE_URI + "/users/" + userId);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.put(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() == 200) {
            System.out.println(CC.GREEN_BOLD_BRIGHT + "\nUsuario modificado con éxito" + CC.RESET);
        } else {
            System.out.println(CC.RED_BOLD_BRIGHT + "\n" + response.readEntity(String.class) + CC.RESET);
        }
        showUser(userId, BASE_URI + "/users/" + userId + "/recommendations");
    }

    private static void deleteUser(int userId) {
        System.out.print(CC.CYAN_BOLD_BRIGHT + "\n¿Estás seguro de que deseas eliminar el usuario? (S/n): " + CC.RESET);
        String option = scanner.nextLine();

        if (option.equalsIgnoreCase("n")) {
            showUser(userId, BASE_URI + "/users/" + userId + "/recommendations");
            return;
        } else if (!option.equalsIgnoreCase("s") && !option.isBlank()) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nOpción no válida" + CC.RESET);
            deleteUser(userId);
            return;
        }

        WebTarget target = client.target(BASE_URI + "/users/" + userId);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.delete();

        if (response.getStatus() == 200) {
            System.out.println(CC.GREEN_BOLD_BRIGHT + "\nUsuario eliminado con éxito" + CC.RESET);
        } else {
            System.out.println(CC.RED_BOLD_BRIGHT + "\n" + response.readEntity(String.class) + CC.RESET);
        }
        return;
    }

    private static void showFollowers(User followedUser) {
        showUsers(BASE_URI + "/users/" + followedUser.getUserId() + "/followers", followedUser, null, false);
    }

    private static void followUser(User followedUser, int followingUserId) {
        WebTarget target = client.target(BASE_URI + "/users/" + followedUser.getUserId() + "/follows/" + followingUserId);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() == 201) {
            System.out.println(CC.GREEN_BOLD_BRIGHT + "\nUsuario seguido con éxito" + CC.RESET);
        } else {
            System.out.println(CC.RED_BOLD_BRIGHT + "\n" + response.readEntity(String.class) + CC.RESET);
        }
        showUsers(BASE_URI + "/users/"+ followedUser.getUserId() + "/followers", followedUser, null, false);
    }

    private static void unFollowUser(int followedId, int followingUserId) {
        WebTarget target = client.target(BASE_URI + "/users/" + followedId + "/follows/" + followingUserId);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.delete();

        if (response.getStatus() == 200) {
            System.out.println(CC.GREEN_BOLD_BRIGHT + "\nDejaste de seguir al usuario con éxito" + CC.RESET);
        } else {
            System.out.println(CC.RED_BOLD_BRIGHT + "\n" + response.readEntity(String.class) + CC.RESET);
        }
        showUser(followedId, BASE_URI + "/users/" + followedId + "/recommendations");
        return;
    }

    private static void showRatedWines(User user) {
        showWines(BASE_URI + "/users/" + user.getUserId() + "/wines", user, false, false);
    }

    private static void showWines(String uri, User user, boolean searched, boolean viewAllFromUserProfile) {
        boolean isUserWines = user != null;
        String nextUri = null;
        String prevUri = null;
        WebTarget target = client.target(uri);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.get();
        List<Wine> wines = response.readEntity(new GenericType<List<Wine>>() {});
        String divider;
        if (!isUserWines || viewAllFromUserProfile) {
            String attributesFormat = "%-30s %-20s %-20s %-10s %-40s %-10s %-10s";
            divider = CC.WHITE + "-".repeat(30 + 20 + 20 + 10 + 40 + 10 + 10 + 9) + CC.RESET; // Suma de anchos + espacios extra para separadores
            System.out.println(divider);
            System.out.println(CC.WHITE_BOLD_BRIGHT + "\nVinos existentes:\n" + CC.RESET);
            // Calcular el ancho de la línea basado en la longitud de las columnas
            String headerFormat = CC.CYAN_BOLD_BRIGHT + "%-5s " + attributesFormat + CC.RESET;
            String rowFormat = CC.GREEN_BOLD_BRIGHT + "%-5d " + CC.RESET + attributesFormat;
            
            System.out.println(String.format(headerFormat, "ID", "Nombre", "Bodega", "Origen", "Tipo", "Uvas", "Añada", "Incorporación"));
            if (wines.isEmpty()) {
                System.out.println(CC.WHITE_BRIGHT + "\nNo se encontraron vinos" + CC.RESET);
            }
            for (Wine wine : wines) {
                System.out.println(String.format(rowFormat, 
                    wine.getId(),
                    trimField(wine.getName(), 30),
                    trimField(wine.getWinery(), 20),
                    trimField(wine.getOrigin(), 20),
                    trimField(wine.getType(), 10),
                    trimField(String.join(", ", wine.getGrapes()), 40),
                    wine.getVintage(),                  
                    wine.getIncorporation()
                ));
            }
        } else {
            divider = CC.WHITE + "-".repeat(5 + 30 + 20 + 20 + 10 + 40 + 10 + 10 + 15 + 9) + CC.RESET; // Suma de anchos + espacios extra para separadores
            System.out.println(divider);
            System.out.println(CC.WHITE_BOLD_BRIGHT + "\nVinos puntuados por " + user.getUsername() + "\n" + CC.RESET);

            String headerFormat = CC.CYAN_BOLD_BRIGHT + "%-5s %-30s %-20s %-20s %-10s %-40s %-10s %-15s %-10s" + CC.RESET;
            String rowFormat = CC.GREEN_BOLD_BRIGHT + "%-5d " + CC.RESET  + "%-30s %-20s %-20s %-10s %-40s %-10s %-15d %-10s";

            System.out.println(String.format(headerFormat, "ID", "Nombre", "Bodega", "Origen", "Tipo", "Uvas", "Añada", "Puntuación", "Valorado"));
            if (wines.isEmpty()) {
                System.out.println(CC.WHITE_BRIGHT + "\nNo se encontraron vinos" + CC.RESET);
            }
            for (Wine wine : wines) {
                System.out.println(String.format(rowFormat, 
                    wine.getId(),
                    trimField(wine.getName(), 30),
                    trimField(wine.getWinery(), 20),
                    trimField(wine.getOrigin(), 20),
                    trimField(wine.getType(), 10),
                    trimField(String.join(", ", wine.getGrapes()), 40),
                    wine.getVintage(),
                    wine.getRating(),
                    wine.getDateAdded()
                ));
            }
        }
        String linkHeader = response.getHeaderString("Link");

        if (linkHeader != null) {
            Map<String, String> links = parseLinkHeader(linkHeader);
            nextUri = links.get("next");
            prevUri = links.get("prev");
        }
        System.out.println(divider);
        if (prevUri != null) {
            System.out.print(CC.CYAN_BRIGHT + " P - Página Anterior " + CC.RESET);
        }
        if (nextUri != null) {
            System.out.print(CC.CYAN_BRIGHT + " N - Página Siguiente " + CC.RESET);
        }
        if (!searched) {
            System.out.print(CC.CYAN_BRIGHT + "  F - Filtrar vinos " + CC.RESET);
        } else {
            System.out.print(CC.CYAN_BRIGHT + "  T - Mostrar todos " + CC.RESET);
        }
        if (!isUserWines) {
            System.out.print(CC.CYAN_BRIGHT + "  S - Seleccionar vino " + CC.RESET);
        } else {
            if (!viewAllFromUserProfile) {
                System.out.print(CC.CYAN_BRIGHT + "  V - Ver el resto de vinos " + CC.RESET);
            }
            System.out.print(CC.CYAN_BRIGHT + "  R - Puntuar vino " + CC.RESET);
            System.out.print(CC.CYAN_BRIGHT + "  A - Volver atrás " + CC.RESET);
        }
        System.out.println();
        System.out.println(CC.WHITE_BRIGHT + "\nPresione cualquier otra tecla para regresar al menú principal" + CC.RESET);
        System.out.print(CC.WHITE_BOLD_BRIGHT + "\nSelecciona una opción: " + CC.RESET);

        String option = scanner.nextLine();
        if (option.equalsIgnoreCase("P") && prevUri != null) {
            showWines(prevUri, user, searched, viewAllFromUserProfile);
        } else if (option.equalsIgnoreCase("N") && nextUri != null) {
            showWines(nextUri, user, searched, viewAllFromUserProfile);
        } else if (option.equalsIgnoreCase("F") && !searched) {
            
            filterWine(uri, user, viewAllFromUserProfile);
            
        } else if (option.equalsIgnoreCase("T")) {
            if (isUserWines && !viewAllFromUserProfile) {
                showWines(BASE_URI + "/users/" + user.getUserId() + "/wines", user, false, viewAllFromUserProfile);
            } else {
                showWines(BASE_URI + "/wines", user, false, viewAllFromUserProfile);
            }
        } else if (option.equalsIgnoreCase("S") && !isUserWines) {
        
            System.out.print(CC.WHITE_BOLD_BRIGHT + "Introduce el ID del vino: " + CC.RESET);
            String wineIdStr = scanner.nextLine();
            Integer wineId = null;
            try {
                wineId = Integer.parseInt(wineIdStr);
            } catch (Exception e) {
                System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de ID inválido" + CC.RESET);
                return;
            }
            showWine(wineId);
            
        } else if (option.equalsIgnoreCase("A") && isUserWines) {
            if (!viewAllFromUserProfile) {
                showUser(user.getUserId(), BASE_URI + "/users/" + user.getUserId() + "/recommendations");
            } else {
                showRatedWines(user);
            }
        } else if (option.equalsIgnoreCase("R") && isUserWines) {
            rateWine(user);
        } else if (option.equalsIgnoreCase("V") && isUserWines && !viewAllFromUserProfile) {
            showWines(BASE_URI + "/wines", user, false, true);
        }
    }
    private static void filterWine(String uri, User user, boolean viewAllFromUserProfile) {
        boolean isUserWines = user != null;
        StringBuilder filter = new StringBuilder();

        System.out.println(CC.WHITE_BRIGHT + "\nSi no desea establecer un filtro presione la tecla enter\n" + CC.RESET);
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Nombre: " + CC.RESET);
        String name = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Bodega: " + CC.RESET);
        String winery = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Origen: " + CC.RESET);
        String origin = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Tipo: " + CC.RESET);
        String type = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Uva: " + CC.RESET);
        String grape = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Añada: " + CC.RESET);
        String vintageStr = scanner.nextLine();
        String ratingStr = "";
        if (isUserWines && !viewAllFromUserProfile) {
            System.out.print(CC.CYAN_BOLD_BRIGHT + "Puntuación: " + CC.RESET);
            ratingStr = scanner.nextLine();
        } 

        if (!isUserWines || viewAllFromUserProfile) {
            System.out.print(CC.CYAN_BOLD_BRIGHT + "Fecha de Incorporación (yyyy-MM-dd): " + CC.RESET);
        } else {
            System.out.print(CC.CYAN_BOLD_BRIGHT + "Fecha de Valoración (yyyy-MM-dd): " + CC.RESET);
        }
        String fecha = scanner.nextLine();

        if (name.isBlank() && winery.isBlank() && origin.isBlank() && type.isBlank() && grape.isBlank() && vintageStr.isBlank() && ratingStr.isBlank() && fecha.isBlank()) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nNo se ha establecido ningún filtro" + CC.RESET);
            return;
        }

        if (!name.isBlank()) {
            filter.append("namePattern=").append(name);
        }

        if (!winery.isBlank()) {
            if (filter.length() > 0) 
                filter.append("&");
            
            filter.append("winery=").append(winery);
        }

        if (!origin.isBlank()) {
            if (filter.length() > 0) 
                filter.append("&");
            
            filter.append("origin=").append(origin);
        }

        if (!type.isBlank()) {
            if (filter.length() > 0) 
                filter.append("&");
            
            filter.append("type=").append(type);
        }

        if (!grape.isBlank()) {
            if (filter.length() > 0) 
                filter.append("&");
            
            filter.append("grape=").append(grape);
        }

        if (!vintageStr.isBlank()) {
            if (!isNumeric(vintageStr)) {
                System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de añada inválido, si no tiene añada establece 0" + CC.RESET);
                return;
            }
            if (filter.length() > 0) 
                filter.append("&");
            
            filter.append("vintage=").append(vintageStr);
        }

        if (isUserWines && !viewAllFromUserProfile) {
            if (!ratingStr.isBlank()) {
                if (!isNumeric(ratingStr)) {
                    System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de puntuación inválido" + CC.RESET);
                    return;
                }
                if (filter.length() > 0) 
                    filter.append("&");
                
                filter.append("rating=").append(ratingStr);
            }
        }

        if (!fecha.isBlank()) {
            if (!isDateFormat(fecha)) {
                System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de fecha inválido" + CC.RESET);
                return;
            }
            if (filter.length() > 0) 
                filter.append("&");

            if (!isUserWines || viewAllFromUserProfile)
                filter.append("incorporation=").append(fecha);
            else
                filter.append("dateAdded=").append(fecha);
        }

        if (filter.length() == 0) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nNo se ha establecido ningún filtro" + CC.RESET);
            return;
        }
        if (!isUserWines || viewAllFromUserProfile) 
            showWines(BASE_URI + "/wines?" + filter.toString(), user, true, viewAllFromUserProfile);
        else
            showWines(BASE_URI + "/users/" + user.getUserId() + "/wines?" + filter.toString(), user, true, viewAllFromUserProfile);
    }
    
    private static void rateWine(User user) {
        System.out.print(CC.CYAN_BOLD_BRIGHT + "\nIntroduce el ID del vino: " + CC.RESET);
        String wineIdStr = scanner.nextLine().trim();
        Integer wineId = null;
        
        try {
            wineId = Integer.parseInt(wineIdStr);
        } catch (Exception e) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de ID inválido" + CC.RESET);
            showRatedWines(user);
            return;
        } 

        System.out.print(CC.CYAN_BOLD_BRIGHT + "Introduce la puntuación (0-10): " + CC.RESET);
        String ratingStr = scanner.nextLine().trim();
        Short rating = null;

        try {
            rating = Short.parseShort(ratingStr);
        } catch (Exception e) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de puntuación inválido" + CC.RESET);
            showRatedWines(user);
            return;
        }
        

        if (rating < 0 || rating > 10) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nPuntuación inválida" + CC.RESET);
            showRatedWines(user);
            return;
        }
        UserWine userRating = new UserWine();
        userRating.setWineId(wineId);
        userRating.setRating(rating);
        WebTarget target = client.target(BASE_URI + "/users/" + user.getUserId() + "/wines");
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.post(Entity.entity(userRating, MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() == 200) {
            System.out.println(CC.GREEN_BOLD_BRIGHT + "\nVino puntuado con éxito" + CC.RESET);
        } else {
            System.out.println(CC.RED_BOLD_BRIGHT + "\n" + response.readEntity(String.class) + CC.RESET);
        }
        showRatedWines(user);
    }


    private static void showWine(int wineId) {
        WebTarget target = client.target(BASE_URI + "/wines/" + wineId);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.get();
        if (response.getStatus() == 404) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nVino no encontrado" + CC.RESET);
            return;
        } else if (response.getStatus() != 200) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nError interno del servidor" + CC.RESET);
            return;
        }
        Wine wine = response.readEntity(Wine.class);

        System.out.println(CC.WHITE_BOLD_BRIGHT + "\nInformación del vino:\n" + CC.RESET);
        System.out.println(CC.CYAN_BOLD_BRIGHT + "ID: " + CC.RESET + wine.getId());
        System.out.println(CC.CYAN_BOLD_BRIGHT + "Nombre: " + CC.RESET + wine.getName());
        System.out.println(CC.CYAN_BOLD_BRIGHT + "Bodega: " + CC.RESET + wine.getWinery());
        System.out.println(CC.CYAN_BOLD_BRIGHT + "Origen: " + CC.RESET + wine.getOrigin());
        System.out.println(CC.CYAN_BOLD_BRIGHT + "Tipo: " + CC.RESET + wine.getType());
        System.out.println(CC.CYAN_BOLD_BRIGHT + "Uvas: " + CC.RESET + String.join(", ", wine.getGrapes()));
        System.out.println(CC.CYAN_BOLD_BRIGHT + "Añada: " + CC.RESET + wine.getVintage());
        System.out.println(CC.CYAN_BOLD_BRIGHT + "Fecha de Incorporación: " + CC.RESET + wine.getIncorporation());
        
        System.out.println(CC.WHITE + "-".repeat(30) + CC.RESET);
        System.out.print(CC.CYAN_BRIGHT + " M - Modificar vino " + CC.RESET);
        System.out.println(CC.CYAN_BRIGHT + " E - Eliminar vino " + CC.RESET);
        System.out.println("\nPresione cualquier otra tecla para regresar al menú principal");
        System.out.print(CC.WHITE_BOLD_BRIGHT + "\nSelecciona una opción: " + CC.RESET);
        
        String option = scanner.nextLine();
        if (option.equalsIgnoreCase("M")) {
            updateWine(wineId);
        } else if (option.equalsIgnoreCase("E")) {
            deleteWine(wineId);
        }
    } 

    private static void updateWine(int wineId) {
        System.out.println(CC.WHITE_BRIGHT + "\nSi no desea modificar un campo presione la tecla enter\n" + CC.RESET);

        System.out.print(CC.CYAN_BOLD_BRIGHT + "Nombre: " + CC.RESET);
        String name = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Bodega: " + CC.RESET);
        String winery = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Origen: " + CC.RESET);
        String origin = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Tipo: " + CC.RESET);
        String type = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Uvas (separadoras por comas): " + CC.RESET);
        String grapes = scanner.nextLine();
        System.out.print(CC.CYAN_BOLD_BRIGHT + "Añada: " + CC.RESET);
        String vintageStr = scanner.nextLine();

        Short vintage = null;

        if (!vintageStr.isBlank()) {
            try {
                vintage = Short.parseShort(vintageStr);
            } catch (Exception e) {
                System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de añada inválido, si no tiene añada establece 0" + CC.RESET);
                return;
            }
        }
        
        if (name.isBlank() && winery.isBlank() && origin.isBlank() && type.isBlank() && grapes.isBlank() && vintageStr.isBlank()) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nNo se ha modificado ningún campo" + CC.RESET);
            return;
        }
        
        Wine wine = new Wine();
        if (!name.isBlank()) {
            wine.setName(name);
        }
        if (!winery.isBlank()) {
            wine.setWinery(winery);
        }
        if (!origin.isBlank()) {
            wine.setOrigin(origin);
        }
        if (!type.isBlank()) {
            wine.setType(type);
        }
        if (!grapes.isBlank()) {
            // Dividir la entrada por comas, aplicar trim a cada elemento, y agrupar en una lista
            List<String> grapesList = Arrays.stream(grapes.split(","))
                                            .map(String::trim)
                                            .collect(Collectors.toList());

            wine.setGrapes(grapesList);
        }
        if (vintage != null) {
            if (vintage < 0) {
                System.out.println(CC.RED_BOLD_BRIGHT + "\nFormato de añada inválido, si no tiene añada establece 0" + CC.RESET);
                return;
            }
            wine.setVintage(vintage);
        }

        WebTarget target = client.target(BASE_URI + "/wines/" + wineId);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.put(Entity.entity(wine, MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() == 200) {
            System.out.println(CC.GREEN_BOLD_BRIGHT + "\nVino modificado con éxito" + CC.RESET);
            return;
        }

        System.out.println(CC.RED_BOLD_BRIGHT + "\nError al modificar el vino" + CC.RESET);
    }

    private static void deleteWine (int wineId) {
        System.out.print(CC.CYAN_BOLD_BRIGHT + "\n¿Estás seguro de que deseas eliminar el vino? (S/n): " + CC.RESET);
        String option = scanner.nextLine();

        if (option.equalsIgnoreCase("n")) {
            showWine(wineId);
            return;
        } else if (!option.equalsIgnoreCase("s") && !option.isBlank()) {
            System.out.println(CC.RED_BOLD_BRIGHT + "\nOpción no válida" + CC.RESET);
            deleteWine(wineId);
            return;
        }

        WebTarget target = client.target(BASE_URI + "/wines/" + wineId);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.delete();

        if (response.getStatus() == 200) {
            System.out.println(CC.GREEN_BOLD_BRIGHT + "\nVino eliminado con éxito" + CC.RESET);
            
        } else {
            System.out.println(CC.RED_BOLD_BRIGHT + "\n" + response.readEntity(String.class) + CC.RESET);
        }
        return;
    }

    private static Response showUserRecommendations(String uri) {
        String divider = CC.WHITE + "-".repeat(5 + 30 + 20 + 20 + 10 + 40 + 10 + 10 + 15 + 15) + CC.RESET;
        WebTarget target = client.target(uri);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.get();
        UserRecommendations recommendations = response.readEntity(new GenericType<UserRecommendations>() {});
        
        Wine [] lastAddedWines = recommendations.getLastAddedWines();

        System.out.println(divider);
        printWines(lastAddedWines, CC.WHITE_BOLD_BRIGHT + "\n" + "Últimos 5 vinos añadidos" +":\n" + CC.RESET);

        Wine [] topRatedWines = recommendations.getTopRatedWines();

        System.out.println(divider);
        printWines(topRatedWines, CC.WHITE_BOLD_BRIGHT + "\n" + "Top 5 vinos mejor valorados" +":\n" + CC.RESET);

        List<FriendWines> recommendedWines = recommendations.getFriendsWines();

        System.out.println(divider);
        System.out.println(CC.WHITE_BOLD_BRIGHT + "\nVinos recomendados por amigos:\n" + CC.RESET);
        for (FriendWines friendWines : recommendedWines) {
            System.out.println(divider);
            System.out.println(CC.YELLOW_BOLD_BRIGHT + friendWines.getFriend().getUsername() + "\n" + CC.RESET);
            System.out.println(CC.CYAN_BOLD_BRIGHT + "ID: " + CC.RESET + friendWines.getFriend().getUserId());
            System.out.println(CC.CYAN_BOLD_BRIGHT + "Nombre: " + CC.RESET + friendWines.getFriend().getUsername());
            System.out.println(CC.CYAN_BOLD_BRIGHT + "Correo Electrónico: " + CC.RESET + friendWines.getFriend().getEmail());
            System.out.println(CC.CYAN_BOLD_BRIGHT + "Fecha de Nacimiento: " + CC.RESET + friendWines.getFriend().getDateOfBirth());
            printWines(friendWines.getWines(), CC.WHITE_BRIGHT + "\nTop 5 vinos recomendados por " + friendWines.getFriend().getUsername() + ":\n" + CC.RESET);
            System.out.println(divider);
        }

        return response;
    }

    private static void printWines(Wine[] wines, String title) {
        System.out.println(title);

        String headerFormat = CC.CYAN_BOLD_BRIGHT + "%-5s %-30s %-20s %-20s %-10s %-40s %-10s %-15s %-10s" + CC.RESET;
        String rowFormat = CC.GREEN_BOLD_BRIGHT + "%-5d " + CC.RESET  + "%-30s %-20s %-20s %-10s %-40s %-10s %-15d %-10s";

        System.out.println(String.format(headerFormat, "ID", "Nombre", "Bodega", "Origen", "Tipo", "Uvas", "Añada", "Puntuación", "Valorado"));
        int nullCount = 0;
        for (Wine wine : wines) {
            if (wine == null) {
                nullCount++;
                continue;
            }
            System.out.println(String.format(rowFormat, 
                wine.getId(),
                trimField(wine.getName(), 30),
                trimField(wine.getWinery(), 20),
                trimField(wine.getOrigin(), 20),
                trimField(wine.getType(), 10),
                trimField(String.join(", ", wine.getGrapes()), 40),
                wine.getVintage(),
                wine.getRating(),
                wine.getDateAdded()
            ));
        }
        if (nullCount == wines.length || wines.length == 0) {
            System.out.println("\nNo se encontraron vinos");
        }
    }

    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isDateFormat(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String trimField(String field, int length) {
        return field.length() > length ? field.substring(0, length - 3) + "..." : field;
    }

    private static Map<String, String> parseLinkHeader(String linkHeader) {
        Map<String, String> links = new HashMap<>();

        String[] parts = linkHeader.split(",");
        for (String part : parts) {
            String[] section = part.split(";");

            if (section.length != 2) {
                continue;
            }

            String url = section[0].replace("<", "").replace(">", "").trim();
            String rel = section[1].replace("rel=", "").replace("\"", "").trim();

            links.put(rel, url);
        }
        return links;
    }
}
