package br.com.pedroxsqueiroz.bt.crypto.exceptions;

public class ImpossibleToStopException extends Exception {

    public ImpossibleToStopException(Exception inner)
    {
        super(inner);
    }

}
