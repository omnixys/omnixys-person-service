package com.gentlecorp.person.models.inputs;

import com.gentlecorp.person.models.dto.CustomerDTO;
import com.gentlecorp.person.models.dto.PersonDTO;

public record UpdateCustomerInput(
    PersonDTO personInput,
    CustomerDTO customerInput
) {
}
