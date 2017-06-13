package org.openqa.grid.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.openqa.grid.common.exception.GridException;
import org.openqa.grid.internal.ProxySet;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.web.servlet.RegistryBasedServlet;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GridInformationJson extends RegistryBasedServlet {

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
        response.setContentType("text/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(200);
        JsonObject res;
        try {
            res = getFreeBrowsers();
            response.getWriter().print(res);
            response.getWriter().close();
        } catch (JsonIOException e) {
            throw new GridException(e.getMessage());
        }
    }
    
    private JsonObject getFreeBrowsers() throws IOException, JsonIOException {
        JsonObject requestJSON = new JsonObject();
        ProxySet proxies = this.getRegistry().getAllProxies();

        int total = 0;
        int used = 0;
        int chrome_total = 0;
        int chrome_free = 0;
        int ie_total = 0;
        int ie_free = 0;
        int other_total = 0;
        int other_free = 0;
        JsonObject platformsbrowsers = new JsonObject();
        for (RemoteProxy proxy : proxies) {
        	total += proxy.getTestSlots().size();
            used += proxy.getTotalUsed();
            for (TestSlot slot : proxy.getTestSlots()) {
            	DesiredCapabilities cap = new DesiredCapabilities(slot.getCapabilities());
//                Platform ptf = getPlatform(slot);                  
//                if (ptf != null){
//                  JsonArray browserlist = new JsonArray();
//                  if (!platformsbrowsers.has(ptf.toString())){
//                      platformsbrowsers.add(ptf.toString(), browserlist);
//                  }
//                  JsonArray platform = platformsbrowsers.getAsJsonArray(ptf.toString());
//                  
//                  JsonPrimitive browser = new JsonPrimitive(cap.getBrowserName());
//                  if (!platform.contains(browser)){
//                      platform.add(browser);
//                  }
//              }
                	if(cap.getBrowserName().equals("chrome")){
                		chrome_total++;
                		if(slot.getSession() == null){
                			chrome_free++;
                		}
                	}
                	else if(cap.getBrowserName().equals("internet explorer")){
                		ie_total++;
                		if(slot.getSession() == null){
                			ie_free++;
                		}
                	}
                	else {
                		other_total++;
                		if(slot.getSession() == null){
                			other_free++;
                		}
                	}
            }
        }
//        System.out.println("Total Chrome : " + chrome_total + "Free Chrome : " + chrome_free);
//    	System.out.println("Total IE : " + ie_total + "Free IE : " + ie_free);
//        requestJSON.add("PlatformsBrowsers", platformsbrowsers);
        requestJSON.addProperty("Total", total);
        requestJSON.addProperty("Free", total - used);
        requestJSON.addProperty("Chrome Total", chrome_total);
        requestJSON.addProperty("Chrome Free", chrome_free);
        requestJSON.addProperty("IE Total", ie_total);
        requestJSON.addProperty("IE Free", ie_free);
        requestJSON.addProperty("Others Total", other_total);
        requestJSON.addProperty("Others Free", other_free);
        return requestJSON;
    }

    private static Platform getPlatform(TestSlot slot) {
        Object o = slot.getCapabilities().get(CapabilityType.PLATFORM);
        if (o == null) {
            return Platform.ANY;
        } else {
            if (o instanceof String) {
                return Platform.valueOf((String) o);
            } else if (o instanceof Platform) {
                return (Platform) o;
            } else {
                throw new GridException("Cannot cast " + o + " to org.openqa.selenium.Platform");
            }
        }
    }
}
