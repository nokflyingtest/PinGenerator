package pingenerator.tvtelecom.com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

@WebServlet("/PinLoadX")
public class PinLoadX extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public PinLoadX() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinCompare3X.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String jobId = request.getParameter("jobId");
		String userId = request.getParameter("userId");
		
LOG.log(Level.INFO,"{0} {1}",new Object[]{"PinLoadX jobId: ",jobId});
        
		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from job where status = 'I' and jobid = '" + jobId + "'";
		ResultSet rs1 = null;
		
		PreparedStatement st2 = null;
		String sql2 = "insert into pin (SERIAL,PIN,DIGIT,STATUS,JOBID,UPDATEDBY,UPDATEDDATE) values (?,?,?,'D','"+jobId+"',"+userId+",CURRENT_TIMESTAMP)";
		
		PreparedStatement st3 = null;
		String sql3 = "update pin set SERIAL = ?, DIGIT = ?, status = 'D', jobid = '" + jobId + "' where status <> 'D' and pin = ?";

		PreparedStatement st31 = null;
		String sql31 = "update job set DESC2 = ? where jobid = '" + jobId + "'";
		
		Statement st4 = null;
		String sql4 = "update job set status = '_status', dupcount = _ratio, amount = _amount where jobid = '" + jobId + "'";
		String sql4r = "";
		
		String result="failed";
		int lineAmount = 0;
		String uploadFolder = Utils.PathUpload;
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

			String fileName;

			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				result="failed";
				fileName = rs1.getString("DESC1");

				LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(new File(uploadFolder + fileName)));
				lineNumberReader.skip(Long.MAX_VALUE);
				lineAmount = lineNumberReader.getLineNumber() + 1;
				lineNumberReader.close();
				
	            sql4r = sql4.replaceAll("_status", "P");
	            sql4r = sql4r.replaceAll("_ratio", "0");
	            sql4r = sql4r.replaceAll("_amount", Integer.toString(lineAmount));
				st4 = con.createStatement();
				st4.executeUpdate(sql4r);
				
				int count = 0;int cDup = 0;
				String [] aline; int res;
				st2 = con.prepareStatement(sql2);st3 = con.prepareStatement(sql3);st31 = con.prepareStatement(sql31);

				BufferedReader br = new BufferedReader(new FileReader(new File(uploadFolder + fileName)));
			    for(String line; (line = br.readLine()) != null; ) {count++;
			    	st31.setString(1, Integer.toString(count));st31.executeUpdate();
			    	if (!line.trim().isEmpty()) {
			    		aline = line.split(",");
				        st2.setString(1, aline[0]);
				        st2.setString(2, aline[1]);
				        st2.setInt(3, aline[1].length());
				        try {
					        st2.executeUpdate();
				        } catch (java.sql.SQLIntegrityConstraintViolationException ex) {
				        	st3.setString(1, aline[0]);
				        	st3.setString(3, aline[1]);
				        	st3.setInt(2, aline[1].length());
				        	res = st3.executeUpdate();
				        	if (res > 0) cDup++;
				        }
			    	}
			    }
			    br.close();
			    st31.setString(1, Integer.toString(lineAmount));st31.executeUpdate();
			    
	            sql4r = sql4.replaceAll("_status", "S");
	            sql4r = sql4r.replaceAll("_ratio", Integer.toString(cDup));
	            sql4r = sql4r.replaceAll("_amount", Integer.toString(lineAmount));
				st4.executeUpdate(sql4r);
				result = "succeed";
LOG.log(Level.INFO,"PinLoadX Done! fileName: {0}",new Object[]{fileName});
			}
		} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
            try {
            	if (!result.equals("succeed")) {
            		sql4r = sql4.replaceAll("_status", "F");
            		sql4r = sql4r.replaceAll("_ratio", "0");
            		sql4r = sql4r.replaceAll("_amount", Integer.toString(lineAmount));
        			st4.executeUpdate(sql4r);
            	}
                if (rs1 != null) {rs1.close();}if (st1 != null) {st1.close();}
                if (st2 != null) {st2.close();}
                if (st3 != null) {st3.close();}
                if (st4 != null) {st4.close();}
                if (con != null) {con.close();}
            } catch (SQLException ex) {
            	LOG.log(Level.WARNING, ex.getMessage(), ex);
            }
		}

		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"jobId\":"+jobId+",\"result\":\""+result+"\"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
