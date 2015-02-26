/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ebbitsproject.peoplemanager.rest;

import eu.ebbitsproject.peoplemanager.Event;
import eu.ebbitsproject.peoplemanager.Person;
import eu.ebbitsproject.peoplemanager.RuleEngine;
import eu.ebbitsproject.peoplemanager.RuleEnginePortType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Proxy extends HttpServlet {

    private final QName SERVICE_NAME = new QName("http://peoplemanager.ebbitsproject.eu/", "RuleEngine");   

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        URL wsdlUrl = new URL("http://localhost:8081/ruleengine?wsdl");
        RuleEngine engine = new RuleEngine(wsdlUrl, SERVICE_NAME);
        RuleEnginePortType port = engine.getRuleEnginePort();
        
        String action = request.getParameter("action");
        
        PrintWriter out = response.getWriter();
        
        switch(action) {
            case "availability":
                String by = request.getParameter("by");
                if (by.equals("erpId")) {
                    String erpId = request.getParameter("id");
                    if (erpId != null) {
                        boolean avail = port.isPersonAvailableByErpId(erpId);
                        response.setContentType("application/json");          
                        out.println(avail);
                    }
                }
                break;
            case "events":
                List<Event> events = port.getEvents();
                response.setContentType("application/json");                             
                out.println(eventsToJson(events));
            case "list":
                String byList = request.getParameter("by");
                if (byList == null) {
                    System.out.println("General List");
                    break;
                }
                if (byList.equals("id")) {
                    String id = request.getParameter("id");
                    if (id != null) {
                        Person p = port.getPersonById(id);
                        response.setContentType("application/json");        
                        out.println(personToJson(p));
                    }
                }
                else if (byList.equals("erpId")) {
                    String id = request.getParameter("id");
                    if (id != null) {
                        Person p = port.getPersonByErpId(id);
                        response.setContentType("application/json");        
                        out.println(personToJson(p));
                    }
                }
                break;
            default:
                response.setContentType("application/json");        
                out.println("error");
                break;
        }
        
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            reader.close();
        }
        
        URL wsdlUrl = new URL("http://localhost:8081/ruleengine?wsdl");
        RuleEngine engine = new RuleEngine(wsdlUrl, SERVICE_NAME);
        RuleEnginePortType port = engine.getRuleEnginePort();
        
        String ev = sb.toString();                
        
        String action = request.getParameter("action");
        if (action != null && "update".equals(action)) {
            port.updateEvent(ev);
        } else {
            port.insertEvent(ev);   
        }                       

    }

    private String personToJson(Person p) {

        // Parsing person serialized object.
        Map properties = new HashMap<String, Object>();
        for (Person.Props.Entry e : p.getProps().getEntry()) {
            properties.put(e.getKey(), e.getValue());
        }
        
        JSONObject person = new JSONObject();
        person.put("id", p.getPersonId());
        person.put("available", properties.get("available"));
        
        return person.toJSONString();
    }        
    
    private String eventsToJson(List<Event> events) {
        // {“type”:”vibration”,”sensorid”:”L1_ST10_R3_S2”,”timestamp”:” Oct 07 2014 16:30:00”}
        JSONArray list = new JSONArray();
        for (Event e : events) {
            JSONObject obj = new JSONObject();
            obj.put("id", e.getId());
            obj.put("type", e.getType());
            // Toto zmenit...!!!
            for (Event.Props.Entry en : e.getProps().getEntry()) {
                if ("sensorid".equals(en.getKey())) {
                    obj.put("sensorid", en.getValue());
                }
                if ("timestamp".equals(en.getKey())) {
                    obj.put("timestamp", en.getValue());
                }                
                if ("status".equals(en.getKey())) {
                    obj.put("status", en.getValue());
                }

                if ("responsibleId".equals(en.getKey())) {
                    obj.put("responsibleId", en.getValue());
                }
                if ("responsibleName".equals(en.getKey())) {
                    obj.put("responsibleName", en.getValue());
                }
                
            }
            list.add(obj);
        }
        return list.toJSONString();
    }

}
