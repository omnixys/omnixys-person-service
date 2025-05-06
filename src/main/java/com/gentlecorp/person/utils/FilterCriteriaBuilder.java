package com.gentlecorp.person.utils;

import com.gentlecorp.person.models.filter.FilterField;
import com.gentlecorp.person.models.filter.FilterOperator;
import org.springframework.data.mongodb.core.query.Criteria;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility-Klasse zum automatisierten Erzeugen von MongoDB-Criteria anhand eines Filter-DTOs.
 */
public class FilterCriteriaBuilder {

    public static List<Criteria> buildCriteria(Object filterDTO) {
        List<Criteria> criteriaList = new ArrayList<>();

        for (Field field : filterDTO.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            FilterField annotation = field.getAnnotation(FilterField.class);
            if (annotation == null) continue;

            try {
                Object value = field.get(filterDTO);
                if (value == null) continue;

                String path = annotation.path().isEmpty() ? field.getName() : annotation.path();
                FilterOperator operator = annotation.operator();

                Criteria criteria = switch (operator) {
                    case IS -> Criteria.where(path).is(value);
                    case REGEX -> Criteria.where(path).regex(value.toString(), "i");
                    case GT -> Criteria.where(path).gt(value);
                    case LT -> Criteria.where(path).lt(value);
                };

                criteriaList.add(criteria);

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Zugriff auf Filterfeld fehlgeschlagen: " + field.getName(), e);
            }
        }

        return criteriaList;
    }
}
