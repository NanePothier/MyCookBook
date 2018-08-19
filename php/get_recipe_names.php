<?php

    $json = file_get_contents('php://input');
    $object = json_decode($json);

    // get email address
    $user_email = $object->userEmail;

	include 'db_connect.php';
	
	$query = "SELECT recipe_id FROM userrecipes WHERE email_address = '$user_email'";
	$result = mysqli_query($link, $query);
	
	$response = array();

	// error_log('in cat php', 1, 'nane.pothier@gmail.com');
	
	if(mysqli_num_rows($result) > 0){
		
		while($row = mysqli_fetch_row($result)){
            
            $recipe_name_query = "SELECT recipe_name FROM recipes WHERE recipe_id = '$row'";
            $name_result = mysqli_query($link, $recipe_name_query);

            $recipe_category_query = "SELECT category_name FROM recipecategory WHERE recipe_id = '$row'";
            $cat_result = mysqli_query($link, $recipe_category_query);

            $recipe = mysqli_fetch_row($name_result);
            $recipe_category = mysqli_fetch_row($cat_result);

            $name["recipename"] = $recipe;
            $name["recipecategory"] = $recipe_category;
            array_push($response, $name);	
            
		}	
	}

	echo json_encode($response);
?>