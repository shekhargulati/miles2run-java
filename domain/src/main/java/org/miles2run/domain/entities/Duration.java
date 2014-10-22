package org.miles2run.domain.entities;

import org.hibernate.annotations.Type;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Embeddable
@Access(AccessType.FIELD)
public class Duration implements Serializable {

    @NotNull
    @Type(type = "org.jadira.usertype.dateandtime.legacyjdk.PersistentDate")
    private Date startDate;

    @Type(type = "org.jadira.usertype.dateandtime.legacyjdk.PersistentDate")
    private Date endDate;

    protected Duration() {
    }

    public Duration(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
