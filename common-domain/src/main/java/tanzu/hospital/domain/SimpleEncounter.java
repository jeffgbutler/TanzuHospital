package tanzu.hospital.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SimpleEncounter {
    List<String> types;
    List<String> locations;
    String serviceProvider;
    Instant startDate;
    Instant endDate;
}
