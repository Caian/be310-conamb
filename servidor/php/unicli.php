<?php
$cmd = $_POST["cmd"];

switch ($cmd) {
case "NEAR":
    update_location($_POST["latfrom"], $_POST["lonfrom"], $_POST["latto"], $_POST["lonto"]);
    break;

case "GETD":
    update_uid($_POST["uid"]);
    break;

case "UPVT":
    break;

case "DNVT":
    break;

case "VALU":
    break;

case "POSM":
    break;

case "POSN":
    break;
}

function update_location ($latfrom, $lonfrom, $latto, $lonto) {
    $results = $sql->getMarkersAndNews ($latfrom, $lonfrom, $latto, $lonto);

    while ($row = mysql_fetch_array($results[0])) {
        $response = "NEAR M " . $row["uid"] . " " . $row["date"] . "\n";
        echo "Sending " $response;
        send ($responsta);
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

function set_upvote ($username, $password, $uid) {
    var $uus;
    
    if ($uus = validate_user($username, $password) <= 0) {
        echo "Invalid authentication";
        return;
    }

    $result = $sql->select ("VOTES", array("dir"), array("uid", "uus"), array($uid, $uus), "");


    /////////////////////////////////  TERMINAR ESSE MÉTODO!!!!!!!!!!!!!!!!!!!!!!!!!!!  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
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
        echo "Warning: Duplicated user detected in utalbe";

    if ($uus == 1) {
        echo "Error: Blocked connection as administrator";
        $uus = -1;
    }

    return $uus;
}

// descobrir como envia
function send ($str) {
    echo $str; 
}


?>