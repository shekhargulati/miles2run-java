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
    private Long id;

    @Version
    private int version;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.legacyjdk.PersistentDate")
    private Date updatedAt;

    @Column(updatable = false)
    @Type(type = "org.jadira.usertype.dateandtime.legacyjdk.PersistentDate")
    private Date createdAt;


    protected BaseEntity() {
    }

    public Long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public Date getLastModified() {
        return getLastUpdatedAt() == null ? getCreatedAt() : getLastUpdatedAt();
    }

    public Date getLastUpdatedAt() {
        return updatedAt == null ? null : (Date) updatedAt.clone();
    }

    public Date getCreatedAt() {
        return (Date) createdAt.clone();
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
