package com.axway.apim.promote.mvn.plugin.exceptions;

import com.axway.apim.lib.ErrorCode;

/**
 * Exception for API related errors
 * This should be thrown from api-axway-promote tool and should be handle as warning
 */
public class NoChangesInApiForPublishException extends AbstractPublishingPluginException {

    public NoChangesInApiForPublishException(final String message, final Exception cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public NoChangesInApiForPublishException(final String message, final Throwable cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public NoChangesInApiForPublishException(final String message, final ErrorCode errorCode) {
        super(message, errorCode);
    }
}
