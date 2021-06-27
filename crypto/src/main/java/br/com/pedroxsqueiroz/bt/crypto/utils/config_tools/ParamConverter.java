package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools;

import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public interface ParamConverter<S, R>
{
     R convert(S source);

     Class<R> convertTo();

     Class<S> convertFrom();

}
