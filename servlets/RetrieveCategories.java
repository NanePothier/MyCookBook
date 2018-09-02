import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Servlet implementation class RetrieveCategories
 */

@WebServlet("/RetrieveCategories")
public class RetrieveCategories extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");
       
    public RetrieveCategories() {
        
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			
		try {			
			String line = "";
			String result = "";
			String userEmail;
			JSONArray jsonArray = new JSONArray();
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			userEmail = jsonObject.getString("user");			
			LOGGER.info("Email address: " + userEmail);
			
			Connection connection = null;
			Statement queryStatement = null;
			Statement userCategoriesStatement = null;
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			// retrieve categories available to all users
			queryStatement = connection.createStatement();
			String sqlString = "SELECT category_name FROM categories WHERE visible_to_all = 'y'";
			ResultSet resultSet = queryStatement.executeQuery(sqlString);
			
			jsonArray = addToResponseArray(resultSet, jsonArray);
			
			// retrieve user specific categories 
			userCategoriesStatement = connection.createStatement();
			String sqlCategory = "SELECT category_name FROM usercategory WHERE email_address = '" + userEmail + "'";
			ResultSet catResultSet = userCategoriesStatement.executeQuery(sqlCategory);
			
			jsonArray = addToResponseArray(catResultSet, jsonArray);
			
			LOGGER.info("Sending categories back to app now");
			
			// return category array to app
			String json = jsonArray.toString();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
			
		}catch(SQLException sqlException) {	
			sqlException.printStackTrace();	
		}catch(ClassNotFoundException classNotFoundEx) {
			classNotFoundEx.printStackTrace();
		}catch(JSONException jsonException) {
			jsonException.printStackTrace();
		}		
	}
	
	public JSONArray addToResponseArray(ResultSet set, JSONArray array) {
		
		try {
		
			while(set.next()) {
				
				JSONObject object = new JSONObject();
				object.put("category", set.getObject("category_name"));			
				array.put(object);
			}
			
		}catch(SQLException sqlException) {	
			sqlException.printStackTrace();	
		}catch(JSONException jsonException) {
			jsonException.printStackTrace();
		}
		
		return array;	
	}
}
