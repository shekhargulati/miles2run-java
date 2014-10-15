package org.miles2run.business.recommender;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.miles2run.business.services.mongo.ProfileMongoService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class LocationBasedFriendRecommenderTest {

    @Mock
    private ProfileMongoService profileMongoService;

    @InjectMocks
    private LocationBasedFriendRecommender recommender = new LocationBasedFriendRecommender();

    @Test
    public void recommend_NoUserInTheSystem_RecommendsNoFriends() throws Exception {
        String username = "test_user";
        Mockito.when(profileMongoService.getUserLngLat(username)).thenReturn(new BasicDBObject());
        DBCursor mockDBCursor = Mockito.mock(DBCursor.class);
        Mockito.when(mockDBCursor.hasNext()).thenReturn(false);
        Mockito.when(profileMongoService.findUsersByProximity(Mockito.<DBObject>any(), Mockito.anyInt())).thenReturn(mockDBCursor);
        List<String> recommendations = recommender.recommend(username);
        Assert.assertThat(recommendations.size(), IsEqual.equalTo(0));
        Mockito.verify(profileMongoService).getUserLngLat(username);
        Mockito.verify(profileMongoService).findUsersByProximity(Mockito.<DBObject>any(), Mockito.anyInt());
        Mockito.verify(mockDBCursor).hasNext();
    }
}