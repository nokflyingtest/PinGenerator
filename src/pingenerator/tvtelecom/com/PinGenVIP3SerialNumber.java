package pingenerator.tvtelecom.com;

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

@WebServlet("/PinGenVIP3SerialNumber")
public class PinGenVIP3SerialNumber extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public PinGenVIP3SerialNumber() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Logger LOG = Logger.getLogger(PinGenVIP3SerialNumber.class.getName());
        request.setCharacterEncoding(Utils.CharacterEncoding);

        String serialNumber = "";
        String serialPrefix = "";
		try {
			Path pathVIPSerialNumber = Paths.get(Utils.PathFileMappingSerialVIPSerialNumber3);
			long maxSerial = 0;
			if (Files.exists(pathVIPSerialNumber)) {
				byte[] byteMaxSerial = Files.readAllBytes(pathVIPSerialNumber);
				String stringMaxSerialTemp = new String(byteMaxSerial);
				String[] stringMaxSerial = stringMaxSerialTemp.split("\\|");
LOG.log(Level.INFO,"PinGenVIP3SerialNumber - stringMaxSerial:{0},{1},{2}",new Object[]{stringMaxSerialTemp,stringMaxSerial[0],stringMaxSerial[1]});
				serialPrefix = stringMaxSerial[0];
				maxSerial = Long.parseLong(stringMaxSerial[1]) + 1;

				if (maxSerial >= 1000000000) {
					maxSerial = 1;
					//Files.write(pathVIPSerialNumber, (serialPrefix+"|"+Long.toString(maxSerial)).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				}
			} else {
				maxSerial = 999999999;
				serialPrefix = "VIP";
				Files.write(pathVIPSerialNumber, (serialPrefix+"|"+Long.toString(maxSerial)).getBytes(), StandardOpenOption.CREATE);
			}
			
			String maxBatchFormat = "9" + String.format("%0$" + "9d", 0).replace(' ', '0');
			
			maxSerial = Long.parseLong(maxBatchFormat) + maxSerial;
			
			//batchNumber = "BAT" + Long.toString(maxBatch).substring(1);
			serialNumber = Long.toString(maxSerial).substring(1);
LOG.log(Level.INFO,"PinGenVIP3SerialNumber - serialNumber:{0}",new Object[]{serialNumber});
		} catch(Exception ex) {
LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
		PrintWriter out = response.getWriter();
		out.print(serialPrefix+"|"+serialNumber);
		out.flush();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
