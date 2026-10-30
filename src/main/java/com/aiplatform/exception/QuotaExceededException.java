package com.aiplatform.exception;

public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(long remaining, long required) {
        super("Tu cuota mensual no alcanza: quedan " + remaining + " tokens y la solicitud requiere al menos " + required + ".");
    }
}
