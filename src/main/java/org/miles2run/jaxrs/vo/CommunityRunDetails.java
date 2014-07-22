package org.miles2run.jaxrs.vo;

import org.miles2run.business.domain.jpa.CommunityRun;
import org.miles2run.business.domain.redis.CommunityRunCounter;

/**
 * Created by shekhargulati on 23/07/14.
 */
public class CommunityRunDetails {

    private final CommunityRun communityRun;
    private final CommunityRunCounter communityRunCounter;

    public CommunityRunDetails(CommunityRun communityRun, CommunityRunCounter communityRunCounter) {
        this.communityRun = communityRun;
        this.communityRunCounter = communityRunCounter;
    }

    public CommunityRun getCommunityRun() {
        return communityRun;
    }

    public CommunityRunCounter getCommunityRunCounter() {
        return communityRunCounter;
    }
}
