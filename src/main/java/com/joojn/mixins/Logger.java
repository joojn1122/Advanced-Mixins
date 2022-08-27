package com.joojn.mixins;

public class Logger {

    private final String name;

    public Logger(String name)
    {
        this.name = name;
    }

    public void error(Object error)
    {
        System.err.println(String.format("[%s] Error: %s", this.name, error.toString()));
    }

    public void info(Object info)
    {
        System.out.println(String.format("[%s] Info: %s", this.name, info.toString()));
    }

    public void info(Object info, Object... args)
    {
        info(String.format(String.valueOf(info), args));
    }
}
