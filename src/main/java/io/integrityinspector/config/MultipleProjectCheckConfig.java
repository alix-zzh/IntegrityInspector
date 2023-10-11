package io.integrityinspector.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MultipleProjectCheckConfig {
    private int maxUniquenessPercentageForCreatingReport;
}