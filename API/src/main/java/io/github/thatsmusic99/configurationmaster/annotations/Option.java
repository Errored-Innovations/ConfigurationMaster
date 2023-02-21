package io.github.thatsmusic99.configurationmaster.annotations;


import io.github.thatsmusic99.configurationmaster.annotations.handlers.DefaultOptionHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

    String path() default "";

    String comment() default "";

    String section() default "";

    boolean lenient() default false;

    Class<? extends OptionHandler> optionHandler() default DefaultOptionHandler.class;
}
