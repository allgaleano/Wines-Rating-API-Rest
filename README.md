# Guía de Instalación y Configuración de WinesRatingApp

Esta guía te ayudará a instalar y configurar la aplicación WinesRatingApp en tu entorno local. Sigue los pasos detallados a continuación para asegurarte de que la aplicación se ejecute correctamente.

## Prerrequisitos

Antes de comenzar, asegúrate de tener instalados los siguientes programas en tu sistema:
- Git
- MySQL
- Java 11 (o superior)
- Maven
- Tomcat

## 1. Clonación del Repositorio (Alternativa descargar el [.war](https://github.com/allgaleano/Wines-Rating-API-Rest/raw/main/exec/WinesRatingApp.war) manualmente)

Primero, necesitas clonar el repositorio de la aplicación WinesRatingApp a tu máquina local. Abre una terminal y ejecuta el siguiente comando: `git clone git@github.com:allgaleano/Wines-Rating-API-Rest.git`

Una vez clonado, navega al directorio del proyecto: `cd Wines-Rating-API-Rest`

## 2. Configuración de MySQL

Antes de proceder, debes crear una base de datos en MySQL para la aplicación. Luego, realiza los siguientes pasos para configurar la conexión a la base de datos:

1. Abre el archivo de configuración situado en `src/main/java/es/config/Config.java` con tu editor de texto preferido.
2. Modifica las líneas correspondientes para cambiar el usuario y la contraseña de acuerdo a las credenciales de tu servidor MySQL.
3. Crea una base de datos con el siguiente comando: 
    ```sql
    CREATE DATABASE WineCommunity;
    ```

## 3. Importación del Dump SQL

Para importar el esquema de la base de datos y los datos iniciales, ejecuta el siguiente comando, reemplazando `WineCommunity` con el nombre de tu base de datos: `mysql -u <USUARIO> -p WineCommunity < dump/dump.sql`

## 4. Despliegue en Tomcat

Para desplegar la aplicación en un servidor Tomcat, sigue estos pasos:

1. Asegúrate de que tu servidor Tomcat esté corriendo.
2. En la raíz del proyecto, ejecuta el siguiente comando Maven para construir la aplicación: `mvn clean package`
3. Una vez completado el proceso, toma el archivo `.war` generado en `target/WinesRatingApp.war` y colócalo en el directorio `webapps` de tu servidor Tomcat. Alternativamente puedes descargar el archivo de este repositorio [WinesRatingApp.war](https://github.com/allgaleano/Wines-Rating-API-Rest/raw/main/exec/WinesRatingApp.war)

Ahora, la aplicación WinesRatingApp debería estar corriendo en tu servidor Tomcat y lista para ser accedida desde un cliente.
Asegúrate de que el servidor tomcat esté corriendo en el puerto 8080 de localhost

## 5. Acceso a la Aplicación

Finalmente, para iniciar la aplicación, ejecuta: `mvn exec:java` desde la raíz del proyecto.

¡Eso es todo! Ahora deberías poder utilizar la aplicación WinesRatingApp en tu entorno local desde tu terminal.


