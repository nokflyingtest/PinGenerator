package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet("/LoginUsersTable")
public class LoginUsersTable extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public LoginUsersTable() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(LoginUsersTable.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        
		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from usr where roleid <> 0 order by username";
		ResultSet rs1 = null;
		
		String result="failed";
		
		int USERID;
		String USERNAME;
		String NAME;
		
		String selectString = "";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			while (rs1.next()) {
				USERID = rs1.getInt("USERID");
				USERNAME = rs1.getString("USERNAME");
				NAME = rs1.getString("NAME");
				selectString += "<tr><td class='align-left'>"+USERNAME+"</td><td class='align-left'>"+NAME+"</td>";
				if (USERID != 1) {
					selectString += "<td class='align-center' onclick='manageUsersEditClick(\""+USERID+"\");'><i class='fa fa-pencil-square-o'></i> Edit</td><td class='align-center' onclick='manageUsersDeleteClick(\""+USERID+"\");'><i class='fa fa-trash'></i> Delete</td></tr>";
				} else {
					selectString += "<td class='align-center'></td><td class='align-center'></td></tr>";
				}
							}
			result = "succeed";
		} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
            try {
                if (rs1 != null) {rs1.close();}if (st1 != null) {st1.close();}
                if (con != null) {con.close();}
            } catch (SQLException ex) {
            	LOG.log(Level.WARNING, ex.getMessage(), ex);
            }
		}
/*
		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"result\":\""+result+"\",\"selectString\":"+selectString+"}");
		out.flush();
*/		
LOG.log(Level.INFO,"{0} {1}",new Object[]{"PatternTable: ",result});
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print(selectString);
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
