package org.miles2run.core.repositories.jpa;

import org.miles2run.domain.entities.SocialConnection;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class SocialConnectionRepository {

    @Inject
    private EntityManager entityManager;

    public SocialConnection save(SocialConnection socialConnection) {
        SocialConnection merged = entityManager.merge(socialConnection);
        entityManager.persist(merged);
        return merged;
    }

    public SocialConnection findByConnectionId(String connectionId) {
        final String findQuery = "SELECT s from SocialConnection s WHERE s.connectionId =:connectionId";
        TypedQuery<SocialConnection> query = entityManager.createQuery(findQuery, SocialConnection.class);
        query.setParameter("connectionId", connectionId);
        try {
            SocialConnection socialConnection = query.getSingleResult();
            return socialConnection;
        } catch (NoResultException e) {
            return null;
        }
    }

}
