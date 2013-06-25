<?php
include_once 'db.php';
include_once 'unicli.php';

$sql = new SQL( "aesculetum.db", "conamb", "DSQ8TFqd2Gt75C3c", "conamb" );
$sql->connect();

/*
$link = mysql_connect( "aesculetum.db", "conamb", "DSQ8TFqd2Gt75C3c" );
$db = mysql_select_db( "conamb" );
mysql_query( "INSERT INTO USERS (`name`,`passw`) VALUES ('dudu','edu');", $link );
mysql_close( $link );
*/

$cmd = $_POST["cmd"];

switch ($cmd) {
case "NEAR":
    update_location($sql, $_POST["latfrom"], $_POST["lonfrom"], $_POST["latto"], $_POST["lonto"]);
    break;

case "GETD":
    update_uid($sql, $_POST["uid"]);
    break;

case "UPVT":
	set_upvote( $sql, $_POST["uus"], $_POST["passw"], $_POST["uid"] );
    break;

case "DNVT":
	set_dnvote( $sql, $_POST["uus"], $_POST["passw"], $_POST["uid"] );
    break;

case "VALU":
	$uus = validate_user($sql, $_POST["uus"], $_POST["passw"]);
	if ($uus > 1) {
		send ("UUSID ".$uus."\n");
	} else {
		send("UFAIL\n");
	}
    break;

case "POSM":
	post_marker( $sql, $_POST["uus"], $_POST["passw"], $_POST["type"], $_POST["icon"], $_POST["lat"], $_POST["lon"] );
    break;

case "POSN":
	post_news( $sql, $_POST["uus"], $_POST["passw"], $_POST["name"], $_POST["text"],  $_POST["lat"], $_POST["lon"] );
    break;
}

$sql->disconnect();

?>
