# SchedulingEngine

The SchedulingEngine tries to solve an optimization problem:

Find the best Schedule.

The Schedule has a score which will be calculated as the cumultative partial scores of Courses divided equaly to the events by time.
Deductions will be applied for conflicts and deviation from target ECTs.

For an optimization problem we define the following constraints.

## Constraints

- Every Event in a Schedule must start after and end before the same Frame
- The complement of a Response contains every Event of every Course in the Query that did not make it into the schedule.
- The primary event of a collision is always the one with a higher partial score.
