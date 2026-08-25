package com.schedio.shared.api;

public record ValidationError(
	String field,
	String message
) {
}
