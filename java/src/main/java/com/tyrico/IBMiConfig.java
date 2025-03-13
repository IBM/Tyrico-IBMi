package com.tyrico;

public class IBMiConfig {
    private String systemName;
    private String username;
    private String password;
    private String libraryName;
    private String outQueueNameKeyed;
    private String inQueueName;

    IBMiConfig(){     
        EnvLoader envLoader = new EnvLoader();
        systemName = envLoader.getEnvironmentVariable("systemName");
        username = envLoader.getEnvironmentVariable("userName");
        password = envLoader.getEnvironmentVariable("password");
        libraryName = envLoader.getEnvironmentVariable("libraryName");
        outQueueNameKeyed = envLoader.getEnvironmentVariable("outQueueNameKeyed");
        inQueueName = envLoader.getEnvironmentVariable("inQueueName");
    }
    
    public String getSystemName(){
        return systemName;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }
    public String getLibraryName(){
        return libraryName;
    }
    public String getOutQueueNameKeyed(){
        return outQueueNameKeyed;
    }
    public String getInQueueName() {return inQueueName;}
}
