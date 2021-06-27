package br.com.pedroxsqueiroz.bt.crypto.dtos.binance;

import lombok.Data;

import java.util.List;


@Data
public class BinanceAccount {

    private List<BinanceBalance> balances;

}
