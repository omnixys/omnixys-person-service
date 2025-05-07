package com.omnixys.person.models.mapper;

import com.omnixys.person.models.dto.ContactDTO;
import com.omnixys.person.models.entities.Contact;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContactMapper {
  Contact toContact(ContactDTO contactDTO);

  ContactDTO toDTO(Contact contact);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Contact partialUpdate(ContactDTO contactDTO, @MappingTarget Contact contact);
}
