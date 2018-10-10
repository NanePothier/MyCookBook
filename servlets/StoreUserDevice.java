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
 * Servlet implementation class StoreUserDevice
 */
@WebServlet("/StoreUserDevice")
public class StoreUserDevice extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    
    public StoreUserDevice() {}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

	/**
	 * store device Id and user email it belongs to
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = "";
		String deviceId;
		String userEmail;
		String result = "";
		Connection connection = null;
		PreparedStatement deviceStatement = null;
		Statement statement = null;
		ResultSet resultSet = null;
		String successIndicator = "fail";
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			userEmail = jsonObject.getString("user_email");
			deviceId = jsonObject.getString("device_id");
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			String duplicateQuery = "SELECT * FROM deviceidentification WHERE device_id = '" + deviceId + "'";
			statement = connection.createStatement();
			resultSet = statement.executeQuery(duplicateQuery);
			
			if(!(resultSet.next())) {
				
				String queryString = "INSERT INTO deviceidentification VALUES(?,?)";
				deviceStatement = connection.prepareStatement(queryString);
				
				deviceStatement.setString(1, deviceId);
				deviceStatement.setString(2, userEmail);
				deviceStatement.executeUpdate();
				
				successIndicator = "success";
			}
			
			JSONObject jObject = new JSONObject();
			jObject.put("successIndicator", successIndicator);
			
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
				if(statement != null) {
					statement.close();
				}
				if(resultSet != null) {
					resultSet.close();
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
