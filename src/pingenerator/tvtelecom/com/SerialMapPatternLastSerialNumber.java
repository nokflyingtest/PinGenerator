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

@WebServlet("/SerialMapPatternLastSerialNumber")
public class SerialMapPatternLastSerialNumber extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public SerialMapPatternLastSerialNumber() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(SerialMapPatternLastSerialNumber.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        String patternId = request.getParameter("patternId");
        
		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from pattern where patternid = " + patternId;
		ResultSet rs1 = null;
		
		LOG.log(Level.INFO,"SerialMapPatternLastSerialNumber sql {0}",new Object[]{sql1});		
		String result="failed";
		String CHANNELCODE = null;
		int DIGIT = 0;
		int LASTSERIALNUMBER = 0;
		String SerialNumber = null;
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			if (rs1.next()) {
				CHANNELCODE = rs1.getString("CHANNELCODE");
				DIGIT = rs1.getInt("DIGIT");
				LASTSERIALNUMBER = rs1.getInt("LASTSERIALNUMBER");
				Long maxSerial = Long.parseLong(String.format("%1$" + (DIGIT-CHANNELCODE.length()) + "d", 9).replace(' ', '9'));
				LASTSERIALNUMBER++;
				if (LASTSERIALNUMBER > maxSerial) {LASTSERIALNUMBER = 1;}
				String serialFormat = "9" + String.format("%0$" + (DIGIT-CHANNELCODE.length()) + "d", 0).replace(' ', '0');
				long serialNumber = Long.parseLong(serialFormat) + LASTSERIALNUMBER;
				SerialNumber = Long.toString(serialNumber).substring(1);
				
				//batchNumber = "BAT" + Long.toString(maxBatch).substring(1);
				//batchNumber = Long.toString(serialNumber).substring(1);
			}
			result = "succeed";
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
LOG.log(Level.INFO,"SerialMapPatternLastSerialNumber ChannelCode {0} SerialNumber {1}",new Object[]{CHANNELCODE,SerialNumber});
		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"result\":\""+result+"\",\"CHANNELCODE\":\""+CHANNELCODE+"\",\"SerialNumber\":\""+SerialNumber+"\"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
