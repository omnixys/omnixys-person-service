package com.gentlecorp.person.models.inputs;

import com.gentlecorp.person.models.dto.PersonDTO;
import com.gentlecorp.person.models.dto.UserDTO;
import com.gentlecorp.person.models.dto.CustomerDTO;
import jakarta.validation.Valid;

public record CreateCustomerInput(
    @Valid PersonDTO personInput,
    @Valid CustomerDTO customerInput,
    @Valid UserDTO userInput
) {
}
