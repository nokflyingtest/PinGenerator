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

import org.json.simple.JSONObject;

@WebServlet("/MainDashboard")
public class MainDashboard extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public MainDashboard() {
        super();
    }

    @SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(MainDashboard.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);

		Connection con = null;
		Statement st1 = null;
		String sql1 = "select count(*) cA, digit from pin where status = 'A' and serial is null group by digit";
		
		
		ResultSet rs1 = null;

		
		String result="failed";
        //JSONObject json;
        //JSONArray jsonA = new JSONArray();
		JSONObject json = new JSONObject();
		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/PinGen");
			con = ds.getConnection();
			st1 = con.createStatement();
			rs1 = st1.executeQuery(sql1);
			while (rs1.next()) {
                //json = new JSONObject();
                //json.put("digit",rs1.getInt("digit"));
                //json.put("count",rs1.getLong("cA"));
                //jsonA.add(json);
                json.put("count"+rs1.getInt("digit"),rs1.getLong("cA"));
			}
			rs1.close();
			result = "succeed";
LOG.log(Level.INFO,"MainDashboard result: {0}",new Object[]{result});
			
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
		//JSONObject res = new JSONObject();
		//res.put("result", result);
		//res.put("countlist", jsonA);
//LOG.log(Level.INFO,"{0}-{1}",new Object[]{"JobList","jsonZ: "+res.toJSONString()});
		json.put("result",result);
LOG.log(Level.INFO,"{0}-{1}",new Object[]{"json",json.toJSONString()});
		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		//res.writeJSONString(out);
		json.writeJSONString(out);
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
