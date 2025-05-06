package com.gentlecorp.person.models.inputs;

import com.gentlecorp.person.models.dto.EmployeeDTO;
import com.gentlecorp.person.models.dto.PersonDTO;
import com.gentlecorp.person.models.dto.UserDTO;

public record CreateEmployeeInput(
    PersonDTO personInput,
    EmployeeDTO employeeInput,
    UserDTO userInput
) {
}
