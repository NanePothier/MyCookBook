import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Servlet implementation class SaveRecipe
 */

@WebServlet("/SaveRecipe")
public class SaveRecipe extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    public SaveRecipe() {}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = "";
		String userEmail, uniqueRecipeId, recipeName, primaryCategory;
		int prepTime, ovenTime, ovenTemp, servings, calories, numIngredients;
		String prTime, ovTime, ovTemp, serv, cal;
		String instructions;
		JSONArray categoryJsonArray = new JSONArray();
		boolean unique;
		String result = "";
		String responseToApp = "success";
		int totalTime;
		String ingredientName, quantityUnit;
		double quantity;
		String measSystemIndicator;
		String actionIndicator;
		Connection connection = null;
		PreparedStatement queryStatement = null;
		PreparedStatement deleteStatement = null;
		JSONArray ingredientJsonArray = new JSONArray();
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			// retrieve data sent
			userEmail = jsonObject.getString("userEmail");
			uniqueRecipeId = jsonObject.getString("unique");
			recipeName = jsonObject.getString("name");
			ingredientJsonArray = jsonObject.getJSONArray("ingredientObjectArray");
			primaryCategory = jsonObject.getString("primCategory");
			categoryJsonArray = jsonObject.getJSONArray("other_categories");
			prTime = jsonObject.getString("prepTime");
			ovTime = jsonObject.getString("ovenTime");
			ovTemp = jsonObject.getString("ovenTemp");
			serv = jsonObject.getString("servings");
			cal = jsonObject.getString("calories");
			instructions = jsonObject.getString("instructions");
			numIngredients = ingredientJsonArray.length();
			measSystemIndicator = jsonObject.getString("systemInd");
			actionIndicator = jsonObject.getString("actionInd");
			
			// -------------------------------------------------------------------------------------------------------------------------------------
			// 
			// enter -1 for properties that do not have a value
			// 
			if(!(prTime.equals(""))) {
				prepTime = Integer.parseInt(prTime);
			}else {
				prepTime = -1;
			}
			
			if(!(ovTime.equals(""))) {
				ovenTime = Integer.parseInt(ovTime);
			}else {
				ovenTime = -1;
			}
			
			if(!(ovTemp.equals(""))) {
				ovenTemp = Integer.parseInt(ovTemp);
			}else {
				ovenTemp = -1;
			}
			
			if(!(serv.equals(""))) {
				servings = Integer.parseInt(serv);
			}else {
				servings = -1;
			}
			
			if(!(cal.equals(""))) {
				calories = Integer.parseInt(cal);
			}else {
				calories = -1;
			}
			
			if(prepTime == -1 || ovenTime == -1) {
				totalTime = prepTime + ovenTime + 1;
			}else {
				totalTime = prepTime + ovenTime;
			}
			
			if(instructions.equals("")) {
				instructions = "none";
			}
			
			// make the first letter of the recipe name upper case
			recipeName = recipeName.substring(0, 1).toUpperCase() + recipeName.substring(1);
			
			// get current date
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			//
			// convert measurements to US system if they have been entered using the metric system
			//
			if(measSystemIndicator.equals("Metric")) {
							
				ingredientJsonArray = convertUnits(connection, ingredientJsonArray);	
				
				if(ovenTemp != -1) {
					
					ovenTemp = (int) convertTemperature(ovenTemp);
				}
			}
			
			// --------------------------------------------------------------------------------------------------------------------
			// 
			// store/update general recipe properties depending on if user is creating a new recipe or editing an existing one
			//
			if(actionIndicator.equals("NewRecipe")) {
				
				String recipeString = "INSERT INTO recipes VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				queryStatement = connection.prepareStatement(recipeString);
				
				queryStatement.setString(1, uniqueRecipeId);
				queryStatement.setString(2, recipeName);
				queryStatement.setInt(3, servings);
				queryStatement.setInt(4, prepTime);
				queryStatement.setInt(5, totalTime);
				queryStatement.setInt(6, ovenTime);
				queryStatement.setInt(7, ovenTemp);
				queryStatement.setInt(8, numIngredients);
				queryStatement.setInt(9, calories);
				queryStatement.setString(10, instructions);
				queryStatement.setTimestamp(11, timestamp);
				queryStatement.setString(12, measSystemIndicator);
				
			}else {
				
				String recipeString = "UPDATE recipes SET recipe_name=?, servings=?, preparation_time=?, total_time=?, oven_time=?, oven_temperature=?, number_ingredients=?, calories_per_serving=?, instructions=?, date_created=?, system=? WHERE recipe_id=?";
				queryStatement = connection.prepareStatement(recipeString);
				
				queryStatement.setString(1, recipeName);
				queryStatement.setInt(2, servings);
				queryStatement.setInt(3, prepTime);
				queryStatement.setInt(4, totalTime);
				queryStatement.setInt(5, ovenTime);
				queryStatement.setInt(6, ovenTemp);
				queryStatement.setInt(7, numIngredients);
				queryStatement.setInt(8, calories);
				queryStatement.setString(9, instructions);
				queryStatement.setTimestamp(10, timestamp);
				queryStatement.setString(11, measSystemIndicator);
				queryStatement.setString(12, uniqueRecipeId);	
			}
			queryStatement.executeUpdate();
			queryStatement.close();
			
			// ----------------------------------------------------------------------------------------------------------------------
			//
			// store/update recipe-primary_category connection
			//
			if(actionIndicator.equals("NewRecipe")) {
				
				String recCatString = "INSERT INTO recipecategory VALUES(?, ?, ?)";	
				queryStatement = connection.prepareStatement(recCatString);
				
				queryStatement.setString(1, uniqueRecipeId);
				queryStatement.setString(2, primaryCategory);
				queryStatement.setString(3, "y");
				
			}else {
				
				String recCatString = "UPDATE recipecategory SET category_name=?, primary_cat=? WHERE recipe_id=? AND primary_cat=?";
				queryStatement = connection.prepareStatement(recCatString);
				
				queryStatement.setString(1, primaryCategory);
				queryStatement.setString(2, "y");
				queryStatement.setString(3, uniqueRecipeId);
				queryStatement.setString(4, "y");
			}
			queryStatement.executeUpdate();
			queryStatement.close();
			
			// -----------------------------------------------------------------------------------------------------------------------
			//
			// store user-recipe connection 
			// (when user is editing an existing recipe this action is not needed since connection already exists)
			//
			if(actionIndicator.equals("NewRecipe")) {
				
				// store user recipe connection
				String userRecString = "INSERT INTO userrecipes VALUES(?, ?)";
				queryStatement = connection.prepareStatement(userRecString);
				
				queryStatement.setString(1, userEmail);
				queryStatement.setString(2, uniqueRecipeId);
				
				queryStatement.executeUpdate();
				queryStatement.close();			
			}
			
			// ---------------------------------------------------------------------------------------------------------------------
			//
			// store ingredients of this recipe
			// if user is editing an existing recipe, delete all ingredients first and then add ingredients
			//
			if(actionIndicator.equals("EditRecipe")) {
				
				String deleteString = "DELETE FROM recipeingredients WHERE recipe_id = ?";
				deleteStatement = connection.prepareStatement(deleteString);
				deleteStatement.setString(1, uniqueRecipeId);
				deleteStatement.executeUpdate();
				deleteStatement.close();
			}
			
			//
			// store ingredients for this recipe
			//
			String ingString = "INSERT INTO recipeingredients VALUES(?, ?, ?, ?)";
			String duplicateString;
			Statement duplicateQuery;
			ResultSet duplicateSet;
			queryStatement = connection.prepareStatement(ingString);
			queryStatement.setString(1, uniqueRecipeId);
			String qnty;
				
			for(int x = 0; x < ingredientJsonArray.length(); x++) {
				
				JSONObject object = ingredientJsonArray.getJSONObject(x);
				
				ingredientName = object.getString("ing_name");
				qnty = object.getString("quantity");
				quantityUnit = object.getString("quantity_unit");
				
				duplicateString = "SELECT * FROM recipeingredients WHERE recipe_id = '" + uniqueRecipeId + "' AND ingredient_name = '" + ingredientName + "'";
				duplicateQuery = connection.createStatement();
				duplicateSet = duplicateQuery.executeQuery(duplicateString);
				
				if(!(duplicateSet.next())) {
					
					// if no quantity value is given, there should also be no quantity unit
					if(qnty.equals("")) {
						quantity = -1.0;
						if(!(quantityUnit.equals(" "))) {
							quantityUnit = " ";
						}
					}else {
						quantity = Double.parseDouble(qnty);
					}
					
					queryStatement.setString(2, ingredientName);
					queryStatement.setDouble(3, quantity);
					queryStatement.setString(4, quantityUnit);	
					
					queryStatement.executeUpdate();	
				}
				
				duplicateQuery.close();
				duplicateSet.close();
				
			}
			queryStatement.close();
			
			// ------------------------------------------------------------------------------------------------------------------
			//
			// store other categories assigned to this recipe
			// if user is editing existing recipe, delete categories first then add categories
			//
			if(actionIndicator.equals("EditRecipe")) {
				
				String deleteString = "DELETE FROM recipecategory WHERE recipe_id = ? AND primary_cat = ?";
				deleteStatement = connection.prepareStatement(deleteString);
				deleteStatement.setString(1, uniqueRecipeId);
				deleteStatement.setString(2, "n");
				deleteStatement.executeUpdate();
				deleteStatement.close();
			}
			
			//
			// insert other categories this recipe belongs to
			//
			String catString = "INSERT INTO recipecategory VALUES(?, ?, ?)";
			queryStatement = connection.prepareStatement(catString);
			queryStatement.setString(1, uniqueRecipeId);
			
			for(int i = 0; i < categoryJsonArray.length(); i++) {
				
				JSONObject object = categoryJsonArray.getJSONObject(i);
				
				String catName = object.getString("cat_name");
				Boolean isPrim = object.getBoolean("cat_prime");
				
				duplicateString = "SELECT * FROM recipecategory WHERE recipe_id = '" + uniqueRecipeId + "' AND category_name = '" + catName + "'";
				duplicateQuery = connection.createStatement();
				duplicateSet = duplicateQuery.executeQuery(duplicateString);
				
				if(!(duplicateSet.next())) {
					
					queryStatement.setString(2, catName);
					queryStatement.setString(3, "n");
									
					queryStatement.executeUpdate();	
				}
				
				duplicateQuery.close();
				duplicateSet.close();	
			}
			
			// ------------------------------------------------------------------------------------------------------------
			//
			// return response to application
			//
			JSONObject responseObject = new JSONObject();
			responseObject.put("successIndicator", responseToApp);
			
			String json = responseObject.toString();
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
			
		}catch(SQLException e) {
			
			e.printStackTrace();	
		}catch(JSONException ex) {
			ex.printStackTrace();
		}catch(ClassNotFoundException en) {
			en.printStackTrace();
		}finally {
			try {	
				if(queryStatement != null) {
					queryStatement.close();
				}
				
				if(connection != null) {
					connection.close();
				}
			}catch(SQLException s) {
				s.printStackTrace();
			}		
		}
	}
	
	// convert ingredient quantities from celsius to fahrenheit
	private JSONArray convertUnits(Connection conn, JSONArray jsonArray) {
		
		String qnty;
		double quantity;
		String unit;
		JSONObject jObject;
		JSONObject convObject;
		String factorQuery;
		String ingName;
		ResultSet set;
		Statement factorStatement;
		double gFactor, kgFactor, mlFactor, lFactor;
		int arraySize = jsonArray.length();
		JSONArray convArray = new JSONArray();
		
		try {
			
			factorQuery = "SELECT factor FROM conversion WHERE measure_from = 'gramm' AND measure_to = 'ounces'";
			factorStatement = conn.createStatement();
			set = factorStatement.executeQuery(factorQuery);
			set.next();
			gFactor = set.getFloat("factor");
			factorStatement.close();
			set.close();
			
			factorQuery = "SELECT factor FROM conversion WHERE measure_from = 'kilogramm' AND measure_to = 'pounds'";
			factorStatement = conn.createStatement();
			set = factorStatement.executeQuery(factorQuery);
			set.next();
			kgFactor = set.getFloat("factor");
			factorStatement.close();
			set.close();
			
			factorQuery = "SELECT factor FROM conversion WHERE measure_from = 'milliliter' AND measure_to = 'cup'";
			factorStatement = conn.createStatement();
			set = factorStatement.executeQuery(factorQuery);
			set.next();
			mlFactor = set.getFloat("factor");
			factorStatement.close();
			set.close();
			
			factorQuery = "SELECT factor FROM conversion WHERE measure_from = 'liter' AND measure_to = 'quart'";
			factorStatement = conn.createStatement();
			set = factorStatement.executeQuery(factorQuery);
			set.next();
			lFactor = set.getFloat("factor");
			factorStatement.close();
			set.close();
			
			// convert quantity for each ingredient
			for(int x = 0; x < arraySize; x++) {
				
				jObject = jsonArray.getJSONObject(x);
				
				convObject = new JSONObject();
				
				ingName = jObject.getString("ing_name");
				qnty = jObject.getString("quantity");
				unit = jObject.getString("quantity_unit");
				
				// if unit is equal to count, then no conversion necessary
				if(!(unit.equals("ct"))) {
					
					// if there is no quantity and no quantity unit, then there is nothing to convert
					// therefore, return original json array
					if(!(unit.equals(" ")) && !(qnty.equals(""))) {
						
						// if quantity and unit is given, convert and add to new array
						quantity = Double.parseDouble(qnty);
						
						if(unit.equals("g")) {
							
							quantity = quantity * gFactor;
							convObject.put("quantity_unit", "oz");	
								
						}else if(unit.equals("kg")){
							
							quantity = quantity * kgFactor;
							convObject.put("quantity_unit", "lb");
							
						}else if(unit.equals("ml")) {
							
							quantity = quantity * mlFactor;
							convObject.put("quantity_unit", "cup");
							
						}else if(unit.equals("L")) {
							
							quantity = quantity * lFactor;
							convObject.put("quantity_unit", "qt");	
							
						}else if(unit.equals("tbsp")) {
							
							convObject.put("quantity_unit", "tbsp");
							
						}else if(unit.equals("tsp")) {
							
							convObject.put("quantity_unit", "tsp");
						}
						
						convObject.put("ing_name", ingName);
						convObject.put("quantity", quantity);
						
						convArray.put(convObject);
					}else {
						
						convObject.put("ing_name", ingName);
						convObject.put("quantity", qnty);
						convObject.put("quantity_unit", unit);
						
						convArray.put(convObject);
					}		
				}else {
					
					convObject.put("ing_name", ingName);
					convObject.put("quantity", qnty);
					convObject.put("quantity_unit", unit);
					
					convArray.put(convObject);
				}
			}
				
		}catch(JSONException jException) {
			jException.printStackTrace();
		}catch(SQLException sqlException) {
			sqlException.printStackTrace();
		}
		
		return convArray;
	}
	
	// convert temperature from celsius to fahrenheit
	public double convertTemperature(int ovenTempCelsius) {
		
		double ovenTempFahrenheit = ovenTempCelsius * (9.0/5.0) + 32;
		return ovenTempFahrenheit;
	}

}
