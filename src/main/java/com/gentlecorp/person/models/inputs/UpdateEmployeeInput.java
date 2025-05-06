package com.gentlecorp.person.models.inputs;

import com.gentlecorp.person.models.dto.EmployeeDTO;
import com.gentlecorp.person.models.dto.PersonDTO;

public record UpdateEmployeeInput(
    PersonDTO personInput,
    EmployeeDTO employeeInput
) {
}
