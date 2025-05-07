package com.omnixys.person.models.events;

import java.util.UUID;

public record ShoppingCartDTO(
  UUID customerId,
  String customerUsername,
  String token
) {
}
