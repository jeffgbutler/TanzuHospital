package tanzu.hospital.bulkimporter.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class TransactionImporter {

    private final FhirContext fhirContext;
    private final String fhirEndpoint;

    public TransactionImporter(FhirContext fhirContext, @Value("${fhir.endpoint}") String fhirEndpoint) {
        this.fhirContext = fhirContext;
        this.fhirEndpoint = fhirEndpoint;
    }

    public void importTransaction(File inputFile) throws IOException {
        Bundle bundle = parseBundle(inputFile);
        IGenericClient fhirClient = fhirClient();
        fhirClient.transaction().withBundle(bundle).execute();
    }

    private Bundle parseBundle(File inputFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            return fhirContext.newJsonParser().parseResource(Bundle.class, fis);
        }
    }

    public IGenericClient fhirClient() {
        return fhirContext.newRestfulGenericClient(fhirEndpoint);
    }
}
