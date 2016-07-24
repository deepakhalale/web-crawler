package com.locusintellect

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

import java.util.concurrent.Callable

import static org.junit.Assert.fail
import static org.mockito.BDDMockito.given
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.verify

@RunWith(MockitoJUnitRunner)
class DelayedRetryExecutionWrapperTest {

    @Mock
    private Callable mockCallable

    @Test
    public void shouldReturnValueAfterSuccessfulExecution() {
        given(mockCallable.call()).willReturn("Pass")

        final String returnValue = new DelayedRetryExecutionWrapper<String>().execute(mockCallable)

        verify(mockCallable).call()
        assert returnValue == "Pass"
    }

    @Test
    public void shouldErrorAfterExhaustingRetries() {
        SocketTimeoutException toBeThrown = new SocketTimeoutException("Some error")
        try {
            doThrow(toBeThrown).when(mockCallable).call()

            new DelayedRetryExecutionWrapper().execute(mockCallable)

            fail("Expected to throw exception.")
        } catch (Exception e) {
            assert toBeThrown == e
        }

    }

}
