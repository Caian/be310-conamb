<?php

function update_location ($latfrom, $lonfrom, $latto, $lonto) {
    $results = $sql->getMarkersAndNews ($latfrom, $lonfrom, $latto, $lonto);

    while ($row = mysql_fetch_array($results[0])) {
        $response = "NEAR M " . $row["uid"] . " " . $row["date"] . "\n";
        send ($response);
    }

    while ($row = mysql_fetch_array($results[1])) {
        $response = "NEAR N " . $row["uid"] . " " . $row["date"] . "\n";
        send ($response);
    }

    send ("EORQ\n");
}

function update_uid ($uid) {
    $result = $sql->select("MARKERS", array("uid", "date", "type", "icon", "lat", "lon"), array("uid"), array($uid), "");

    while ($row = mysql_fetch_array($result)) {
        $response = "MARK " . $row["uid"] . " " . $row["date"] . " " . $row["type"] . " " . $row["icon"] . " " . $row["lat"] . " " . $row["lon"] . "\n";
        send ($response);
    }

    $result = $sql->select("NEWS", array("uid", "date", "lat", "lon", "upvt", "dnvt", "name", "text"), array("uid"), array($uid), "");

    while ($row = mysql_fetch_array($result)) {
        $response = "MARK " . $row["uid"] . " " . $row["date"] . " " . $row["lat"] . " " . $row["lon"] . " " . $row["upvt"] . " " . $row["dnvt"] . " " . $row["name"]  . " " . $row["text"] . "\n";
        send ($response);
    }
}



/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * 	function name:		set_upvote
 * 	parameters:			$username	(self-explanatory),
 * 						$password	(self-explanatory),
 * 						$uid		(unique identifier of the item being
 * 									 up-voted)
 * 	return:				none
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
function set_upvote( $username, $password, $uid )
{
	if ( $uus = validate_user( $username, $password ) <= 0 ) {
		return;
	}
	$count = $sql->vote( $uus, $uid, 1 );
	if ( $count == 1) {
		update_uid( $uid );
	}
}



/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * 	function name:		set_dnvote
 * 	parameters:			$username	(self-explanatory),
 * 						$password	(self-explanatory),
 * 						$uid		(unique identifier of the item being
 * 									 down-voted)
 * 	return:				none
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
function set_dnvote( $username, $password, $uid )
{
	if ( $uus = validate_user( $username, $password ) <= 0 ) {
		return;
	}
	$count = $sql->vote( $uus, $uid, -1 );
	if ( $count == 1) {
		update_uid( $uid );
	}
}


function validate_user ($username, $password) {
    $result = $sql->select ("USERS", array("uus"), array("name", "passw"), array($username, $password), "");

    $uus = -1;
    $uuc = 0;

    while ($row = mysql_fetch_array($result)) {
        $uuc++;

        if ($uus == -1)
            $uus = $row["uus"];
    }

    if ($uuc > 1)
        //echo "Warning: Duplicated user detected in utalbe";

    if ($uus == 1) {
        //echo "Error: Blocked connection as administrator";
        $uus = -1;
    }

	return $uss;
}

function post_news( $username, $password, $nname, $ntext, $lat, $lon ){
	if( ($uus = validate_user($username, $password)) <= 0 ){
		// something about authentication failure here
	}

	$utcd = gettimeofday();

	$uid = $sql->insert( "NEWS", array("uus", "date", "name", "text", "lat", "lon", "upvt", "dnvt"), array($uus, $utcd['sec'], $nname, $ntext, $lat, $lon, 0, 0) );

	update_uid($uid);
}

function post_marker( $username, $password, $type, $icon, $lat, $lon ){
	if( ($uus = validate_user($username, $password)) <= 0 ){
		// something about authentication failure here
	}

	$utcd = gettimeofday();

	$uid = $sql->insert( "MARKERS", array("uus", "date", "type", "icon", "lat", "lon"), array($uus, $utcd['sec'], $type, $icon, $lat, $lon) );

	update_uid($uid);
}

/*
 * Envia uma string ao cliente.
 */
function send ($str) {
    echo $str;
}


?>
