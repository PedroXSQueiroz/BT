package br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands;

import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;
import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStopException;

public interface Stopable {

    void stop() throws ImpossibleToStopException;

}
