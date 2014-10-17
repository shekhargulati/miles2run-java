package org.miles2run.business.recommender;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.miles2run.business.repository.mongo.UserProfileRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class LocationBasedFriendRecommenderTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private LocationBasedFriendRecommender recommender = new LocationBasedFriendRecommender();

    @Test
    public void recommend_NoUserInTheSystem_RecommendsNoFriends() throws Exception {
        String username = "test_user";
        Mockito.when(userProfileRepository.getUserLngLat(username)).thenReturn(new BasicDBObject());
        DBCursor mockDBCursor = Mockito.mock(DBCursor.class);
        Mockito.when(mockDBCursor.hasNext()).thenReturn(false);
        Mockito.when(userProfileRepository.findUsersByProximity(Mockito.<DBObject>any(), Mockito.anyInt())).thenReturn(mockDBCursor);
        List<String> recommendations = recommender.recommend(username);
        Assert.assertThat(recommendations.size(), IsEqual.equalTo(0));
        Mockito.verify(userProfileRepository).getUserLngLat(username);
        Mockito.verify(userProfileRepository).findUsersByProximity(Mockito.<DBObject>any(), Mockito.anyInt());
        Mockito.verify(mockDBCursor).hasNext();
    }

    @Test
    public void recommend_UsersInTheSystem_RecommendUsers() throws Exception {
        String username = "test_user";
        Mockito.when(userProfileRepository.getUserLngLat(username)).thenReturn(new BasicDBObject());
        DBCursor mockDBCursor = Mockito.mock(DBCursor.class);
        Mockito.when(mockDBCursor.hasNext()).thenReturn(true, true, false);
        Mockito.when(mockDBCursor.next()).thenReturn(new BasicDBObject("username", "test_user_1"));
        Mockito.when(mockDBCursor.next()).thenReturn(new BasicDBObject("username", "test_user_2"));
        Mockito.when(userProfileRepository.findUsersByProximity(Mockito.<DBObject>any(), Mockito.anyInt())).thenReturn(mockDBCursor);
        List<String> recommendations = recommender.recommend(username);
        Assert.assertThat(recommendations.size(), IsEqual.equalTo(2));
        Mockito.verify(userProfileRepository).getUserLngLat(username);
        Mockito.verify(userProfileRepository).findUsersByProximity(Mockito.<DBObject>any(), Mockito.anyInt());
        Mockito.verify(mockDBCursor, Mockito.times(3)).hasNext();
        Mockito.verify(mockDBCursor, Mockito.times(2)).next();

    }
}