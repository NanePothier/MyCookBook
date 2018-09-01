import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.logging.*;
import org.json.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
		boolean exists;
		String result = "";
		String responseAndroid = "neutral";
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			firstName = jsonObject.getString("first");
			lastName = jsonObject.getString("last");
			userEmail = jsonObject.getString("user");
			userPassword = jsonObject.getString("password");
			
			LOGGER.info("first name: " + firstName);
			LOGGER.info("last name: " + lastName);
			LOGGER.info("email address: " + userEmail);
			LOGGER.info("user password: " + userPassword);
			
			Connection connection = null;
			Statement duplicateQueryStatement = null;
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			duplicateQueryStatement = connection.createStatement();
			String sqlString = "SELECT * FROM useraccount WHERE email_address = '" + userEmail + "'";
			
			// check if email address already exists
			ResultSet resultSet = duplicateQueryStatement.executeQuery(sqlString);
			exists = checkIfUnique(resultSet);
			
			LOGGER.info("value in exists: " + exists);
			
			if(!exists) {
				
				Calendar calendar = Calendar.getInstance();
				java.util.Date currentDate = calendar.getTime();
				java.sql.Date date = new java.sql.Date(currentDate.getTime());
				
				String sqlQueryString = "INSERT INTO useraccount VALUES(?, ?, ?, ?, ?)";
				PreparedStatement queryStatement = connection.prepareStatement(sqlQueryString);
				
				queryStatement.setString(1, userEmail);
				queryStatement.setString(2,  firstName);
				queryStatement.setString(3, lastName);
				queryStatement.setString(4, userPassword);
				queryStatement.setString(5, date.toString());
				
				queryStatement.executeUpdate();
				
				responseAndroid = "success";
				
			}else {
			
				responseAndroid = "exists";
			}
			
			// return value of responseAndroid to client
			JSONObject responseObject = new JSONObject();
			responseObject.put("successIndicator", responseAndroid);
			
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
		}
			
	}
	
	// returns false if no rows exist
	public boolean checkIfUnique(ResultSet set) throws SQLException {
		return set.first();
	}

}

