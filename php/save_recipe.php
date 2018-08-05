<?php

    $json = file_get_contents('php://input');
    $object = json_decode($json);
    
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

    $response["successIndicator"] = "neutral";

    include 'db_connect.php';

    $recipe_query = "INSERT INTO recipes VALUES()";
    $user_query = "INSERT INTO ? VALUES()";
    $category_query = "INSERT INTO ? VALUES()";







?>