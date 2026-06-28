package com.tnf.cas.db;

import java.beans.PropertyDescriptor;

import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class BeanWrapperTest {

    static class Asdf {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    };

    static class Asdf2 extends Asdf {
        private String lastName;

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

    };

    @Test
    public void test() {
        Asdf2 asdf = new Asdf2();
        asdf.setName("a name");
        asdf.setLastName("a lastname");

        BeanWrapper w = new BeanWrapperImpl(asdf);
        PropertyDescriptor[] dss = w.getPropertyDescriptors();
        for (PropertyDescriptor ds : dss) {
            System.out.println(ds);
        }

    }

}
