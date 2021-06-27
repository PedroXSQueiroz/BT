package br.com.pedroxsqueiroz.bt.crypto.services.deprecated;

import java.util.List;

public interface OutputAdpater<T> {

    void setup();

    void out(T item);

    void out(List<T> itens );

}
