package cz.mzk.k4.processUI.rss.servlets;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import cz.mzk.k4.processUI.domain.KrameriusProcess;
import cz.mzk.k4.processUI.domain.ProcessLog;
import cz.mzk.k4.processUI.utils.ProcessManager;

/**
 * 
 * @author holmanj
 * 
 */
public class ProcessDescServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String host;
	private static ProcessManager pm;
	private InputStream inputStream;
	private static org.apache.log4j.Logger LOGGER = Logger.getLogger(ProcessDescServlet.class);
	static final String CONF_FILE_NAME = "process_rss_config.properties";

	/**
	 * Creates a simple html page with details of a given process
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws IOException
	 * @throws ServletException
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		printPageStart(out);
		// get properties file (/home/{user}/conf.properties)
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

		String username = properties.getProperty("username");
		String password = properties.getProperty("password");

		pm = new ProcessManager(host, username, password);

		@SuppressWarnings("unchecked")
		Enumeration<String> en = request.getParameterNames();

		while (en.hasMoreElements()) {
			String paramName = en.nextElement();
			if (paramName.equals("uuid")) {
				String uuid = request.getParameter(paramName);
				// fetch process
				KrameriusProcess process = pm.getProcessByUuid(uuid);
				out.println(process.toHtml(""));
				// fetch process logs
				ProcessLog log = pm.getLog(uuid);
				out.println(log.toHtml());
			} else {
				LOGGER.fatal("Wrong parameters. Accepts process uuid only.");
				// redirect to the feed
				response.sendRedirect(properties.getProperty("feed_url")
						+ properties.getProperty("feed_name"));
			}
		}

		printPageEnd(out);
	}

	/**
	 * Prints out the start of the html page
	 * 
	 * @param out the PrintWriter object
	 */
	private void printPageStart(PrintWriter out) {

		out.println("<html>");
		out.println("<head>");
		out.println("<title>MZK Kramerius process</title>");
		out.println("</head>");
		out.println("<body>");

	}

	/**
	 * Prints out the end of the html page
	 * 
	 * @param out the PrintWriter object
	 */
	private void printPageEnd(PrintWriter out) {

		out.println("</body>");
		out.println("</html>");
		out.close();
	}
}
