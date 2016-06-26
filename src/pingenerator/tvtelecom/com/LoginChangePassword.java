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

@WebServlet("/LoginChangePassword")
public class LoginChangePassword extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public LoginChangePassword() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(LoginChangePassword.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        
        HttpSession session = request.getSession(false);
		String userId = ((Integer)session.getAttribute("userId")).toString();
		
        String pCurrent = request.getParameter("pCurrent").trim();
        String pNow = request.getParameter("pNow").trim();
        //String pNow2 = request.getParameter("pNow2").trim();
        
		MessageDigest mdCurrent;
		MessageDigest mdNow;
		try {
			mdCurrent = MessageDigest.getInstance("MD5");
			mdCurrent.update(pCurrent.getBytes());
			byte byteData[] = mdCurrent.digest();

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
			 sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			pCurrent = sb.toString();
			
			mdNow = MessageDigest.getInstance("MD5");
			mdNow.update(pNow.getBytes());
			byteData = mdNow.digest();
			sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
			 sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			pNow = sb.toString();
			
LOG.log(Level.INFO,"pCurrent:{0} pNow:{1}",new Object[]{pCurrent,pNow});
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from usr where userId = " + userId + " and password = '" + pCurrent + "'";
		ResultSet rs1 = null;

		String sql2 = "update usr set password = '" + pNow + "' where userId = " + userId;
		
		String result="failed";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				st1.executeUpdate(sql2);
				result = "succeed";
			} else {
				result="Invalid password";
			}
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
		out.print("{\"result\":\""+result+"\"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
