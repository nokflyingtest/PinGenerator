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

@WebServlet("/LoginUserGetById")
public class LoginUserGetById extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public LoginUserGetById() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(LoginUserGetById.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String userID = request.getParameter("userID");
        
		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from usr where userid = " + userID;
		ResultSet rs1 = null;
		
		String result="failed";
		int USERID = 0;
		String NAME = null;
		String USERNAME = null;
		String PASSWORD = null;
		
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				USERID = rs1.getInt("USERID");
				NAME = rs1.getString("NAME");
				USERNAME = rs1.getString("USERNAME");
				PASSWORD = rs1.getString("PASSWORD");
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

		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"result\":\""+result+"\",\"USERID\":"+USERID+",\"NAME\":\""+NAME+"\",\"USERNAME\":\""+USERNAME+"\",\"PASSWORD\":\""+PASSWORD+"\"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
