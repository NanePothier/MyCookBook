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
 * Servlet implementation class DeleteCategory
 */

@WebServlet("/DeleteCategory")
public class DeleteCategory extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    public DeleteCategory() {}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = "";
		String result = "";
		String responseToApp = "neutral";
		Connection connection = null;
		PreparedStatement queryStatement = null;
		Statement existsQuery = null;
		Statement constraintStatement = null;
		Statement existsStatement = null;
		ResultSet existsSet = null;
		ResultSet constraintSet = null;
		ResultSet set = null;
		String item;
		String userEmail;
		boolean categoryIsUsed = false;
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			userEmail = jsonObject.getString("user");
			item = jsonObject.getString("category");
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			String existsString = "SELECT * FROM usercategory WHERE email_address='" + userEmail + "' AND category_name = '" + item + "'";
			existsQuery = connection.createStatement();
			existsSet = existsQuery.executeQuery(existsString);
			
			/**
			 *  if user-category-connection exists
			 */
			if(existsSet.next()) {
				
				String idString = "SELECT recipe_id FROM userrecipes WHERE email_address = '" + userEmail + "'";
				constraintStatement = connection.createStatement();
				constraintSet = constraintStatement.executeQuery(idString);
				String recipeCatString;
				
				/**
				 *  extract all recipe IDs for this user and check if any of the user's recipes use the category to be deleted
				 */
				while(constraintSet.next()) {
					
					recipeCatString = "SELECT * FROM recipecategory WHERE recipe_id = '" + constraintSet.getString("recipe_id") + "' AND category_name = '" + item + "'";
					existsStatement = connection.createStatement();
					set = existsStatement.executeQuery(recipeCatString);
					
					if(set.next()) {
						
						categoryIsUsed = true;
					}	
				}
				
				/**
				 *  if the category to be deleted is not used by any of the user's recipes, go ahead and delete user-category
				 *  connection
				 */
				if(!categoryIsUsed) {
					
					String deleteString = "DELETE FROM usercategory WHERE email_address = ? AND category_name = ?";
					queryStatement = connection.prepareStatement(deleteString);
					queryStatement.setString(1, userEmail);
					queryStatement.setString(2, item);
					queryStatement.executeUpdate();
					
					queryStatement.close();
					deleteString = "DELETE FROM categories WHERE category_name = ? AND visible_to_all = ?";
					queryStatement = connection.prepareStatement(deleteString);
					queryStatement.setString(1, item);
					queryStatement.setString(2, "n");
					queryStatement.executeUpdate();
					
					responseToApp = "success";
					
				}else {
					
					responseToApp = "categoryIsUsed";	
				}
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
				
				if(constraintStatement != null) {
					constraintStatement.close();
				}
				
				if(existsStatement != null) {
					existsStatement.close();
				}
				
				if(existsQuery != null) {
					existsQuery.close();
				}
				
				if(existsSet != null) {
					existsSet.close();
				}
				
				if(constraintSet != null) {
					constraintSet.close();
				}
				
				if(set != null) {
					set.close();
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
