import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class GetRecipeNames
 */

@WebServlet("/GetRecipeNames")
public class GetRecipeNames extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");
       
    public GetRecipeNames() {
       
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = "";
		String userEmail;
		String result = "";
		String id = "";
		JSONArray jsonRecipeArray = new JSONArray();
		int count;
		Connection connection = null;
		Statement queryStatement = null;
		Statement recipeStatement = null;
		Statement catStatement = null;
		ResultSet set, catSet, resultSet;
		set = null;
		catSet = null;
		resultSet = null;
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			userEmail = jsonObject.getString("userEmail");
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			/**
			 *  get all recipe IDs for this user
			 */
			String sqlRecipe = "SELECT recipe_id FROM userrecipes WHERE email_address = '" + userEmail + "'";
			queryStatement = connection.createStatement();
			resultSet = queryStatement.executeQuery(sqlRecipe);
			recipeStatement = connection.createStatement();
			String sqlRecipeName;
			JSONObject jsonOb;
			
			/**
			 *  get recipe name for each recipe ID and store in JSONObject
			 *  then store JSONObject in response JSONArray
			 */
			while(resultSet.next()) {
				
				id = resultSet.getString("recipe_id");
				sqlRecipeName = "SELECT recipe_name FROM recipes WHERE recipe_id = '" + id + "'";		
				set = recipeStatement.executeQuery(sqlRecipeName);
				jsonOb = new JSONObject();
				
				if(set.next()) {
					
					LOGGER.info("Recipe name: " + set.getString("recipe_name") + " belonging to this recipe ID: " + id);
					
					jsonOb.put("recipe_id", id);
					jsonOb.put("recipe_name", set.getString("recipe_name"));	
				}
				
				jsonRecipeArray.put(jsonOb);
			}
			
			// set to beginning before getting category names 
			resultSet.beforeFirst();
			
			/**
			 * get primary category that each recipe belongs to and store in corresponding JSONObject
			 */
			catStatement = connection.createStatement();
			count = 0;
			JSONObject object;
			
			while(resultSet.next()) {
				
				id = resultSet.getString("recipe_id");
				String sqlCategory = "SELECT category_name FROM recipecategory WHERE recipe_id = '" + id + "' AND primary_cat = 'y'";
				
				catSet = catStatement.executeQuery(sqlCategory);
				
				if(catSet.next()) {
					
					LOGGER.info("Category name: " + catSet.getString("category_name") + " belonging to this recipe ID: " + id);
					
					jsonRecipeArray.getJSONObject(count).put("category", catSet.getString("category_name"));
				}
				
				count++;			
			}
			
			LOGGER.info("Sending array back to app now");
			
			/**
			 * send array holding objects with recipe ID, name and category back to application
			 */
			String json = jsonRecipeArray.toString();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
			
			
		}catch(SQLException sqlException) {
			
			sqlException.printStackTrace();	
		}catch(JSONException jsonEx) {
			jsonEx.printStackTrace();
		}catch(ClassNotFoundException classEx) {
			classEx.printStackTrace();
		}finally {
			
			try {
				
				if(queryStatement != null) {
					queryStatement.close();
				}
				
				if(recipeStatement != null) {
					recipeStatement.close();
				}
				
				if(catStatement != null) {
					catStatement.close();
				}
				
				if(resultSet != null) {
					resultSet.close();
				}
				
				if(set != null) {
					set.close();
				}
				
				if(catSet != null) {
					catSet.close();
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
