package org.miles2run.business.domain.jpa;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by shekhargulati on 07/07/14.
 */
@Entity
@Table(name = "duration_goal")
public class DurationGoal extends NewGoal {

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date start;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date end;

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DurationGoal{");
        sb.append("id=").append(this.id);
        sb.append(", purpose=").append(this.purpose);
        sb.append(", start=").append(start);
        sb.append(", end=").append(end);
        sb.append('}');
        return sb.toString();
    }
}
