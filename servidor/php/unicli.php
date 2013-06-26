<?php

function update_location ($sql, $latfrom, $lonfrom, $latto, $lonto) {
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

function update_uid ($sql, $uid) {
    $result = $sql->select("MARKERS", array("uid", "date", "type", "icon", "lat", "lon"), array("uid"), array($uid), "AND");

	while ($row = mysql_fetch_array($result)) {
		$response = "MARK " . $row["uid"] . " " . $row["date"] . " " . $row["type"] . " " . $row["icon"] . " " . $row["lat"] . " " . $row["lon"] . "\n";
		send ($response);
	}

    $result = $sql->select("NEWS", array("uid", "date", "name", "text", "lat", "lon", "upvt", "dnvt"), array("uid"), array($uid), "AND");

	while ($row = mysql_fetch_array($result)) {
		$response = "NEWS " . $row["uid"] . " " . $row["date"] . " " . $row["lat"] . " " . $row["lon"] . " " . $row["upvt"] . " " . $row["dnvt"] . " " . $row["name"]  . " " . $row["text"] . "\n";
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
function set_upvote( $sql, $username, $password, $uid )
{
	if ( ($uus = validate_user( $sql, $username, $password )) <= 0 ) {
		error_log("Authentication failure from " . $_SERVER['REMOTE_ADDR'] . "\n", 3, "conamb.log");
		return;
	}
    
	$count = $sql->vote( $uus, $uid, 1 );
	error_log("Up voted " . $uid . " from " . $_SERVER['REMOTE_ADDR'] . "\n", 3, "conamb.log");
	if ( $count == 1) {
		update_uid( $sql, $uid );
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
function set_dnvote( $sql, $username, $password, $uid )
{
	if ( ($uus = validate_user( $sql, $username, $password )) <= 0 ) {
		error_log("Authentication failure from " . $_SERVER['REMOTE_ADDR'] . "\n", 3, "conamb.log");
		return;
	}
    
	$count = $sql->vote( $uus, $uid, -1 );
	error_log("Down voted " . $uid . " from " . $_SERVER['REMOTE_ADDR'] . "\n", 3, "conamb.log");
	if ( $count == 1) {
		update_uid( $sql, $uid );
	}
}


function validate_user ($sql, $username, $password) {
    $result = $sql->select ("USERS", array("uus"), array("name", "passw"), array($username, $password), "AND");

	if( $result == false ) echo mysql_error();

    $uus = -1;
    $uuc = 0;

	if ($row = mysql_fetch_array($result)) {
		$uuc++;

		if ($uus == -1){
			$uus = $row["uus"];
		}
	}

    if ($uuc > 1) {
	error_log("Warning: Duplicated user " . $username . "\n", 3, "conamb.log");
	}
    if ($uus == 1) {
	error_log("Blocked connection as administrator from " . $_SERVER['REMOTE_ADDR'] . "\n", 3, "conamb.log");
        $uus = -1;
    }
    
	return $uus;
}

function post_news( $sql, $username, $password, $nname, $ntext, $lat, $lon ){
	if( ($uus = validate_user($sql, $username, $password)) <= 0 ){
		error_log("Authentication failure from " . $_SERVER['REMOTE_ADDR'] . "\n", 3, "conamb.log");
	}
	else{
		$utcd = gettimeofday();

		$uid = $sql->insert( "NEWS", array("uus", "date", "name", "text", "lat", "lon", "upvt", "dnvt"), array($uus, $utcd['sec'], $nname, $ntext, $lat, $lon, "0", "0") );
		error_log("NEWS " . $uid . " inserted from " . $_SERVER['REMOTE_ADDR'] . "\n", 3, "conamb.log");

		update_uid($sql, $uid);
	}
}

function post_marker( $sql, $username, $password, $type, $icon, $lat, $lon ){
	if( ($uus = validate_user($sql, $username, $password)) <= 0 ){
		error_log("Authentication failure from " . $_SERVER['REMOTE_ADDR'] . "\n", 3, "conamb.log");
	}
	else{
		$utcd = gettimeofday();

		$uid = $sql->insert( "MARKERS", array("uus", "date", "type", "icon", "lat", "lon"), array($uus, $utcd['sec'], $type, $icon, $lat, $lon) );
		error_log("MARKER " . $uid . " inserted from " . $_SERVER['REMOTE_ADDR'] . "\n", 3, "conamb.log");		

		update_uid($sql, $uid);
	}
}

/*
 * Envia uma string ao cliente.
 */
function send ($str) {
    echo $str;
}


?>
