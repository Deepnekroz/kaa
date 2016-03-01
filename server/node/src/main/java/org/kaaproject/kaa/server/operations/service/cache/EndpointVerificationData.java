package org.kaaproject.kaa.server.operations.service.cache;

import java.security.PublicKey;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Contains the data required for endpoint verification during the session
 * initialization process.
 *
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public class EndpointVerificationData {

    private final PublicKey publicKey;
    private final String applicationId;

    public EndpointVerificationData(PublicKey publicKey, String applicationId) {
        this.publicKey = publicKey;
        this.applicationId = applicationId;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
