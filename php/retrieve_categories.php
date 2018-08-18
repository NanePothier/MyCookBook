<?php

	include 'db_connect.php';
	
	$query = "SELECT category_name FROM categories";
	$result = mysqli_query($link, $query);
	
	$response = array();

	// error_log('in cat php', 1, 'nane.pothier@gmail.com');
	
	if(mysqli_num_rows($result) > 0){
		
		while($row = mysqli_fetch_row($result)){
			
			$cat["category"] = $row;
			array_push($response, $cat);	
		}	
	}

	echo json_encode($response);
?>
