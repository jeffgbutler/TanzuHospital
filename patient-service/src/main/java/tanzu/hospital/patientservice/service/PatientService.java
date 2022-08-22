package tanzu.hospital.patientservice.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tanzu.hospital.domain.Allergy;
import tanzu.hospital.domain.PatientDetails;
import tanzu.hospital.domain.SimpleEncounter;
import tanzu.hospital.domain.SimplePatient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {
    private final FhirContext fhirContext;
    private final String fhirEndpoint;
    private final AllergyService allergyService;
    private final EncounterService encounterService;

    public PatientService(FhirContext fhirContext, @Value("${fhir.endpoint}") String fhirEndpoint,
                          AllergyService allergyService, EncounterService encounterService) {
        this.fhirContext = fhirContext;
        this.fhirEndpoint = fhirEndpoint;
        this.allergyService = allergyService;
        this.encounterService = encounterService;
    }

    public List<SimplePatient> findAllPatients() {
        IGenericClient genericClient = getClient();

        Bundle results = genericClient.search().forResource(Patient.class)
                .returnBundle(Bundle.class)
                .execute();

        return transformPatientBundle(results);
    }

    public List<SimplePatient> searchPatients(String firstName, String lastName) {

        if (firstName == null) {
            if (lastName == null) {
                return findAllPatients();
            } else {
                return searchLastNameOnly(lastName);
            }
        } else {
            if (lastName == null) {
                return searchFirstNameOnly(firstName);
            } else {
                return searchFirstNameAndLastName(firstName, lastName);
            }
        }
    }

    private List<SimplePatient> searchFirstNameAndLastName(String firstName, String lastName) {

        IGenericClient genericClient = getClient();

        Bundle results = genericClient.search().forResource(Patient.class)
                .where(Patient.FAMILY.contains().value(lastName))
                .and(Patient.GIVEN.contains().value(firstName))
                .returnBundle(Bundle.class)
                .execute();

        return transformPatientBundle(results);
    }

    private List<SimplePatient> searchFirstNameOnly(String firstName) {

        IGenericClient genericClient = getClient();

        Bundle results = genericClient.search().forResource(Patient.class)
                .where(Patient.GIVEN.contains().value(firstName))
                .returnBundle(Bundle.class)
                .execute();

        return transformPatientBundle(results);
    }

    private List<SimplePatient> searchLastNameOnly(String lastName) {

        IGenericClient genericClient = getClient();

        Bundle results = genericClient.search().forResource(Patient.class)
                .where(Patient.FAMILY.contains().value(lastName))
                .returnBundle(Bundle.class)
                .execute();

        return transformPatientBundle(results);
    }

    private List<SimplePatient> transformPatientBundle(Bundle bundle) {
        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Patient.class::isInstance)
                .map(Patient.class::cast)
                .map(this::toPatient)
                .sorted()
                .collect(Collectors.toList());
    }

    private SimplePatient toPatient(Patient patient) {
        HumanName name = patient.getNameFirstRep();
        Enumerations.AdministrativeGender gender =  patient.getGender();
        String id = patient.getIdElement().getIdPart();
        LocalDate birthDate = patient.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return new SimplePatient(id, name.getGivenAsSingleString(), name.getFamily(), gender.name(), birthDate);
    }

    public Optional<PatientDetails> findPatient(String id) {
        return findFhirPatient(id).map(this::toPatient).map(this::toDetails);
    }

    private PatientDetails toDetails(SimplePatient simplePatient) {
        List<Allergy> allergies = allergyService.findAllergies(simplePatient.getId());
        List<SimpleEncounter> encounters = encounterService.findEncounters(simplePatient.getId());
        return new PatientDetails(simplePatient, allergies, encounters);
    }

    private Optional<Patient> findFhirPatient(String id) {
        IGenericClient genericClient = getClient();

        try {
            Patient patient = genericClient.read().resource(Patient.class)
                    .withId(id)
                    .execute();
            return Optional.of(patient);
        } catch (Throwable e) {
            return Optional.empty();
        }

    }

    private IGenericClient getClient() {
        return fhirContext.newRestfulGenericClient(fhirEndpoint);
    }
}
