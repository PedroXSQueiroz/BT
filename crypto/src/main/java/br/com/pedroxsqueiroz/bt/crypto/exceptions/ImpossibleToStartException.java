package br.com.pedroxsqueiroz.bt.crypto.exceptions;

public class ImpossibleToStartException extends Exception {

    public ImpossibleToStartException(Exception inner)
    {
        super(inner);
    }

}
