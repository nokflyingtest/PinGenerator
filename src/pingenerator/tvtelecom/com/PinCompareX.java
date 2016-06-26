package pingenerator.tvtelecom.com;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

@WebServlet("/PinCompareX")
public class PinCompareX extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinCompareX() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinCompareX.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String jobId = request.getParameter("jobId");
        String userId = request.getParameter("userId");
		
LOG.log(Level.INFO,"{0} {1}",new Object[]{"PinCompareX-jobId: ",jobId});
        
		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from job where status = 'I' and jobid = '" + jobId + "'";
		ResultSet rs1 = null;
		
		Statement st2 = null;
		String sql20 ="select count(pin) c from pin where status = 'A' and serial is null";
		String sql2 ="select * from pin where status = 'A' and serial is null";
		ResultSet rs2 = null;
		
		PreparedStatement st3 = null;
		String sql3 = "update job set desc2 = ? where jobid = '" + jobId + "'";
		PreparedStatement st31 = null;
		String sql31 = "update pin set status = 'D', jobid = '" + jobId + "' where pin = ?";
		PreparedStatement st32 = null;
		String sql32 = "delete from pin where pin = ?";
		
		Statement st4 = null;
		String sql4 = "update job set status = '_status', dupcount = _ratio where jobid = '" + jobId + "'";
		String sql4r = "";
		
		String result="failed";
		
		//String uploadFolder = getServletContext().getInitParameter("uploadFolder");
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

	            sql4r = sql4.replaceAll("_status", "P");
	            sql4r = sql4r.replaceAll("_ratio", Long.toString(0));
				st4 = con.createStatement();
				st4.executeUpdate(sql4r);

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

				long cAll = 1;
				st2 = con.createStatement();
				rs2 = st2.executeQuery(sql20);
				if (rs2.next()) {cAll = rs2.getLong("c");}
//LOG.log(Level.INFO,"{0} {1}",new Object[]{"PinCompareX","cAll: " + cAll});
				long cTotal = 0; long cDup = 0;
				String pin = "";int pos = 0;
				int percent = 0;
				int checkCalc = 0; int checkPrev = 0;
				
				st3 = con.prepareStatement(sql3);
				st3.setString(1, "0");
				st3.executeUpdate();
				
				st31 = con.prepareStatement(sql31);
				st32 = con.prepareStatement(sql32);
				
				rs2.close();
				rs2 = st2.executeQuery(sql2);
				while (rs2.next()) {cTotal++;
					pin = rs2.getString("PIN");
					//pos = buffer.indexOf("\r\n"+pin+"\r\n");
					pos = buffer.indexOf(","+pin+"\r\n");
					if (pos > -1) {cDup++;
						st31.setString(1, pin);
						st31.executeUpdate();
						st32.setString(1, pin);
						st32.executeUpdate();
					}
					percent = (int)Math.floor((cTotal/cAll)*100);
					checkCalc = (int)Math.floor(percent/10);
//LOG.log(Level.INFO,"{0} {1}",new Object[]{"PinCompareX","percent: " + percent + " checkCalc: " + checkCalc});
					if (checkCalc > checkPrev) {
						checkPrev = checkCalc;
						st3.setString(1, Integer.toString(percent));
    					st3.executeUpdate();
LOG.log(Level.INFO,"{0} {1}",new Object[]{"PinCompareX","update Job: " + percent});
					}
				}
LOG.log(Level.INFO,"{0} {1}",new Object[]{"PinCompareX","All record: " + cAll + " Total: " + cTotal + " Dup: " + cDup});
				st3.setString(1, Integer.toString(100));
				st3.executeUpdate();
				
	            sql4r = sql4.replaceAll("_status", "S");
	            sql4r = sql4r.replaceAll("_ratio", Long.toString(cDup));
				st4.executeUpdate(sql4r);
				result = "succeed";
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"PinCompareX","Done!"});
			}
		} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
            try {
            	if (!result.equals("succeed")) {
            		sql4r = sql4.replaceAll("_status", "F");
            		sql4r = sql4r.replaceAll("_ratio", "0");
        			st4.executeUpdate(sql4r);
            	}
                if (rs1 != null) {rs1.close();}if (st1 != null) {st1.close();}
                if (rs2 != null) {rs2.close();}if (st2 != null) {st2.close();}
                if (st3 != null) {st3.close();}
                if (st4 != null) {st4.close();}
                if (con != null) {con.close();}
            } catch (SQLException ex) {
            	LOG.log(Level.WARNING, ex.getMessage(), ex);
            }
		}

		if (!result.equals("failed")) {
			URLConnection urlcon;
			try {
				String urlString = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+Utils.appPath+"PinLoad?jobId="+jobId+"&userId="+userId;
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"urlString",urlString});	
				URL url = new URL(urlString);
				urlcon = url.openConnection();
				urlcon.setConnectTimeout(100);
				urlcon.setReadTimeout(100);
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"call PinLoad",urlcon.getDate()});
			} catch (MalformedURLException e) { 
				LOG.log(Level.SEVERE, e.getMessage(), e);
				result = "failed";
			} catch (IOException e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
				result = "failed";
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
