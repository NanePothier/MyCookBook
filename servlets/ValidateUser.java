import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.*;
import org.json.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Servlet implementation class CreateAccount
 */
@WebServlet("/ValidateUser")
public class ValidateUser extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");
       
    public ValidateUser() {
        
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = null;
		String userEmail, userPassword;
		boolean exists;
		
		String result = "";
		String responseToApp = "";
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			LOGGER.info("Result string: " + result);
			
			JSONObject jsonObject = new JSONObject(result);
			
			userEmail = jsonObject.getString("user");
			userPassword = jsonObject.getString("password");
			
			LOGGER.info("users email address: " + userEmail);
			LOGGER.info("user password: " + userPassword);
			
			Connection connection = null;
			Statement queryStatement = null;
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			queryStatement = connection.createStatement();
			String sqlString = "SELECT password FROM useraccount WHERE email_address ='" + userEmail + "'";
			
			ResultSet resultSet = queryStatement.executeQuery(sqlString);
			
			exists = checkIfUserExists(resultSet);
			
			if(exists) {
				
				String pw = resultSet.getString(1);
				
				if(pw.equals(userPassword)) {
					
					responseToApp = "match";
				}else {
					responseToApp = "wrong_password";
				}
			}else {
				
				responseToApp = "user_does_not_exist";
			}
			
			System.out.println("response : " + responseToApp);
			
			// return value of responseToApp to android app
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
		}	
	}
	
	public boolean checkIfUserExists(ResultSet set) throws SQLException{
		return set.first();
	}
}
