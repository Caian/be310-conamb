<?php
include_once 'db.php';
include_once 'unicli.php';

$sql = new SQL( "aesculetum.db", "conamb", "e37RvmfcdAR5FncK", "conamb" );

$cmd = $_POST["cmd"];

switch ($cmd) {
case "NEAR":
    update_location($_POST["latfrom"], $_POST["lonfrom"], $_POST["latto"], $_POST["lonto"]);
    break;

case "GETD":
    update_uid($_POST["uid"]);
    break;

case "UPVT":
	set_upvote( $_POST["uus"], $_POST["passw"], $_POST["uid"] );
    break;

case "DNVT":
	set_dnvote( $_POST["uus"], $_POST["passw"], $_POST["uid"] );
    break;

case "VALU":
	$uus = validate_user($_POST["uus"], $_POST["passw"]);
	if ($uus > 1) {
		send ("UUSID ".$uus."\n");
	} else {
		send("UFAIL\n");
	}
    break;

case "POSM":
	post_marker( $_POST["uus"], $_POST["passw"], $_POST["type"], $_POST["icon"], $_POST["lat"], $_POST["lon"] );
    break;

case "POSN":
	post_news( $_POST["uus"], $_POST["passw"], $_POST["name"], $_POST["text"],  $_POST["lat"], $_POST["lon"] );
    break;
}

echo "PASSEI";
?>
