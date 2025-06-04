package org.cdpg.dx.common.util;

import io.vertx.ext.web.validation.ParameterProcessorException;
import org.cdpg.dx.common.exception.BaseDxException;

public class ThrowableUtils {

  private ThrowableUtils() {
    // Utility class, prevent instantiation
  }

  public static boolean isSafeToExpose(Throwable throwable) {
    return throwable instanceof IllegalArgumentException
        || throwable instanceof BaseDxException
        || throwable instanceof ParameterProcessorException;
  }
}
