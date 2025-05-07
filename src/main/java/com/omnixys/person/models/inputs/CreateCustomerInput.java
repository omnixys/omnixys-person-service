package com.omnixys.person.models.inputs;

import com.omnixys.person.models.dto.PersonDTO;
import com.omnixys.person.models.dto.UserDTO;
import com.omnixys.person.models.dto.CustomerDTO;
import jakarta.validation.Valid;

public record CreateCustomerInput(
    @Valid PersonDTO personInput,
    @Valid CustomerDTO customerInput,
    @Valid UserDTO userInput
) {
}
