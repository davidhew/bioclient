package com.davidhew.bioclient;

/**
 * Created by shouru on 2018/9/16.
 */
public enum Command {


    ComputePrime("Compute Prime","求质数"),ComputeSum("Compute Sum","累积求和");

    /**
     * 命令类型
     */
    private final String command;

    /**
     * 命令类型的注释
     */
    private final String comment;

    private Command(String command, String comment){
        this.command = command;
        this.comment = comment;
    }
}
