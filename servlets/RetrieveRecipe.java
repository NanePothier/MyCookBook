import java.io.BufferedReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Servlet implementation class RetrieveRecipe
 */

@WebServlet("/RetrieveRecipe")
public class RetrieveRecipe extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");

   
    public RetrieveRecipe() {
       
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = null;
		String username;
		String recipe;
		String recipeId;
		String result = "";
		JSONObject responseObject = new JSONObject();
		JSONArray jsonArrayCategories, jsonArrayIngredients;
		Connection connection = null;
		Statement recipeQuery = null;
		ResultSet recipeResultSet = null;
		
		try{
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {			
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			username = jsonObject.getString("email");
			recipe = jsonObject.getString("recipe");
			recipeId = jsonObject.getString("id");
			
			LOGGER.info("username: " + username);
			LOGGER.info("recipe name: " + recipe);
			LOGGER.info("id: " + recipeId);
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			// retrieve general recipe details
			recipeQuery = connection.createStatement();
			String sqlRecipe = "SELECT * FROM recipes WHERE recipe_id = '" + recipeId + "'";
			recipeResultSet = recipeQuery.executeQuery(sqlRecipe);
			
			// add results of queries to responseObject
			if(recipeResultSet.first()) {
				
				responseObject = addGeneralRecipeToJsonResponseObject(recipeResultSet, responseObject);
				recipeQuery.close();
			}
			
			// retrieve ingredients for this recipe
			recipeQuery = connection.createStatement();
			String sqlIngredients = "SELECT * FROM recipeingredients WHERE recipe_id = '" + recipeId + "'";
			recipeResultSet = recipeQuery.executeQuery(sqlIngredients);
			
			if(recipeResultSet.first()) {
				
				recipeResultSet.beforeFirst();
				
				jsonArrayIngredients = addIngredientsToJsonArray(connection, recipeResultSet);
				responseObject.put("ingredients", jsonArrayIngredients);
				
				recipeQuery.close();
			}
			
			// retrieve categories for this recipe
			recipeQuery = connection.createStatement();
			String sqlCategory = "SELECT category_name, primary_cat FROM recipecategory WHERE recipe_id = '" + recipeId + "'";
			recipeResultSet = recipeQuery.executeQuery(sqlCategory);
			
			if(recipeResultSet.first()) {
				
				recipeResultSet.beforeFirst();
				
				jsonArrayCategories = addCategoriesToJsonArray(recipeResultSet);
				responseObject.put("categories", jsonArrayCategories);
			}
			
			LOGGER.info("Sending response back to app");
			
			// send data stored in responseObject back to app
			String json = responseObject.toString();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);		
			
		}catch(JSONException jsonException){
			jsonException.printStackTrace();
		}catch(SQLException sqlException) {
			sqlException.printStackTrace();
		}catch(ClassNotFoundException classNotFoundEx) {
			classNotFoundEx.printStackTrace();
		}finally {
			
			try {
				
				if(recipeQuery != null) {
					recipeQuery.close();
				}
				
				if(connection != null) {
					connection.close();
				}
				
				if(recipeResultSet != null) {
					recipeResultSet.close();
				}
				
			}catch(SQLException s) {
				s.printStackTrace();
			}
			
		}	
	}
	
	public JSONArray addIngredientsToJsonArray(Connection conn, ResultSet set) {
		
		JSONArray array = new JSONArray();
		ResultSet defSet = null;
		Statement defStatement = null;
		String defQuery;
		String name;
		
		try {
			
			while(set.next()) {
				
				name = set.getString("ingredient_name");
				defQuery = "SELECT def_measure_cat FROM ingredients WHERE ingredient_name = '" + name + "'";
				defStatement = conn.createStatement();
				defSet = defStatement.executeQuery(defQuery);
				
				JSONObject ingredientObject = new JSONObject();
				
				ingredientObject.put("ingredient_name", set.getObject("ingredient_name"));
				ingredientObject.put("quantity", set.getObject("quantity"));
				ingredientObject.put("quantity_unit", set.getObject("quantity_unit"));
				
				if(defSet.next()) {
					ingredientObject.put("default_meas", defSet.getString("def_measure_cat"));
				}else {
					ingredientObject.put("default_meas", "w");
				}
				
				array.put(ingredientObject);
			}
		}catch(JSONException jsonException) {
			jsonException.printStackTrace();
		}catch(SQLException sqlException) {
			sqlException.printStackTrace();
		}finally {
			
			try {
				
				if(defStatement != null) {
					defStatement.close();
				}
				if(defSet != null) {
					defSet.close();
				}
			}catch(SQLException sEx) {
				sEx.printStackTrace();
			}
			
		}
		
		return array;
	}
	
	
	public JSONArray addCategoriesToJsonArray(ResultSet set) {
		
		JSONArray array = new JSONArray();
		
		try {
			
			while(set.next()) {
				
				JSONObject categoryObject = new JSONObject();
				
				categoryObject.put("category", set.getObject("category_name"));
				categoryObject.put("primary", set.getObject("primary_cat"));
				
				array.put(categoryObject);
			}
		}catch(JSONException jsonException) {
			jsonException.printStackTrace();
		}catch(SQLException sqlException) {
			sqlException.printStackTrace();
		}
		
		return array;
	}
	
	public JSONObject addGeneralRecipeToJsonResponseObject(ResultSet set, JSONObject jsonOb) {
		
		try {		
			jsonOb.put("servings", set.getObject("servings"));
			jsonOb.put("prep_time", set.getObject("preparation_time"));
			jsonOb.put("total_time", set.getObject("total_time"));
			jsonOb.put("oven_time", set.getObject("oven_time"));
			jsonOb.put("oven_temp", set.getObject("oven_temperature"));
			jsonOb.put("num_ing", set.getObject("number_ingredients"));
			jsonOb.put("calories", set.getObject("calories_per_serving"));
			jsonOb.put("instructions", set.getObject("instructions"));
			
		}catch(JSONException jsonException) {
			jsonException.printStackTrace();
		}catch(SQLException sqlException) {
			sqlException.printStackTrace();
		}
		
		return jsonOb;
	}

}
