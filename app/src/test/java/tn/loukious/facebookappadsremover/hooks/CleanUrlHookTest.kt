package tn.loukious.facebookappadsremover.hooks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.URLEncoder

class CleanUrlHookTest {

    private fun wrapper(query: String) = "https://www.facebook.com/l.php?$query"
    private fun enc(value: String) = URLEncoder.encode(value, "UTF-8")

    /**
     * The shape stock Facebook actually emits: raw `&` before fbclid. The
     * decoded mod pattern only accepted an encoded separator, so it never
     * matched — this is the regression the parse fixes.
     */
    @Test fun unwrapsRealLinkWithRawFbclidSeparator() {
        val dest = "https://example.org/article?a=1&b=2#section"
        assertEquals(dest, extractDestination(wrapper("u=${enc(dest)}&fbclid=IwAR123")))
    }

    @Test fun unwrapsWhenOtherParamsPrecedeOrFollowU() {
        val dest = "https://example.org/x?y=1"
        assertEquals(dest, extractDestination(wrapper("h=AbC&u=${enc(dest)}&fbclid=IwAR1")))
        assertEquals(dest, extractDestination(wrapper("u=${enc(dest)}&h=AbC&fbclid=IwAR1")))
        assertEquals(dest, extractDestination(wrapper("u=${enc(dest)}")))
    }

    /** A `%26` inside the encoded destination is destination data, not a separator. */
    @Test fun destinationQuerySeparatorsArePreserved() {
        val dest = "https://example.org/a?x=1&y=2&z=3"
        assertEquals(dest, extractDestination(wrapper("u=${enc(dest)}&fbclid=IwAR1")))
    }

    @Test fun nonHttpDestinationsAreRejected() {
        assertNull(extractDestination(wrapper("u=${enc("javascript:alert(1)")}&fbclid=x")))
        assertNull(extractDestination(wrapper("u=${enc("intent://evil#Intent;end")}&fbclid=x")))
        assertNull(extractDestination(wrapper("u=${enc("file:///etc/hosts")}&fbclid=x")))
    }

    @Test fun unwrapsLinkWithFragmentInQueryIgnored() {
        val dest = "https://example.org/page"
        // Fragment appears after query; we must not treat it as part of u=
        assertEquals(dest, extractDestination(wrapper("u=${enc(dest)}#fragment&fbclid=x")))
    }

