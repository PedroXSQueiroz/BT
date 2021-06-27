package br.com.pedroxsqueiroz.bt.crypto.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.File;
import java.util.List;

@Configuration
public class StartupConfig {

    @Scope("thread")
    @Bean("file_database")
    File fileToProcess(@Autowired ApplicationArguments args )
    {
        List<String> file_database = args.getOptionValues("file_database");

        if(file_database.size() != 1)
        {
            return null;
        }

        return new File( file_database.get(0) );
    }

    @Scope("thread")
    @Bean("output_trade")
    File outputTrade(@Autowired ApplicationArguments args)
    {
        List<String> file_database = args.getOptionValues("output_trade");

        if(file_database.size() != 1)
        {
            return null;
        }

        return new File( file_database.get(0) );
    }

}
