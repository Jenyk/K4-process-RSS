package cz.mzk.k4.processUI.utils;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import cz.mzk.k4.processUI.domain.ProcessLog;
import cz.mzk.k4.processUI.domain.KrameriusProcess;
import javax.xml.bind.DatatypeConverter;
// import org.apache.log4j.Logger;

/**
 * 
 * @author holmanj
 * 
 */
public class JsonParser {

	Gson gson;
//	private static org.apache.log4j.Logger LOGGER = Logger.getLogger(JsonParser.class);

	public JsonParser() {
		gson = new Gson();
	}

	public KrameriusProcess parseProcess(String strJson)
			throws JsonSyntaxException {
		return gson.fromJson(strJson, KrameriusProcess.class);
	}

	public List<KrameriusProcess> parseProcessList(String strJson)
			throws JsonSyntaxException {

		List<KrameriusProcess> processes = gson.fromJson(strJson,
				new TypeToken<ArrayList<KrameriusProcess>>() {
				}.getType());

		return processes;
	}

	public ProcessLog parseLog(String strLog) throws JsonSyntaxException {

		ProcessLog log = gson.fromJson(strLog, ProcessLog.class);

		// sout and serr in Json are Base64 encoded
		byte[] decodedSout = DatatypeConverter.parseBase64Binary(log.getSout());
		byte[] decodedSerr = DatatypeConverter.parseBase64Binary(log.getSerr());

		log.setSout(new String(decodedSout));
		log.setSerr(new String(decodedSerr));
		
		return log;
	}
/*
	public void cleanUp() {
		try {
			Integer threadLocalCount;
			threadLocalCount = GSONThreadLocalImmolater.immolate();
			LOGGER.info("gsonImmolator.immolate() completed: immolated "
					+ threadLocalCount + " GSON values in ThreadLocals");
		} catch (Exception e) {
			LOGGER.warn(("caught exception raised by gsonImmolator.immolate() " + e
					.getStackTrace().toString()));
		}
	}
*/
}