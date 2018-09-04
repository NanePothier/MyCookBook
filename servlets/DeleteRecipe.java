import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
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
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");
       
    public DeleteRecipe() {}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = null;
		String recipeId;
		String result = "";
		String responseToApp = "deleted";
		int rowsDeleted;
		Connection connection = null;
		PreparedStatement deleteStatement = null;
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			recipeId = jsonObject.getString("recipeId");
			
			LOGGER.info("recipe id: " + recipeId);
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			String deleteQuery = "DELETE FROM userrecipes WHERE recipe_id = ?";
			deleteStatement = connection.prepareStatement(deleteQuery);
			deleteStatement.setString(1, recipeId);
			rowsDeleted = deleteStatement.executeUpdate();
			
			if(rowsDeleted > 0) {
				responseToApp += "First";
			}
			
			LOGGER.info(responseToApp);
			
			deleteStatement.close();
			
			
			deleteQuery = "DELETE FROM recipecategory WHERE recipe_id = ?";
			deleteStatement = connection.prepareStatement(deleteQuery);
			deleteStatement.setString(1, recipeId);
			rowsDeleted = deleteStatement.executeUpdate();
			
			if(rowsDeleted > 0) {
				responseToApp += "Second";
			}
			
			LOGGER.info(responseToApp);
			
			deleteStatement.close();
			
			deleteQuery = "DELETE FROM recipeingredients WHERE recipe_id = ?";
			deleteStatement = connection.prepareStatement(deleteQuery);
			deleteStatement.setString(1, recipeId);
			rowsDeleted = deleteStatement.executeUpdate();
			
			if(rowsDeleted > 0) {
				responseToApp += "Third";
			}
			
			LOGGER.info(responseToApp);
			
			deleteStatement.close();
			
			deleteQuery = "DELETE FROM recipes WHERE recipe_id = ?";
			deleteStatement = connection.prepareStatement(deleteQuery);
			deleteStatement.setString(1, recipeId);
			rowsDeleted = deleteStatement.executeUpdate();
			
			if(rowsDeleted > 0) {
				responseToApp += "Fourth";
			}
			
			LOGGER.info(responseToApp);
			
			deleteStatement.close();
			
			
			// return value of responseAndroid to client
			JSONObject responseObject = new JSONObject();
			responseObject.put("successIndicator", responseToApp);
			String json = responseObject.toString();
						
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
			
			
		}catch(SQLException e) {
			LOGGER.info("Error sqlexception");
			e.printStackTrace();	
		}catch(JSONException ex) {
			LOGGER.info("Error jsonexception");
			ex.printStackTrace();
		}catch(ClassNotFoundException en) {
			LOGGER.info("Error class not found exception");
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

}
