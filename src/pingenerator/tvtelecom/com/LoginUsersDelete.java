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
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

@WebServlet("/LoginUsersDelete")
public class LoginUsersDelete extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public LoginUsersDelete() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(LoginUsersUpdate.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        String userID = request.getParameter("userId");
        
		HttpSession session = request.getSession(false);
		String userId = ((Integer)session.getAttribute("userId")).toString();
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String sql = "update USR set ROLEID = 0, UPDATEDBY = "+userId+", UPDATEDDATE = CURRENT_TIMESTAMP where USERID = " + userID;
		
		String result="failed";

		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

			con = ds.getConnection();
			st = con.createStatement();
LOG.log(Level.INFO,"sql:{0}",new Object[]{sql});
			st.executeUpdate(sql);
			result = "succeed";
		} catch(NamingException | SQLException ex) {
LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
		    try {
		    	if (rs != null) {rs.close();}
		        if (st != null) {st.close();}
		        if (con != null) {con.close();}
		    } catch (SQLException ex) {
LOG.log(Level.WARNING, ex.getMessage(), ex);
				result = "failed";
		    }
		}

		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"result\":\""+result+"\"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
