<?php

	//class to connect to mysql database
	
	$username = "S0280202";
	$password = "New2018";
	$database = "S0280202";
	$server = "127.0.0.1:3306";
	$link = mysqli_connect($server, $username, $password, $database);
	
	
	
	/*
	class DB_CONNECT{
		
		//constructor
		function __construct() {
			$this->connect();
		}
		
		//destructor
		function __destruct() {
			$this->close();
		}
		
		function connect(){
			
			//db_config.php file is needed
			require_once __DIR__.'/db_config.php';
			
			//create connection to server
			$connection = mysqli_connect(DB_SERVER, DB_USER, DB_PASSWORD) or die(mysqli_error());
			
			//choose database to connect to
			$database = mysqli_select_db(DB_NAME) or die(mysqli_error());
			
			return $connection;
			
		}
		
		function close(){
			mysqli_close();
		}	
		
	}
	*/
	
?>