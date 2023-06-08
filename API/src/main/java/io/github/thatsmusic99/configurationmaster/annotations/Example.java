package io.github.thatsmusic99.configurationmaster.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Examples.class)
public @interface Example {

    String key();

    String value();
}
