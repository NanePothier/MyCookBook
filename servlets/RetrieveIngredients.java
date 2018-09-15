import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
       
    public RetrieveIngredients() {}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			
		Connection connection = null;
		Statement queryStatement = null;
		ResultSet resultSet = null;
		
		try {
			
			JSONArray jsonArray = new JSONArray();
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			queryStatement = connection.createStatement();
			String sqlString = "SELECT ingredient_name FROM ingredients";
			resultSet = queryStatement.executeQuery(sqlString);
			
			while(resultSet.next()) {
				
				JSONObject object = new JSONObject();
				object.put("ingredient", resultSet.getObject("ingredient_name"));			
				jsonArray.put(object);
			}
				
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
		}finally {	
			try {
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
