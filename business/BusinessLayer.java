package business;
import service.*;
import java.util.*;
import components.data.*;
import business.*;
import javax.xml.*;
import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.w3c.dom.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;


public class BusinessLayer{
   
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
   public LAMSService lams;
public String addAppointment(String xmlStyle){
      String string = "";
      dbSingleton = DBSingleton.getInstance();
      
      
      try{
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         InputSource is = new InputSource();
         is.setCharacterStream(new StringReader(xmlStyle));
         
         Document doc = db.parse(is);
          date = doc.getElementsByTagName("date").item(0).getTextContent();
          time = doc.getElementsByTagName("time").item(0).getTextContent();
          apptId = doc.getElementsByTagName("id").item(0).getTextContent();
          patientId = doc.getElementsByTagName("patientId").item(0).getTextContent();
          physicianId = doc.getElementsByTagName("physicianId").item(0).getTextContent();
          pscId = doc.getElementsByTagName("pscId").item(0).getTextContent();
          phlebotomistId = doc.getElementsByTagName("phlebotomistId").item(0).getTextContent();
         NodeList labTests = doc.getElementsByTagName("labTests");
         Element labTestEle = (Element) labTests.item(0);
         
         NodeList innerElementList = labTestEle.getChildNodes();
         Element innerElement = null;
    for(int ii=0; ii <labTests.getLength(); ii++){     
         for(int i=0; i<innerElementList.getLength(); ++i){
            if(innerElementList.item(i) instanceof Element){
               innerElement = (Element) innerElementList.item(i);
               break;
            }
         }
        
         NamedNodeMap innerElementAttr = innerElement.getAttributes();
         for(int i=0; i<innerElementAttr.getLength(); ++i){
            Node attr = innerElementAttr.item(i);
            if(attr.getNodeName() =="dxcode"){
               dxcode =attr.getNodeValue();
            } else if(attr.getNodeName() == "id"){
               labTestId = attr.getNodeValue();
            }
           
         }
       }
       
      String dateFormat = "HH:mm:ss";
      String startTime = "07:59:59";
      String endTime = "16:45:00";
    
      cStart = setTimeToCalendar(dateFormat, startTime, false);
      cEnd = setTimeToCalendar(dateFormat, endTime, true);
      cNow = setTimeToCalendar(dateFormat, time, true); 
      curDate = cNow.getTime();
      
      if (curDate.after(cStart.getTime()) && curDate.before(cEnd.getTime()) == true) {
                
      } else {
         string += "Time is out of range";
         error.add("Time is out of range");
      } 
      
      SimpleDateFormat tc1 = new SimpleDateFormat("HH:mm");

      Date t1 = tc1.parse(time);
      Calendar cal1 = Calendar.getInstance();
      cal1.setTime(t1);
      cal1.add(Calendar.MINUTE, -15);
      timec1 = tc1.format(cal1.getTime());
   
      SimpleDateFormat tc2 = new SimpleDateFormat("HH:mm");
      Date t2 = tc2.parse(time);
      Calendar cal2 = Calendar.getInstance();
      cal2.setTime(t2);
      cal2.add(Calendar.MINUTE, 15);
      timec2 = tc2.format(cal2.getTime());
  
      newRecord = dbSingleton.db.getData("Appointment","pscid='"+pscId+"'"+"AND phlebid='"+phlebotomistId+"'"+"AND apptdate='"+date+"'");
      String atTime = "";
      int size =0;
      timeWithin = false;
      checker = false;
      size = newRecord.size();
      Date pastTime = null;
      Date currentTime = null;
      Date futureTime = null;
  
      if(size > 0){
          for(Object o: newRecord){
               atTime = (((Appointment)o).getAppttime()).toString();
             SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
               pastTime = sdf.parse(timec1);
             currentTime = sdf.parse(atTime);
             futureTime = sdf.parse(timec2);
             checker = (futureTime.compareTo(pastTime) < 0);
             if(checker == true){
                timeWithin = (currentTime.after(pastTime) || currentTime.before(futureTime));
             } else {
                  timeWithin = (currentTime.after(pastTime) && currentTime.before(futureTime));
             }
             if(timeWithin == true){
                string += "Phlebotomist is busy at the time. Please schedule at another time";
                error.add("PHLEB IS BUSY");
            
             }
         }
      } 

                     
      } catch(NullPointerException e){
         e.printStackTrace();
      }  catch(Exception ee){
         ee.printStackTrace();
      }

      Patient patient = getPatient(patientId);
      Phlebotomist phleb = getPhleb(phlebotomistId);
      PSC psc = getPSC(pscId); 
      Appointment appt = getAppointmentObject(apptId);
      Physician phys = getPhysician(physicianId);
      AppointmentLabTestPK altpk = getAppointmentLabTestPK(apptId,labTestId,dxcode);
      List<AppointmentLabTest> altList = null;
      
       if(dbSingleton.db.getData("Appointment","").contains(appt)){
             string += "<?xml version='1.0' encoding='UTF-8' standalone='no'?><AppointmentList><error>ERROR:Appointment is not available</error></AppointmentList>";
             error.add("Appointment exists");
       } else {
      
            if(error.isEmpty() || error == null){  
     
                if(dbSingleton.db.getData("Patient", "").contains(patient) &&
                   dbSingleton.db.getData("Phlebotomist", "").contains(phleb) &&
                   dbSingleton.db.getData("PSC", "").contains(psc) &&
                   dbSingleton.db.getData("Physician", "").contains(phys) && 
                   isValidDate(date) &&
                   curDate.after(cStart.getTime()) && curDate.before(cEnd.getTime()) == true){
                   Appointment newAppt = new Appointment(apptId,java.sql.Date.valueOf(date),java.sql.Time.valueOf(time));
         
         
                  List<AppointmentLabTest> tests = new ArrayList<AppointmentLabTest>();
                  AppointmentLabTest test = new AppointmentLabTest(apptId,labTestId,dxcode);
        
          
                  test.setLabTest((LabTest)dbSingleton.db.getData("LabTest","id='"+labTestId+"'").get(0));
                  test.setDiagnosis((Diagnosis)dbSingleton.db.getData("Diagnosis", "code='"+dxcode+"'").get(0));
                  tests.add(test);
         
        
         newAppt.setAppointmentLabTestCollection(tests);
         newAppt.setPatientid(patient);
         newAppt.setPhlebid(phleb);
         newAppt.setPscid(psc);
         DBSingleton dbSingleton = DBSingleton.getInstance();
         boolean good = dbSingleton.db.addData(newAppt);
         List<Object> objs = dbSingleton.db.getData("Appointment", "id='" +apptId+"'");
          
            for (Object obj : objs){         
              patient = ((Appointment)obj).getPatientid();
              phleb = ((Appointment)obj).getPhlebid();
              psc = ((Appointment)obj).getPscid();
              altList = ((Appointment)obj).getAppointmentLabTestCollection();
                    for(Object ob : altList){
                        altpk= ((AppointmentLabTest)ob).getAppointmentLabTestPK();  
                     }

        string = createAppointment(newAppt) + createPatient(patient) + createPhlebotomist(phleb) +
                 createPsc(psc) + createAppointmentLabTestPK(altpk);
                 

            }
        
         } 
       } else {
         string = "<?xml version='1.0' encoding='UTF-8' standalone='no'?><AppointmentList><error>ERROR:Appointment is not available</error></AppointmentList>";
     
        }  
   }      
              return string;
   }
   
  public Patient getPatient(String id){
      Patient patient = new Patient(id);
      return patient;
   }
   
   public PSC getPSC(String id){
      PSC psc = new PSC(id);
      return psc;
   }
   
   public Phlebotomist getPhleb(String id){
      Phlebotomist phleb = new Phlebotomist(id);
      return phleb;
   }
   
   public Physician getPhysician(String id){
      Physician phys = new Physician(id);
      return phys;
   }
   
   public Diagnosis getDiagnosis(String id){
      Diagnosis diag = new Diagnosis(id);
      return diag;
   }  
   
   public Appointment getAppointmentObject(String id){
      Appointment appt = new Appointment(id);
      return appt;
   }
   
   public AppointmentLabTestPK getAppointmentLabTestPK(String apptid, String labtestid, String dxcode){
      AppointmentLabTestPK altPK = new AppointmentLabTestPK(apptid, labtestid, dxcode);
      return altPK;
   }
   
   
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
   
   public String createPhlebotomist(Phlebotomist phleb){
      String phlebId = phleb.getId();
      String phlebName = phleb.getName();
      String xml = " <phlebotomist id='" + phlebId +
                   "'> <name>" + phlebName + 
                   "</name> </phlebotomist>";
      return xml;
   }
   
   public String createPsc(PSC psc){
      String pscId = psc.getId();
      String pscName = psc.getName();
      String xml = " <psc id='" + pscId + 
                   "'> <name>" + pscName + 
                   "</name> </psc>";
      return xml;
   }
   
   public String createPhysician(Physician phys){
      String physId = phys.getId();
      String physName = phys.getName();
      String xml = " <physician id='" + physId + 
                   "'> <name>" + physName + 
                   "</name> </physician>";
      return xml;
   }
   
   private Calendar setTimeToCalendar(String dateFormat, String date, boolean addADay) throws ParseException {
      Date time = new SimpleDateFormat(dateFormat).parse(date);
      Calendar cal = Calendar.getInstance();
      cal.setTime(time );
      return cal;
   }



   public String createAppointmentLabTestPK(AppointmentLabTestPK altPK){
      String apptId = altPK.getApptid();
      String labTestId = altPK.getLabtestid();
      String dxCode = altPK.getDxcode();
      String xml = "<allLabTests><appointmentLabTest appointmentId='" +
                     apptId+"' dxcode='" +dxCode +
                     "' labTestId='" +labTestId+"'/></allLabTests></appointment>";
      return xml;
   }
   
   public String createAppointment(Appointment appt){
      java.sql.Date date = appt.getApptdate();
      java.sql.Time time = appt.getAppttime();
      String id = appt.getId();
      String xml = "<appointment date='"+date+"' id='"+id+"' time='"+time+"'>";
      return xml;
   } 
 
   public static boolean isValidDate(String inDate) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setLenient(false);
    try {
      dateFormat.parse(inDate.trim());
    } catch (ParseException pe) {
      return false;
    }
    return true;
  }

}