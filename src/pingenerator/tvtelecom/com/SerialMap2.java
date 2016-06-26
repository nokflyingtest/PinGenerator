package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

@WebServlet("/SerialMap2")
public class SerialMap2 extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public SerialMap2() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(SerialMap2.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        String jobId = request.getParameter("jobId");
        String batchNumber = request.getParameter("batchNumber");
        String serialPattern = request.getParameter("serialPattern");
        String pinAmount = request.getParameter("pinAmount");


			HttpSession session = request.getSession(false);
			String userId = ((Integer)session.getAttribute("userId")).toString();
			
			//SimpleDateFormat dFormat = new SimpleDateFormat("yyMMddhhmmss");
			//String jobId = dFormat.format(new Date());
			
	LOG.log(Level.INFO,"userId:{0} serialPattern:{1} pinAmount:{2} jobId:{3}",new Object[]{userId,serialPattern,pinAmount,jobId});


			Connection con = null;
			Statement st = null;
			ResultSet rs = null;
			String sql0 = "SELECT * FROM PATTERN WHERE PATTERNID = "+serialPattern;
			String sql = "update job set DIGIT = _DIGIT, AMOUNT = " + pinAmount + ", PATTERNID = "+serialPattern+", DESC3 = '" + batchNumber + "', STATUS = 'P', UPDATEDBY = "+ userId + ", UPDATEDDATE = CURRENT_TIMESTAMP where jobId = '" + jobId + "'";
			
			String result="failed";
			
			try {
				Context ctx = new InitialContext();
				DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

				con = ds.getConnection();
				st = con.createStatement();
	LOG.log(Level.INFO,"sql0:{0}",new Object[]{sql0});
				rs = st.executeQuery(sql0);
				if (rs.next()) {
					//String CHANNEL = rs.getString("CHANNEL");
					//int DIGIT = rs.getInt("DIGIT");
					int PINDIGIT = rs.getInt("PINDIGIT");
					sql = sql.replaceAll("_DIGIT", Integer.toString(PINDIGIT));
	LOG.log(Level.INFO,"sql:{0}",new Object[]{sql});
					st.executeUpdate(sql);
					
					Path pathBatchNumber = Paths.get(Utils.PathFileMappingSerialBatchNumber);
					long maxBatch = Long.parseLong(batchNumber);
					Files.write(pathBatchNumber, Long.toString(maxBatch+1).getBytes(), StandardOpenOption.CREATE);
					
					result = "succeed";
				}
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

			if (!result.equals("failed")) {
				URLConnection urlcon;
				try {
					String urlString = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+Utils.appPath+"SerialMap2X?jobId="+jobId+"&userId="+userId;
	LOG.log(Level.INFO,"SerialMap call SerialMap2X url:{0}",new Object[]{urlString});
					URL url = new URL(urlString);
					urlcon = url.openConnection();
					urlcon.setConnectTimeout(100);
					urlcon.setReadTimeout(100);
	LOG.log(Level.INFO,"call SerialMap2X: {0}",new Object[]{urlcon.getDate()});
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
			out.print("{\"result\":\""+result+"\",\"jobId\":"+jobId+"}");
			out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
