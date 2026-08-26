package io.github.khram0v.gymcrm.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TransactionIdFilterTest {

    private final TransactionIdFilter filter = new TransactionIdFilter();

    @Test
    void doFilter_whenNoIncomingHeader_generatesNewTransactionId_setsResponseHeader_andClearsMdcAfter()
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader("X-Transaction-Id")).isNotBlank();
        assertThat(MDC.get("transactionId")).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenIncomingHeaderPresent_reusesIt() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Transaction-Id", "tx-existing-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader("X-Transaction-Id")).isEqualTo("tx-existing-123");
    }

    @Test
    void doFilter_putsTransactionIdInMdc_duringChainExecution() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Transaction-Id", "tx-during-chain");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) ->
                assertThat(MDC.get("transactionId")).isEqualTo("tx-during-chain"));
    }
}
