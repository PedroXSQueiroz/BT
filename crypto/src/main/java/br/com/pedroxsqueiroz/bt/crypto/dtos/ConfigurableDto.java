package br.com.pedroxsqueiroz.bt.crypto.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurableDto {

    private String name;

    private Map<String, Object> params;

}
