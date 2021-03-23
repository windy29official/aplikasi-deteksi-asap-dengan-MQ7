<?php 
require_once('koneksi.php');

if (empty($_REQUEST['batas'])){
    $batas = " ";
} else {
    $batas = $_REQUEST['batas'];
    $batas = " LIMIT ".$batas;
}

if(empty($_REQUEST['idasap'])){
	if ($_REQUEST['waktu'] == 'Y'){
		$waktu = '';
	//	WHERE DATE(waktu) = CURRENT_DATE()
	} else {
		$waktu_awal = $_REQUEST['waktu_awal'];
		$waktu_akhir = $_REQUEST['waktu_akhir'];
		$waktu = "WHERE DATE(waktu) BETWEEN '$waktu_awal' AND '$waktu_akhir'";
	}
	
	$query = mysqli_query($conn, "SELECT * FROM data_asap $waktu ORDER BY idasap DESC $batas");

	$result = array();
	while($row = mysqli_fetch_array($query)){
		$jumlah_aman = mysqli_query($conn, "SELECT COUNT(*) AS jumlah_aman FROM data_asap $waktu AND asap <= 40")->fetch_assoc();
		$jumlah_bahaya = mysqli_query($conn, "SELECT COUNT(*) AS jumlah_bahaya FROM data_asap $waktu AND asap > 40")->fetch_assoc();
		array_push($result,array(
		    'idasap' => $row['idasap'],
			'asap' => $row['asap'],
			'waktu' => $row['waktu'],
			'jumlah_aman' => (int)$jumlah_aman['jumlah_aman'],
			'jumlah_bahaya' => (int)$jumlah_bahaya['jumlah_bahaya'],
		));
	}
	
	echo json_encode(array(
		'result'	=> $result,
	));
} else {
    $idasap = $_REQUEST['idasap'];
	$query = mysqli_query($conn, "SELECT * FROM data_asap where idasap = '$idasap'");

	$result = array();
	while($row = mysqli_fetch_array($query)){
		array_push($result,array(
		    'idasap' => $row['idasap'],
			'asap' => $row['asap'],
			'waktu' => $row['waktu'],
		));
	}
	
	echo json_encode(array(
		'result'	=> $result
	));
}
mysqli_close($conn);
?>