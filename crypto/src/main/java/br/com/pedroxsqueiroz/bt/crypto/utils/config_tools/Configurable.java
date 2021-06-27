package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools;

import br.com.pedroxsqueiroz.bt.crypto.exceptions.ConfigParamNotFoundException;

import java.util.Map;

public interface Configurable {

    void config(Map<String, Object> configParams);

    Map<String, Class<?>> getConfigParamsNameAndType();

    boolean isConfigured();

    Object getConfigParamValue(String propertyFromSource) throws IllegalAccessException, ConfigParamNotFoundException;
}
