package org.miles2run.business.producers;

import com.mongodb.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 20/03/14.
 */
@ApplicationScoped
public class MongoProducer {

    @Inject
    private Logger logger;

    private DB db;

    @PostConstruct
    void constructMongoDBInstance() {
        try {
            logger.info("Creating MongoDB instance...");
            String host = System.getenv("OPENSHIFT_MONGODB_DB_HOST");
            if (host == null) {
                this.db = new MongoClient("localhost", 27017).getDB("miles2run");
                return;
            }
            int port = Integer.parseInt(System.getenv("OPENSHIFT_MONGODB_DB_PORT"));
            String dbname = System.getenv("OPENSHIFT_APP_NAME");
            String username = System.getenv("OPENSHIFT_MONGODB_DB_USERNAME");
            String password = System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD");
            MongoClientOptions mongoClientOptions = MongoClientOptions.builder().connectionsPerHost(20).build();
            MongoClient mongoClient = new MongoClient(new ServerAddress(host, port), mongoClientOptions);
            mongoClient.setWriteConcern(WriteConcern.SAFE);
            DB db = mongoClient.getDB(dbname);
            if (db.authenticate(username, password.toCharArray())) {
                this.db = db;
            } else {
                throw new RuntimeException("Not able to authenticate with MongoDB");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Produces
    @Named
    public DB db() {
        return this.db;
    }

}
