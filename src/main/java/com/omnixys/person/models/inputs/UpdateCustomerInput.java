package com.omnixys.person.models.inputs;

import com.omnixys.person.models.dto.CustomerDTO;
import com.omnixys.person.models.dto.PersonDTO;

public record UpdateCustomerInput(
    PersonDTO personInput,
    CustomerDTO customerInput
) {
}
