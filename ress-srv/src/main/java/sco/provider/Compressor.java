package sco.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.tnf.cas.db.Switcher;

@Provider
public class Compressor implements WriterInterceptor {
    @Context
    HttpServletRequest mRequest;
    
    @Autowired
    Switcher mSwitcher;

    @Override
    public void aroundWriteTo(WriterInterceptorContext context)
            throws IOException, WebApplicationException {
        

        boolean gzip = mRequest.getParameter("gzip") != null;
        boolean switcher = mSwitcher.isCompress();
        if (gzip || switcher) {
            MultivaluedMap<String, Object> headers = context.getHeaders();
            headers.add("Content-Encoding", "gzip");

            final OutputStream outputStream = context.getOutputStream();
            context.setOutputStream(new GZIPOutputStream(outputStream));
        }
        context.proceed();
    }
}