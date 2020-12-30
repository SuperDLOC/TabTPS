/*
 * This file is part of TabTPS, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.tabtps.util;

import com.sun.management.OperatingSystemMXBean;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.TabTPS;

import java.lang.management.ManagementFactory;
import java.util.DoubleSummaryStatistics;

public class CPUUtil {
  private final TabTPS tabTPS;
  private volatile double recentProcessCpuLoadSnapshot = 0;
  private volatile double recentSystemCpuLoadSnapshot = 0;
  private final double[] recentSystemUsage = new double[20];
  private final double[] recentProcessUsage = new double[20];
  private int index = 0;
  private BukkitTask monitorTask = null;

  public CPUUtil(final @NonNull TabTPS tabTPS) {
    this.tabTPS = tabTPS;
  }

  public void startRecordingUsage() {
    this.stopRecordingUsage();
    this.monitorTask = Bukkit.getScheduler()
      .runTaskTimerAsynchronously(this.tabTPS, this::recordUsage, 0L, 10L);
  }

  public void stopRecordingUsage() {
    if (this.monitorTask != null) {
      this.monitorTask.cancel();
    }
  }

  private void nextIndex() {
    this.index++;
    if (this.index == 20) {
      this.index = 0;
    }
  }

  private void recordUsage() {
    this.recentProcessUsage[this.index] = currentProcessCpuLoad();
    this.recentSystemUsage[this.index] = currentSystemCpuLoad();
    this.recentProcessCpuLoadSnapshot = this.recentProcessCpuLoad();
    this.recentSystemCpuLoadSnapshot = this.recentSystemCpuLoad();
    this.nextIndex();
  }

  public double recentProcessCpuLoadSnapshot() {
    return this.recentProcessCpuLoadSnapshot;
  }

  public double recentSystemCpuLoadSnapshot() {
    return this.recentSystemCpuLoadSnapshot;
  }

  private double recentProcessCpuLoad() {
    return round(average(this.recentProcessUsage));
  }

  private double recentSystemCpuLoad() {
    return round(average(this.recentSystemUsage));
  }

  private static double average(final double[] values) {
    final DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
    for (final double d : values.clone()) {
      if (d != 0 && !Double.isNaN(d)) {
        statistics.accept(d);
      }
    }
    return statistics.getAverage();
  }

  private static double round(final double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private static double currentProcessCpuLoad() {
    return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad() * 100;
  }

  private static double currentSystemCpuLoad() {
    return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getSystemCpuLoad() * 100;
  }
}
