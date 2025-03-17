package com.tyrico;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    private static Dotenv dotenv;

    EnvLoader(){
        if (dotenv == null){
            dotenv = Dotenv.load();
        }
    }

    public String getEnvironmentVariable(String variable) throws RuntimeException{
        String value = dotenv.get(variable);
        if (value == null){
            throw new RuntimeException("Environment variable " + variable + " not found");
        }
        return value;
    }

    boolean isDebug(){
        return getEnvironmentVariable("debug").equals("true");
    }
}
