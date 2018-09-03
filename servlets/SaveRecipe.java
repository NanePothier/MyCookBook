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
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
		Connection connection = null;
		PreparedStatement queryStatement = null;
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			// retrieve data sent
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
			
			// get current date
			Calendar calendar = Calendar.getInstance();
			java.util.Date currentDate = calendar.getTime();
			java.sql.Date date = new java.sql.Date(currentDate.getTime());
			String currDate = date.toString();
			
			LOGGER.info("Recipe Name: " + recipeName);
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			// store general recipe attributes
			String recipeString = "INSERT INTO recipes VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			queryStatement = connection.prepareStatement(recipeString);
			
			queryStatement.setString(1, uniqueUserId);
			queryStatement.setString(2, recipeName);
			queryStatement.setInt(3, servings);
			queryStatement.setInt(4, prepTime);
			queryStatement.setInt(5, totalTime);
			queryStatement.setInt(6, ovenTime);
			queryStatement.setInt(7, ovenTemp);
			queryStatement.setInt(8, numIngredients);
			queryStatement.setInt(9, calories);
			queryStatement.setString(10, instructions);
			queryStatement.setString(11, currDate);
			
			queryStatement.executeUpdate();
			queryStatement.close();
			
			// store recipe-primary category connection
			String recCatString = "INSERT INTO recipecategory VALUES(?, ?, ?)";	
			queryStatement = connection.prepareStatement(recCatString);
			
			queryStatement.setString(1, uniqueUserId);
			queryStatement.setString(2, primaryCategory);
			queryStatement.setString(3, "y");
			
			queryStatement.executeUpdate();
			queryStatement.close();
			
			// store user recipe connection
			String userRecString = "INSERT INTO userrecipes VALUES(?, ?)";
			queryStatement = connection.prepareStatement(userRecString);
			
			queryStatement.setString(1, userEmail);
			queryStatement.setString(2, uniqueUserId);
			
			queryStatement.executeUpdate();
			queryStatement.close();
			
			// store ingredients for this recipe
			String ingString = "INSERT INTO recipeingredients VALUES(?, ?, ?, ?)";
			queryStatement = connection.prepareStatement(ingString);
			queryStatement.setString(1, uniqueUserId);
			
			
			for(int x = 0; x < ingredientJsonArray.length(); x++) {
				
				JSONObject object = ingredientJsonArray.getJSONObject(x);
				
				ingredientName = object.getString("ing_name");
				quantity = Integer.parseInt(object.getString("quantity"));
				quantityUnit = object.getString("quantity_unit");
				
				queryStatement.setString(2, ingredientName);
				queryStatement.setInt(3, quantity);
				queryStatement.setString(4, quantityUnit);
				
				queryStatement.executeUpdate();
			}
			
			LOGGER.info("Sending back response to app now");
			
			// return response to app
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

}
