package com.vaxjaz.business.flow.state_machine.exception;

public class SysEx {

    public static void throwDirect(String msg) {
        throw new RuntimeException(msg);
    }

}
