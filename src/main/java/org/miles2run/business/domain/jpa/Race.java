package org.miles2run.business.domain.jpa;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by shekhargulati on 08/07/14.
 */
@Entity
@Access(AccessType.FIELD)
@Table(name = "race")
public class Race extends BaseEntity {

}
