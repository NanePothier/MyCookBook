import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class GetConversionFactors
 */

@WebServlet("/GetConversionFactors")
public class GetConversionFactors extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger("InfoLogging");
       
    public GetConversionFactors() {
        
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String line = "";
		String result = "";
		Connection connection = null;
		Statement queryStatement = null;
		ResultSet resultSet = null;
		String measureFrom, measureTo, measureCategory;
		double factor;
		JSONArray responseArray = new JSONArray();
		
		try {
			
			BufferedReader reader = request.getReader();
			
			while((line = reader.readLine()) != null) {
				result += line;
			}
			
			JSONObject jsonObject = new JSONObject(result);
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://173.244.1.42:3306/S0280202", "S0280202", "New2018");
			
			String selectQuery = "SELECT * FROM conversion";
			queryStatement = connection.createStatement();
			resultSet = queryStatement.executeQuery(selectQuery);
			
			while(resultSet.next()) {
				
				JSONObject jObject = new JSONObject();
				
				jObject.put("measure_from", resultSet.getString("measure_from"));
				jObject.put("measure_to", resultSet.getString("measure_to"));
				jObject.put("measure_cat", resultSet.getString("measure_category"));
				jObject.put("factor", resultSet.getDouble("factor"));
				
				responseArray.put(jObject);
			}
			
			String json = responseArray.toString();
			
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
