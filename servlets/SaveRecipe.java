import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.logging.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Servlet implementation class SaveRecipe
 */

@WebServlet("/SaveRecipe")
public class SaveRecipe extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");
       
    public SaveRecipe() {
        
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = "";
		String userEmail, uniqueUserId, recipeName, primaryCategory;
		int prepTime, ovenTime, ovenTemp, servings, calories, numIngredients;
		String instructions;
		JSONArray ingredientJsonArray = new JSONArray();
		boolean unique;
		String result = "";
		String responseToApp = "success";
		int totalTime;
		String ingredientName, quantityUnit;
		int quantity;
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			userEmail = jsonObject.getString("userEmail");
			uniqueUserId = jsonObject.getString("unique");
			recipeName = jsonObject.getString("name");
			ingredientJsonArray = jsonObject.getJSONArray("ingredientObjectArray");
			primaryCategory = jsonObject.getString("primCategory");
			prepTime = jsonObject.getInt("prepTime");
			ovenTime = jsonObject.getInt("ovenTime");
			ovenTemp = jsonObject.getInt("ovenTemp");
			servings = jsonObject.getInt("servings");
			calories = jsonObject.getInt("calories");
			instructions = jsonObject.getString("instructions");
			totalTime = prepTime + ovenTime;
			numIngredients = ingredientJsonArray.length();
			
			Calendar calendar = Calendar.getInstance();
			java.util.Date currentDate = calendar.getTime();
			java.sql.Date date = new java.sql.Date(currentDate.getTime());
			String currDate = date.toString();
			
			LOGGER.info("number of servings: " + servings);
			
			Connection connection = null;
			Statement queryStatement = null;
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			String recipeString = "INSERT INTO recipes VALUES('uniqueUserId', 'recipeName', 'servings', 'prepTime', 'totalTime', 'ovenTime', 'ovenTemp', 'numIngredients', 'calories', 'instructions', 'currDate');";
			String recCatString = "INSERT INTO recipecategory VALUES('uniqueUserId', 'primCategory', 'y')";
			String userRecString = "INSERT INTO userrecipes VALUES('userEmail', 'uniqueUserId')";
			String ingString = "INSERT INTO recipeingredients VALUES('uniqueUserId', 'ingredientName', 'quantity', 'quantityUnit')";
			
			queryStatement = connection.createStatement();
			
			queryStatement.executeQuery(recipeString);
			queryStatement.executeQuery(recCatString);
			queryStatement.executeQuery(userRecString);
			
			for(int x = 0; x < ingredientJsonArray.length(); x++) {
				
				JSONObject object = ingredientJsonArray.getJSONObject(x);
				
				ingredientName = object.getString("ing_name");
				quantity = Integer.parseInt(object.getString("quantity"));
				quantityUnit = object.getString("quantity_unit");
				
				queryStatement.executeQuery(ingString);
			}
			
			LOGGER.info("sending back response to app now");
			
			// return response
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
		}
	}

}
