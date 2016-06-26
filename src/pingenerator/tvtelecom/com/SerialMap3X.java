package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

@WebServlet("/SerialMap3X")
public class SerialMap3X extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public SerialMap3X() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(SerialMap3X.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String jobId = request.getParameter("jobId");
        String userId = request.getParameter("userId");
		
LOG.log(Level.INFO,"SerialMap3X jobId:{0}",new Object[]{jobId});

		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from job where status = 'I' and jobid = '" + jobId + "'";
		String sql11 = "select * from pattern where patternid = _patternid";
		ResultSet rs1 = null;
		
		Statement st2 = null;
		String sql2 = "update job set status = '_status', desc1 = '_desc1', desc3 = '_desc3' where jobid = '" + jobId + "'";
		String sql2r = "";
		
		Statement st3 = null;
		String sql3 = "select count(pin) c from pin where status = 'A' and serial is null and digit = _digit";
		//String sql31 = "select * from pin where status = 'A' and serial is null FETCH FIRST _amount ROWS ONLY";
		String sql31 = "select * from pin where status = 'A' and serial is null and digit = _digit FETCH FIRST 1 ROWS ONLY";
		ResultSet rs3 = null;
		
		PreparedStatement st5 = null;
		String sql5 = "update pin set serial = ?, status = 'M', jobid = '" + jobId + "', updatedby = "+userId+", updateddate = CURRENT_TIMESTAMP where pin = ?";
		String sql51 = "update pattern set LASTSERIALNUMBER = _maxserial where patternid = _patternid";
		
		String result="undefined";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			
			int patternId;
			long amount;
			int pindigit;
			String desc2;
			String batchNumberPrefix;
			String batchNumber;
			String serialNumberPrefix;
			String serialNumber;
			
			//String channel;
			int digit;
			//int pinDigit;
			
			con = ds.getConnection();
			st1 = con.createStatement();
			
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				patternId = rs1.getInt("PATTERNID");
				amount = rs1.getLong("AMOUNT");
				pindigit = rs1.getInt("DIGIT");
				desc2 = rs1.getString("DESC2");
				String[] desc2a = desc2.split("\\|"); 
				batchNumberPrefix = desc2a[0];
				batchNumber = desc2a[1];
				serialNumberPrefix = desc2a[2];
				serialNumber = desc2a[3];
				
				sql11 = sql11.replaceAll("_patternid", Integer.toString(patternId));
LOG.log(Level.INFO,"SerialMap3X sql11:{0}",new Object[]{sql11});
				rs1.close();
				rs1 = st1.executeQuery(sql11);
					if (rs1.next()) {
						//channel = rs1.getString("CHANNEL");
						digit = rs1.getInt("DIGIT");
						//pinDigit = rs1.getInt("PINDIGIT");
						
						long maxBatch = Long.parseLong(batchNumber);
						if (maxBatch >= 1000000) { maxBatch = 1L; }
						String maxBatchFormat = "9" + String.format("%0$" + "6d", 0).replace(' ', '0');
						maxBatch = Long.parseLong(maxBatchFormat) + maxBatch;
						batchNumber = Long.toString(maxBatch).substring(1);
						
						Path pathBatchNumber = Paths.get(Utils.PathFileMappingSerialBatchNumber3);
						Files.write(pathBatchNumber, (batchNumberPrefix+"|"+batchNumber).getBytes(), StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);

			            sql2r = sql2.replaceAll("_status", "P");
			            sql2r = sql2r.replaceAll("_desc1", "");
			            sql2r = sql2r.replaceAll("_desc3", batchNumberPrefix+"|"+batchNumber);
						st2 = con.createStatement();
						st2.executeUpdate(sql2r);
	
						st3 = con.createStatement();
						sql3 = sql3.replaceAll("_digit", Integer.toString(pindigit));
						rs3 = st3.executeQuery(sql3);
						if (rs3.next()) {
							long cAvailablePin = rs3.getLong("c");
							if (cAvailablePin >= amount) {
								
								long maxSerial = Long.parseLong(serialNumber);
								long maximumSerial = Long.parseLong(String.format("%1$" + (digit-serialNumberPrefix.length()) + "d", 9).replace(' ', '9'));
//LOG.log(Level.INFO,"SerialMap3X XXXXXXXX:{0}",new Object[]{maximumSerial});
								if (maxSerial > maximumSerial) {maxSerial = 1L;}
								String maxSerialFormat = "9" + String.format("%0$" + (digit-serialNumberPrefix.length()) + "d", 0).replace(' ', '0');
								maxSerial = Long.parseLong(maxSerialFormat) + maxSerial;
								//serialNumber = Long.toString(maxSerial).substring(1);
								
								long serialOne = Long.parseLong(maxSerialFormat) + 1;
								maximumSerial = Long.parseLong(String.format("%1$" + (digit-serialNumberPrefix.length()+1) + "d", 9).replace(' ', '9'));
//LOG.log(Level.INFO,"SerialMap3X YYYYYYYY:{0}",new Object[]{maximumSerial});
								long serial = maxSerial;
								String pin = "";
								st5 = con.prepareStatement(sql5);
								rs3.close();
								sql31 = sql31.replaceAll("_digit", Integer.toString(pindigit));
								for (int i = 1; i <= amount; i++) {
									rs3 = st3.executeQuery(sql31);
									while (rs3.next()) {
//LOG.log(Level.INFO,"SerialMap3X ZZZZZZZ:{0}",new Object[]{serial});
										pin = rs3.getString("PIN");
										st5.setString(1, serialNumberPrefix + Long.toString(serial).substring(1));
										st5.setString(2, pin);
										st5.executeUpdate();
										serial++;
										if (serial > maximumSerial) {serial = serialOne;}
									}
								}
								if (serial != serialOne) {serial--;}
								sql51 = sql51.replaceAll("_patternid", Integer.toString(patternId));
								sql51 = sql51.replaceAll("_maxserial", Long.toString(Long.parseLong(Long.toString(serial).substring(1))));
LOG.log(Level.INFO,"SerialMap3X sql51:{0}",new Object[]{sql51});
								st2.executeUpdate(sql51);
								
					            sql2r = sql2.replaceAll("_status", "S");
					            sql2r = sql2r.replaceAll("_desc1", "");
					            sql2r = sql2r.replaceAll("_desc3", "");
								st2.executeUpdate(sql2r);
								result = "succeed";
							} else {
					            sql2r = sql2.replaceAll("_status", "F");
					            sql2r = sql2r.replaceAll("_desc1", "Available PIN is not enough");
								st2.executeUpdate(sql2r);
								result = "failed";
							}
						}
					}
	LOG.log(Level.INFO,"SerialMap3X Done!",new Object[]{});

			} else {
	            sql2r = sql2.replaceAll("_status", "F");
	            sql2r = sql2r.replaceAll("_desc1", "Found unknown error, please contact system administrator.");
	            st2 = con.createStatement();
				st2.executeUpdate(sql2r);
				result = "failed";
			}
		} catch(NamingException | SQLException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
            try {
            	if (result.equals("undefined")) {
    	            sql2r = sql2.replaceAll("_status", "F");
    	            sql2r = sql2r.replaceAll("_desc1", "Found unknown error, please contact system administrator.");
    	            st2 = con.createStatement();
    				st2.executeUpdate(sql2r);
    				result = "failed";
            	}
                if (rs1 != null) {rs1.close();}
                if (st1 != null) {st1.close();}
                if (st2 != null) {st2.close();}
                if (st3 != null) {st3.close();}
                if (st5 != null) {st5.close();}
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
