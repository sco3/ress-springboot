package com.tnf.cas.provider;

import java.beans.PropertyDescriptor;
import java.util.Enumeration;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tnf.cas.db.Switcher;

import sco.web.WebServerConstants;

@Component("switchSetter")
@Path(WebServerConstants.REST_V1_PATH)
public class SwitchSetter implements WebServerConstants {
    Logger mTrace = LoggerFactory.getLogger(SwitchSetter.class);
    private static final String ADDR_127_0_0_1 = "127.0.0.1";

    @Autowired
    Switcher mSwitcher;

    @Context
    HttpServletRequest mRequest;

    @GET
    @Path("/switch")
    @PermitAll
    public String imsi() {
        String result = ADDR_127_0_0_1;
        if (ADDR_127_0_0_1.equals(mRequest.getLocalAddr())) {
            try {
                BeanWrapperImpl bean = new BeanWrapperImpl(mSwitcher);
                String msg = null;
                Enumeration<String> names = mRequest.getParameterNames();
                if (names != null) {
                    while (names.hasMoreElements()) {
                        String name = names.nextElement();
                        String val = mRequest.getParameter(name);
                        if (val != null && val.trim().length() > 0) {
                            try {
                                bean.setPropertyValue(name, val);
                            } catch (Exception e) {
                                msg = e.getMessage();
                                mTrace.error(e.getMessage());
                            }
                        }
                    }
                }
                PropertyDescriptor[] descs = bean.getPropertyDescriptors();
                result = "<pre>\n";
                for (PropertyDescriptor desc : descs) {
                    String name = desc.getName();
                    if ("class".equals(name)) {
                        continue;
                    }
                    result += "\n";
                    result += name + "=" + bean.getPropertyValue(name);
                    result += "\n";
                }
                result += "\n</pre>";
                if (msg != null) {
                    result += "\nError: " + msg;
                }
            } catch (Exception e) {
                result = e.getMessage();
            }
        }
        return result;
    }
}
