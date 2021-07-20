package br.com.pedroxsqueiroz.bt.crypto.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StringToUUIDConverter implements Converter<String, UUID> {

    @Override
    public UUID convert(String s) {
        try {
            return UUID.fromString(s);
        }
        catch (IllegalArgumentException ex){
            throw new IllegalArgumentException("Invalid input UUID string");
        }
    }
}
