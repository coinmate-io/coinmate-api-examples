package org.example.coinmate.model;

/**
 * Base response wrapper for all Coinmate API responses.
 *
 * @param <T> The type of data in the response
 */
public class CoinmateResponse<T> {
    private boolean error;
    private String errorMessage;
    private T data;

    public CoinmateResponse() {
    }

    public CoinmateResponse(boolean error, String errorMessage, T data) {
        this.error = error;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return !error;
    }

    @Override
    public String toString() {
        return "CoinmateResponse{" +
                "error=" + error +
                ", errorMessage='" + errorMessage + '\'' +
                ", data=" + data +
                '}';
    }
}
