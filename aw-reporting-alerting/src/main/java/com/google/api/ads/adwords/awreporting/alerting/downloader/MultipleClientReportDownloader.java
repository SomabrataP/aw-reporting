// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.adwords.awreporting.alerting.downloader;

import com.google.api.ads.adwords.awreporting.alerting.report.ReportQuery;
import com.google.api.ads.adwords.awreporting.alerting.util.AdWordsSessionBuilderSynchronizer;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.common.base.Stopwatch;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class to handle all the concurrent file downloads from the report API.
 *
 * An {@link ExecutorService} is created in order to handle all the threads. To initialize the
 * executor is necessary to call {@code initializeExecutorService}, and to finalize the executor is
 * necessary to call {@code finalizeExecutorService} after all the downloads are done, and the
 * downloader will not be used again.
 *
 * @author zhuoc@google.com (Zhuo Chen)
 */
public class MultipleClientReportDownloader {

  private static final Logger LOGGER = Logger.getLogger(MultipleClientReportDownloader.class);

  private static final int NUM_THREADS = 20;

  private static final int RETRIES_COUNT = 5;

  private static final int BACKOFF_INTERVAL = 1000 * 5;

  private static final int BUF_SIZE = 0x1000;

  private ExecutorService executorService;

  private int numThreads = NUM_THREADS;

  private int retriesCount = RETRIES_COUNT;

  private int backoffInterval = BACKOFF_INTERVAL;

  private int bufferSize = BUF_SIZE;

  /**
   * Downloads the specified report for all specified CIDs. Prints out list of failed CIDs. Returns
   * List<File> for all successful downloads.
   *
   * @param sessionBuilder the session builder to build AdWords session for each account.
   * @param reportQuery Report to download.
   * @param accountIds CIDs to download the report for.
   * @return Collection of File objects reports have been downloaded to.
   * @throws InterruptedException error trying to stop downloader thread.
   * @throws ValidationException.
   */  
  public Collection<File> downloadReports(final AdWordsSessionBuilderSynchronizer sessionBuilder,
      final ReportQuery reportQuery,
      final Set<Long> accountIds) throws InterruptedException, ValidationException {

    final Collection<Long> failed = new ConcurrentSkipListSet<Long>();
    final Collection<File> results = new ConcurrentSkipListSet<File>();

    // We use a Latch so the main thread knows when all the worker threads are complete.
    final CountDownLatch latch = new CountDownLatch(accountIds.size());

    Stopwatch stopwatch = Stopwatch.createStarted();

    for (final Long accountId : accountIds) {
      
      // We create a copy of the AdWordsSession specific for the Account
      AdWordsSession adWordsSession = sessionBuilder.getAdWordsSessionCopy(accountId);
      
      RunnableDownloader downloader = new RunnableDownloader(this.retriesCount,
          this.backoffInterval,
          this.bufferSize,
          accountId,
          reportQuery,
          adWordsSession,
          results);
      downloader.setFailed(failed);
      downloader.setLatch(latch);
      this.executorService.execute(downloader);
    }

    latch.await();
    stopwatch.stop();
    return this.printResultsAndReturn(
        results, stopwatch.elapsed(TimeUnit.MILLISECONDS), failed, accountIds);
  }

  /**
   * Prints the results and return the file list.
   *
   * @param results the list of downloaded file results.
   * @param elapsedTime the elapsed time.
   * @param failed the list of failures.
   * @param cids the account cids.
   * @return the list of downloaded files.
   */
  private Collection<File> printResultsAndReturn(final Collection<File> results, long elapsedTime,
      final Collection<Long> failed, final Set<Long> cids) {
    LOGGER.info("\n Downloaded reports for " + cids.size() + " accounts in " + (elapsedTime / 1000)
        + " s. " + failed.size() + " failures:\n");
    for (Long failure : failed) {
      LOGGER.error(failure);
    }
    return results;
  }

  /**
   * Creates the {@code ExecutorService} to run the concurrent threads.
   */
  public void initializeExecutorService() {
    // The ExecutorService will process all Runnables passed to it via .execute in
    // the order they are received with up to numThreads processing concurrently.
    this.executorService = Executors.newFixedThreadPool(this.numThreads);
  }

  /**
   * Finalizes the {@code ExecutorService}.
   */
  public void finalizeExecutorService() {
    executorService.shutdown();
  }

  /**
   * @param numThreads the numThreads to set. Default value = 20
   */
  public void setNumThreads(int numThreads) {
    this.numThreads = numThreads;
  }

  /**
   * @param retriesCount the retriesCount to set. Default value = 5
   */
  public void setRetriesCount(int retriesCount) {
    this.retriesCount = retriesCount;
  }

  /**
   * @param backoffInterval the backoffInterval to set. Default value = 1000 * 5
   */
  public void setBackoffInterval(int backoffInterval) {
    this.backoffInterval = backoffInterval;
  }

  /**
   * @param bufferSize the bufSize to set. Default value = 0x1000
   */
  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }
}