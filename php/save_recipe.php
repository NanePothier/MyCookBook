<?php

    $json = file_get_contents('php://input');
    $object = json_decode($json);
    
    // get data from passed object
    $user_email = $object->userEmail;
    $unique_ID = $object->unique;
    $recipe_name = $object->name;
    $ingredients = $object->ingredientObject;
    $prim_category = $object->primCategory;
    $prep_time = $object->prepTime;
    $oven_time = $object->ovenTime;
    $oven_temp = $object->ovenTemp;
    $servings = $object->servings;
    $calories = $object->calories;
    $num_ingredients = $object->numIngredients;
    $instructions = $object->instructions;

    $success_string = 'success';

    $response["successIndicator"] = 'neutral';

    include 'db_connect.php';

    $recipe_query = "INSERT INTO recipes VALUES('$unique_ID')";
    $recipe_result = mysqli_query($link, $recipe_query);

    /*
    // store recipe
    $recipe_query = "INSERT INTO recipes VALUES('$unique_ID', '$recipe_name', '$servings', '$prep_time', '$total_time', 
                                                '$oven_time', '$oven_temp', '$num_ingredients', '$calories', '$instructions', now())";
    $recipe_result = mysqli_query($link, $recipe_query);

    // store ingredients
    $recipe_ingredient_query = "INSERT INTO recipeingredients VALUES('$unique_ID', '$ingredients')";
    $ingredient_result = mysqli_query($link, $recipe_ingredient_query);

    // store user recipe connection
    $user_query = "INSERT INTO userrecipes VALUES('$user_email', '$unique_ID')";
    $user_recipe_result = mysqli_query($link, $user_query);

    // store recipe and category connection
    $category_query = "INSERT INTO recipecategory VALUES('$unique_ID', '$prim_category', 'y')";
    $category_result = mysqli_query($link, $category_query);

    // store user and category connection
    $user_category_query = "INSERT INTO usercategory VALUES('$user_email', '$prim_category')";
    $user_category_result = mysqli_query($link, $user_category_query);
    */
    
    // check if queries were successful
    if(!empty($recipe_result)){

        $success_string = $success_string + '1';
        $response["successIndicator"] = $success_string;
    }

    if(!empty($ingredient_result)){

        $success_string = $success_string + '2';
        $response["successIndicator"] = $success_string;
    }

    if(!empty($user_recipe_result)){

        $success_string = $success_string + '3';
        $response["successIndicator"] = $success_string;
    }

    if(!empty($category_result)){

        $success_string = $success_string + '4';
        $response["successIndicator"] = $success_string;
    }

    if(!empty($user_category_result)){

        $success_string = $success_string + '5';
        $response["successIndicator"] = $success_string;
    }

    echo json_encode($response);
    
?>