package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
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

@WebServlet("/PinGenVIP3")
public class PinGenVIP3 extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinGenVIP3() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinGenVIP3.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        String status = request.getParameter("s");
        String jobId = request.getParameter("jobid");
        String batchNumberPrefix = request.getParameter("batchNumberPrefix");
        String batchNumber = request.getParameter("batchNumber");
        String serialNumberPrefix = request.getParameter("serialNumberPrefix");
        String serialNumber = request.getParameter("serialNumber");
        
		HttpSession session = request.getSession(false);
		String userId = ((Integer)session.getAttribute("userId")).toString();
		
		if (status.equals("P")) {
			SimpleDateFormat dFormat = new SimpleDateFormat("yyMMddhhmmss");
			jobId = dFormat.format(new Date());
		}
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String sql = "insert into job (JOBID,TYPE,STATUS,DESC2,UPDATEDBY,UPDATEDDATE) values ('" + jobId + "','PV','P','"+ batchNumberPrefix+"|"+batchNumber+"|"+serialNumberPrefix+"|"+serialNumber +"',"+ userId + ",CURRENT_TIMESTAMP)";
		if (!status.equals("P")) {sql = "UPDATE job SET STATUS = '"+status+"' WHERE jobid = '"+jobId+"'";}
		
		
		String result="failed";

		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

			con = ds.getConnection();
			st = con.createStatement();
LOG.log(Level.INFO,"sql:{0}",new Object[]{sql});
			st.executeUpdate(sql);
			
			if (status.equals("P")) {
				long maxBatch = Long.parseLong(batchNumber);
				if (maxBatch >= 1000000) { maxBatch = 1L; }
				String maxBatchFormat = "9" + String.format("%0$" + "6d", 0).replace(' ', '0');
				maxBatch = Long.parseLong(maxBatchFormat) + maxBatch;
				batchNumber = Long.toString(maxBatch).substring(1);
				
				Path pathBatchNumber = Paths.get(Utils.PathFileMappingSerialBatchNumber3);
				Files.write(pathBatchNumber, (batchNumberPrefix+"|"+batchNumber).getBytes(), StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
			}

			result = "succeed";
		} catch(NamingException | SQLException ex) {
LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
		    try {
		    	if (rs != null) {rs.close();}
		        if (st != null) {st.close();}
		        if (con != null) {con.close();}
		    } catch (SQLException ex) {
LOG.log(Level.WARNING, ex.getMessage(), ex);
				result = "failed";
		    }
		}
LOG.log(Level.INFO,"PinGenVIP3:{0}",new Object[]{result});
		PrintWriter out = response.getWriter();
		out.print(jobId);
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
