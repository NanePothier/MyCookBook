<?php

    $json = file_get_contents('php://input');
    $object = json_decode($json);

    // get email address
    $user_email = $object->userEmail;

	include 'db_connect.php';
    
    // get all recipe Id's belonging to this user
	$query = "SELECT recipe_id FROM userrecipes WHERE email_address = '$user_email'";
	$result = mysqli_query($link, $query);
    
    $categories = array();
    $recipe_names = array();
    $response = array();

	// error_log('in cat php', 1, 'nane.pothier@gmail.com');
    
    // get categories visible to all users
    $category_query = "SELECT category_name FROM categories";
    $category_result = mysqli_query($link, $category_query);

    // get all categories belonging to this user
    $user_category_query = "SELECT category_name FROM usercategory WHERE email_address = '$user_email'";
    $user_category_result = mysqli_query($link, $user_category_query);

    // add all categories for this user to one array
    if(mysqli_num_rows($category_result) > 0){

        while($value = mysqli_fetch_row($category_result)){

            array_push($categories, $value);
        }
    }

    if(mysqli_num_rows($user_category_result) > 0){

        while($val = mysqli_fetch_row($user_category_result)){

            array_push($categories, $val);
        }
    }

    // for each category
    for($x = 0; $x < count($categories; $x++)){

        // get all recipe Id's belonging to a category
        $category_recipe_query = "SELECT recipe_id FROM recipecategory WHERE category_name = '$categories[$x]'";

        if(mysqli_num_rows($category_recipe_query) > 0){

            // for each recipe Id 
            while($item = mysqli_fetch_row($category_recipe_query)){

                // retrieve the name for that recipe Id
                $recipe_name_query = "SELECT recipe_name FROM recipes WHERE recipe_id = '$item'";
                $recipe_name_result = mysqli_query($link, $recipe_name_query);

                $recipe_name = mysqli_fetch_row($recipe_name_result);

                array_push($recipe_names, $recipe_name);
            }
        }

        $category_recipe_object["category"] = $categories[$x];
        $category_recipe_object["recipeArray"] = $recipe_names;

        array_push($response, $category_recipe_object);
    }

    /*
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
    */

	echo json_encode($response);
?>