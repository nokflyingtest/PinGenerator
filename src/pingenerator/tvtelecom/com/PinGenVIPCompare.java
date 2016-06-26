package pingenerator.tvtelecom.com;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/PinGenVIPCompare")
public class PinGenVIPCompare extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinGenVIPCompare() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinGenVIPCompare.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);
        
        String pin = request.getParameter("pin");
        String fileName = request.getParameter("fileName");
        
		HttpSession session = request.getSession(false);
		String userId = ((Integer)session.getAttribute("userId")).toString();
		
LOG.log(Level.INFO,"PinGenVIPCompare - userId:{0} pin:{1} fileName:{2}",new Object[]{userId,pin,fileName});

		//String uploadFolder = getServletContext().getInitParameter("uploadFolder");
		String uploadFolder = Utils.PathUpload;
		
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

		String result="";
		String fileNameResult = 'R' + fileName;
		
		//int pos = buffer.indexOf("\r\n"+pin+"\r\n");
		int pos = buffer.indexOf(","+pin+"\r\n");
		if (pos > -1) {
			result = "duplicated";
		} else {
			result = "succeed";
			try {
				Path pathDummy = Paths.get(Utils.PathFileVIPSerial);
				long maxVIPDummySerial = 0;
				if (Files.exists(pathDummy)) {
					byte[] byteMaxVIPDummySerial = Files.readAllBytes(pathDummy);
					String stringMaxVIPDummySerial = new String(byteMaxVIPDummySerial);
					maxVIPDummySerial = Long.parseLong(stringMaxVIPDummySerial);
					Files.write(pathDummy, Long.toString(maxVIPDummySerial+1).getBytes(), StandardOpenOption.CREATE);
				} else {
					Files.write(pathDummy, "1".getBytes(), StandardOpenOption.CREATE);
				}
LOG.log(Level.INFO,"PinGenVIPCompare - test:{0}",new Object[]{Files.exists(pathDummy)});
LOG.log(Level.INFO,"PinGenVIPCompare - test:{0}",new Object[]{maxVIPDummySerial});
				
				String serial = "VIP" + String.format("%0$7d", maxVIPDummySerial+1).replace(' ', '0');
				Path pathFileNameResult = Paths.get(uploadFolder + fileNameResult);
				if (Files.exists(pathFileNameResult)) {
					Files.write(pathFileNameResult, (serial+","+pin+"\r\n").getBytes(), StandardOpenOption.APPEND);
				} else {
					Files.write(pathFileNameResult, (serial+","+pin+"\r\n").getBytes(), StandardOpenOption.CREATE);
				}
				
				
				/**
				File file = new File(uploadFolder + fileNameResult);
				if (!file.exists()) {file.createNewFile();}

				FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(serial+","+pin+"\r\n");
				bw.close();
				**/
				
			} catch (Exception e) {
				result = "failed";
				e.printStackTrace();
			}
		}

		response.setContentType("application/json");
		response.setCharacterEncoding(Utils.CharacterEncoding);
		PrintWriter out = response.getWriter();
		out.print("{\"result\":\""+result+"\",\"pin\":"+pin+",\"fileName\":\""+fileName+"\",\"fileNameResult\":\""+fileNameResult+"\"}");
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
