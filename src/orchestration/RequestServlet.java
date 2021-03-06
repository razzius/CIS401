package orchestration;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.DataOutputStream;

import java.net.InetSocketAddress;
import java.net.URL;
import java.net.HttpURLConnection;
import com.google.gson.Gson;
import org.apache.log4j.Logger;


import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Central system component which accepts and handles customer requests.
 *
 * @author      Dong Young Kim, Alex Brashear, Alex Lyons, Razzi Abuissa
 * @version     1.0
 * @since       2015-04-21
 */

public class RequestServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(RequestServlet.class.getName());

	/** Static container class for network resources */
	public static class Container {
    	public List<Map> hosts;
      	public List<Map> services;
      	public Map resourceManager;
      	public List<Map> customers;
   	}

	/** An instance of the StateManager class, which monitors the state of network resources. */
    private static StateManager stateManager;

    /** An instance of the AlgorithmSolver class, which solves for virtual configuations and can be swapped out. */
    private static AlgorithmSolver algorithmSolver;

    private static Analytics analytics;

    /** An instance of the in-memory HardwareCluster, which is used to call methods on the network. */
    private static HardwareCluster hardwareCluster;

     /** Initialize the request servlet. */
	public void init() throws ServletException {
		ServletContext context = getServletContext();
		try {
			InputStream configJson = context.getResourceAsStream("/WEB-INF/config.json");
			// new FileInputStream("config.json")
			BufferedReader br = new BufferedReader(new InputStreamReader(configJson));
	        Gson g = new Gson();
	        Container c = g.fromJson(br, Container.class);
	        String endpoint = (String) c.resourceManager.get("analyticsEndPoint");
			logger.info("Analyitcs endpoint: " + endpoint);

	        // System.out.println(c.hosts.get(0).get("memory"));
	        // System.out.println(c.services.get(0).get("command"));

	        // set up hosts from config.json
	        ArrayList<HostConfig> hostConfigs = new ArrayList<HostConfig>();
	        for (Map host : c.hosts) {
	            int bandwidth = Integer.parseInt((String) host.get("bandwidth"));
	            int memory = Integer.parseInt((String) host.get("memory"));
	            int numCores = Integer.parseInt((String) host.get("numCores"));
	            String ipAddress = (String) host.get("ipAddress");
	            HostConfig hc = new HostConfig(bandwidth, memory, numCores, ipAddress);
	            hostConfigs.add(hc);
	        }

	        for (Map customer : c.customers) {
	        	String ipAddress = (String) customer.get("ipAddress");
	        	Map request = (Map) customer.get("request");
        		String numPackets= (String) request.get("numPackets");
        		String deadline = (String) request.get("startNode");
        		String endNode = (String) request.get("endNode");
        		List<String> services = (List<String>) request.get("services");

        		// POST to each customer the requests they will be making for this trial
        		URL url = new URL("http://"+ipAddress+":8080/customer");
        		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        		connection.setRequestMethod("POST");
        		String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
        		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();

				int responseCode = connection.getResponseCode();
	        }

	        // Create a HostConfig for the local machine with 1Mbps of bandwidth, 4 cores, 2048 MB of RAM
	        // TODO clean this up
	        HostConfig localhostConfig = new HostConfig(1048576, 2048, 4, "127.0.0.1");
	        hostConfigs.add(localhostConfig);

	        analytics = new Analytics(endpoint);
	        hardwareCluster = new HardwareCluster();
	        logger.info("Starting StateManager");
	        stateManager = new StateManager(hardwareCluster, hostConfigs);

	        // set up services from config.json
	        for (Map service : c.services) {
	            String name = (String) service.get("name");
	            int wcet = Integer.parseInt((String) service.get("wcet"));
	            String command = (String) service.get("command");

	            stateManager.addService(new Service(name, wcet, command));
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }

	}

	/** @return the value of the indicated parameter. */
	private String getParam(HttpServletRequest request, String key) {
		String param = request.getParameter(key);
		if (param == null) throw new IllegalArgumentException("\"" + key + "\" is not a valid parameter");
		else return param;
	}

	/** @return a request object generated from the HTTP Servlet Request. */
	private Request processInput(HttpServletRequest request, PrintWriter out) {

		try {
			String requestID = getParam(request, "requestID");
			String startNode = getParam(request, "startNode");
			String endNode   = getParam(request, "endNode");
			String services  = getParam(request, "services");

			int packageRate = Integer.parseInt(getParam(request, "packageRate"));
			int deadline    = Integer.parseInt(getParam(request, "deadline"));
			int packageSize = Integer.parseInt(getParam(request, "packageSize"));
			int price       = Integer.parseInt(getParam(request, "price"));

			// Request req = new Request(
			// 	requestID,
			// 	startNode,
			// 	endNode,
			// 	packageRate,
			// 	deadline,
			// 	services,
			// 	packageSize,
			// 	price);

			// TODO: fix this
			Request req = null;

			out.write("<html><head><title>DAAR 2015</head></title><body>");
			out.write("Received parameters:");
			out.write("\n  requestID = " + requestID);
			out.write("\n, startNode = " + startNode);
			out.write("\n, endNode = " + endNode);
			out.write("\n, packageRate = " + packageRate);
			out.write("\n, deadline = " + deadline);
			out.write("\n, services = " + services);
			out.write("\n, packageSize = " + packageSize);
			out.write("\n, price = " + price);

			return req;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			PrintWriter out = response.getWriter();

			Request req = processInput(request, out);
			if (req == null) {
				response.sendError(400, "invalid parameters");
			}


			CustomerResponse cr = stateManager.queryAlgorithmSolver(req);
			String res = "";
            if (cr.accepted) {
                res += "Request Accepted";
            } else {
                res += "Request Denied";
            }

            logger.info("Server responding with: " + res);
            out.write("<p>" + res + "</p>");
            out.write("</body></html>");
	    } catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		try {

			HashMap<String, VM> vms = new HashMap<String, VM>();

			for (RemoteHost host : stateManager.getState().getRemoteHosts().values()) {
				for (String vmID : host.getVMs().keySet()) {
					vms.put(vmID, host.getVMs().get(vmID));
				}
			}

			PrintWriter out = response.getWriter();
			out.write("<html><body>"

				+ "<form method=\"GET\">"
				+ "<input type=\"submit\" value=\"Get Virtual Machine Statuses\">"
				+ "</form>"
				+ "<br>");

				out.write("Here are the VMs:");
				for (String vmID : vms.keySet()) {
					out.write("Found VM " + vmID + "<br>");

				}


				out.write("</body></html>");
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
}
