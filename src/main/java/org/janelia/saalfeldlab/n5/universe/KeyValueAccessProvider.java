package org.janelia.saalfeldlab.n5.universe;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;

import org.janelia.saalfeldlab.n5.KeyValueAccess;

/**
 * A {@link KeyValueAccess} backend supplied by another library, shaped like a
 * {@link KeyValueAccessBackend}: {@link #test} claims a URI, {@link #apply}
 * opens it. Providers are asked before the built-in backends, so they can claim
 * URIs those would otherwise take, such as an {@code s3://} URI of a store that
 * is not plain objects in a bucket.
 * <p>
 * Implementations are discovered with {@link ServiceLoader}: list the class in
 * {@code META-INF/services/org.janelia.saalfeldlab.n5.universe.KeyValueAccessProvider}.
 */
public interface KeyValueAccessProvider extends Predicate<URI> {

	KeyValueAccess apply(URI uri, N5Factory factory, boolean readOnly);

	/** Loaded once: the backend lookup runs on every open. */
	List<KeyValueAccessProvider> PROVIDERS = load();

	static List<KeyValueAccessProvider> load() {

		final List<KeyValueAccessProvider> providers = new ArrayList<>();
		ServiceLoader.load(KeyValueAccessProvider.class).forEach(providers::add);
		return providers;
	}
}
