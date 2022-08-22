package tanzu.hospital.encounterservice.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tanzu.hospital.domain.SimpleEncounter;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EncounterService {
    private final FhirContext fhirContext;
    private final String fhirEndpoint;

    public EncounterService(FhirContext fhirContext, @Value("${fhir.endpoint}") String fhirEndpoint) {
        this.fhirContext = fhirContext;
        this.fhirEndpoint = fhirEndpoint;
    }

    public List<SimpleEncounter> findEncounters(String patientId) {
        IGenericClient genericClient = getClient();

        Bundle bundle = genericClient.search().forResource(Encounter.class)
                .where(Encounter.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Encounter.class::isInstance)
                .map(Encounter.class::cast)
                .map(this::toEncounter)
                .collect(Collectors.toList());
    }

    private SimpleEncounter toEncounter(Encounter encounter) {
        List<String> types = encounter.getType().stream().map(CodeableConcept::getText).collect(Collectors.toList());
        List<String> locations = encounter.getLocation().stream()
                .map(Encounter.EncounterLocationComponent::getLocation)
                .map(Reference::getDisplay)
                .collect(Collectors.toList());
        String serviceProvider = encounter.getServiceProvider().getDisplay();
        Instant startTime = encounter.getPeriod().getStart().toInstant();
        Instant endTime = encounter.getPeriod().getEnd().toInstant();

        SimpleEncounter simpleEncounter = new SimpleEncounter();
        simpleEncounter.setTypes(types);
        simpleEncounter.setLocations(locations);
        simpleEncounter.setServiceProvider(serviceProvider);
        simpleEncounter.setStartDate(startTime);
        simpleEncounter.setEndDate(endTime);
        return simpleEncounter;
    }

    private IGenericClient getClient() {
        return fhirContext.newRestfulGenericClient(fhirEndpoint);
    }
}
