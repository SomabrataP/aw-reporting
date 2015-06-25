//Copyright 2013 Google Inc. All Rights Reserved.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.api.ads.adwords.awreporting.alerting.downloader;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.api.ads.adwords.awreporting.alerting.downloader.MultipleClientReportDownloader;
import com.google.api.ads.adwords.awreporting.alerting.downloader.RunnableReportDownloader;
import com.google.api.ads.adwords.awreporting.alerting.util.TestEntitiesGenerator;
import com.google.api.ads.adwords.awreporting.util.AdWordsSessionBuilderSynchronizer;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
* Test case for the {@link MultipleClientReportDownloader} class
* 
 * @author zhuoc@google.com (Zhuo Chen)
*/
public class MultipleClientReportDownloaderTest {
  
  @Spy
  private MultipleClientReportDownloader mockedMultipleClientReportDownloader;
  
  @Before
  public void setUp() {
   mockedMultipleClientReportDownloader = new MultipleClientReportDownloader();
   
   MockitoAnnotations.initMocks(this);
  }
  
  /**
  * Tests the downloadReports(...).
  * @throws ValidationException 
  *
  */
  @Test
  public void testDownloadReports() throws InterruptedException, ValidationException {
   Mockito.doAnswer(new Answer<Void>() {
     @Override
     public Void answer(InvocationOnMock invocation) throws Throwable {
       ((CountDownLatch) invocation.getArguments()[1]).countDown();
       return null;
     }
   }).when(mockedMultipleClientReportDownloader).executeRunnableDownloader(
       Mockito.<RunnableReportDownloader>anyObject(), Mockito.<CountDownLatch>anyObject());
  
   AdWordsSession.Builder builder = TestEntitiesGenerator.getTestAdWordsSessionBuilder();
   AdWordsSessionBuilderSynchronizer adWordsSessionBuilderSynchronizer = new AdWordsSessionBuilderSynchronizer(builder);
   
   Set<Long> cids = ImmutableSet.of(1L, 2L, 3L, 4L, 5L);
   mockedMultipleClientReportDownloader.downloadReports(adWordsSessionBuilderSynchronizer, null, cids);
  
   ArgumentCaptor<CountDownLatch> argument = ArgumentCaptor.forClass(CountDownLatch.class);
  
   verify(mockedMultipleClientReportDownloader, times(5))
       .executeRunnableDownloader(Mockito.<RunnableReportDownloader>anyObject(), argument.capture());
  
   assertEquals(argument.getValue().getCount(), 0);
  }
}
