package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.param_converters;

import br.com.pedroxsqueiroz.bt.crypto.utils.config_tools.ParamConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class PathStringToInpuStreamParamConverter implements ParamConverter<String, InputStream> {

    @Override
    public InputStream convert(String source) {

        try {
            return new FileInputStream( new File(source));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Class<InputStream> convertTo() {
        return InputStream.class;
    }

    @Override
    public Class<String> convertFrom() {
        return String.class;
    }

}
