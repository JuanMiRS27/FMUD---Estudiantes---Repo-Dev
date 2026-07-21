package com.fmud.gateway.web;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(Instant timestamp, int status, String error, String code, String message, String path, List<String> details) {
}
