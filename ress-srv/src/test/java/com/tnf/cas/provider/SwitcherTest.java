package com.tnf.cas.provider;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Test;

public class SwitcherTest {

    @Test
    public void test() throws Exception {
        ToStringBuilder.setDefaultStyle(ToStringStyle.MULTI_LINE_STYLE);
        System.out.println(ToStringBuilder.reflectionToString(this));
    }
}
