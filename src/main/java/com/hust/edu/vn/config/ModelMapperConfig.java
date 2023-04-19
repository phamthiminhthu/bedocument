package com.hust.edu.vn.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean(name = "modelMapperShallow")
    public ModelMapper modelMapperShallow(){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setDeepCopyEnabled(false);
        return modelMapper;
    }
//    @Bean(name = "modelMapperDeep")
//    public ModelMapper modelMapperDepp(){
//        ModelMapper modelMapper = new ModelMapper();
//        modelMapper.getConfiguration()
//                .setMatchingStrategy(MatchingStrategies.STRICT)
//                .setSkipNullEnabled(true)
//                .setDeepCopyEnabled(true);
//        return modelMapper;
//    }

}
