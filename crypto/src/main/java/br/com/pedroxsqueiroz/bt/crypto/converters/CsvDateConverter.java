package br.com.pedroxsqueiroz.bt.crypto.converters;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CsvDateConverter extends AbstractBeanField<Date, String> {

    @Override
    protected Date convert(String s) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        return  Date.from( LocalDateTime
                .parse(s, DateTimeFormatter.ofPattern("dd.MM.yyyy") )
                .atZone(ZoneId.systemDefault())
                .toInstant() );
    }
}
