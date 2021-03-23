<?php 
require_once('koneksi.php');

$query = mysqli_query($conn, "SELECT * FROM intensitas_asap");

$result = array();
while($row = mysqli_fetch_array($query)){
	array_push($result,array(
		'intensitas_asap' => (int)$row['intensitas_asap'],
	));
}

echo json_encode(array(
	'result'	=> $result,
));
mysqli_close($conn);
?>