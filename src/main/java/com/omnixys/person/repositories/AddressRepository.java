package com.omnixys.person.repositories;

import com.omnixys.person.models.entities.Address;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AddressRepository extends MongoRepository<Address, String> {
    List<Address> findByCity(String city);
}
