<?php
define('HOST', 'localhost');
define('USER', 'root');
define('PASS','');
define('DB','skripsi_asap');
$conn = mysqli_connect(HOST,USER,PASS,DB) or die('Unable to Connect');

date_default_timezone_set("ASIA/JAKARTA");

function tanggal_indo($tanggal)
{
	$bulan = array (1 =>   'Januari', 'Februari', 'Maret', 'April', 'Mei', 'Juni', 'Juli', 'Agustus', 'September', 'Oktober', 'November', 'Desember'
);
	$split = explode('-', $tanggal);
	return $split[2] . ' ' . $bulan[ (int)$split[1] ] . ' ' . $split[0];
}
$hari_indo = array ( 1 =>    'Senin', 'Selasa', 'Rabu', 'Kamis', 'Jumat', 'Sabtu', 'Minggu'
);
?>