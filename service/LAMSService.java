package service;
import business.*;
import java.util.*;
import javax.jws.*;
import components.data.*;
import business.*;
import javax.xml.*;
import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.w3c.dom.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;


@WebService(serviceName= "LAMSService")
public class LAMSService{
   
   public BusinessLayer bl = new BusinessLayer();
   public DBSingleton dbSingleton;
   public String date = null;
   public String time = null;
   public String apptId = null;
   public String patientId = null;
   public String physicianId = null;
   public String pscId = null;
   public String phlebotomistId = null;
   public String dxcode = null;
   public String labTestId = null;
   public Calendar cStart = null;
   public Calendar cEnd = null;
   public Calendar cNow = null;
   public Date curDate = null;
   public Calendar curMin = null;
   public String appointments = null;
   public boolean timeWithin = false;
   public boolean checker = false;
   public ArrayList<String> error = new ArrayList<String>();
   public Date pastTime = null;
   public Date currentTime = null;
   public Date futureTime = null;
   public List<Object> newRecord;
   public String timec1 = "", timec2 = "";
   //public Patient patient = null;
   
   @WebMethod(operationName="initialize")
   public String initialize(){
      dbSingleton = DBSingleton.getInstance();
      dbSingleton.db.initialLoad("LAMS");
      System.out.println(dbSingleton.db.getDataWithColNames("select * from appointment where pscId = '510'"));

      return "Database Initialized";
   }
   
   @WebMethod(operationName="getAllAppointments")
   public String getAllAppointments(){
  
      dbSingleton = DBSingleton.getInstance();        //10-insert line to get instance of singleton object
      //dbSingleton.db.initialLoad("LAMS");
           
        String output = "All appointments\n";
        List<Object> objs = dbSingleton.db.getData("Appointment", "");
        if(objs == null || objs.isEmpty()){
           dbSingleton.db.initialLoad("LAMS");
           for(Object obj : objs){
               output += obj.toString() + "\n\n";
           }
         output += "Object was empty. DB reloaded";
         
        } else { 
            Patient patient = null;
            Phlebotomist phleb = null;
            PSC psc = null;
            List<AppointmentLabTest> altList = null;
            AppointmentLabTestPK pk = null;
            LabTest lt = null;
            Appointment appointment = null;
            java.sql.Date date = null;
            java.sql.Time time = null;
            String apptId = null;
            output +="<?xml version='1.0' encoding='utf-8' standalone='no'?><AppointmentList>";

            for (Object obj : objs){
                  patient = ((Appointment)obj).getPatientid();
                  phleb = ((Appointment)obj).getPhlebid();
                  psc = ((Appointment)obj).getPscid();
                  altList = ((Appointment)obj).getAppointmentLabTestCollection();
                     for(Object ob : altList){
                        pk= ((AppointmentLabTest)ob).getAppointmentLabTestPK();  
                     }
                  date = ((Appointment)obj).getApptdate();
                  time = ((Appointment)obj).getAppttime();
                  apptId = ((Appointment)obj).getId();                  
                  patient.getId();
                  
                 
            output += "\n<appointment date='"+date+"' id='"+apptId+"' time='"+time+"'>" + createPatient(patient) + createPhlebotomist(phleb) + createPsc(psc) + createAppointmentLabTestPK(pk);// + "<allLabTests>";
           }
           output +="</AppointmentList>";
         }   
           
      return output;
         
   }
   
 
   @WebMethod(operationName="getAppointment")
   public String getAppointment(String appointNumber){
      dbSingleton = DBSingleton.getInstance(); 
      
      List<Object> objs = dbSingleton.db.getData("Appointment", "id='"+appointNumber+"'");
        if(objs == null || objs.isEmpty()){
          appointments = "Appointment doesn't exist";
        } else{
        Patient patient = null;
            Phlebotomist phleb = null;
            PSC psc = null;
            List<AppointmentLabTest> altList = null;
            AppointmentLabTestPK pk = null;
            LabTest lt = null;
            Appointment appointment = null;
            java.sql.Date date = null;
            java.sql.Time time = null;
            String apptId = null;
        
        for (Object obj : objs){
            
            patient = ((Appointment)obj).getPatientid();
                  phleb = ((Appointment)obj).getPhlebid();
                  psc = ((Appointment)obj).getPscid();
                  altList = ((Appointment)obj).getAppointmentLabTestCollection();
                     for(Object ob : altList){
                        pk= ((AppointmentLabTest)ob).getAppointmentLabTestPK();  
                     }
                  date = ((Appointment)obj).getApptdate();
                  time = ((Appointment)obj).getAppttime();
                  apptId = ((Appointment)obj).getId();           
            appointments += "\n<?xml version='1.0' encoding='utf-8' standalone='no'?><AppointmentList><appointment date='"+date+"' id='"+apptId+"' time='"+time+"'>" + createPatient(patient) + createPhlebotomist(phleb) + createPsc(psc) + createAppointmentLabTestPK(pk)+"</AppointmentList>";
           
        }
     
       }
       return appointments;

   }
   
   @WebMethod(operationName="addAppointment")
   public String addAppointment(String xmlStyle){
      String output;
      output = bl.addAppointment(xmlStyle);
      return output;
   }
   
   @WebMethod(operationName="getPatient")
    public Patient getPatient(String id){
      Patient patient = new Patient(id);
      return patient;
   }
   
   @WebMethod(operationName="getPSC")
   public PSC getPSC(String id){
      PSC psc = new PSC(id);
      return psc;
   }
   
   @WebMethod(operationName="getPhleb")
   public Phlebotomist getPhleb(String id){
      Phlebotomist phleb = new Phlebotomist(id);
      return phleb;
   }
   
   @WebMethod(operationName="getPhysician")
   public Physician getPhysician(String id){
      Physician phys = new Physician(id);
      return phys;
   }
   
   @WebMethod(operationName="getDiagnosis")
   public Diagnosis getDiagnosis(String id){
      Diagnosis diag = new Diagnosis(id);
      return diag;
   }  
   
   @WebMethod(operationName="getAppointmentObject")
   public Appointment getAppointmentObject(String id){
      Appointment appt = new Appointment(id);
      return appt;
   }
   
   @WebMethod(operationName="getAppointmentLabTestPK")
   public AppointmentLabTestPK getAppointmentLabTestPK(String apptid, String labtestid, String dxcode){
      AppointmentLabTestPK altPK = new AppointmentLabTestPK(apptid, labtestid, dxcode);
      return altPK;
   }
   
   @WebMethod(operationName="createPatient")
   public String createPatient(Patient patient){
      String pId = patient.getId();
      String pName = patient.getName();
      String pAddress = patient.getAddress();
      char pInsurance = patient.getInsurance();
      Date pDob = patient.getDateofbirth();
      String xml = "<patient id='" + pId +
                    "'> <name>"+pName+
                    "</name> <address>"+pAddress+
                    "</address> <insurance>" +pInsurance+
                    "</insurance> <dob>" + pDob + "</dob> </patient>";
      return xml;
   }
   
   @WebMethod(operationName="createPhlebotomist")
   public String createPhlebotomist(Phlebotomist phleb){
      String phlebId = phleb.getId();
      String phlebName = phleb.getName();
      String xml = " <phlebotomist id='" + phlebId +
                   "'> <name>" + phlebName + 
                   "</name> </phlebotomist>";
      return xml;
   }
   
   @WebMethod(operationName="createPsc")
   public String createPsc(PSC psc){
      String pscId = psc.getId();
      String pscName = psc.getName();
      String xml = " <psc id='" + pscId + 
                   "'> <name>" + pscName + 
                   "</name> </psc>";
      return xml;
   }
   
   @WebMethod(operationName="createPhysician")
   public String createPhysician(Physician phys){
      String physId = phys.getId();
      String physName = phys.getName();
      String xml = " <physician id='" + physId + 
                   "'> <name>" + physName + 
                   "</name> </physician>";
      return xml;
   }
   
   @WebMethod(operationName="setTimeToCalendar")
   private Calendar setTimeToCalendar(String dateFormat, String date, boolean addADay) throws ParseException {
      Date time = new SimpleDateFormat(dateFormat).parse(date);
      Calendar cal = Calendar.getInstance();
      cal.setTime(time );
      return cal;
   }


   @WebMethod(operationName="createAppointmentLabTestPK")
   public String createAppointmentLabTestPK(AppointmentLabTestPK altPK){
      String apptId = altPK.getApptid();
      String labTestId = altPK.getLabtestid();
      String dxCode = altPK.getDxcode();
      String xml = "<allLabTests><appointmentLabTest appointmentId='" +
                     apptId+"' dxcode='" +dxCode +
                     "' labTestId='" +labTestId+"'/></allLabTests></appointment>";
      return xml;
   }
   
   @WebMethod(operationName="createAppointment")
   public String createAppointment(Appointment appt){
      java.sql.Date date = appt.getApptdate();
      java.sql.Time time = appt.getAppttime();
      String id = appt.getId();
      String xml = "<appointment date='"+date+"' id='"+id+"' time='"+time+"'>";
      return xml;
   } 
 



 }