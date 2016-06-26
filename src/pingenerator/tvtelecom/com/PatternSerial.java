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

@WebServlet("/PatternSerial")
public class PatternSerial extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PatternSerial() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PatternSerial.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);    
        //String jobId = request.getParameter("jobId");
        
		Connection con = null;
		Statement st1 = null;
		String sql1 ="select * from pattern order by channel";
		ResultSet rs1 = null;
		
		String result="failed";
		int PATTERNID;
		String CHANNEL;
		String CHANNELCODE;
		int DIGIT;
		int LASTSERIALNUMBER;
		
		String serialFormat = "";
		long serial = 0;
		
		String selectString = "";
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			while (rs1.next()) {
				PATTERNID = rs1.getInt("PATTERNID");
				CHANNEL = rs1.getString("CHANNEL");
				CHANNELCODE = rs1.getString("CHANNELCODE");
				DIGIT = rs1.getInt("DIGIT");
				LASTSERIALNUMBER = rs1.getInt("LASTSERIALNUMBER");
				
				serialFormat = "9" + String.format("%0$" + (DIGIT-CHANNELCODE.length()) + "d", 0).replace(' ', '0');
				serial = Long.parseLong(serialFormat) + LASTSERIALNUMBER;
				
				selectString += "<tr><td class='align-left'>"+CHANNEL+"</td><td class='align-center'>"+CHANNELCODE+"</td><td class='align-center'>"+Long.toString(serial).substring(1)+"</td>";
				selectString += "<td class='align-center' onclick='manageSerialEditClick(\""+PATTERNID+"\");'><i class='fa fa-pencil-square-o'></i> Edit</td></tr>";
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
/*
		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"result\":\""+result+"\",\"selectString\":"+selectString+"}");
		out.flush();
*/		
LOG.log(Level.INFO,"{0} {1}",new Object[]{"PatternTable: ",result});
		PrintWriter out = response.getWriter();
		out.print(selectString);
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
