<?php
class dht22{
 public $link='';
 function __construct($asap){
  $this->connect();
  $this->storeInDB($asap);
 }
 
 function connect(){
  $this->link = mysqli_connect('localhost','root','') or die('Cannot connect to the DB');
  mysqli_select_db($this->link,'skripsi_asap') or die('Cannot select the DB');
 }
 
 function storeInDB($asap){
  $query = "insert into data_asap set asap='".$asap."'";
  $result = mysqli_query($this->link,$query) or die('Errant query:  '.$query);
  if($result === TRUE){echo "Data Tersimpan";}else{echo "Gagal Menyimpan data";}
 }
 
}
if($_GET['asap'] != ''){
 $dht22=new dht22($_GET['asap']);
}

?>