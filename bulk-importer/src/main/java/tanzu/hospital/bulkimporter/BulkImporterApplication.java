package tanzu.hospital.bulkimporter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tanzu.hospital.bulkimporter.service.TransactionImporter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Stream;

@SpringBootApplication
public class BulkImporterApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(BulkImporterApplication.class, args);
	}

	private final TransactionImporter transactionImporter;

	public BulkImporterApplication(TransactionImporter transactionImporter) {
		this.transactionImporter = transactionImporter;
	}

	@Override
	public void run(String... args) throws URISyntaxException, IOException {
		URL url = this.getClass().getResource("/generated_patient_data");
		File directory = new File(url.toURI());

		// first import the practitioners
		Stream.of(directory.listFiles())
				.filter(this::isPractitionerInformation)
				.findFirst()
				.ifPresent(this::importFhirData);

		// then import the hospitals
		Stream.of(directory.listFiles())
				.filter(this::isHospitalInformation)
				.findFirst()
				.ifPresent(this::importFhirData);

		// then import the patients
		Stream.of(directory.listFiles())
				.filter(this::isFhirPatientData)
				.forEach(this::importFhirData);
	}

	private boolean isFhirPatientData(File file) {
		return !file.isDirectory()
				&& file.getName().endsWith(".json")
				&& !isPractitionerInformation(file)
				&& !isHospitalInformation(file);
	}

	private boolean isPractitionerInformation(File file) {
		return file.getName().startsWith("practitionerInformation");
	}

	private boolean isHospitalInformation(File file) {
		return file.getName().startsWith("hospitalInformation");
	}

	private void importFhirData(File file) {
		try {
			System.out.print("Importing " + file.getName() + "...");
			long start = System.currentTimeMillis();
			transactionImporter.importTransaction(file);
			long end = System.currentTimeMillis();
			long seconds = (end - start) / 1000;
			System.out.println(" (" + seconds + " seconds)");
		} catch (Exception e) {
			System.out.println(" (Error)");
			e.printStackTrace(System.out);
		}
	}
}
