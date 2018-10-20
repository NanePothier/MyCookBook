import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class DeleteRecipe
 */

@WebServlet("/DeleteRecipe")
public class DeleteRecipe extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    public DeleteRecipe() {}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = null;
		String recipeId;
		String result = "";
		String responseToApp = "deleted";
		Connection connection = null;
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			// retrieve sent data
			JSONObject jsonObject = new JSONObject(result);
			recipeId = jsonObject.getString("recipeId");
			
			// create connection to database
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			// delete recipe data
			responseToApp = deleteUserRecipeConnection(connection, recipeId, responseToApp);
			responseToApp = deleteRecipeCategoryConnection(connection, recipeId, responseToApp);
			responseToApp = deleteRecipeIngredients(connection, recipeId, responseToApp);
			responseToApp = deleteRecipe(connection, recipeId, responseToApp);
			
			// return value of responseToApp to client
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
				if(connection != null) {
					connection.close();
				}
			}catch(SQLException s) {
				s.printStackTrace();
			}
		}		
	}
	
	
	public String deleteUserRecipeConnection(Connection connection, String recipeId, String responseToApp) {
		
		PreparedStatement deleteStatement = null;
		Statement existsStatement = null;
		ResultSet existsSet = null;
		String deleteQuery;
		String existsQuery;
		int rowsDeleted;
		
		try {
			
			// ensure user recipe connection exists before trying to delete it
			existsQuery = "SELECT * FROM userrecipes WHERE recipe_id = '" + recipeId + "'";
			existsStatement = connection.createStatement();
			existsSet = existsStatement.executeQuery(existsQuery);
			
			if(existsSet.next()) {
				
				// delete user recipe connection
				deleteQuery = "DELETE FROM userrecipes WHERE recipe_id = ?";
				deleteStatement = connection.prepareStatement(deleteQuery);
				deleteStatement.setString(1, recipeId);
				rowsDeleted = deleteStatement.executeUpdate();
				
				if(rowsDeleted > 0) {
					responseToApp += "D";
				}
				
				if(deleteStatement != null) {
					deleteStatement.close();
				}
			}
			
			if(existsStatement != null) {
				existsStatement.close();
			}
			if(existsSet != null) {
				existsSet.close();
			}
			
			// if the recipe to be deleted is a shared recipe, also delete shared recipe connection
			existsQuery = "SELECT * FROM sharedrecipes WHERE recipe_id = '" + recipeId + "'";
			existsStatement = connection.createStatement();
			existsSet = existsStatement.executeQuery(existsQuery);
			
			if(existsSet.next()) {
				
				deleteQuery = "DELETE FROM sharedrecipes WHERE recipe_id = ?";
				deleteStatement = connection.prepareStatement(deleteQuery);
				deleteStatement.setString(1, recipeId);
				deleteStatement.executeUpdate();
			}
			
		}catch(SQLException sqlEx) {
			sqlEx.printStackTrace();
		}finally {
			
			try {
				if(existsStatement != null) {
					existsStatement.close();
				}
				if(existsSet != null) {
					existsSet.close();
				}
				if(deleteStatement != null) {
					deleteStatement.close();
				}
			}catch(SQLException sqlE) {
				sqlE.printStackTrace();
			}	
		}
		
		return responseToApp;
	}
	
	public String deleteRecipeCategoryConnection(Connection connection, String recipeId, String responseToApp) {
		
		PreparedStatement deleteStatement = null;
		Statement existsStatement = null;
		ResultSet existsSet = null;
		String deleteQuery;
		String existsQuery;
		int rowsDeleted;
		
		try {
			
			// ensure recipe category connection exists before trying to delete it
			existsQuery = "SELECT * FROM recipecategory WHERE recipe_id = '" + recipeId + "'";
			existsStatement = connection.createStatement();
			existsSet = existsStatement.executeQuery(existsQuery);
			
			if(existsSet.next()) {
				
				// delete recipe category connection
				deleteQuery = "DELETE FROM recipecategory WHERE recipe_id = ?";
				deleteStatement = connection.prepareStatement(deleteQuery);
				deleteStatement.setString(1, recipeId);
				rowsDeleted = deleteStatement.executeUpdate();
				
				if(rowsDeleted > 0) {
					responseToApp += "D";
				}
				if(deleteStatement != null) {
					deleteStatement.close();
				}	
			}
			
			if(existsStatement != null) {
				existsStatement.close();
			}
			if(existsSet != null) {
				existsSet.close();
			}
			
		}catch(SQLException sqlEx) {
			sqlEx.printStackTrace();
		}
		
		return responseToApp;	
	}
	
	public String deleteRecipeIngredients(Connection connection, String recipeId, String responseToApp) {
		
		PreparedStatement deleteStatement = null;
		Statement existsStatement = null;
		ResultSet existsSet = null;
		String deleteQuery;
		String existsQuery;
		int rowsDeleted;
		
		try {
			
			// ensure recipe ingredients exists before trying to delete them
			existsQuery = "SELECT * FROM recipeingredients WHERE recipe_id = '" + recipeId + "'";
			existsStatement = connection.createStatement();
			existsSet = existsStatement.executeQuery(existsQuery);
			
			if(existsSet.next()) {
				
				// delete recipe ingredients
				deleteQuery = "DELETE FROM recipeingredients WHERE recipe_id = ?";
				deleteStatement = connection.prepareStatement(deleteQuery);
				deleteStatement.setString(1, recipeId);
				rowsDeleted = deleteStatement.executeUpdate();
				
				if(rowsDeleted > 0) {
					responseToApp += "D";
				}			
				if(deleteStatement != null) {
					deleteStatement.close();
				}		
			}
			
			if(existsStatement != null) {
				existsStatement.close();
			}
			if(existsSet != null) {
				existsSet.close();
			}
			
		}catch(SQLException sqlEx) {
			sqlEx.printStackTrace();
		}
		
		return responseToApp;		
	}
	
	public String deleteRecipe(Connection connection, String recipeId, String responseToApp) {
		
		PreparedStatement deleteStatement = null;
		Statement existsStatement = null;
		ResultSet existsSet = null;
		String deleteQuery;
		String existsQuery;
		int rowsDeleted;
		
		try {
			
			// ensure recipe exists before trying to delete it
			existsQuery = "SELECT * FROM recipes WHERE recipe_id = '" + recipeId + "'";
			existsStatement = connection.createStatement();
			existsSet = existsStatement.executeQuery(existsQuery);
			
			if(existsSet.next()) {
				
				// delete recipe
				deleteQuery = "DELETE FROM recipes WHERE recipe_id = ?";
				deleteStatement = connection.prepareStatement(deleteQuery);
				deleteStatement.setString(1, recipeId);
				rowsDeleted = deleteStatement.executeUpdate();
				
				if(rowsDeleted > 0) {
					responseToApp += "D";
				}				
				if(deleteStatement != null) {
					deleteStatement.close();
				}				
			}
			
			if(existsStatement != null) {
				existsStatement.close();
			}
			if(existsSet != null) {
				existsSet.close();
			}
			
		}catch(SQLException sqlEx) {
			sqlEx.printStackTrace();
		}
		
		return responseToApp;	
	}
	
}
