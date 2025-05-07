package com.omnixys.person.models.inputs;

import com.omnixys.person.models.dto.EmployeeDTO;
import com.omnixys.person.models.dto.PersonDTO;
import com.omnixys.person.models.dto.UserDTO;

public record CreateEmployeeInput(
    PersonDTO personInput,
    EmployeeDTO employeeInput,
    UserDTO userInput
) {
}
