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
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class SaveItem
 */

@WebServlet("/SaveItem")
public class SaveItem extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");
       
    public SaveItem() {
        
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
		ResultSet set = null;
		ResultSet catSet = null;
		String flag;
		String item;
		String defaultMeasure;
		String userEmail;
		
		try {
		
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			flag = jsonObject.getString("indicator");
			item = jsonObject.getString("item");
			
			item = item.substring(0, 1).toUpperCase() + item.substring(1);
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			if(flag.equals("ingredient")) {
				
				String query = "SELECT * FROM ingredients WHERE ingredient_name='" + item + "'";
				duplicateQuery = connection.createStatement();
				set = duplicateQuery.executeQuery(query);
				
				if(!(set.next())) {
					
					defaultMeasure = jsonObject.getString("def");
					
					if(defaultMeasure.equals("weight")) {
						defaultMeasure = "w";
						LOGGER.info("defaultmeasure is weight");
					}else if(defaultMeasure.equals("liquid")) {
						defaultMeasure = "v";
					}else {
						defaultMeasure = "w";
					}
					
					String ingredientQuery = "INSERT INTO ingredients VALUES(?,?)";
					queryStatement = connection.prepareStatement(ingredientQuery);
					queryStatement.setString(1, item);
					queryStatement.setString(2, defaultMeasure);
					queryStatement.executeUpdate();
					queryStatement.close();
					
					responseToApp = "success";
				}else {
					
					responseToApp = "exists";
				}
				
			}else if(flag.equals("category")) {
				
				userEmail = jsonObject.getString("userEmail");
				
				LOGGER.info("user email: " + userEmail);
				
				String query = "SELECT * FROM categories WHERE category_name='" + item + "'";
				duplicateQuery = connection.createStatement();
				set = duplicateQuery.executeQuery(query);
				
				if(!(set.next())) {
					
					String catQuery = "INSERT INTO categories VALUES(?,?)";
					queryStatement = connection.prepareStatement(catQuery);
					queryStatement.setString(1, item);
					queryStatement.setString(2, "n");
					queryStatement.executeUpdate();
					queryStatement.close();
				}
				
				duplicateQuery.close();
				query = "SELECT * FROM usercategory WHERE email_address='" + userEmail + "' AND category_name='" + item + "'";
				duplicateQuery = connection.createStatement();
				catSet = duplicateQuery.executeQuery(query);
				
				if(!(catSet.next())) {
					
					String categoryQuery = "INSERT INTO usercategory VALUES(?, ?)";
					queryStatement = connection.prepareStatement(categoryQuery);
					queryStatement.setString(1, userEmail);	
					queryStatement.setString(2, item);
					queryStatement.executeUpdate();
					
					responseToApp = "success";		
				}else {
					responseToApp = "exists";
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
				
				if(duplicateQuery != null) {
					duplicateQuery.close();
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
