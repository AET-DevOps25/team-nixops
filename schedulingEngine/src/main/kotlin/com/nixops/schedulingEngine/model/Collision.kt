package com.nixops.schedulingEngine.model

import java.time.Duration

data class Collision(val primary: Event, val secondary: Event, val duration: Duration)
