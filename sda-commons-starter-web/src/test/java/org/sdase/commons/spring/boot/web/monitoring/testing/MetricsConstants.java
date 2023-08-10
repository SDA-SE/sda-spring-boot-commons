/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring.testing;

import java.util.List;

/** Class that contains metrics tags that are exported by default */
public final class MetricsConstants {

  private MetricsConstants() {}

  public static final List<String> PROMETHEUS_METRICS_NAMES =
      List.of(
          "tomcat_sessions_created_sessions_total",
          "jvm_memory_usage_after_gc_percent",
          "http_client_requests_seconds_count",
          "http_client_requests_seconds_sum",
          "http_client_requests_seconds_max",
          "process_uptime_seconds",
          "jvm_gc_memory_promoted_bytes_total",
          "some_operation_success_counter_total",
          "executor_pool_size_threads",
          "jvm_threads_peak_threads",
          "jvm_threads_live_threads",
          "application_ready_time_seconds",
          "jvm_threads_states_threads",
          "jvm_gc_pause_seconds_count",
          "jvm_gc_pause_seconds_sum",
          "jvm_gc_pause_seconds_max",
          "jvm_memory_committed_bytes",
          "process_start_time_seconds",
          "application_started_time_seconds",
          "some_operation_error_counter_total",
          "executor_pool_core_threads",
          "tomcat_sessions_alive_max_seconds",
          "jvm_memory_max_bytes",
          "executor_active_threads",
          "jvm_buffer_total_capacity_bytes",
          "jvm_gc_live_data_size_bytes",
          "executor_queued_tasks",
          "system_cpu_usage",
          "jvm_classes_unloaded_classes_total",
          "tomcat_sessions_active_current_sessions",
          "jvm_gc_memory_allocated_bytes_total",
          "executor_pool_max_threads",
          "jvm_buffer_count_buffers",
          "jvm_memory_used_bytes",
          "executor_queue_remaining_tasks",
          "jvm_gc_overhead_percent",
          "disk_free_bytes",
          "tomcat_sessions_active_max_sessions",
          "disk_total_bytes",
          "executor_completed_tasks_total",
          "tomcat_sessions_rejected_sessions_total",
          "jvm_classes_loaded_classes",
          "process_cpu_usage",
          "jvm_buffer_memory_used_bytes",
          "jvm_gc_max_data_size_bytes",
          "jvm_threads_daemon_threads",
          "tomcat_sessions_expired_sessions_total",
          "logback_events_total",
          "system_cpu_count");

  public static final List<String> METRIC_NAMES =
      List.of(
          "application.ready.time",
          "application.started.time",
          "disk.free",
          "disk.total",
          "executor.active",
          "executor.completed",
          "executor.pool.core",
          "executor.pool.max",
          "executor.pool.size",
          "executor.queue.remaining",
          "executor.queued",
          "http.client.requests",
          "http.server.requests",
          "jvm.buffer.count",
          "jvm.buffer.memory.used",
          "jvm.buffer.total.capacity",
          "jvm.classes.loaded",
          "jvm.classes.unloaded",
          "jvm.gc.live.data.size",
          "jvm.gc.max.data.size",
          "jvm.gc.memory.allocated",
          "jvm.gc.memory.promoted",
          "jvm.gc.overhead",
          "jvm.gc.pause",
          "jvm.memory.committed",
          "jvm.memory.max",
          "jvm.memory.usage.after.gc",
          "jvm.memory.used",
          "jvm.threads.daemon",
          "jvm.threads.live",
          "jvm.threads.peak",
          "jvm.threads.states",
          "logback.events",
          "process.cpu.usage",
          "process.start.time",
          "process.uptime",
          "some_operation_error_counter_total",
          "some_operation_success_counter_total",
          "system.cpu.count",
          "system.cpu.usage",
          "tomcat.sessions.active.current",
          "tomcat.sessions.active.max",
          "tomcat.sessions.alive.max",
          "tomcat.sessions.created",
          "tomcat.sessions.expired",
          "tomcat.sessions.rejected");
}
