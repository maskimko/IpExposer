package ua.pp.msk;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.keycloak.KeycloakSecurityContext;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author maskimko
 */
@WebServlet
public class IpExposer extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(IpExposer.class.getCanonicalName());
    private static final String XFORWARDEDFOR = "x-forwarded-for";

static    {
        LOGGER.setLevel(Level.INFO);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private String getFullInfo(HttpServletRequest req, HttpServletResponse resp) {
        String remoteUser = req.getRemoteUser();
        String[] rolesToCheck = null;
        Map<String, String> headerLines = new HashMap<>();
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hName = headerNames.nextElement();
            headerLines.put(hName, req.getHeader(hName));
        }
        Map<String, String[]> parameterMap = req.getParameterMap();
        resp.setContentType("text/html");
        StringBuilder sb = new StringBuilder("<html><head><title>List of header items and parameters</title></head><body>");
        sb.append("<h1>Header</h1>");
        for (String hName : headerLines.keySet()) {
            sb.append("<li>").append(hName).append("=").append(headerLines.get(hName));
        }
        sb.append("<h1>Parameters</h1>");
        for (String pName : parameterMap.keySet()) {
             if (pName.equals("role")){
                rolesToCheck = parameterMap.get(pName);
            }
            sb.append("<li>").append(pName).append("=").append(Arrays.asList(parameterMap.get(pName)));
        }
        sb.append("<h1>Network details</h1>");
        sb.append("<h2>Local</h2>");
        sb.append("<br/>Local address: ").append(req.getLocalAddr()).append("<br/>Local hostname: ").append(req.getLocalName()).append("<br/>Local port: ").append(req.getLocalPort());
        sb.append("<h2>Remote</h2>");
        sb.append("<br/>Remote address: ").append(req.getRemoteAddr()).append("<br/>Remote hostname: ").append(req.getRemoteHost()).append("<br/>Remote port: ").append(req.getRemotePort())
                .append("<br/>Remote user: ").append(remoteUser);
        sb.append("<h1>Attributes</h1>");
        Enumeration<String> attrs = req.getAttributeNames();
        while (attrs.hasMoreElements()) {
            String a = attrs.nextElement();
            Object aObj = req.getAttribute(a);
            
           if (a.equals(KeycloakSecurityContext.class.getName())){ if (aObj instanceof KeycloakSecurityContext)  {
                KeycloakSecurityContext c = (KeycloakSecurityContext) aObj;
                KeykloakSecurityConstraintParser parser = new KeykloakSecurityConstraintParser(c);
                sb.append("<li>").append(a).append(" = ").append(parser.toHtmlString());
           }
      }      else {
                sb.append("<li>").append(a).append("=").append(req.getAttribute(a));
            }
            
        }
        Cookie[] cookies = req.getCookies();
        if (cookies != null){
            sb.append("<h1>Cookies</h1>");
            if (cookies.length == 0) {
                 sb.append("<li>No cookies");
            } else {
                for (Cookie c : cookies){
                    if (c != null) {
                    sb.append("<li>&nbsp;").append(String.format("Name:&nbsp;%s&nbsp;Value:&nbsp%s;&nbsp;Domain:&nbsp;%s&nbsp;Path:&nbsp;%s&nbsp;Max age:&nbsp;%d&nbsp;Version:&nbsp;%d&nbsp;Secure:&nbsp;%s&nbsp;Comment:&nbsp;%s ", 
                            c.getName(), c.getValue(), c.getDomain(), c.getPath(), c.getMaxAge(), c.getVersion(), c.getSecure(), c.getComment()));
                    }
                }
            }
        }
        if (rolesToCheck != null && rolesToCheck.length > 0){
             sb.append("<h1>Roles</h1>List from request uri<br/>");
             for (String role : rolesToCheck){
                  sb.append("<li>User: ").append(req.isUserInRole(role) ? " is " : " is not ").append(" in role ").append(role);
             }
        } 
        sb.append("<h1>Session details</h1>");
        sb.append("<li>Requested Session id: ").append(req.getRequestedSessionId());
        sb.append("<li>Request URI: ").append(req.getRequestURI());
        sb.append("<li>Request URL: ").append(req.getRequestURL());
        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * Method returns IP Address of the source host It takes two url parameters:
     * full - if true it returns detailed description of the received request
     * proxy - if true it respects the reverse proxy web server source address,
     * by default if "x-forwarded-for" header exists, than it's value is treated
     * as a source host address, not proxy host address.
     *
     * @param req Standard servlet request
     * @param resp Standard servlet response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String full = req.getParameter("full");
        String proxy = req.getParameter("proxy");
        PrintWriter writer = resp.getWriter();

        if (full != null && full.equals("true")) {
            LOGGER.info("Requesting detailed information");
            writer.println(getFullInfo(req, resp));
        } else {
            resp.setContentType("text");
            String remoteAddr = null;
            if (proxy != null && proxy.equals("true")) {
                LOGGER.info("will use proxy host address as a source host");
                remoteAddr = req.getRemoteAddr();
            } else {
                LOGGER.info("Will ignore proxy host as the source host");
                remoteAddr = getXForwardedFor(req);
                if (remoteAddr == null) {
                    LOGGER.info("Got the source host address from the request");
                    remoteAddr = req.getRemoteAddr();
                }
            }
            writer.println(remoteAddr);
        }
        writer.close();
    }

    private String getXForwardedFor(HttpServletRequest req) {
        String value = null;
        Enumeration<String> headerNames = req.getHeaderNames();
        LOGGER.info("List of header entries:");
        while (headerNames.hasMoreElements()) {

            String hName = headerNames.nextElement();
            LOGGER.info("\t\"" + hName + "\"");
            if (hName.toLowerCase().trim().equals(XFORWARDEDFOR)) {
                value = req.getHeader(hName);
                LOGGER.info("Actual source host ip address is " + value);
                LOGGER.info("Got source address from the header");
                break;
            }
        }
        if (value == null) {
            LOGGER.info("It seems that request was not proxied");
        }
        return value;
    }
    
 

}
