package com.stiggpwnz.vibes.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import com.stiggpwnz.vibes.Player;
import com.stiggpwnz.vibes.Player.State;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.activities.MainActivity;

@RunWith(RobolectricTestRunner.class)
public class PlayerTest {

	private final CountDownLatch lock = new CountDownLatch(1);

	@Test
	public void testHelloWorld() {
		String name = Robolectric.buildActivity(MainActivity.class).get().getResources().getString(R.string.app_name);
		Assert.assertEquals(name, "Vibes");
	}

	public void testReseting() throws InterruptedException {
		Player player = new Player();
		player.prepare("http://api.soundcloud.com/tracks/99174750/stream?client_id=b45b1aa10f1ac2941910a7f0d10f8e28");
		lock.await(10, TimeUnit.MILLISECONDS);
		Assert.assertEquals(player.getState(), State.PAUSED);
	}

}
