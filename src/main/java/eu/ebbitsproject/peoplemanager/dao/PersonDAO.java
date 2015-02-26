/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.ebbitsproject.peoplemanager.dao;

import eu.ebbitsproject.peoplemanager.Person;
import eu.ebbitsproject.peoplemanager.RuleEngine;
import eu.ebbitsproject.peoplemanager.RuleEnginePortType;
import eu.ebbitsproject.peoplemanager.rest.OMProxy;
import eu.ebbitsproject.peoplemanager.utils.PropertiesUtils;
import eu.ebbitsproject.peoplemanager.utils.VPOSClient;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

public class PersonDAO {
    private static volatile PersonDAO instance = null;
    
    private List<ERPPerson> persons;
    
    public static synchronized PersonDAO getInstance() {
        if (instance == null) {
            instance = new PersonDAO();
        }
        return instance;
    }

    private PersonDAO() {
        this.persons = new ArrayList<>();
        this.initData();
    }                
    
    public ERPPerson getERPPersonById(String id) {
        for (ERPPerson p : this.persons) {
            if (p.id.equals(id)) {
                return p;
            }
        }
        return null;
    }
    
    public List<ERPPerson> getERPPersonsByLocations(Set<String> locations) throws MalformedURLException {
        List<ERPPerson> ps = new ArrayList<>();
        for (ERPPerson p : this.persons) {
            String personResp = p.getProperties().get("responsibility").toString();
            System.out.println("RRRR: " + personResp);
            if (locations.contains(personResp)) {
                QName SERVICE_NAME = new QName("http://peoplemanager.ebbitsproject.eu/", "RuleEngine"); 
                URL wsdlUrl = new URL("http://localhost:8081/ruleengine?wsdl");
                RuleEngine engine = new RuleEngine(wsdlUrl, SERVICE_NAME);
                RuleEnginePortType port = engine.getRuleEnginePort();
                boolean isAvailable = port.isPersonAvailableByErpId(p.id);
                if (isAvailable) {
                    ps.add(p);
                }                
            }
        }
        return ps;
    }
    
    public List<ERPPerson> getERPPersonsBySensorId(String sensorId) {
        // TODO: Fetch vsetkych lokacii, v ktorych sa nachadza senzor so "sensorId"
        // TODO: Porovnanie fetchnutych lokacii s responsiblity property. 
        //       Ak sa v zozname nachadza lokacia zhodna s responsiblity, vratim cloveka
        //       ako responsible pre dany senzor.
        Set<String> locations = OMProxy.getLocations(sensorId);
        System.out.println("LLLLLL>>>> " + locations);
        try {
            return this.getERPPersonsByLocations(locations);
        } catch (MalformedURLException ex) {
            Logger.getLogger(PersonDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }
    
    public List<ERPPerson> getERPPersonsByCompetence(String competence, String whoIsAsking) {
        List<ERPPerson> ps = new ArrayList<>();
        
        VPOSClient vpos = new VPOSClient();
        String vposDir = PropertiesUtils.getProperty("vpos.dir");
        
        for (ERPPerson p : this.persons) {
            if (p.getProperties().get("reference") != null) {
                System.out.println("He has reference: " + p.getProperties().get("reference").toString());
                if (vpos.employeeHasCompetence(vposDir, whoIsAsking,
                        p.getProperties().get("reference").toString(), competence)) {
                    ps.add(p);
                }
            }
        }
        
        return ps;
    }
    
    private void initData() {
        // 1. person
        Map<String, Object> m1 = new HashMap<>();
        m1.put("erp:breakduration", 10);
        m1.put("roles", new LinkedList(Arrays.asList("role:butcher")));
        m1.put("rfid", "1001-0110-0210-0310-0410-0000");
        m1.put("responsibility", "building 1");
        ERPPerson p1 = new ERPPerson("1", "John Butcher", m1);
        this.persons.add(p1);
        // 2. person
        Map<String, Object> m2 = new HashMap<>();
        m2.put("erp:breakduration", 10);
        m2.put("roles", new LinkedList(Arrays.asList("role:butcher", "role:manager")));
        m2.put("rfid", "1001-0110-0210-0310-0410-0002");
        m2.put("responsibility", "building 1");
        ERPPerson p2 = new ERPPerson("2", "Daisy Butcher", m2);
        this.persons.add(p2);
        
        // Manufacturing scenario data
        Map<String, Object> m3 = new HashMap<>();
        m3.put("erp:breakduration", 10);
        m3.put("roles", new LinkedList(Arrays.asList("1", "2")));
        m3.put("rfid", "1001-0110-0210-0310-0410-059D"); /// seems to be real
        m3.put("responsibility", "demo-b1:production_line_A_1");
        ERPPerson p3 = new ERPPerson("3", "John Doe", m3);
        this.persons.add(p3);
        
        Map<String, Object> m4 = new HashMap<>();
        m4.put("erp:breakduration", 10);
        m4.put("roles", new LinkedList(Arrays.asList("1", "2")));
        m4.put("rfid", "1001-0110-0210-0310-0410-0001");
        m4.put("responsibility", "demo-b1:production_line_A_1");
        ERPPerson p4 = new ERPPerson("4", "Joan Smith", m4);
        this.persons.add(p4);
        
        Map<String, Object> m5 = new HashMap<>();
        m5.put("rfid", "1001-0110-0210-0310-0410-0003");
        m5.put("responsibility", "demo-b1:production_line_D_1");
        m5.put("reference", "http://www.ebbits-project.eu/ontologies/M48_HR.owl#Bob");
        m5.put("competence", "http://www.ebbits-project.eu/ontologies/M48_HR.owl#AbleToRepairRobots");
        ERPPerson p5 = new ERPPerson("5", "Bob Serviceman", m5);
        this.persons.add(p5);
    }    

    public static class ERPPerson {
        
        private String id;
        private String name;
        private Map<String, Object> properties;

        public ERPPerson() {
        }

        public ERPPerson(String id, String name, Map properties) {
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

        public Map<String, Object> getProperties() {
            return properties;
        }
                
    }
    
}

