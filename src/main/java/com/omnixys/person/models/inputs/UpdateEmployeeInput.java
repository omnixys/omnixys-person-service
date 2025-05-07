package com.omnixys.person.models.inputs;

import com.omnixys.person.models.dto.EmployeeDTO;
import com.omnixys.person.models.dto.PersonDTO;

public record UpdateEmployeeInput(
    PersonDTO personInput,
    EmployeeDTO employeeInput
) {
}
