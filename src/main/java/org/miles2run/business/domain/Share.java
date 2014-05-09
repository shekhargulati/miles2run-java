package org.miles2run.business.domain;

import javax.persistence.*;

/**
 * Created by shekhargulati on 13/03/14.
 */
@Entity
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean twitter = false;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean facebook = false;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private  boolean googlePlus = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
