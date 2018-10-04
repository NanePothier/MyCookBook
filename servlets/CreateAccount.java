import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Servlet implementation class CreateAccount
 */

@WebServlet("/CreateAccount")
public class CreateAccount extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");
       
    public CreateAccount() {}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = null;
		String firstName, lastName, userEmail, userPassword;
		String result = "";
		String responseToApp = "neutral";
		Connection connection = null;
		Statement duplicateQueryStatement = null;
		PreparedStatement queryStatement = null;
		ResultSet resultSet = null;
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			// retrieve sent user data
			firstName = jsonObject.getString("first");
			lastName = jsonObject.getString("last");
			userEmail = jsonObject.getString("user");
			userPassword = jsonObject.getString("password");
			
			if(!firstName.equals("")) {
				firstName = firstName.substring(0,1).toUpperCase() + firstName.substring(1);
			}
			
			if(!lastName.equals("")) {
				lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
			}
			
			// Establish connection with database
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			// check if email address already exists
			String sqlString = "SELECT * FROM useraccount WHERE email_address = '" + userEmail + "'";
			duplicateQueryStatement = connection.createStatement();
			resultSet = duplicateQueryStatement.executeQuery(sqlString);
			
			// if this user does not have an account yet, insert information into database
			if(!resultSet.next()) {
				
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				
				String sqlQueryString = "INSERT INTO useraccount VALUES(?, ?, ?, ?, ?)";
				queryStatement = connection.prepareStatement(sqlQueryString);
				
				queryStatement.setString(1, userEmail);
				queryStatement.setString(2,  firstName);
				queryStatement.setString(3, lastName);
				queryStatement.setString(4, userPassword);
				queryStatement.setTimestamp(5, timestamp);
				
				queryStatement.executeUpdate();
				
				responseToApp = "success";
				
			}else {
				responseToApp = "exists";
			}
			
			// send response back to client
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
				if(duplicateQueryStatement != null) {
					duplicateQueryStatement.close();
				}
				if(queryStatement != null) {
					queryStatement.close();
				}
				if(resultSet != null) {
					resultSet.close();
				}
				if(connection != null) {
					connection.close();
				}
			}catch(SQLException sqlEx) {
				sqlEx.printStackTrace();
			}
		}
	}
}

