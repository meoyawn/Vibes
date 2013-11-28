package com.stiggpwnz.vibes.adapters;

import com.stiggpwnz.vibes.test.RobolectricGradleTestRunner;
import com.stiggpwnz.vibes.vk.models.Feed;
import com.stiggpwnz.vibes.vk.models.Post;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by adelnizamutdinov on 28/11/2013
 */
@RunWith(RobolectricGradleTestRunner.class)
public class FeedAdapterTest {

    Feed feed;

    @Before
    public void setUp() throws Exception {
        feed = new Feed();
        feed.items = new ArrayList<Post>();
        feed.items.add(new Post());
    }

    @Test
    public void testGetCount() throws Exception {
        assertEquals(0, new FeedAdapter(Robolectric.application, null).getCount());
        assertEquals(1, new FeedAdapter(Robolectric.application, feed).getCount());
    }

    @Test
    public void testGetItem() throws Exception {
        assertNull(new FeedAdapter(Robolectric.application, feed).getItem(0).attachments);
    }

    @Test
    public void testGetView() throws Exception {

    }
}
