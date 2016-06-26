package pingenerator.tvtelecom.com;

import java.io.File;
import java.io.FileReader;
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

@WebServlet("/PinGenVIP4X")
public class PinGenVIP4X extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public PinGenVIP4X() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinGenVIP4X.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String pin = request.getParameter("pin");
        String pinId = request.getParameter("pinId");
        String jobId = request.getParameter("jobId");
        
        HttpSession session = request.getSession(false);
		String userId = ((Integer)session.getAttribute("userId")).toString();
        
LOG.log(Level.INFO,"PinGenVIP4X pin: {0}",new Object[]{pin});
        
		Connection con = null;
		Statement st1 = null;
		String sql11 = "select * from job where jobId = '" + jobId + "'";
		String sql12 = "insert into pin (PIN,DIGIT,STATUS,JOBID,UPDATEDBY,UPDATEDDATE) values ('"+pin+"',"+pin.length()+",'A','"+jobId+"',"+userId+",CURRENT_TIMESTAMP)";
		ResultSet rs1 = null;
		
		String uploadFolder = Utils.PathUpload;
		String fileName;
		String result="failed";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql11);
			if (rs1.next()) {
				fileName = rs1.getString("DESC1").trim();
				if (!fileName.isEmpty()) {
					int len;
					char[] chr = new char[4096];
					final StringBuffer buffer = new StringBuffer();
					final FileReader reader = new FileReader(new File(uploadFolder + fileName));
					try {
						while ((len = reader.read(chr)) > 0) {
							buffer.append(chr, 0, len);
						}
					} finally {
						reader.close();
					}
					
					int pos = buffer.indexOf(","+pin+"\r\n");
					if (pos > -1) {
						result = "duplicated";
LOG.log(Level.INFO,"PinGenVIP4X found loading file and it duplicates{0}",new Object[]{});
					} else {
						result = "succeed";
LOG.log(Level.INFO,"PinGenVIP4X found loading file sql12: {0}",new Object[]{sql12});
						st1.executeUpdate(sql12);
					}
				} else {
LOG.log(Level.INFO,"PinGenVIP4X found job with no file sql12: {0}",new Object[]{sql12});
					st1.executeUpdate(sql12);
					result = "succeed";
				}
			} else {
LOG.log(Level.INFO,"PinGenVIP4X no job sql12: {0}",new Object[]{sql12});
				st1.executeUpdate(sql12);
				result = "succeed";
			}
		} catch (java.sql.SQLIntegrityConstraintViolationException e) {
LOG.log(Level.INFO,"PinGenVIP4X found duplicated pin: {0} {1}",new Object[]{pin});
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
