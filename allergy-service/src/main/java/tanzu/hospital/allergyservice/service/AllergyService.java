package tanzu.hospital.allergyservice.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumeration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tanzu.hospital.domain.Allergy;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AllergyService {
    private final FhirContext fhirContext;
    private final String fhirEndpoint;

    public AllergyService(FhirContext fhirContext, @Value("${fhir.endpoint}") String fhirEndpoint) {
        this.fhirContext = fhirContext;
        this.fhirEndpoint = fhirEndpoint;
    }

    public List<Allergy> findAllergyIntolerance(String patientId) {
        IGenericClient genericClient = getClient();

        Bundle bundle = genericClient.search().forResource(AllergyIntolerance.class)
                .where(AllergyIntolerance.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(AllergyIntolerance.class::isInstance)
                .map(AllergyIntolerance.class::cast)
                .map(this::toAllergy)
                .collect(Collectors.toList());
    }

    private Allergy toAllergy(AllergyIntolerance allergyIntolerance) {
        String type = allergyIntolerance.getType().toCode();
        List<String> categories = allergyIntolerance.getCategory().stream().map(Enumeration::getCode).collect(Collectors.toList());
        String text = allergyIntolerance.getCode().getText();
        return new Allergy(type, categories, text);
    }

    private IGenericClient getClient() {
        return fhirContext.newRestfulGenericClient(fhirEndpoint);
    }
}
