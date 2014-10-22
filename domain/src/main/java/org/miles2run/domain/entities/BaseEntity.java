package org.miles2run.domain.entities;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @TableGenerator(name = "id_generator", table = "id_gen", allocationSize = 100)
    @GeneratedValue(generator = "id_generator")
    Long id;

    @Version
    Long version;

    @Column(updatable = false)
    @Type(type = "org.jadira.usertype.dateandtime.legacyjdk.PersistentDate")
    Date createdAt;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.legacyjdk.PersistentDate")
    Date updatedAt;

    protected BaseEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Date getLastModified() {
        return getLastUpdatedAt() == null ? getCreatedAt() : getLastUpdatedAt();
    }

    public Date getCreatedAt() {
        return (Date) createdAt.clone();
    }

    public Date getLastUpdatedAt() {
        return updatedAt == null ? null : (Date) updatedAt.clone();
    }

    @PrePersist
    void prePersist() {
        createdAt = new Date();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = new Date();
    }
}
