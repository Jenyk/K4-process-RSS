package cz.mzk.k4.processUI.utils;

import com.google.gson.Gson;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

class GSONThreadLocalImmolater {

	/*
	 * Prevents:
	 * org.apache.catalina.loader.WebappClassLoader checkThreadLocalMapForLeaks
	 * SEVERE: The web application [/RSS-0.0.1-SNAPSHOT] created a ThreadLocal
	 * with key of type [com.google.gson.Gson$1] (value
	 * [com.google.gson.Gson$1@6fe30af]) and a value of type [java.util.HashMap]
	 * (value [{}]) but failed to remove it when the web application was
	 * stopped. Threads are going to be renewed over time to try and avoid a
	 * probable memory leak.
	 * 
	 * http://code.google.com/p/google-gson/issues/detail?id=402
	 */

	static int immolate() {
		try {
			int count = 0;
			final Field threadLocalsField = Thread.class
					.getDeclaredField("threadLocals");
			threadLocalsField.setAccessible(true);
			final Field inheritableThreadLocalsField = Thread.class
					.getDeclaredField("inheritableThreadLocals");
			inheritableThreadLocalsField.setAccessible(true);
			for (final Thread thread : Thread.getAllStackTraces().keySet()) {
				count += clear(threadLocalsField.get(thread));
				count += clear(inheritableThreadLocalsField.get(thread));
			}
			return count;
		} catch (final Exception e) {
			throw new Error("die", e);
		}
	}

	private static int clear(final Object threadLocalMap) throws Exception {
		if (threadLocalMap == null)
			return 0;
		int count = 0;
		final Field tableField = threadLocalMap.getClass().getDeclaredField(
				"table");
		tableField.setAccessible(true);
		final Object table = tableField.get(threadLocalMap);
		for (int i = 0, length = Array.getLength(table); i < length; ++i) {
			final Object entry = Array.get(table, i);
			if (entry != null) {
				final Object threadLocal = ((WeakReference) entry).get();
				if (threadLocal != null
						&& threadLocal.getClass().getEnclosingClass() == Gson.class) {
					Array.set(table, i, null);
					++count;
				}
			}
		}
		return count;
	}
	
}
