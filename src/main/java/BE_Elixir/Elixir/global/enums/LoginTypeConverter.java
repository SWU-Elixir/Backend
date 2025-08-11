package BE_Elixir.Elixir.global.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class LoginTypeConverter implements AttributeConverter<LoginType, String> {

    @Override
    public String convertToDatabaseColumn(LoginType provider) {
        return provider != null ? provider.name() : null;
    }

    @Override
    public LoginType convertToEntityAttribute(String dbData) {
        return dbData != null ? LoginType.valueOf(dbData.toUpperCase()) : null;
    }
}
