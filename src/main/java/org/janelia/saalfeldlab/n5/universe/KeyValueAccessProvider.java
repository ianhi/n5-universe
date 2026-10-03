package org.janelia.saalfeldlab.n5.universe;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import org.janelia.saalfeldlab.n5.KeyValueAccess;

/**
 * A {@link KeyValueAccess} backend supplied by another library, shaped like a
 * {@link KeyValueAccessBackend}: {@link #test} claims a URI, {@link #apply}
 * opens it. Providers are asked before the built-in backends, so they can claim
 * URIs those would otherwise take, such as an {@code s3://} URI of a store that
 * is not plain objects in a bucket.
 * <p>
 * A provider is found in one of two ways:
 * <ul>
 * <li>a library lists the class in
 * {@code META-INF/services/org.janelia.saalfeldlab.n5.universe.KeyValueAccessProvider},
 * and {@link ServiceLoader} finds it on the classpath;</li>
 * <li>an application calls {@link #register} at runtime, for URIs that only it
 * can resolve, such as the names of stores it has open.</li>
 * </ul>
 * Registered providers are asked first, most recently registered first, then
 * the ones on the classpath.
 */
public interface KeyValueAccessProvider extends Predicate<URI> {

	KeyValueAccess apply(URI uri, N5Factory factory, boolean readOnly);

	/** Ask {@code provider} before every provider registered earlier and every provider on the classpath. */
	static void register(final KeyValueAccessProvider provider) {

		Providers.REGISTERED.add(0, provider);
	}

	/** Stop asking {@code provider}; returns false if it was not registered. */
	static boolean unregister(final KeyValueAccessProvider provider) {

		return Providers.REGISTERED.remove(provider);
	}

	/** The providers in the order they are asked: registered ones, then the ones on the classpath. */
	static List<KeyValueAccessProvider> providers() {

		final List<KeyValueAccessProvider> providers = new ArrayList<>(Providers.REGISTERED);
		providers.addAll(Providers.ON_CLASSPATH);
		return providers;
	}

	final class Providers {

		private static final List<KeyValueAccessProvider> REGISTERED = new CopyOnWriteArrayList<>();

		/** Loaded once: the backend lookup runs on every open. */
		private static final List<KeyValueAccessProvider> ON_CLASSPATH = load();

		private static List<KeyValueAccessProvider> load() {

			final List<KeyValueAccessProvider> providers = new ArrayList<>();
			ServiceLoader.load(KeyValueAccessProvider.class).forEach(providers::add);
			return providers;
		}

		private Providers() {}
	}
}
