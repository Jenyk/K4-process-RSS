package cz.mzk.k4.processUI.rss.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;
import cz.mzk.k4.processUI.domain.KrameriusProcess;
import cz.mzk.k4.processUI.utils.ProcessManager;

/**
 * 
 * @author holmanj
 * 
 */
public class RssFeedServlet extends HttpServlet {

	private static final long serialVersionUID = -2635991662902637019L;
	private String host;
	private static ProcessManager pm;
	private static Integer defSize; // default number of processes fetched from Kramerius
	private InputStream inputStream;
	private static org.apache.log4j.Logger LOGGER = Logger.getLogger(RssFeedServlet.class);
	static final String CONF_FILE_NAME = "process_rss_config.properties";

	/**
	 * Creates an RSS feed xml file and redirects to it
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws IOException
	 * @throws ServletException
	 */
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		// get properties file (/home/{user}/properties)
		String home = System.getProperty("user.home");
		File f = new File(home + "/" + CONF_FILE_NAME);

		Properties properties = new Properties();
		try {
			inputStream = new FileInputStream(f);
			properties.load(inputStream);
		} catch (IOException e) {
			LOGGER.fatal("Cannot load properties file");
		}
		host = properties.getProperty("host");
		defSize = Integer.parseInt(properties.getProperty("resultSize"));

		String username = properties.getProperty("username");
		String password = properties.getProperty("password");

		String fileName = properties.getProperty("feed_name");
		ServletContext context = this.getServletContext();
		String pathname = context.getRealPath(fileName);
		createFeed(request, username, password, pathname);			

		// redirect to the feed
		response.sendRedirect(properties.getProperty("feed_url") + properties.getProperty("feed_name"));
	}

	/**
	 * Creates an RSS feed xml file
	 * 
	 * @param request servlet request
	 * @param username kramerius username
	 * @param password  kramerius password
	 * @param fileName path of the xml file created
	 */
	public void createFeed(HttpServletRequest request, String username,
			String password, String fileName) {
		pm = new ProcessManager(host, username, password);

		// get properties file (/home/{user}/properties
		String home = System.getProperty("user.home");
		File f = new File(home + "/" + CONF_FILE_NAME);
		Properties properties = new Properties();
		try {
			inputStream = new FileInputStream(f);
			properties.load(inputStream);
		} catch (IOException e) {
			LOGGER.fatal("Cannot load properties file");
		}

		try {
			// prepare feed
			SyndFeed feed = new SyndFeedImpl();
			feed.setFeedType("rss_2.0");
			feed.setTitle("Kramerius processes");
			feed.setLink("http://" + properties.getProperty("host") + "/");
			feed.setDescription("MZK Kramerius process feed");

			List<SyndEntry> entries = new ArrayList<SyndEntry>();
			SyndEntry entry;
			SyndContent description;

			// handle URL parameters
			@SuppressWarnings("unchecked")
			Enumeration<String> en = request.getParameterNames();
			Map<String, String> params = new HashMap<String, String>();
			while (en.hasMoreElements()) {
				String paramName = en.nextElement();
				String paramBody = request.getParameter(paramName);
				params.put(paramName, paramBody);
			}
			if (!params.containsKey("ordering")) {
				params.put("ordering", "DESC");
			}			
			if (!params.containsKey("resultSize")) {
				params.put("resultSize", defSize.toString());
			}
			int resultSize = Integer.parseInt(params.get("resultSize").toString());
			
			// fetch processes
			List<KrameriusProcess> processes = pm.searchByParams(params);

			// specified resultSize could be bigger than the actual number of
			// processes fetched from Kramerius
			if (resultSize > processes.size()) {
				resultSize = processes.size();
			}
			// add the first resultSize processes to the feed
			for (int i = 0; i < processes.size(); i++) {
				KrameriusProcess p = processes.get(i);
				entry = new SyndEntryImpl();
				entry.setTitle(p.getDef() + " started on " + p.getStarted());
				entry.setLink(properties.getProperty("process_url")
						+ p.getUuid());

				description = new SyndContentImpl();
				description.setType("application/xml");
				String desc = "Kramerius process - " + p.getState();
				description.setValue(desc);
				entry.setDescription(description);
				entries.add(entry);
			}
			feed.setEntries(entries);

			// create the file
			Writer writer = new FileWriter(fileName);
			SyndFeedOutput output = new SyndFeedOutput();
			output.output(feed, writer);
			writer.flush();
			writer.close();
			LOGGER.info("Feed created");

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// pm.cleanUp();
		}
	}
}