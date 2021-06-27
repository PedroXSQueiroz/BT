package br.com.pedroxsqueiroz.bt.crypto.utils.continuos_processors_commands;

import br.com.pedroxsqueiroz.bt.crypto.exceptions.ImpossibleToStartException;

public interface Startable {
    void start() throws ImpossibleToStartException;
}
