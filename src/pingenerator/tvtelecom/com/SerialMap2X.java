package pingenerator.tvtelecom.com;

import java.io.BufferedReader;
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

@WebServlet("/SerialMap2X")
public class SerialMap2X extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public SerialMap2X() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(SerialMap2X.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String jobId = request.getParameter("jobId");
        String userId = request.getParameter("userId");
		
LOG.log(Level.INFO,"SerialMap2X jobId:{0}",new Object[]{jobId});

		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from job where status = 'P' and jobid = '" + jobId + "'";
		String sql11 = "select * from pattern where patternid = _patternid";
		String sql12 = "select * from pin where status = 'A' and serial is null and digit = _digit FETCH FIRST 1 ROWS ONLY";
		ResultSet rs1 = null;
		
		PreparedStatement st2 = null;
		String sql2 = "update job set desc2 = ? where jobid = '" + jobId + "'";
		
		PreparedStatement st3 = null;
		String sql3 = "update pin set serial = ?, status = 'M', jobid = '" + jobId + "', updatedby = "+userId+", updateddate = CURRENT_TIMESTAMP where pin = ?";

		Statement st4 = null;
		String sql4 = "update job set status = '_status', desc2 = '_desc2' where jobid = '" + jobId + "'";
		String sql4r;
		
		String result="undefined";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			
			int patternId;
			long amount;
			String fileName;
			
			String channelcode;
			int digit;
			int pinDigit;
			
			con = ds.getConnection();
			st1 = con.createStatement();
			
			rs1 = st1.executeQuery(sql1);
            st4 = con.createStatement();
			if (rs1.next()) {
				patternId = rs1.getInt("PATTERNID");
				amount = rs1.getLong("AMOUNT");
				fileName = rs1.getString("DESC1");
				
				sql11 = sql11.replaceAll("_patternid", Integer.toString(patternId));
LOG.log(Level.INFO,"SerialMap2X sql11:{0}",new Object[]{sql11});
				rs1.close();
				rs1 = st1.executeQuery(sql11);
				if (rs1.next()) {
					channelcode = rs1.getString("CHANNELCODE");
					digit = rs1.getInt("DIGIT");
					pinDigit = rs1.getInt("PINDIGIT");
LOG.log(Level.INFO,"SerialMap2X channelcode:{0} digit:{1} pinDigit:{2}",new Object[]{channelcode,digit,pinDigit});
					
					LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(Utils.PathUpload + fileName));
					lineNumberReader.skip(Long.MAX_VALUE);
					int lineAmount = lineNumberReader.getLineNumber() + 1;
					lineNumberReader.close();
					
					String serialString;
					Long serial;Long maxSerial = 0L;
					int count = 0;
					try (BufferedReader br = new BufferedReader(new FileReader(Utils.PathUpload + fileName))) {
						st2 = con.prepareStatement(sql2);
						//Skip empty line
					    for(String line; (line = br.readLine()) != null; ) {
					    	if (!line.trim().isEmpty()) {
					    		serialString = line.substring(0, line.indexOf(","));
//LOG.log(Level.INFO,"SerialMap2X serial:{0}",new Object[]{serialString});
    					        if (serialString.indexOf(channelcode) == 0) {
    					        	serial = Long.parseLong(serialString);
    					        	if (serial > maxSerial) { maxSerial = serial; }
//LOG.log(Level.INFO,"SerialMap2X maxSerial:{0}",new Object[]{maxSerial});
						        }
    					        st2.setString(1, "F"+ lineAmount + "|" + ++count);
    					        st2.executeUpdate();
					    	}
					    }
					}

					if (maxSerial == 0L) {
						String maxSerialString = channelcode + String.format("%0$" + digit + "d", 0).replace(' ', '0');
						maxSerial = Long.parseLong(maxSerialString.substring(0, digit));
//LOG.log(Level.INFO,"SerialMap2X maxPattern:{0}",new Object[]{maxPattern});
					}
					maxSerial += 1;
LOG.log(Level.INFO,"SerialMap2X maxPattern:{0}",new Object[]{maxSerial});
					
//LOG.log(Level.INFO,"SerialMap2X sql3:{0}",new Object[]{sql3});
					st2 = con.prepareStatement(sql2);
					st3 = con.prepareStatement(sql3);

					sql12 = sql12.replaceAll("_digit", Integer.toString(pinDigit));
					for (int i = 1; i <= amount; i++) {
						rs1 = st1.executeQuery(sql12);
						while (rs1.next()) {
							st3.setString(1, maxSerial.toString());
							st3.setString(2, rs1.getString("PIN"));
							st3.executeUpdate();
							maxSerial++;
					        st2.setString(1, "M"+ amount + "|" + i);
					        st2.executeUpdate();
						}
					}

		            sql4r = sql4.replaceAll("_status", "S");
		            sql4r = sql4r.replaceAll("_desc2", "");
					st4.executeUpdate(sql4r);
					result = "succeed";
				} else {
		            sql4r = sql4.replaceAll("_status", "F");
		            sql4r = sql4r.replaceAll("_desc2", "");
					st4.executeUpdate(sql4r);
					result = "failed";
				}
			} else {
	            sql4r = sql4.replaceAll("_status", "F");
	            sql4r = sql4r.replaceAll("_desc2", "");
				st4.executeUpdate(sql4r);
				result = "failed";
			}

		} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
            try {
            	if (result.equals("undefined")) {
    	            sql4r = sql4.replaceAll("_status", "F");
    	            sql4r = sql4r.replaceAll("_desc2", "undefined error");
    	            st4 = con.createStatement();
    				st4.executeUpdate(sql4r);
    				result = "failed";
            	}
                if (rs1 != null) {rs1.close();}
                if (st1 != null) {st1.close();}
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
		out.print("{\"result\":\""+result+"\",\"jobId\":"+jobId+"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
