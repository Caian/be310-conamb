-- phpMyAdmin SQL Dump
-- version 3.5.7
-- http://www.phpmyadmin.net
--
-- Host: aesculetum.db
-- Generation Time: Jun 25, 2013 at 05:18 AM
-- Server version: 5.3.12-MariaDB
-- PHP Version: 5.3.10-nfsn2

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `conamb`
--

-- --------------------------------------------------------

--
-- Table structure for table `MARKERS`
--

CREATE TABLE IF NOT EXISTS `MARKERS` (
  `uid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `uus` int(10) unsigned NOT NULL,
  `date` int(11) unsigned NOT NULL,
  `type` int(11) unsigned NOT NULL,
  `icon` int(11) unsigned NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  PRIMARY KEY (`uid`),
  KEY `uus` (`uus`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=12 ;

--
-- Triggers `MARKERS`
--
DROP TRIGGER IF EXISTS `markers_uid`;
DELIMITER //
CREATE TRIGGER `markers_uid` BEFORE INSERT ON `MARKERS`
 FOR EACH ROW begin 
      set new.uid = greatest(coalesce((select max(uid) from NEWS), 0), 
                            coalesce((select max(uid) from MARKERS), 0)) + 1;
   end
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `NEWS`
--

CREATE TABLE IF NOT EXISTS `NEWS` (
  `uid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `uus` int(10) unsigned NOT NULL,
  `date` int(11) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `text` varchar(255) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `upvt` int(11) NOT NULL,
  `dnvt` int(11) NOT NULL,
  PRIMARY KEY (`uid`),
  KEY `uus` (`uus`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=11 ;

--
-- Triggers `NEWS`
--
DROP TRIGGER IF EXISTS `news_uid`;
DELIMITER //
CREATE TRIGGER `news_uid` BEFORE INSERT ON `NEWS`
 FOR EACH ROW begin 
      set new.uid = greatest(coalesce((select max(uid) from NEWS), 0), 
                            coalesce((select max(uid) from MARKERS), 0)) + 1;
   end
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `USERS`
--

CREATE TABLE IF NOT EXISTS `USERS` (
  `uus` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `passw` varchar(32) NOT NULL,
  PRIMARY KEY (`uus`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=11 ;

--
-- Dumping data for table `USERS`
--

INSERT INTO `USERS` (`uus`, `name`, `passw`) VALUES
(1, 'Caian', 't3n15'),
(2, 'Massao', 'sailormoon'),
(3, 'Kiabo', 'rivegauche'),
(4, 'Irmao', 'vemmao');

-- --------------------------------------------------------

--
-- Table structure for table `VOTES`
--

CREATE TABLE IF NOT EXISTS `VOTES` (
  `uus` int(10) unsigned NOT NULL,
  `uid` int(10) unsigned NOT NULL,
  `dir` int(11) NOT NULL,
  PRIMARY KEY (`uus`,`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
