# SOAP-Laboratory-Appointment-Management-System

SOAP Service returns XML strings based on user queries for an appointment management system from the database. User will also be able to make an appointment with a registered Phlebotomist and Physician.  

### Functionalities:

#### Service Layer
- User will be able to view all appointments
- User will be able to view a specific appointment based on the appointment ID
- User will be able to add an appointment
 
#### Business Layer
- Checks for duplicates and avoids adding duplicate appointments
- Checks for any overlapping appointments and rejects appointments

#### To use functionality, deploy the war file in the GlassFish server
