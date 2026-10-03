package org.janelia.saalfeldlab.n5.universe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;

import org.janelia.saalfeldlab.n5.FileSystemKeyValueAccess;
import org.janelia.saalfeldlab.n5.KeyValueAccess;
import org.junit.Test;

public class KeyValueAccessProviderTest {

	private static final class Claims implements KeyValueAccessProvider {

		final String scheme;
		final KeyValueAccess kva = new FileSystemKeyValueAccess();

		Claims(final String scheme) {

			this.scheme = scheme;
		}

		@Override
		public boolean test(final URI uri) {

			return scheme.equals(uri.getScheme());
		}

		@Override
		public KeyValueAccess apply(final URI uri, final N5Factory factory, final boolean readOnly) {

			return kva;
		}
	}

	@Test
	public void registeredProvidersAreAskedFirstUntilUnregistered() {

		final Claims first = new Claims("kva-test");
		final Claims second = new Claims("kva-test");
		final URI uri = URI.create("kva-test://store/path");
		final N5Factory factory = new N5Factory();

		KeyValueAccessProvider.register(first);
		try {
			assertSame(first.kva, factory.getKeyValueAccess(uri, true));

			KeyValueAccessProvider.register(second);
			assertSame(second.kva, factory.getKeyValueAccess(uri, true));

			assertTrue(KeyValueAccessProvider.unregister(second));
			assertSame(first.kva, factory.getKeyValueAccess(uri, true));
		} finally {
			KeyValueAccessProvider.unregister(first);
			KeyValueAccessProvider.unregister(second);
		}
		assertFalse(KeyValueAccessProvider.unregister(first));
	}

	@Test
	public void registeredProvidersClaimUrisOfBuiltInBackends() {

		final Claims files = new Claims("file");
		final URI uri = new File("store").getAbsoluteFile().toURI();

		KeyValueAccessProvider.register(files);
		try {
			assertSame(files.kva, new N5Factory().getKeyValueAccess(uri, true));
		} finally {
			KeyValueAccessProvider.unregister(files);
		}
	}

	@Test
	public void localPathsKeepTheirUrlStages() {

		final File repo = new File("my repo").getAbsoluteFile();
		final URI uri = N5Factory.parseUriFromString(repo + "|icechunk://branch.main/em data");
		assertEquals("file", uri.getScheme());
		assertEquals(repo.toURI().getPath() + "|icechunk://branch.main/em data", uri.getPath());
	}
}
