/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.ebbitsproject.peoplemanager.rest;

import eu.ebbitsproject.peoplemanager.dao.PersonDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ERP extends HttpServlet {    

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
        
        response.setContentType("application/json");
        
        PersonDAO dao = PersonDAO.getInstance();
        
        PrintWriter out = response.getWriter();        
        String action = request.getParameter("action");
        
        switch (action){
            case "list":
                String byList = request.getParameter("by");
                if (byList == null) {
                    System.out.println("General List");
                    break;
                }
                if (byList.equals("id")) {
                    String id = request.getParameter("id");
                    if (id != null) {
                        PersonDAO.ERPPerson p = dao.getERPPersonById(id);
                        out.println(personToJson(p));
                    }
                } else if (byList.equals("sensorid")) {
                    /// TODO: Rodzsirit tak, aby location bol na zaklade search 
                    /// COize:
                    /// Location moze byt hocico z v ontologii
                    String sensorId = request.getParameter("sensorid");
                    if (sensorId != null) {                        
                        List<PersonDAO.ERPPerson> ps = dao.getERPPersonsBySensorId(sensorId);
                        out.println(personsToJson(ps));
                    }
                }
            case "alternatives":
                String byAlts = request.getParameter("by");
                if (byAlts.equals("roleid")) {
                    String roleId = request.getParameter("roleid");
                    if (roleId != null) {
                        String competence = "http://www.ebbits-project.eu/ontologies/M48_HR.owl#AbleToRepairRobots";
                        List<PersonDAO.ERPPerson> ps = dao.getERPPersonsByCompetence(competence, roleId);
                        out.println(personsToJson(ps));
                    }
                }
        }                
    }    
    
    private String personToJson(PersonDAO.ERPPerson person) {
        JSONObject p = new JSONObject();
        p.put("id", person.getId());
        p.put("name", person.getName());
        p.put("roles", person.getProperties().get("roles").toString());
        p.put("breakduration", person.getProperties().get("erp:breakduration"));
        return p.toJSONString();
    }
    
    private String personsToJson(List<PersonDAO.ERPPerson> persons) {
        JSONArray list = new JSONArray();
        
        for (PersonDAO.ERPPerson p : persons) {
            JSONObject person = new JSONObject();
            person.put("id", p.getId());
            person.put("name", p.getName());
            person.put("roles", p.getProperties().get("roles"));
            person.put("rfid", p.getProperties().get("rfid"));
            person.put("responsibility", p.getProperties().get("responsibility"));            
            list.add(person);
        }
        
        return list.toJSONString();
    }
    
    private class Person {
        private String id;
        private String name;
        private Map properties;

        public Person() {
        }

        public Person(String id, String name, Map properties) {
            this.id = id;
            this.name = name;
            this.properties = properties;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Map getProperties() {
            return properties;
        }                
    }
    
}
