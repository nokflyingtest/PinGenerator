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

@WebServlet("/SerialMap2Count")
public class SerialMap2Count extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public SerialMap2Count() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(SerialMap2Count.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String jobId = request.getParameter("jobId");
        
LOG.log(Level.INFO,"SerialMap2Count jobId: {0}",new Object[]{jobId});

		Connection con = null;
		Statement st1 = null;
		String sql1 = "select * from job where jobid = '" + jobId + "'";
		ResultSet rs1 = null;
		
		String result="failed";
		String status = "F";
		String desc2 = "";
		String jobType = "";
		String amount = "";
		String count = "";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				desc2 = rs1.getString("DESC2");
				status = rs1.getString("status");
				if (desc2.isEmpty()) {
					jobType = "M";
					amount = "100";
					count = "100";
				} else {
					jobType = desc2.substring(0, 1);
					amount = desc2.substring(1, desc2.indexOf("|"));
					count = desc2.substring(desc2.indexOf("|")+1);
				}
				result = "succeed";
LOG.log(Level.INFO,"SerialMap2Count desc2: {0}  jobType: {1} amount: {2} count: {3} status: {4}",new Object[]{desc2,jobType,amount,count,status});
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
		out.print("{\"result\":\""+result+"\",\"jobId\":"+jobId+",\"amount\":"+amount+",\"count\":"+count+",\"status\":\""+status+"\",\"jobType\":\""+jobType+"\"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
