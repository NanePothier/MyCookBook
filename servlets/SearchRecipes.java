import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
 * Servlet implementation class SearchRecipes
 */

@WebServlet("/SearchRecipes")
public class SearchRecipes extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");
   
    public SearchRecipes() {}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = "";
		String result = "";
		String responseToApp = "neutral";
		Connection connection = null;
		Statement queryStatement = null;
		ResultSet set = null;
		ResultSet catSet = null;
		String item;
		String userEmail;
		JSONArray responseArray = new JSONArray();
		
		try {
		
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			userEmail = jsonObject.getString("user_email");
			item = jsonObject.getString("search_item");
			
			item = item.substring(0,1).toUpperCase() + item.substring(1);
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
		
			responseArray = matchRecipeIngredients(connection, responseArray, item, userEmail);
			responseArray = matchRecipeCategory(connection, responseArray, item, userEmail);	
			
			String json = responseArray.toString();
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
	
	
	public JSONArray matchRecipeIngredients(Connection conn, JSONArray array, String searchItem, String userEmail) {
		
		JSONArray jArray = array;
		Statement queryStatement = null;
		ResultSet set = null;
		
		try {
			
			// get all recipe Ids that include the ingredient
			String query = "SELECT recipe_id FROM recipeingredients WHERE ingredient_name = '" + searchItem + "'";
			queryStatement = conn.createStatement();
			set = queryStatement.executeQuery(query);
			
			jArray = matchUserRecipeIdName(conn, set, jArray, userEmail);
			
		}catch(SQLException sqlEx) {
			sqlEx.printStackTrace();
		}
		return jArray;	
	}
	
	public JSONArray matchRecipeCategory(Connection conn, JSONArray array, String searchItem, String userEmail) {
		
		JSONArray jArray = array;
		Statement queryStatement = null;
		ResultSet set = null;
		
		try {
			
			// get all recipe Ids that belong to the category being searched
			String catQuery = "SELECT recipe_id FROM recipecategory WHERE category_name = '" + searchItem + "'";
			queryStatement = conn.createStatement();
			set = queryStatement.executeQuery(catQuery);
			
			jArray = matchUserRecipeIdName(conn, set, jArray, userEmail);
			
		}catch(SQLException sqlEx) {
			sqlEx.printStackTrace();
		}
		return jArray;
	}
	
	public JSONArray matchUserRecipeIdName(Connection conn, ResultSet set, JSONArray array, String userEmail) {
		
		Statement idStatement = null;
		Statement nameStatement = null;
		ResultSet idSet = null;
		ResultSet nameSet = null;
		JSONObject jObject;
		JSONArray jArray = array;
		String id;
		
		try {
		
			while(set.next()) {
				
				id = set.getString("recipe_id");
				
				// get all recipe Ids that include the search item AND belong to this user
				String emailQuery = "SELECT recipe_id FROM userrecipes WHERE email_address = '" + userEmail + "' AND recipe_id = '" + id + "'";
				idStatement = conn.createStatement();
				idSet = idStatement.executeQuery(emailQuery);
				
				while(idSet.next()) {
					
					id = idSet.getString("recipe_id");
					String recipeNameQuery = "SELECT recipe_name FROM recipes WHERE recipe_id = '" + id + "'";
					nameStatement = conn.createStatement();
					nameSet = nameStatement.executeQuery(recipeNameQuery);
					
					if(nameSet.next()) {
						
						jObject = new JSONObject();
						jObject.put("recipe_id", id);
						jObject.put("recipe_name", nameSet.getString("recipe_name"));
						
						jArray.put(jObject);		
					}		
				}	
			}			
		}catch(SQLException sqlEx) {
			sqlEx.printStackTrace();
		}catch(JSONException jsonEx) {
			jsonEx.printStackTrace();
		}
		return jArray;	
	}
}
