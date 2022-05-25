package imports;

import java.sql.*;

import gantt.plugin.PluginAPI;

/**
 * Class for database comunication between the Rob-Ex and a database. 
 * @author fredrikp
 *
 */
public class DbConnection {

	private Connection con;
		
	/**
	 * gets the DbConnection based on the data connection in rob-ex (Database plugin)
	 * @param pAPI
	 * @param dataSrcName
	 */
	public DbConnection(PluginAPI pAPI, String dataSrcName) {
		try {
			con = pAPI.getConnectionByName(dataSrcName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Excecutes non update queries
	 * @param query
	 * @param parameters
	 * @return
	 */
	public ResultSet excecuteQuery(String query, String[] parameters) {
		try {
			PreparedStatement preparedStatement = getStatement(query, parameters);
			return preparedStatement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Excecutes queries that updates values in the database
	 * @param query
	 * @param parameters
	 * @return
	 */
	public int excecuteUpdateQuery(String query, String[] parameters) {
		try {
			PreparedStatement preparedStatement = getStatement(query, parameters);
			int executeUpdate = preparedStatement.executeUpdate();
			preparedStatement.close();
			return executeUpdate;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return Statement.EXECUTE_FAILED;
	}
	
	/**
	 * 
	 * @param query
	 * @param parameters
	 * @return a PreparedStatemanet with the query and parameters
	 */
	private PreparedStatement getStatement(String query, String[] parameters) {
		PreparedStatement preparedStatement = null;
		try {
			System.out.println(query);
			preparedStatement = con.prepareStatement(query);
			if (parameters != null) {
				for (int i = 0; i < parameters.length; i++) {
					preparedStatement.setString(i + 1, parameters[i]);
				}
			}
		} catch (SQLException e) {
			System.err.println("ERROR IN getStatement " + e); 
//			e.printStackTrace();
		}
		return preparedStatement;
	}
	
	/**
	 * Closes the current connection
	 */
	public void closeConnection() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
