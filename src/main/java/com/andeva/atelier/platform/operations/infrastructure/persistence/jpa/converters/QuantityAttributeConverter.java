package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.operations.domain.model.valueobjects.Quantity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Attribute Converter to convert between the Quantity value object and its database representation (Integer).
 * This converter allows JPA to automatically convert Quantity instances to Integer values when persisting to the database, and to convert Integer values back to Quantity instances when reading from the database. The autoApply = true attribute indicates that this converter should be applied automatically to all attributes of type Quantity in the JPA entities.
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class QuantityAttributeConverter implements AttributeConverter<Quantity, Integer> {

    /**
     * Converts a Quantity value object to its database representation (Integer).
     * @param attribute  the entity attribute value to be converted
     * @return the converted database column value (Integer) or null if the attribute is null
     */
    @Override
    public Integer convertToDatabaseColumn(Quantity attribute) {
        return (attribute == null) ? null : attribute.value();
    }

    /**
     * Converts a database column value (Integer) back to a Quantity value object.
     * @param dbData  the data from the database column to be converted
     * @return the converted entity attribute value (Quantity) or null if the dbData is null
     */
    @Override
    public Quantity convertToEntityAttribute(Integer dbData) {
        return (dbData == null) ? null : new Quantity(dbData);
    }
}