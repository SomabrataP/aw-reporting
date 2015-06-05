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

package com.google.api.ads.adwords.awreporting.alerting.action;

import com.google.api.ads.adwords.awreporting.alerting.report.ReportEntry;

public interface AlertAction {  
  // All implementations must have a constructor with a JsonObject parameter
  
  // Process each report entry, it could perform the action here; or record some info
  // and perform some aggregated action in finalizeAction().
  public void initializeAction();
  public void processReportEntry(ReportEntry entry);
  public void finalizeAction();
}