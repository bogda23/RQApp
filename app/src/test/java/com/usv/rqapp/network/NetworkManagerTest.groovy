package com.usv.rqapp.network

import android.app.Application
import android.content.Context
import org.junit.Test

import static org.junit.Assert.assertEquals

class NetworkManagerTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.usv.rqapp", appContext.getPackageName());
    }
    @Test
    void testGetConnectivityStatusString() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();


    }
}
