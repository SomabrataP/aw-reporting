//Copyright 2012 Google Inc. All Rights Reserved.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.api.ads.adwords.awreporting.server.rest.kratu;

import com.google.api.ads.adwords.awreporting.server.kratu.KratuProcessor;
import com.google.api.ads.adwords.awreporting.server.kratu.RunnableKratu;
import com.google.api.ads.adwords.awreporting.server.rest.AbstractBaseResource;
import com.google.api.ads.adwords.awreporting.server.rest.RestServer;

import org.restlet.representation.Representation;
import org.restlet.service.TaskService;

import java.util.Date;

/**
 * GenerateKratusRest
 * 
 * @author jtoledo@google.com (Julian Toledo)
 */
public class GenerateKratusRest extends AbstractBaseResource {

  static TaskService taskService = new TaskService();

  public Representation getHandler() {
    String result = null;

    try {

      Long topAccountId = getParameterAsLong("topAccountId");
      Date dateStart = getParameterAsDate("dateStart");
      Date dateEnd = getParameterAsDate("dateEnd");

      //Generate Kratus at Mcc level
      if (topAccountId != null && dateStart != null && dateEnd != null) {

        // Launching a new Service(Thread) to make the request async.
        KratuProcessor kratuProcessor = RestServer.getApplicationContext().getBean(KratuProcessor.class);
        RunnableKratu runnableKratu = kratuProcessor.createRunnableKratu(
            topAccountId, null, RestServer.getStorageHelper(), dateStart, dateEnd);

        taskService.submit(runnableKratu);

        result = "OK - Task created, this usually takes 1-2mins for each 1000 accounts/month";
      } else {
        taskService.getContext();
      }
    } catch (Exception exception) {
      return handleException(exception);
    }
    addReadOnlyHeaders();
    return createJsonResult(result);
  }
}
