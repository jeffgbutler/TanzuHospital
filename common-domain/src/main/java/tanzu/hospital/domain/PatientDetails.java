package tanzu.hospital.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientDetails {
    private SimplePatient basicPatientInformation;
    private List<Allergy> allergies;
    private List<SimpleEncounter> encounters;
}
