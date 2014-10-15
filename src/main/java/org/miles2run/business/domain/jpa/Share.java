package org.miles2run.business.domain.jpa;

import javax.persistence.*;

/**
 * Created by shekhargulati on 13/03/14.
 */
@Entity
@Table(name = "share")
@Access(AccessType.FIELD)
public class Share extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean twitter = false;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean facebook = false;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean googlePlus = false;

    public boolean isTwitter() {
        return twitter;
    }

    public void setTwitter(boolean twitter) {
        this.twitter = twitter;
    }

    public boolean isFacebook() {
        return facebook;
    }

    public void setFacebook(boolean facebook) {
        this.facebook = facebook;
    }

    public boolean isGooglePlus() {
        return googlePlus;
    }

    public void setGooglePlus(boolean googlePlus) {
        this.googlePlus = googlePlus;
    }

    @Override
    public String toString() {
        return "Share{" +
                "twitter=" + twitter +
                ", facebook=" + facebook +
                ", googlePlus=" + googlePlus +
                '}';
    }
}
