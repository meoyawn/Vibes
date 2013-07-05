package com.stiggpwnz.vibes.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.stiggpwnz.vibes.media.Player;
import com.stiggpwnz.vibes.media.Player.State;

@RunWith(RobolectricTestRunner.class)
public class PlayerTest {

	private static final String URL = "http://api.soundcloud.com/tracks/99174750/stream?client_id=b45b1aa10f1ac2941910a7f0d10f8e28";

	private final CountDownLatch lock = new CountDownLatch(1);
	private final Player player = new Player();

	@Test
	public void testPrepare() throws InterruptedException {
		player.prepare(URL);
		Assert.assertEquals(URL, player.getSource());
		lock.await(5, TimeUnit.SECONDS);
		Assert.assertEquals(State.PREPARED, player.getState());
	}

	@Test
	public void testSeek() throws InterruptedException {
		player.prepare(URL);
		player.seekTo(10);
		lock.await(5, TimeUnit.SECONDS);
		Assert.assertEquals(State.PREPARED, player.getState());
		Assert.assertEquals(10, player.getCurrentPosition());
	}
}
