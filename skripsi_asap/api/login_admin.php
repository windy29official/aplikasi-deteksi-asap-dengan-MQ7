<?php 
    require_once('koneksi.php');

	$username_admin	= $_REQUEST['username'];
	$password_admin	= $_REQUEST['password'];
	
	$sql = "SELECT * FROM admin 
		WHERE username = '$username_admin' 
		AND status_aktif = 'Y'"; 
	$result = $conn->query($sql);
	$row = $result->fetch_array(MYSQLI_ASSOC);
	$hashedPassword = $row["password"];
	
	if(base64_encode($password_admin) == $hashedPassword) {
		echo json_encode(array(
		    'response'=>'1',
			'idadmin'	=> $row['idadmin'],
		//	'intensitas_asap'	=> (int)$row['intensitas_asap'],
		));
  }          
  else {
	echo json_encode(array(
		    'response'=>'0',
		));
  }
	mysqli_close($conn);

 ?>