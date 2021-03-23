<?php 
require_once('koneksi.php');

$waktu = $_REQUEST['waktu'];
if (empty($_REQUEST['batas'])){
    $batas = " ";
} else {
    $batas = $_REQUEST['batas'];
    $batas = " LIMIT ".$batas;
}

if ($waktu == 'N'){
    $waktu = '';
    $waktu_jumlah = ' AND DATE(waktu) = CURRENT_DATE()';
} else {
    $waktu_awal = $_REQUEST['waktu_awal'];
	$waktu_akhir = $_REQUEST['waktu_akhir'];
    $waktu = "WHERE DATE(waktu) BETWEEN '$waktu_awal' AND '$waktu_akhir'";
    $waktu_jumlah = "AND DATE(waktu) BETWEEN '$waktu_awal' AND '$waktu_akhir'";
}
	
$query = mysqli_query($conn, "SELECT * FROM data_asap $waktu ORDER BY idasap DESC $batas");
$result = array();
while($row = mysqli_fetch_array($query)){
    $jumlah_aman = mysqli_query($conn, "SELECT COUNT(*) AS jumlah_aman FROM data_asap WHERE asap <= 40 $waktu_jumlah")->fetch_assoc();
    $jumlah_bahaya = mysqli_query($conn, "SELECT COUNT(*) AS jumlah_bahaya FROM data_asap WHERE asap > 40 $waktu_jumlah")->fetch_assoc();
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
mysqli_close($conn);
?>