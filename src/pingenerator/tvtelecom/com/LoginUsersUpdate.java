package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

@WebServlet("/LoginUsersUpdate")
public class LoginUsersUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public LoginUsersUpdate() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(LoginUsersUpdate.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        String userID = request.getParameter("userId");
        String name = request.getParameter("name");
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        
		HttpSession session = request.getSession(false);
		String userId = ((Integer)session.getAttribute("userId")).toString();
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String sql = "update USR set _NAME _USERNAME _PASSWORD UPDATEDBY = "+userId+", UPDATEDDATE = CURRENT_TIMESTAMP where USERID = " + userID;
		
		String result="failed";

		try {
			if (!name.trim().isEmpty()) {sql = sql.replaceAll("_NAME", "name = '"+name+"',");} else {sql = sql.replaceAll("_NAME", "");}
			if (!userName.trim().isEmpty()) {sql = sql.replaceAll("_USERNAME", "username = '"+userName.trim().toUpperCase()+"',");} else {sql = sql.replaceAll("_USERNAME", "");}
			if (!password.trim().isEmpty()) {
				MessageDigest md;
				
				md = MessageDigest.getInstance("MD5");
				md.update(password.trim().getBytes());
				byte byteData[] = md.digest();

				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < byteData.length; i++) {
				 sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
				}
				password = sb.toString();

				sql = sql.replaceAll("_PASSWORD", "password = '"+password+"',");
			} else {sql = sql.replaceAll("_PASSWORD", "");}
			
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

			con = ds.getConnection();
			st = con.createStatement();
LOG.log(Level.INFO,"sql:{0}",new Object[]{sql});
			st.executeUpdate(sql);
			result = "succeed";
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
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
