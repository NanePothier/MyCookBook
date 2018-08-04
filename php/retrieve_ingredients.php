<?php


	include 'db_connect.php';
	
	$query = "SELECT ingredient_name FROM ingredients";
	$result = mysqli_query($link, $query);
	
	//$hello["success"] = "no";
	
	$response = array();
	
	if(mysqli_num_rows($result) > 0){
		
		//$hello["success"] = "yes";
		
		
		while($row = mysqli_fetch_row($result)){
			
			$hello["ingredient"] = $row;
			array_push($response, $hello);
			
		}
		
		
	}

	echo json_encode($response);



?>
