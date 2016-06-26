package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

@WebServlet("/PinLoad")
public class PinLoad extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public PinLoad() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinLoad.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);       

        String jobId = request.getParameter("jobId");
        String userId = request.getParameter("userId");
		
		//SimpleDateFormat dFormat = new SimpleDateFormat("yyMMddhhmmss");
		//String jobLoadId = dFormat.format(new Date());
		String jobLoadId = jobId.substring(0, 11) + "X";
		
		Connection con = null;
		Statement st1 = null;
		String sql11 = "select * from job where jobid = '" + jobId + "'";
		String sql12 = "insert into job (JOBID,TYPE,STATUS,DESC1,UPDATEDBY,UPDATEDDATE) values ('" + jobLoadId + "','PL','I','_fileName',"+ userId + ",CURRENT_TIMESTAMP)";
		
		ResultSet rs1 = null;
		
		
		String result="failed";

		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");

			String fileName;

			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql11);
			if (rs1.next()) {
				result="failed";
				fileName = rs1.getString("DESC1");
				
				sql12 = sql12.replaceAll("_fileName", fileName);
LOG.log(Level.INFO,"PinLoad sql12: {0}",new Object[]{sql12});	
				st1.executeUpdate(sql12);
			}
			result = "succeed";
		} catch(NamingException | SQLException ex) {
LOG.log(Level.SEVERE, ex.getMessage(), ex);
			result = "failed";
		} finally {
		    try {
		    	if (rs1 != null) {rs1.close();}
		        if (st1 != null) {st1.close();}
		        if (con != null) {con.close();}
		    } catch (SQLException ex) {
LOG.log(Level.WARNING, ex.getMessage(), ex);
				result = "failed";
		    }
		}

		if (!result.equals("failed")) {
			URLConnection urlcon;
			try {
				String urlString = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+Utils.appPath+"PinLoadX?jobId="+jobLoadId+"&userId="+userId;
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"urlString",urlString});	
				URL url = new URL(urlString);
				urlcon = url.openConnection();
				urlcon.setConnectTimeout(100);
				urlcon.setReadTimeout(100);
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"call PinLoadX",urlcon.getDate()});
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
