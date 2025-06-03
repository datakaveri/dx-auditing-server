package org.cdpg.dx.common.request;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.common.exception.DxBadRequestException;

public class TemporalRequestHelper {
  private static final Logger LOGGER = LogManager.getLogger(TemporalRequestHelper.class);

  /**
   * Builds a TemporalRequest based on the provided parameters.
   *
   * @param timeField The field representing the time in the request.
   * @param timeRel The relationship of the time (e.g., "before", "after", "between", "during").
   * @param time The start time for the request.
   * @param endtime The end time for the request (optional, required for "between" and "during").
   * @return A TemporalRequest object or null if no valid relationship is provided.
   * @throws DxBadRequestException if the parameters are invalid.
   */
  public static TemporalRequest buildTemporalRequest(
      String timeField, String timeRel, String time, String endtime) {

    LOGGER.info(
        "Building TemporalRequest with timeField: {}, timeRel: {}, time: {}, endtime: {}",
        timeField,
        timeRel,
        time,
        endtime);

    if (timeField == null || timeField.isBlank()) return null;

    if (("between".equalsIgnoreCase(timeRel) || "during".equalsIgnoreCase(timeRel))) {
      if (time == null || endtime == null)
        throw new DxBadRequestException(
            "Both time and endtime must be provided for '" + timeRel + "'");
      if (!isTimeBeforeOrEqual(time, endtime))
        throw new DxBadRequestException(
            "time must be before or equal to endtime for '" + timeRel + "'");
      return new TemporalRequest(timeField, timeRel, time, endtime);
    } else if ("after".equalsIgnoreCase(timeRel)) {
      if (time == null) throw new DxBadRequestException("time must be provided for 'after'");
      return new TemporalRequest(timeField, timeRel, time, null);
    } else if ("before".equalsIgnoreCase(timeRel)) {
      if (time == null) throw new DxBadRequestException("time must be provided for 'before'");
      return new TemporalRequest(timeField, timeRel, time, null);
    }
    return null;
  }

  private static boolean isTimeBeforeOrEqual(String time, String endtime) {
    try {
      LocalDateTime t1 = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      LocalDateTime t2 = LocalDateTime.parse(endtime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      return !t1.isAfter(t2);
    } catch (DateTimeParseException e) {
      throw new DxBadRequestException("Invalid datetime format: " + e.getMessage());
    }
  }
}
