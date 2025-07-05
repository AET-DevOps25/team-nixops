package com.nixops.schedulingEngine.engine

import com.nixops.schedulingEngine.model.Query
import com.nixops.schedulingEngine.model.Response

interface SchedulingEngine {
  fun createSchedule(query: Query): Response
}
