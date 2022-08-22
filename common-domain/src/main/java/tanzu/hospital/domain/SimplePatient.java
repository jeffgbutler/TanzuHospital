package tanzu.hospital.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimplePatient implements Comparable<SimplePatient> {
    private String id;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate birthDate;

    @Override
    public int compareTo(SimplePatient o) {
        int answer = compare(this.lastName, o.lastName);
        if (answer == 0) {
            answer = compare(this.firstName, o.firstName);
        }
        if (answer == 0) {
            answer = compare(this.birthDate, o.birthDate);
        }

        return answer;
    }

    private <R extends Comparable<R>> int compare(R o1, R o2) {
        if (o1 == null) {
            return -1;
        } else {
            if (o2 == null) {
                return 1;
            } else {
                return o1.compareTo(o2);
            }
        }
    }
}
