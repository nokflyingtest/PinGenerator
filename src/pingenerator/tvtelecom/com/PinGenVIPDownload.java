package pingenerator.tvtelecom.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/PinGenVIPDownload")
public class PinGenVIPDownload extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinGenVIPDownload() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinGenVIPDownload.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
		//HttpSession session = request.getSession(true);
		//String userId = (String)session.getAttribute("userId");
        String fileName = request.getParameter("fileName");
        
LOG.log(Level.INFO,"PinGenVIPDownload fileName:{0}",new Object[]{fileName});
		//String uploadFolder = getServletContext().getInitParameter("uploadFolder");
		String uploadFolder = Utils.PathUpload;
		fileName = uploadFolder + 'R' + fileName;
		String mappedVIP = "";
		String result="succeed";
		try {
			Path pathFileName = Paths.get(fileName);
			if (Files.exists(pathFileName)) {
				byte[] byteMappedVIP = Files.readAllBytes(pathFileName);
				mappedVIP = new String(byteMappedVIP);
			}
		} catch (Exception e) {
			result = "failed";
			e.printStackTrace();
		}
LOG.log(Level.INFO,"PinGenVIPDownload result:{0} mappedVIP: {1}",new Object[]{result,mappedVIP});
		SimpleDateFormat dFileFormat = new SimpleDateFormat("yyMMdd_hhmmss");
		String fileNameCSV = "PinVIP_"+dFileFormat.format(new Date());
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=\""+fileNameCSV+".csv\"");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print(mappedVIP);
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
