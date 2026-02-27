/*
    Copyright 2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.fx.service.probe;

import java.util.List;

public sealed interface ProbeEvent permits
        ProbeEvent.JobCreated,
        ProbeEvent.StepStarted,
        ProbeEvent.StepCompleted,
        ProbeEvent.StepFailed,
        ProbeEvent.JobCompleted,
        ProbeEvent.JobFailed {

    record JobCreated(List<ProbeStep> steps) implements ProbeEvent {}

    record StepStarted(ProbeStep step) implements ProbeEvent {}

    record StepCompleted(ProbeStep step) implements ProbeEvent {}

    record StepFailed(ProbeStep step) implements ProbeEvent {}

    record JobCompleted() implements ProbeEvent {}

    record JobFailed(Throwable error) implements ProbeEvent {}
}