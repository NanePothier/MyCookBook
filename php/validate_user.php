<?php

	//retrieve the password for a certain email address and compare with password sent
	
	$json = file_get_contents('php://input');
	$object = json_decode($json);
	
	$username = $object->user;
	$pw = $object->password;
	
	$response["successIndicator"] = "neutral";
	
	include 'db_connect.php';
	
	$queryUser = sprintf("SELECT `password` FROM useraccount WHERE email_address = '%s'", mysqli_real_escape_string($username));
	$result = mysqli_query($link, $queryUser);
	
	//$response["successIndicator"] = $result;
	
	if(mysqli_num_rows($result) > 0){
		
		//retrieve password and compare
		$container = mysqli_fetch_field($result);
		
		$passwordDB = $container->password;
		
		//compare password to received password
		if(strcmp($passwordDB, $pw) == 0){
			
			$response["successIndicator"] = $passwordDB;
			
		}else{
			
			$response["successIndicator"] = $passwordDB;
		}
		
	}
	
	echo json_encode($response);
	
	
?>