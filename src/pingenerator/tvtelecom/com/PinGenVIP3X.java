package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
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

@WebServlet("/PinGenVIP3X")
public class PinGenVIP3X extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinGenVIP3X() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinGenVIP3X.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String pin = request.getParameter("pin");
        String pinId = request.getParameter("pinId");
        String jobId = request.getParameter("jobId");
        
        HttpSession session = request.getSession(false);
		String userId = ((Integer)session.getAttribute("userId")).toString();
        
LOG.log(Level.INFO,"PinGenVIP3X pin: {0}",new Object[]{pin});
        
		Connection con = null;
		Statement st1 = null;
		String sql1 = "insert into pin (PIN,DIGIT,STATUS,JOBID,UPDATEDBY,UPDATEDDATE) values ('"+pin+"',"+pin.length()+",'A','"+jobId+"',"+userId+",CURRENT_TIMESTAMP)";
		
		String result="failed";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

			con = ds.getConnection();
			st1 = con.createStatement();
			st1.executeUpdate(sql1);
LOG.log(Level.INFO,"PinGenVIP3X sql1: {0}",new Object[]{sql1});

			result="succeed";
		} catch (java.sql.SQLIntegrityConstraintViolationException e) {
LOG.log(Level.INFO,"PinGenVIP3X found duplicated pin: {0} {1}",new Object[]{pin});
			result = "duplicated";
		} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
            try {
                if (st1 != null) {st1.close();}
                if (con != null) {con.close();}
            } catch (SQLException ex) {
            	LOG.log(Level.WARNING, ex.getMessage(), ex);
            }
		}

		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"result\":\""+result+"\",\"jobId\":"+jobId+",\"pinId\":"+pinId+",\"pin\":\""+pin+"\"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
