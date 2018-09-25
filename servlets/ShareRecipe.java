import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class ShareRecipe
 */

@WebServlet("/ShareRecipe")
public class ShareRecipe extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");
  
    public ShareRecipe() {
        
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = "";
		String result = "";
		String responseToApp = "neutral";
		Connection connection = null;
		PreparedStatement queryStatement = null;
		Statement duplicateQuery = null;
		ResultSet duplicateSet = null;
		String userEmail, recipeId, sharedByEmail;
		
		try {
		
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			recipeId = jsonObject.getString("recipe_id");
			userEmail = jsonObject.getString("user_email");
			sharedByEmail = jsonObject.getString("shared_by_email");
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			// check if recipe has already been shared with this user
			String duplicateString = "SELECT * FROM sharedrecipes WHERE recipe_id = '" + recipeId + "' AND email_address = '" + userEmail + "'";
			duplicateQuery = connection.createStatement();
			duplicateSet = duplicateQuery.executeQuery(duplicateString);
			
			// if recipe has not been shared with this user yet
			if(!duplicateSet.next()) {
				
				// get current date
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				
				// store the recipe
				String queryString = "INSERT INTO sharedrecipes VALUES(?,?,?,?)";
				queryStatement = connection.prepareStatement(queryString);
				queryStatement.setString(1, recipeId);
				queryStatement.setString(2, userEmail);
				queryStatement.setString(3, sharedByEmail);
				queryStatement.setTimestamp(4, timestamp);
				queryStatement.executeUpdate();
				
				responseToApp = "success";
				
			}else {		
				responseToApp = "exists";
			}
			
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
				if(duplicateSet != null) {
					duplicateSet.close();
				}	
				if(duplicateQuery != null) {
					duplicateQuery.close();
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
