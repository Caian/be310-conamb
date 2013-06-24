<!--
By Miguel
-->

<?php
class SQL{
  var $host,$user,$pass,$base;
  var $cn,$db;
  var $table;

// all functions return the query that was made
// except for connect() and disconnect(), that do not execute queries
// and select(), that must return the array with the results

  function SQL($host,$user,$pass,$base){
    $this->host = $host;
    $this->user = $user;
    $this->pass = $pass;
    $this->base = $base;
  }

  // removes possible SQL injection from the query
  function clean_query($query){
    return mysql_real_escape_string($query);
  }

  // connects, duh
  function connect(){
    $this->cn = mysql_connect($this->host, $this->user, $this->pass) or die ("<br><br><center>Problem connecting to server: " . mysql_error() . "</center>");
    $this->db = mysql_select_db($this->base) or die ("<br><br><center>Problem selecting database: " . mysql_error() . "</center>");
  }

  // inserts an element into $table with the respective values for each given field
  function insert($table,$field_names,$field_values){
    $query = "INSERT INTO ".$table." (";

    foreach($field_names as &$name){
      $query = $query."`".$name."`,";
    }

    $query = eregi_replace(',$', '', $query);
    $query = $query.") VALUES (";

    foreach($field_values as &$value){
      if($value == ""){
        $query = $query."NULL,";
      }else{
        $query = $query."'".$value."',";
      }
    }

    $query = eregi_replace(',$', '', $query);    
    $query = $query.")";

    mysql_query($query);
    $id = mysql_insert_id();

    return $id;
  }

  // removes from $table elements that match the values given
  function remove($table,$field_names,$field_values){
    $query = "DELETE FROM ".$table." WHERE ";

    foreach ($field_names as $i => $name) {
      $value = $field_values[$i];
      $query = $query."`".$name."`='".$value."' AND ";
    }

    $query = eregi_replace('(AND )$', '', $query);

    return mysql_query($query);
  }

  // return an array with all elements that match the given values
  function select($table,$selected_fields,$field_names,$field_values,$andor){
    $query = "SELECT ";

    if(count($selected_fields) != 0){
      foreach($selected_fields as &$selected_field){
        $query = $query."`".$selected_field."`,";
      }
      $query = eregi_replace(',$','',$query);
    }else{
      $query = $query."*";
    }

    $query = $query." FROM ".$table;

    if(count($field_names) != 0){
      $query = $query." WHERE ";
      
      foreach ($field_names as $i => $name) {
        $value = $field_values[$i];
        $query = $query."`".$name."`='".$value."' ".$andor." ";
      }
      
      $query = eregi_replace('('.$andor.' )$', '', $query);
    }

    return mysql_query($query);
  }

  // update certain values of the elements that match another set of values
  function update($table,$field_names,$field_values,$new_field_names,$new_field_values){
    $query = "UPDATE ".$table." SET ";

    foreach ($new_field_names as $i => $name) {
      $value = $new_field_values[$i];
      $query = $query."`".$name."`='".$value."',";
    }
    $query = eregi_replace(',$', '', $query);

    $query = $query." WHERE ";

    foreach ($field_names as $i => $name) {
      $value = $field_values[$i];
      $query = $query."`".$name."`='".$value."' AND ";
    }
    $query = eregi_replace('(AND )$', '', $query);

    mysql_query($query);
    return $query;
  }


  // disconnects, duh
  function disconnect(){
    mysql_close($this->cn);
  }

  // ------------------------specific function for the CONAMB project ------------

  // Get markers and news inside a given area
  function getMarkersAndNews ($latfrom, $lonfrom, $latto, $lonto) {
      $query = "SELECT uid, date FROM MARKERS WHERE lat > " . $latfrom . " AND lat < " . $latto " AND lon > " . $lonfrom " AND lon < " > $lonto . ".";

      $resultM = mysql_query($query);
      
      $query = "SELECT uid, date FROM NEWS WHERE lat > " . $latfrom . " AND lat < " . $latto " AND lon > " . $lonfrom " AND lon < " > $lonto . ".";
      
      $resultN = mysql_query($query);

      return array ($resultM, $resultN);
  }
}
?>