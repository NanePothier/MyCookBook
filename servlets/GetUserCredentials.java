import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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
 * Servlet implementation class GetUserCredentials
 */
@WebServlet("/GetUserCredentials")
public class GetUserCredentials extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public GetUserCredentials() {}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

	/**
	 * check if received device Id exists in the database
	 * if it already exists, retrieve user credentials associated with this device
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = "";
		String deviceId;
		String userEmail = "";
		String userPassword = "";
		String result = "";
		boolean deviceIsKnown = false;
		Connection connection = null;
		Statement deviceStatement = null;
		Statement userStatement = null;
		ResultSet resultSet = null;
		ResultSet userSet = null;
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			deviceId = jsonObject.getString("device_id");
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			/**
			 * check if device is known by trying to retrieve a user email address associated with this device id
			 * if device is known and an email address is retrieved, also retrieve the password for that user
			 */
			String queryString = "SELECT email_address FROM deviceidentification WHERE device_id = '" + deviceId + "'";
			deviceStatement = connection.createStatement();
			resultSet = deviceStatement.executeQuery(queryString);
			
			// if device is known
			if(resultSet.next()) {
				
				deviceIsKnown = true;
				userEmail = resultSet.getString("email_address");
				
				String userQuery = "SELECT password FROM useraccount WHERE email_address = '" + userEmail + "'";
				userStatement = connection.createStatement();
				userSet = userStatement.executeQuery(userQuery);
				
				if(userSet.next()) {
					
					userPassword = userSet.getString("password");
				}	
			}
			
			// send data back to app
			JSONObject jObject = new JSONObject();
			jObject.put("device_is_known", deviceIsKnown);
			jObject.put("user_email", userEmail);
			jObject.put("user_password", userPassword);
			
			String json = jObject.toString();
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
				if(deviceStatement != null) {
					deviceStatement.close();
				}		
				if(userStatement != null) {
					userStatement.close();
				}
				if(resultSet != null) {
					resultSet.close();
				}	
				if(userSet != null) {
					userSet.close();
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
