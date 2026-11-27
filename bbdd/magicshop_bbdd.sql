-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: magicshop
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `magicshop`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `magicshop` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;

USE `magicshop`;

--
-- Table structure for table `categorias`
--

DROP TABLE IF EXISTS `categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `categorias` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(500) DEFAULT NULL,
  `imagen` varchar(500) DEFAULT NULL,
  `nombre` varchar(80) NOT NULL,
  `slug` varchar(50) NOT NULL,
  `tipo_api` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7a8rqmhb8umdlquyud4j0j2xt` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categorias`
--

LOCK TABLES `categorias` WRITE;
/*!40000 ALTER TABLE `categorias` DISABLE KEYS */;
INSERT INTO `categorias` VALUES (1,'Seres que combaten por ti en el campo de batalla.','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=130550&type=card','Criaturas','criaturas','Creature'),(2,'Hechizos que puedes lanzar en cualquier momento.','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=132106&type=card','Instantáneos','instantaneos','Instant'),(3,'Hechizos poderosos para tu fase principal.','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129711&type=card','Conjuros','conjuros','Sorcery'),(4,'Efectos permanentes que cambian el juego.','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129710&type=card','Encantamientos','encantamientos','Enchantment'),(5,'Objetos mágicos y equipamiento.','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129466&type=card','Artefactos','artefactos','Artifact'),(6,'La fuente de todo el maná.','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129458&type=card','Tierras','tierras','Land');
/*!40000 ALTER TABLE `categorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orden_items`
--

DROP TABLE IF EXISTS `orden_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orden_items` (
  `pk` bigint(20) NOT NULL AUTO_INCREMENT,
  `cantidad` int(11) DEFAULT NULL,
  `categoria` varchar(50) DEFAULT NULL,
  `imagen` varchar(500) DEFAULT NULL,
  `nombre` varchar(150) DEFAULT NULL,
  `precio_unitario` int(11) DEFAULT NULL,
  `producto_id` varchar(64) NOT NULL,
  `subtotal` int(11) DEFAULT NULL,
  `orden_id` varchar(36) NOT NULL,
  PRIMARY KEY (`pk`),
  KEY `FKaod0hti8wc34ydrspxdy3p1u` (`orden_id`),
  CONSTRAINT `FKaod0hti8wc34ydrspxdy3p1u` FOREIGN KEY (`orden_id`) REFERENCES `ordenes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orden_items`
--

LOCK TABLES `orden_items` WRITE;
/*!40000 ALTER TABLE `orden_items` DISABLE KEYS */;
INSERT INTO `orden_items` VALUES (1,1,'criaturas','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129465&type=card','Angel of Mercy',1125,'57aaebc1-850c-503d-9f6e-bb8d00d8bf7c',1125,'42121379-23e2-47e3-8e4a-d97e7451f765'),(2,2,'criaturas','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129465&type=card','Angel of Mercy',1125,'57aaebc1-850c-503d-9f6e-bb8d00d8bf7c',2250,'b12f5729-35b5-4d31-b67e-8c2ed98fc2c5');
/*!40000 ALTER TABLE `orden_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ordenes`
--

DROP TABLE IF EXISTS `ordenes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ordenes` (
  `id` varchar(36) NOT NULL,
  `cliente_apellidos` varchar(80) DEFAULT NULL,
  `cliente_email` varchar(120) DEFAULT NULL,
  `cliente_nombre` varchar(80) DEFAULT NULL,
  `despacho_calle` varchar(200) DEFAULT NULL,
  `despacho_comuna` varchar(100) DEFAULT NULL,
  `despacho_departamento` varchar(50) DEFAULT NULL,
  `despacho_indicaciones` varchar(300) DEFAULT NULL,
  `despacho_region` varchar(100) DEFAULT NULL,
  `estado` varchar(12) NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `motivo` varchar(300) DEFAULT NULL,
  `numero` varchar(20) NOT NULL,
  `total` int(11) NOT NULL,
  `usuario_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordenes`
--

LOCK TABLES `ordenes` WRITE;
/*!40000 ALTER TABLE `ordenes` DISABLE KEYS */;
INSERT INTO `ordenes` VALUES ('42121379-23e2-47e3-8e4a-d97e7451f765','Palma','comprador@magicshop.com','Felipe','Los Crisantemos 603','Providencia','','','Región Metropolitana de Santiago','pagada','2026-11-27 00:32:12.000000','','202611262369',1125,2),('b12f5729-35b5-4d31-b67e-8c2ed98fc2c5','Palma','comprador@magicshop.com','Felipe','Los Crisantemos 603','Providencia','','','Región Metropolitana de Santiago','pagada','2026-11-27 00:33:45.000000','','202611265566',2250,2);
/*!40000 ALTER TABLE `ordenes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `productos` (
  `id` varchar(64) NOT NULL,
  `categoria` varchar(50) DEFAULT NULL,
  `edicion` varchar(100) DEFAULT NULL,
  `eliminado` bit(1) NOT NULL,
  `imagen` varchar(500) DEFAULT NULL,
  `nombre` varchar(150) NOT NULL,
  `oferta` int(11) NOT NULL,
  `precio` int(11) NOT NULL,
  `rareza` varchar(30) DEFAULT NULL,
  `stock` int(11) NOT NULL,
  `tipo` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES ('11727081-4070-56db-8162-b970dd7f94bc','tierras','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129458&type=card','Adarkar Wastes',0,3850,'Rare',6,'Land'),('2f9c211e-1869-5b3f-94ea-f73b7910a5af','instantaneos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129524&type=card','Demystify',0,900,'Common',15,'Instant'),('36fcaa1e-64e3-56e3-950c-907db167e41f','encantamientos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=132090&type=card','Heart of Light',0,550,'Common',2,'Enchantment — Aura'),('3785490a-01f5-511d-b471-60b1209b3d4f','artefactos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=135279&type=card','Chromatic Star',35,1400,'Uncommon',13,'Artifact'),('3b77bb52-4181-57f5-b3cd-f3a15b95aa29','encantamientos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129710&type=card','Angelic Chorus',0,3550,'Rare',2,'Enchantment'),('3c0d065a-4aa4-55f4-bc11-bd326d23e5a6','conjuros','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129808&type=card','Wrath of God',0,3850,'Rare',14,'Sorcery'),('4fa9bf42-9170-594e-bbf9-42111adea313','conjuros','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129698&type=card','Reminisce',0,1550,'Uncommon',8,'Sorcery'),('546eac7c-1424-597d-ac13-bf8558e88fe3','encantamientos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129572&type=card','Glorious Anthem',0,3950,'Rare',6,'Enchantment'),('55bd38ca-dc73-5c06-8f80-a6ddd2f44382','conjuros','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129711&type=card','Angelic Blessing',0,800,'Common',15,'Sorcery'),('56f4935b-f6c5-59b9-88bf-9bcce20247ce','instantaneos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=130553&type=card','Beacon of Immortality',15,3500,'Rare',5,'Instant'),('57aaebc1-850c-503d-9f6e-bb8d00d8bf7c','criaturas','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129465&type=card','Angel of Mercy',25,1500,'Uncommon',6,'Creature — Angel'),('5f8287b1-5bb6-5f4c-ad17-316a40d5bb0c','criaturas','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=130550&type=card','Ancestor\'s Chosen',0,1450,'Uncommon',10,'Creature — Human Cleric'),('62365fbb-af07-5c2a-976d-e3092bdfc317','criaturas','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=136279&type=card','Benalish Knight',0,550,'Common',16,'Creature — Human Knight'),('668c64a2-cecb-5f48-af16-d7a64814d3e7','criaturas','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=135258&type=card','Field Marshal',25,3900,'Rare',9,'Creature — Human Soldier'),('6d268c95-c176-5766-9a46-c14f739aba1c','instantaneos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=132106&type=card','Bandage',0,700,'Common',7,'Instant'),('7e457744-dd60-5fd0-ae58-1718ef65c1a1','conjuros','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=130542&type=card','Flowstone Slide',0,3500,'Rare',7,'Sorcery'),('7fef665c-36a1-5f7a-9299-cf8938708710','instantaneos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=130528&type=card','Condemn',15,1600,'Uncommon',5,'Instant'),('81daea6a-2735-5a46-a2da-b65a2ad5738f','criaturas','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=130554&type=card','Cho-Manno, Revolutionary',0,3950,'Rare',8,'Legendary Creature — Human Rebel'),('8ac972b5-9f6e-5cc8-91c3-b9a40a98232e','criaturas','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129470&type=card','Aven Cloudchaser',0,950,'Common',16,'Creature — Bird Soldier'),('9bee1901-1125-5635-9e22-3b4f37989c37','criaturas','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129595&type=card','Honor Guard',0,950,'Common',10,'Creature — Human Soldier'),('a69b404f-144a-5317-b10e-7d9dce135b24','criaturas','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129477&type=card','Ballista Squad',0,1400,'Uncommon',15,'Creature — Human Rebel'),('a807430a-5a37-550c-989f-72d125d7b0c8','tierras','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129497&type=card','Caves of Koilos',35,3500,'Rare',2,'Land'),('b0a0f3ea-f483-53e3-ae41-bdb409141fdf','artefactos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=135244&type=card','Citanul Flute',25,3700,'Rare',9,'Artifact'),('c19bcfc2-fc10-5040-8239-1c193098df47','instantaneos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=132107&type=card','Hail of Arrows',0,1550,'Uncommon',6,'Instant'),('ca560ce6-8093-51e9-9b7f-2c11756d6516','tierras','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129496&type=card','Brushland',0,3550,'Rare',2,'Land'),('d68306e2-9877-5987-84b3-12b8234c8eec','criaturas','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=132105&type=card','Ghost Warden',0,900,'Common',11,'Creature — Spirit'),('da0a5791-2fc2-53e4-bc8b-c4d8cd026ded','artefactos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129466&type=card','Angel\'s Feather',35,1500,'Uncommon',13,'Artifact'),('da8f1e81-fc1f-57f4-b4ec-0c5445a299e8','artefactos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=135254&type=card','Chimeric Staff',25,3800,'Rare',9,'Artifact'),('dec9c4f1-455e-56de-aad4-9a1d18cf5588','tierras','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129479&type=card','Battlefield Forge',0,3550,'Rare',12,'Land'),('eb5c94c7-5494-5669-9c54-6f3672d65683','conjuros','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=134757&type=card','Counsel of the Soratami',0,550,'Common',2,'Sorcery'),('fac6ad26-f8c2-51bd-9f6a-a1b0940b4cef','encantamientos','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=132127&type=card','Aura of Silence',0,1550,'Uncommon',14,'Enchantment'),('fadda48c-6226-5ac5-a2b9-e9170d2017cd','criaturas','Tenth Edition','\0','https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=129671&type=card','Angelic Wall',0,650,'Common',12,'Creature — Wall');
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usuarios` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `apellidos` varchar(80) DEFAULT NULL,
  `comuna` varchar(100) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `email` varchar(120) NOT NULL,
  `nombre` varchar(80) NOT NULL,
  `password` varchar(100) NOT NULL,
  `region` varchar(100) DEFAULT NULL,
  `rol` varchar(10) NOT NULL,
  `run` varchar(12) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkfsp0s1tflm1cwlj8idhqsad0` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'Magic-Shop','Santiago','Av. Libertador 1234','admin@magicshop.com','Admin','$2a$10$9/FZJiHtJvTjPTQyvAl1quczO8Ym9GgTEPTACgfncgyI.whg3m/UO','Región Metropolitana de Santiago','admin','11111111-1'),(2,'Palma','Providencia','Los Crisantemos 603','comprador@magicshop.com','Felipe','$2a$10$PBH11BcoqxZ0wrxhteW7de8PYJqVGP4P1y4G9MIjzmZqPo02jA6oK','Región Metropolitana de Santiago','cliente','12345678-5');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-26 23:06:06
