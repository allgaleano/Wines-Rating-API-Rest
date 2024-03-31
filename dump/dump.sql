-- MySQL dump 10.13  Distrib 8.0.36, for Linux (x86_64)
--
-- Host: localhost    Database: WineCommunity
-- ------------------------------------------------------
-- Server version	8.0.36-0ubuntu0.22.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Followers`
--

DROP TABLE IF EXISTS `Followers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Followers` (
  `FollowingUserID` int DEFAULT NULL,
  `FollowedUserID` int DEFAULT NULL,
  KEY `FollowingUserID` (`FollowingUserID`),
  KEY `FollowedUserID` (`FollowedUserID`),
  CONSTRAINT `Followers_ibfk_1` FOREIGN KEY (`FollowingUserID`) REFERENCES `Users` (`UserID`) ON DELETE CASCADE,
  CONSTRAINT `Followers_ibfk_2` FOREIGN KEY (`FollowedUserID`) REFERENCES `Users` (`UserID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Followers`
--

LOCK TABLES `Followers` WRITE;
/*!40000 ALTER TABLE `Followers` DISABLE KEYS */;
INSERT INTO `Followers` VALUES (1,4),(4,1),(27,4),(4,2),(4,8),(4,9),(4,6),(4,10),(31,4),(31,1);
/*!40000 ALTER TABLE `Followers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserWines`
--

DROP TABLE IF EXISTS `UserWines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `UserWines` (
  `UserID` int DEFAULT NULL,
  `WineID` int DEFAULT NULL,
  `Rating` tinyint NOT NULL,
  `DateAdded` timestamp NULL DEFAULT NULL,
  KEY `UserID` (`UserID`),
  KEY `WineID` (`WineID`),
  CONSTRAINT `UserWines_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `Users` (`UserID`) ON DELETE CASCADE,
  CONSTRAINT `UserWines_ibfk_2` FOREIGN KEY (`WineID`) REFERENCES `Wines` (`WineID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserWines`
--

LOCK TABLES `UserWines` WRITE;
/*!40000 ALTER TABLE `UserWines` DISABLE KEYS */;
INSERT INTO `UserWines` VALUES (4,1,10,'2024-03-27 18:48:14'),(4,12,4,'2024-03-27 18:55:11'),(4,11,9,'2024-03-27 18:56:34'),(4,27,8,'2024-03-30 16:03:51'),(4,19,7,'2024-03-30 16:17:18'),(4,21,7,'2024-03-30 17:38:12'),(4,23,3,'2024-03-30 19:07:33'),(1,36,7,'2024-03-30 19:08:15'),(31,27,9,'2024-03-31 11:35:26');
/*!40000 ALTER TABLE `UserWines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Users`
--

DROP TABLE IF EXISTS `Users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Users` (
  `UserID` int NOT NULL AUTO_INCREMENT,
  `Username` varchar(255) NOT NULL,
  `DateOfBirth` varchar(10) NOT NULL,
  `Email` varchar(255) NOT NULL,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `Email` (`Email`),
  UNIQUE KEY `Username` (`Username`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Users`
--

LOCK TABLES `Users` WRITE;
/*!40000 ALTER TABLE `Users` DISABLE KEYS */;
INSERT INTO `Users` VALUES (1,'carlota','2002-01-31','carlota@example.com'),(2,'maria','2005-12-11','maria@example.com'),(3,'juan','1985-04-29','juan@example.com'),(4,'alberto','2003-08-22','alberto@example.com'),(5,'lucia','1987-01-15','lucia@example.com'),(6,'carlos','2004-04-14','carlos@example.com'),(7,'sofia','1994-05-31','sofia@example.com'),(8,'manuel','2001-12-07','manuel@example.com'),(9,'ana','1992-02-04','ana@example.com'),(10,'sergio','1985-10-31','sergio@example.com'),(11,'carmen','1995-08-31','carmen@example.com'),(12,'lorena','2003-05-27','lorena@example.com'),(13,'david','2001-08-28','david@example.com'),(14,'marta','2004-07-31','marta@example.com'),(15,'jorge','2002-03-28','jorge@example.com'),(16,'laura','2004-09-02','laura@example.com'),(17,'oscar','1988-08-16','oscar@example.com'),(18,'teresa','1987-08-05','teresa@example.com'),(24,'raul','2003-09-24','raul@example.com'),(27,'ernesto','1987-03-29','ernesto@example.com'),(28,'enrique','1999-08-27','enrique@example.com'),(29,'nacho','1975-04-23','nacho@example.com'),(30,'pablo','2001-07-04','pablo@example.com'),(31,'tirso','1965-11-02','tirso@example.com');
/*!40000 ALTER TABLE `Users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Wines`
--

DROP TABLE IF EXISTS `Wines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Wines` (
  `WineID` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(255) NOT NULL,
  `Winery` varchar(255) DEFAULT NULL,
  `Vintage` year DEFAULT NULL,
  `Origin` varchar(255) DEFAULT NULL,
  `Type` varchar(50) DEFAULT NULL,
  `Grapes` text,
  `Incorporation` timestamp NOT NULL,
  PRIMARY KEY (`WineID`),
  UNIQUE KEY `Name` (`Name`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Wines`
--

LOCK TABLES `Wines` WRITE;
/*!40000 ALTER TABLE `Wines` DISABLE KEYS */;
INSERT INTO `Wines` VALUES (1,'Vega Sicilia Unico','Vega Sicilia',2010,'Ribera del Duero','Red','Tempranillo,Cabernet Sauvignon','2024-03-27 18:27:11'),(2,'La Rioja Alta Gran Reserva 904','La Rioja Alta',2011,'La Rioja','Red','Tempranillo,Graciano','2024-03-27 18:28:33'),(3,'Albariño de Fefiñanes','Palacio de Fefiñanes',2019,'Rías Baixas','White','Albariño','2024-03-27 18:33:45'),(4,'Pingus','Dominio de Pingus',2012,'Ribera del Duero','Red','Tempranillo','2024-03-27 18:34:33'),(5,'Sierra Cantabria Gran Reserva','Sierra Cantabria',2010,'La Rioja','Red','Tempranillo','2024-03-27 18:34:52'),(6,'Jerez-Xérès-Sherry Tio Pepe','González Byass',0000,'Jerez-Xérès-Sherry','White','Palomino','2024-03-27 18:35:27'),(7,'Cava Brut Nature Gran Reserva','Freixenet',0000,'Catalonia','Sparkling','Macabeo,Parellada,Xarel·lo','2024-03-27 18:37:26'),(8,'Priorat Les Terrasses','Alvaro Palacios',2018,'Priorat','Red','Garnacha,Cariñena','2024-03-27 18:37:59'),(9,'Artadi Viñas de Gain','Bodegas Artadi',2018,'Rioja','Red','Tempranillo','2024-03-27 18:38:26'),(10,'Aalto','Bodegas Aalto',2017,'Ribera del Duero','Red','Tempranillo','2024-03-27 18:38:41'),(11,'Muga Reserva','Bodegas Muga',2016,'Rioja','Red','Tempranillo,Garnacha,Mazuelo,Graciano','2024-03-27 18:38:50'),(12,'Alvaro Palacios L\'Ermita','Alvaro Palacios',2019,'Priorat','Red','Garnacha','2024-03-27 18:38:56'),(13,'Finca Allende Aurus','Finca Allende',2015,'Rioja','Red','Tempranillo,Graciano','2024-03-27 18:39:02'),(14,'Pazo Señorans Selección de Añada','Pazo de Señorans',2011,'Rías Baixas','White','Albariño','2024-03-27 18:39:09'),(15,'Valdespino Inocente Fino','Valdespino',0000,'Jerez','White','Palomino','2024-03-27 18:39:19'),(16,'Numanthia','Bodega Numanthia',2014,'Toro','Red','Tinta de Toro','2024-03-27 18:39:34'),(17,'R. López de Heredia Viña Tondonia Reserva','R. López de Heredia',2007,'Rioja','Red','Tempranillo,Garnacho,Graciano,Mazuelo','2024-03-27 18:39:40'),(18,'El Grifo Malvasía Seco Colección','El Grifo',2019,'Lanzarote','White','Malvasía Volcánica','2024-03-27 18:39:45'),(19,'Flor de Pingus','Dominio de Pingus',2016,'Ribera del Duero','Red','Tempranillo','2024-03-27 18:39:52'),(20,'Viña El Pisón','Bodegas Artadi',2018,'Rioja','Red','Tempranillo','2024-03-27 18:39:58'),(21,'As Sortes','Rafael Palacios',2019,'Valdeorras','White','Godello','2024-03-27 18:40:04'),(22,'Ultreia Saint Jacques','Raúl Pérez',2018,'Bierzo','Red','Mencía','2024-03-27 18:40:10'),(23,'Tio Pepe Fino En Rama','González Byass',0000,'Jerez','White','Palomino','2024-03-27 18:40:21'),(24,'Granbazán Etiqueta Ámbar','Agro de Bazán',2019,'Rías Baixas','White','Albariño','2024-03-27 18:40:27'),(25,'Mas La Plana','Torres',2016,'Penedès','Red','Cabernet Sauvignon','2024-03-27 18:40:33'),(26,'Belondrade y Lurton','Belondrade',2018,'Rueda','White','Verdejo','2024-03-27 18:40:39'),(27,'Prado Enea Gran Reserva','Bodegas Muga',2011,'Rioja','Red','Tempranillo,Garnacha,Mazuelo,Graciano','2024-03-27 18:40:44'),(28,'Espectacle','Espectacle del Montsant',2017,'Montsant','Red','Garnacha','2024-03-27 18:40:51'),(29,'Contino Reserva','Viñedos del Contino',2016,'Rioja','Red','Tempranillo,Graciano,Mazuelo,Garnacha','2024-03-27 18:41:30'),(30,'Viña Ardanza Reserva','La Rioja Alta',2010,'Rioja','Red','Tempranillo,Garnacha','2024-03-27 18:41:43'),(31,'Abadía Retuerta LeDomaine','Abadía Retuerta',2018,'Castilla y León','White','Sauvignon Blanc','2024-03-27 18:41:48'),(32,'Alion','Bodegas Alion',2016,'Ribera del Duero','Red','Tempranillo','2024-03-27 18:41:55'),(33,'Pétalos del Bierzo','Descendientes de J. Palacios',2018,'Bierzo','Red','Mencía','2024-03-27 18:42:01'),(34,'Enate Chardonnay 234','Enate',2019,'Somontano','White','Chardonnay','2024-03-27 18:42:07'),(35,'Recaredo Terrers Brut Nature Gran Reserva','Recaredo',2017,'Cava','Sparkling','Xarel·lo,Macabeo,Parellada','2024-03-27 18:42:18'),(36,'Habla del Silencio','Bodegas Habla',2018,'Extremadura','Red','Syrah,Cabernet Sauvignon,Tempranillo','2024-03-27 18:42:43'),(37,'El Transistor','Telmo Rodríguez',2019,'Rueda','White','Verdejo','2024-03-27 18:42:49');
/*!40000 ALTER TABLE `Wines` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-03-31 13:50:55
