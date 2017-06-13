package org.openqa.grid.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonIOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.openqa.grid.common.exception.GridException;
import org.openqa.grid.internal.ProxySet;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.web.servlet.RegistryBasedServlet;
import org.openqa.selenium.remote.DesiredCapabilities;

public class GridInformation extends RegistryBasedServlet {

    private static final long serialVersionUID = -5559403361498232207L;

    public GridInformation() {
        super(null);
    }

    public GridInformation(Registry registry) {
        super(registry);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    protected void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(200);
        String res;
        try {
            res = getFreeBrowsers();
            response.getWriter().print(res);
            response.getWriter().close();
        } catch (JsonIOException e) {
            throw new GridException(e.getMessage());
        }
    }
    

    
    private String getFreeBrowsers() throws IOException, JsonIOException {
        // Declaring the required objects
        StringBuilder builder = new StringBuilder();
        ProxySet proxies = this.getRegistry().getAllProxies();
        HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();

        // Initializing the Counters
        int total = 0;
        int used = 0;

        // Parsing through each proxy (node machine)
        for (RemoteProxy proxy : proxies) {
        	total += proxy.getTestSlots().size();
            used += proxy.getTotalUsed();
            // Parsing through TestSlots withing a single Proxy machine
            for (TestSlot slot : proxy.getTestSlots()) {
            	DesiredCapabilities cap = new DesiredCapabilities(slot.getCapabilities());
            	int total_count, free_count;
            	String b_name = new String();
            	ArrayList<Integer> browser_count = new ArrayList<Integer>();
            	// Add empty Array List
            	browser_count.add(0);
            	browser_count.add(0);
				// Get browser name
				b_name = cap.getBrowserName();
				if(!map.containsKey(b_name)){
					map.put(b_name, browser_count);
				}
				total_count = map.get(b_name).get(0);
				free_count = map.get(b_name).get(1);
				total_count = total_count + 1;
				if(slot.getSession() == null){
					free_count++;
        		}
				browser_count.set(0, total_count);
				browser_count.set(1, free_count);
				map.replace(b_name, browser_count);
            }
        }
        // Add the overall slot total and free count to the HashMap
        ArrayList<Integer> overall_count = new ArrayList<Integer>();
        overall_count.add(total);
        overall_count.add(total - used);
		map.put("Total", overall_count);

        // Building the HTML content
        builder.append("<html>");
        builder.append("<head>");
        builder.append("<script type='text/javascript'>setTimeout(function(){ location.reload()},30000)</script>");
        builder.append("<script src='/grid/resources/org/openqa/grid/images/jquery-3.1.1.min.js'></script>");
        builder.append("<script src='/grid/resources/org/openqa/grid/images/consoleservlet.js'></script>");
        builder.append("<link href='/grid/resources/org/openqa/grid/images/consoleservlet.css' rel='stylesheet' type='text/css' />");
        builder.append("<link href='/grid/resources/org/openqa/grid/images/favicon.ico' rel='icon' type='image/x-icon' />");
        builder.append("<title>Grid Node Information</title>");
        builder.append("<style>");
        builder.append(".busy {");
        builder.append(" opacity : 0.4;");
        builder.append("filter: alpha(opacity=40);");
        builder.append("}");
        builder.append("</style>");
        builder.append("</head>");

        builder.append("<body>");
        builder.append("<div id='main-content'>");
        builder.append(getHeader());
        builder.append("<style type='text/css'>");
        builder.append(".tg  {border-collapse:collapse;border-spacing:0;}");
        builder.append("td{font-family:Arial, sans-serif;font-size:14px;padding:20px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;}");
        builder.append("th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:20px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;}");
        builder.append(".tg-pyhz{font-weight:bold;font-size:38px;background-color:#cbcefb;text-align:center;vertical-align:top;padding-left:30px;padding-right:30px}");
        builder.append(".tg-4und{font-size:28px;text-align:center;vertical-align:top;padding-left:30px;padding-right:30px}");
        builder.append(".tg-lvth{font-size:28px;text-align:center;vertical-align:top}");
        builder.append(".tg-qir8{font-size:28px;background-color:#9aff99;text-align:center;vertical-align:top}");
        builder.append(".tg-qir9{font-size:28px;background-color:red;text-align:center;vertical-align:top}</style>");
        builder.append("<br/><br/><table class='tg'><tr><th class='tg-pyhz'>Browser</th><th class='tg-pyhz'>Total Count</th><th class='tg-pyhz'>Free Count</th></tr>");
        
        // Hash to maintain browser name and Image mapping
        // Refer - https://insight.io/github.com/SeleniumHQ/selenium/tree/master/java/server/src/org/openqa/grid/images/
        Map<String, String> browser_image = new HashMap<String, String>();
        browser_image.put("chrome" , "chrome");
        browser_image.put("internet explorer" , "internet_explorer");
        browser_image.put("Total" , "unofficial");
        
        // Parsing HashMap and adding the content to Builder HTML table based on our need / styling
        for (String key : map.keySet()) {
        	if(browser_image.containsKey(key)){
        		builder.append("<tr><td class='tg-4und'><img src='/grid/resources/org/openqa/grid/images/").append(browser_image.get(key)).append(".png' width='22' height = '22'>&nbsp;&nbsp;").append(StringUtils.capitalize(key)).append("</td>");
        		}
        	else{
        		builder.append("<tr><td class='tg-4und'>").append(StringUtils.capitalize(key)).append("</td>");
        	}
        	
        	builder.append("<td class='tg-lvth'>").append(map.get(key).get(0)).append("</td>");
        	if(map.get(key).get(1) == 0){
        		builder.append("<td class='tg-qir9'>").append(map.get(key).get(1)).append("</td></tr>");
        	}
        	else {
        		builder.append("<td class='tg-qir8'>").append(map.get(key).get(1)).append("</td></tr>");
        	}
        }
        
        builder.append("</div>");
        builder.append("</body>");
        builder.append("</html>");
        return builder.toString();
    }
    
    private Object getHeader() {
        StringBuilder builder = new StringBuilder();
        builder.append("<div id='header'>");
        builder.append("<h1><a href='/grid/console'>Selenium</a></h1>");
        builder.append("<h2>Grid Node Information");
        builder.append("</h2>");
        builder.append("<div><a id='helplink' target='_blank' href='https://github.com/SeleniumHQ/selenium/wiki/Grid2'>Help</a></div>");
        builder.append("</div>");
        builder.append("");
        return builder.toString();
    }
}