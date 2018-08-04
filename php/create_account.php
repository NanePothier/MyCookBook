<?php

	//store user information in database
	//check if email address already exists before trying to store information
	
	$json = file_get_contents('php://input');
	$object = json_decode($json);
	
	$first_name = $object->first;
	$last_name = $object->last;
	$user = $object->user;
	$pw = $object->password;
	
	$response["successIndicator"] = "neutral";
	
	include 'db_connect.php';
	
	//check if provided email address already exists in the database 
	$duplicate_query = "SELECT * FROM useraccount WHERE email_address = '$user'";
	$result_duplicate = mysqli_query($link, $duplicate_query);
	
	if(mysqli_num_rows($result_duplicate) > 0){
		
		$response["successIndicator"] = "exists";
		
	}else{
		
		$query = "INSERT INTO useraccount VALUES('$user', '$first_name', '$last_name', '$pw', now())";
		$result = mysqli_query($link, $query);
		
		if(!empty($result)){
			
			$response["successIndicator"] = "success";
			
		}else{
			
			$response["successIndicator"] = "failure";
		}
			
	}
		
	echo json_encode($response);
		
	
	
	
	
	
	
		
	
	

	/*
	$response = array();
	
	if(isset($_POST['firstname']) && isset($_POST['lastname']) && isset($_POST['emailaddress']) && isset($_POST['pass'])){
		
		$first = $_POST['firstname'];
		$last = $_POST['lastname'];
		$email = $_POST['emailaddress'];
		$password = $_POST['pass'];
		
		require_once __DIR__.'db_connect.php';
		
		$db = new DB_CONNECT();
		
		//check if provided email address already exists in the database 
		$duplicate_query = "SELECT email_address FROM useraccount WHERE email_address = '$email'";
		$result_duplicate = mysqli_query($duplicate_query);
		
		//only store if email address is unique
		if(empty($result_duplicate)){
			
			$query = "INSERT INTO useraccount VALUES($email, $first, $last, $password, now())";
			$result = mysqli_query($query);
			
			//return success if information has been stored successfully
			if(!empty($result)){
				
				$response['success'] = 1;
				$response['message'] = "User information was successfully stored."
				
				echo json_encode($response);
				
			}else{
				
				$response['success'] = 0;
				$response['message'] = "Error. User information was not stored."
				
				echo json_encode($response);
				
			}
			
		}else{
			
			//email address provided already exists, so do not store information
			$response['success'] = 0;
			$response['message'] = "User email address already exists. User information was not stored."
				
			echo json_encode($response);
		}		
		
	}
	*/

?>