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
 * Servlet implementation class RetrieveIngredients
 */

@WebServlet("/RetrieveIngredients")
public class RetrieveIngredients extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");
       
    public RetrieveIngredients() {
        
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			
		try {
			
			JSONArray jsonArray = new JSONArray();
			// String line = "";
			// String result = "";
			// String userEmail;
			
			/*
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			JSONObject jsonObject = new JSONObject(result);
			userEmail = jsonObject.getString("user");			
			LOGGER.info("Message sent is: " + userEmail);
			*/
			
			Connection connection = null;
			Statement queryStatement = null;
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			queryStatement = connection.createStatement();
			String sqlString = "SELECT ingredient_name FROM ingredients";
			
			ResultSet resultSet = queryStatement.executeQuery(sqlString);
			
			while(resultSet.next()) {
				
				JSONObject object = new JSONObject();
				object.put("ingredient", resultSet.getObject("ingredient_name"));			
				jsonArray.put(object);
			}
			
			LOGGER.info("Sending ingredients back to app now");
				
			String json = jsonArray.toString();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
			
		}catch(SQLException e) {
			e.printStackTrace();	
		}catch(ClassNotFoundException en) {
			en.printStackTrace();
		}catch(JSONException ex) {
			ex.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
