package org.example.coinmate.model;

/**
 * Server time response.
 */
public class ServerTime {
    private Long serverTime;

    public Long getServerTime() {
        return serverTime;
    }

    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }

    @Override
    public String toString() {
        return "ServerTime{" +
                "serverTime=" + serverTime +
                '}';
    }
}
