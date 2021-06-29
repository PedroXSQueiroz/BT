package br.com.pedroxsqueiroz.bt.crypto.utils.config_tools;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ConfigParam {

    String name();

    int priority() default 0;

    boolean getFromParent() default false;

}
