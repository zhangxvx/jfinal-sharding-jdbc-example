package org.example;

import com.jfinal.server.undertow.UndertowServer;

/**
 * @author: zhangxv
 * @date: 2021/12/23
 * @desc:
 */
public class Start {
    public static void main(String[] args) {
        UndertowServer.start(AppConfig.class);
    }
}
