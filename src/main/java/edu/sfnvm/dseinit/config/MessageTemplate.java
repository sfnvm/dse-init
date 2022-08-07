package edu.sfnvm.dseinit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@Configuration
public class MessageTemplate {
  @Autowired
  private MessageSource messageSource;

  public String message(String key, String... value) {
    return messageSource.getMessage(key, value, LocaleContextHolder.getLocale());
  }

  /**
   * <p>Hardcode locale. When i18n confused about client's system language.</p>
   */
  public String messageVi(String key, String... value) {
    return messageSource.getMessage(key, value, new Locale("vi"));
  }
}
