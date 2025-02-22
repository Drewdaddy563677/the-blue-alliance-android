package com.thebluealliance.androidclient.subscribers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.thebluealliance.androidclient.database.Database;
import com.thebluealliance.androidclient.database.DatabaseMocker;
import com.thebluealliance.androidclient.datafeed.APICache;
import com.thebluealliance.androidclient.datafeed.framework.DatafeedTestDriver;
import com.thebluealliance.androidclient.datafeed.framework.ModelMaker;
import com.thebluealliance.androidclient.eventbus.EventAwardsEvent;
import com.thebluealliance.androidclient.listitems.CardedAwardListElement;
import com.thebluealliance.androidclient.listitems.ListItem;
import com.thebluealliance.androidclient.models.Award;
import com.thebluealliance.androidclient.renderers.AwardRenderer;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.LooperMode;

import java.util.List;

@LooperMode(LooperMode.Mode.PAUSED)
@RunWith(AndroidJUnit4.class)
public class AwardsListSubscriberTest {

    @Mock public Database mDb;
    @Mock public APICache mCache;

    private AwardsListSubscriber mSubscriber;
    private AwardRenderer mRenderer;
    private List<Award> mAwards;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mRenderer = new AwardRenderer(mCache);
        mSubscriber = new AwardsListSubscriber(mDb, mRenderer);
        mAwards = ModelMaker.getModelList(Award.class, "2015necmp_awards");
        DatabaseMocker.mockTeamsTable(mDb);
    }

    @Test
    public void testParseNullData()  {
        DatafeedTestDriver.parseNullData(mSubscriber);
    }

    @Test
    public void testSimpleParsing()  {
        DatafeedTestDriver.testSimpleParsing(mSubscriber, mAwards);
    }

    @Test
    public void testParse()  {
        List<ListItem> data = DatafeedTestDriver.getParsedData(mSubscriber, mAwards);

        assertEquals(data.size(), 21);
        for (int i = 0; i < data.size(); i++) {
            assertTrue(data.get(i) instanceof CardedAwardListElement);
        }

        // Test EventBus posting
        assertTrue(mSubscriber.shouldPostToEventBus());
        EventBus eventBus = mock(EventBus.class);
        mSubscriber.postToEventBus(eventBus);
        verify(eventBus).post(any(EventAwardsEvent.class));
    }

    @Test
    public void testSelectedTeam()  {
        mSubscriber.setTeamKey("frc195");
        List<ListItem> data = DatafeedTestDriver.getParsedData(mSubscriber, mAwards);
        CardedAwardListElement element = (CardedAwardListElement) data.get(0);

        assertEquals(element.mSelectedTeamNum, "195");
    }

    @Test
    public void testParseMultiTeamWinner()  {
        assertItemsEqual(0, 0);
    }

    @Test
    public void testParseSinglePersonWinner()  {
        assertItemsEqual(16, 3);
    }

    @Test
    public void testParseMultiPersonWinner()  {
        assertItemsEqual(18, 5);
    }

    @Test
    public void testParseSingleTeamWinner()  {
        assertItemsEqual(2, 2);
    }

    private void assertItemsEqual(int rawIndex, int sortedIndex)  {
        List<ListItem> data = DatafeedTestDriver.getParsedData(mSubscriber, mAwards);
        CardedAwardListElement element = (CardedAwardListElement) data.get(sortedIndex);
        Award award = mAwards.get(rawIndex);

        assertEquals(element.mAwardName, award.getName());
        assertEquals(element.mEventKey, award.getEventKey());
        assertEquals(element.mSelectedTeamNum, "");
        assertTrue(element.mAwardWinners.equals(award.getRecipientList()));
    }
}
